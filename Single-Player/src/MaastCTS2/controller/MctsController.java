package MaastCTS2.controller;

import java.util.ArrayList;
import java.util.HashMap;

import MaastCTS2.Globals;
import MaastCTS2.KnowledgeBase;
import MaastCTS2.gnu.trove.list.array.TIntArrayList;
import MaastCTS2.heuristics.states.IPlayoutEvaluation;
import MaastCTS2.model.ActionLocation;
import MaastCTS2.model.ActionNGram;
import MaastCTS2.model.MctNode;
import MaastCTS2.model.Score;
import MaastCTS2.model.StateObs;
import MaastCTS2.move_selection.IMoveSelectionStrategy;
import MaastCTS2.playout.IPlayoutStrategy;
import MaastCTS2.selection.ISelectionStrategy;
//import MaastCTS2.utils.MctsVisualizer;
import core.competition.CompetitionParameters;
import core.game.StateObservation;
import ontology.Types.ACTIONS;
import ontology.Types.WINNER;
import tools.ElapsedCpuTimer;

public class MctsController implements IController {
	
	/** Buffer (in milliseconds) of the thinking time per cycle that will not be used (to make sure that an action is returned in time) */
	public static int TIME_BUFFER_MILLISEC = 10;
	
	/** 
	 * The maximum number of states we'll generate per action in the level below root for safety prepruning 
	 */
	public static int MAX_NUM_SAFETY_CHECKS = 3;

	/** Total number of iterations of the main loop of MCTS in an entire match */
	public static int TOTAL_ITERATIONS;
	/** The minimum number of iterations of the main loop of MCTS in a single cycle of the match */
	public static int MIN_ITERATIONS_PER_GAME;
	/** The maximum number of iterations of the main loop of MCTS in a single cycle of the match */
	public static int MAX_ITERATIONS_PER_GAME;
	/** Total number of iterations of the main loop of MCTS that ended in a loss in an entire match */
	//public static int TOTAL_LOSS_ITERATIONS;
	
	/** The minimum score found so far in the entire game */
	public double MIN_SCORE;
	/** The maximum score found so far in the entire game */
	public double MAX_SCORE;
	
	/** The minimum action score found so far in the entire game */
	public double MIN_ACTION_SCORE;
	/** The maximum action score found so far in the entire game */
	public double MAX_ACTION_SCORE;
	
	private double avgSimulationTime;
	
	/** The root node of our MCTS tree */
	private MctNode root;
	
	/** The current game tick of the root node */
	public int rootTick;
	
	/** The score in the root game state */
	private double rootScore;
	
	/** Strategy for selecting a node in the selection step of MCTS */
	private final ISelectionStrategy selectionStrategy;
	/** Strategy for selecting an action in the playout step of MCTS */
	private final IPlayoutStrategy playoutStrategy;
	
	/** The strategy for selecting the move to play after the search process */
	private final IMoveSelectionStrategy moveSelectionStrategy;
	
	/** The function to use for evaluating playouts */
	private final IPlayoutEvaluation playoutEval;
	
	/** The last action that we have played in real gameplay (not in Monte-Carlo simulations) */
	private ACTIONS lastAction = ACTIONS.ACTION_NIL;
	private final double treeDecayFactor;
	
	/** 
	 * If true, initializes a shallow tree using breadth-first search at the start of the game, 
	 * instead of starting MCTS with only a root node.
	 */
	private final boolean initBreadthFirst;
	
	/**
	 * If true, we prune some nodes based on novelty-tests
	 */
	private final boolean noveltyBasedPruning;
	
	/** If true, we'll collect Action statistics (for example for use in Progressive History / MAST) */
	private final boolean collectActionStatistics;
	
	/** 
	 * We'll collect statistics for n-grams of actions of all sizes n where 2 <= n <= maxActionNGramSize 
	 * <br> (not using n-grams with n = 1 because we'll use the less computationally expensive actionStatistics EnumMap for that )
	 */
	private final int maxActionNGramSize;
	
	/** 
	 * Table of action statistics for use in Progressive History / MAST
	 */
	private final HashMap<ActionLocation, Score> actionStatistics;
	
	/**
	 * Table of action n-gram statistics for use in NST
	 */
	private final HashMap<ActionNGram, Score> actionNGramStatistics;
	
	/** Factor with which to decay action statistics */
	private final double actionDecayFactor = 0.6;
	
	private ArrayList<ACTIONS> losingActionSequence;
	private StateObservation losingState;
	private final boolean exploreLosses;
	
	private final boolean knowledgeBasedEval;
	private final boolean alwaysKB;
	
	public static int NUM_ADVANCE_OPS;
	
	/** 
	 * The very first state generated by the selection step is evaluated, and the corresponding evaluation is stored here 
	 * this is done to give a small punishment to very short-term decisions (such as staying in the same place for too long)
	 */
	public static double ONE_STEP_EVAL = 0.0;
	
	/** In deterministic games, nodes with this many visits will start caching the state instead of re-generating the same state */
	public static final int DETERMINISTIC_STATE_CACHE_VISIT_THRESHOLD = 3;
	
	private final boolean treeReuse;	
	private final boolean noTreeReuseBFTI;

	public MctsController(ISelectionStrategy selectionStrategy, 
							IPlayoutStrategy playoutStrategy, IMoveSelectionStrategy moveSelectionStrategy, 
							IPlayoutEvaluation playoutEval, boolean initBreadthFirst, boolean noveltyBasedPruning,
							boolean exploreLosses, boolean knowledgeBasedEval, boolean treeReuse, double treeReuseGamma,
							int maxNumSafetyChecks, boolean alwaysKB, boolean noTreeReuseBFTI) {
		this.selectionStrategy = selectionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.moveSelectionStrategy = moveSelectionStrategy;
		this.playoutEval = playoutEval;
		this.initBreadthFirst = initBreadthFirst;
		this.noveltyBasedPruning = noveltyBasedPruning;
		this.exploreLosses = exploreLosses;
		this.knowledgeBasedEval = knowledgeBasedEval;
		this.treeReuse = treeReuse;
		treeDecayFactor = treeReuseGamma;
		MAX_NUM_SAFETY_CHECKS = maxNumSafetyChecks;
		this.alwaysKB = alwaysKB;
		this.noTreeReuseBFTI = noTreeReuseBFTI;
		
		maxActionNGramSize = Math.max
				(selectionStrategy.getDesiredActionNGramSize(), playoutStrategy.getDesiredActionNGramSize());
		
		collectActionStatistics = (
				selectionStrategy.wantsActionStatistics() 	||
				playoutStrategy.wantsActionStatistics()		||
				maxActionNGramSize > 0);
		
		if(collectActionStatistics){
			actionStatistics = new HashMap<ActionLocation, Score>();
			
			if(maxActionNGramSize > 1){
				actionNGramStatistics = new HashMap<ActionNGram, Score>();
			}
			else{
				actionNGramStatistics = null;
			}
		}
		else{
			actionStatistics = null;
			actionNGramStatistics = null;
		}
	}

	@Override
	public void init(StateObservation so, ElapsedCpuTimer elapsedTimer) {
		Globals.knowledgeBase = new KnowledgeBase();
		Globals.knowledgeBase.init(so, true);
		
		root = null;
		
		selectionStrategy.init(so, elapsedTimer);
		playoutStrategy.init(so, elapsedTimer, this);
		
		TOTAL_ITERATIONS = 0;
		MIN_ITERATIONS_PER_GAME = Integer.MAX_VALUE;
		MAX_ITERATIONS_PER_GAME = Integer.MIN_VALUE;
		//TOTAL_LOSS_ITERATIONS = 0;
		
		// these initial values look weird but they're not a mistake
		MIN_SCORE = Globals.HUGE_ENDGAME_SCORE;
		MAX_SCORE = -Globals.HUGE_ENDGAME_SCORE;
		
		MIN_ACTION_SCORE = Globals.HUGE_ENDGAME_SCORE;
		MAX_ACTION_SCORE = -Globals.HUGE_ENDGAME_SCORE;
		
		losingActionSequence = null;
		
		// done with initializing. We'll use the remaining time to start a nice, long MCTS
		
		// we'll do this with a longer than normal time buffer just to be safe, cache the normal buffer here so we can re-set it at the end again
		final int normalTimeBuffer = MctsController.TIME_BUFFER_MILLISEC;
		MctsController.TIME_BUFFER_MILLISEC = Math.max(50, normalTimeBuffer);
		
		// run MCTS
		runMcts(so, elapsedTimer);
		
		//MctsVisualizer.generateGraphFile(root);
		
		// set normal time buffer again
		MctsController.TIME_BUFFER_MILLISEC = normalTimeBuffer;
		
		// resetting these again here to avoid having the MCTS during init messing with these numbers
		TOTAL_ITERATIONS = 0;
		MIN_ITERATIONS_PER_GAME = Integer.MAX_VALUE;
		MAX_ITERATIONS_PER_GAME = Integer.MIN_VALUE;
		//TOTAL_LOSS_ITERATIONS = 0;
	}
	
	@Override
	public ACTIONS chooseAction(StateObservation currentStateObs, ElapsedCpuTimer elapsedTimer){
		lastAction = runMcts(currentStateObs, elapsedTimer);
		//System.out.println("playing " + lastAction);
		//System.out.println("Avg. score of root = " + Globals.normalise(root.getTotalScore() / root.getNumVisits(), MIN_SCORE, MAX_SCORE));
		return lastAction;
	}

	public ACTIONS runMcts(StateObservation rootStateObs, ElapsedCpuTimer elapsedForSimulationTimer) {
		final double oldRootScore = rootScore;
		rootScore = rootStateObs.getGameScore();
		rootTick = rootStateObs.getGameTick();
		
		if(root == null){
			root = new MctNode();
			losingActionSequence = null;
		}
		else{		// we can reuse a part of the tree from the previous search
			if(rootStateObs.getGameTick() != 0){
				// one important exception: if our current game tick is 0, it means that we want to re-use the tree
				// we've been generating during init. This means we do not need to find a matching child or decay
				
				boolean foundChildToReuse = false;
				
				if(treeReuse){
					for(MctNode child : root.getChildren()){
						if(child.getActionFromParent() == rootStateObs.getAvatarLastAction()){
							root = child;
							root.resetParent();
							
							// some previously cached states in this node may be invalid
							// this should only be necessary in stochastic games. However, the
							// Digdug game appears to be classified as deterministic games and
							// can cause crashes if this is not done here, so that game might in
							// fact be stochastic
							root.removeCachedStates();	
							
							if(Globals.knowledgeBase.isGameStochastic()){
								// decay tree only in stochastic games
								decayOldTree(root);
							}
							else{
								// still need to tell every node that its depth is reduced now due to Tree Reuse
								decrementTreeDepth(root);
							}
							
							foundChildToReuse = true;
							break;
						}
					}
				}
				
				decayActionStatistics(rootScore > oldRootScore ? 0.0 : actionDecayFactor);
				
				if(!foundChildToReuse){
					// we didn't find a child node matching the played action, so should just start a new tree
					root = new MctNode();
					losingActionSequence = null;
				}
			}
		}
		
		Globals.knowledgeBase.updateRoot(rootStateObs);
		
		if(initBreadthFirst){
			if(!root.isFullyExpanded()){
				// perform a shallow breadth-first search to build up an initial MCTS tree with only safe actions at the root
				generateBreadthFirstTree(rootStateObs, elapsedForSimulationTimer);
			}
			else if(!noTreeReuseBFTI && rootStateObs.getGameTick() != 0){
				// root is already fully expanded, but should still do safety prepruning
				safetyPreprune(root, rootStateObs, elapsedForSimulationTimer);
			}
		}
		
		/*String actionsString = "[";
		for(int i = 0; i < root.getChildren().size(); ++i){
			actionsString += root.getChildren().get(i).getActionFromParent();
			if(i < root.getChildren().size() - 1){
				actionsString += ", ";
			}
		}
		actionsString += "]";
		System.out.println("Playable actions in root: " + actionsString);*/

		int mctsIterations = 0;
		double iterations = 0.0;
		double timeTaken = 0.0;
		
		// because hasTimeLeft() checks for double the average simulation time, initializing this to 6.0 ensures 
		// we'll take a safety margin of 12 milliseconds for the first iteration, useful in cases where generateBreadthFirstTree
		// took a lot of time
		avgSimulationTime = 6.0;
		long elapsedMillisStart = elapsedForSimulationTimer.elapsedMillis();
		long lastElapsedMillis = elapsedMillisStart;
		long maxTimeMillis = elapsedForSimulationTimer.remainingTimeMillis() + lastElapsedMillis;

		//System.out.println("");
		//System.out.println("Time left before MCTS loop = " + elapsedForSimulationTimer.remainingTimeMillis());
		while (hasTimeLeft(elapsedForSimulationTimer, maxTimeMillis, lastElapsedMillis)){
			ONE_STEP_EVAL = 0.0;
			NUM_ADVANCE_OPS = 0;
			
			root.setStateObs(rootStateObs);

			// selection
			final MctNode selectedNode = selectionStrategy.select(root, elapsedForSimulationTimer);
			
			// play-out
			final MctNode playOutEnd = playoutStrategy.runPlayout(selectedNode, elapsedForSimulationTimer);
			
			//if(playOutEnd.getStateObs().isGameOver() && playOutEnd.getStateObs().getGameWinner() == Types.WINNER.PLAYER_LOSES){
			//	++TOTAL_LOSS_ITERATIONS;
			//}
			
			// backpropagation
			backup(playOutEnd, playoutEval.scorePlayout(playOutEnd.getStateObs()), elapsedForSimulationTimer, false);
			
			if(losingActionSequence != null){
				exploreLosingActionSequence(rootStateObs, playOutEnd, elapsedForSimulationTimer);
			}

			// set values for time management
			iterations += (NUM_ADVANCE_OPS / Math.max(10.0, playOutEnd.getDepth()));	// TODO use max playout depth instead of hardcoded 10.0
			++mctsIterations;
			++TOTAL_ITERATIONS;
			lastElapsedMillis = elapsedForSimulationTimer.elapsedMillis();
			timeTaken = lastElapsedMillis - elapsedMillisStart;
			
			if(iterations > 0.0){
				avgSimulationTime = timeTaken / iterations;
			}
			
			//System.out.println("Finished " + iterations + " iterations with avg time of " + avgSimulationTime);
			
			/*String avgScoresString = "[";
			for(int i = 0; i < root.getChildren().size(); ++i){
				avgScoresString += (root.getChildren().get(i).getTotalScore() / root.getChildren().get(i).getNumVisits());
				if(i < root.getChildren().size() - 1){
					avgScoresString += ", ";
				}
			}
			avgScoresString += "]";
			System.out.println("Avg. Scores after iteration " + iterations + ": " + avgScoresString);*/
		}
		
		/*System.out.println("Stopping MCTS with " + elapsedForSimulationTimer.remainingTimeMillis() + "ms left");
		System.out.println("num sims = " + iterations);
		System.out.println("time taken = " + timeTaken);
		System.out.println("avg. sim time = " + avgSimulationTime);
		System.out.println();*/

		MIN_ITERATIONS_PER_GAME = Math.min(mctsIterations, MIN_ITERATIONS_PER_GAME);
		MAX_ITERATIONS_PER_GAME = Math.max(mctsIterations, MAX_ITERATIONS_PER_GAME);
		
		//System.out.println("Ran " + iterations + " iterations");
		
		return moveSelectionStrategy.selectMove(root);
	}
	
	public void backup(MctNode playOutEnd, double score, ElapsedCpuTimer elapsedForSimulationTimer, boolean inescapableLossFound){
		if(exploreLosses && losingActionSequence == null && !playOutEnd.getParent().isFullyExpanded()){
			StateObservation endState = playOutEnd.getStateObs();
			if(endState.isGameOver() && endState.getGameWinner() == WINNER.PLAYER_LOSES && hasTimeLeft(elapsedForSimulationTimer)){
				losingActionSequence = new ArrayList<ACTIONS>(playOutEnd.getDepth());
				
				MctNode updateNode = playOutEnd;
				while(updateNode != null){
					if(updateNode.getParent() != null){
						losingActionSequence.add(updateNode.getActionFromParent());
					}
					
					updateNode = updateNode.getParent();
				}
				
				losingState = endState;
				
				return;
			}
		}
		
		if((score == rootScore || alwaysKB) && !playOutEnd.getStateObs().isGameOver() && allowsKnowledgeBasedEvaluation()){
			score += Globals.knowledgeBase.knowledgeBasedEval(playOutEnd.getStateObs());
		}
		
		score += ONE_STEP_EVAL;
		
		MAX_SCORE = Math.max(MAX_SCORE, score);
		MIN_SCORE = Math.min(MIN_SCORE, score);
		
		boolean loss = (playOutEnd.getStateObs().isGameOver() && playOutEnd.getStateObs().getGameWinner() == WINNER.PLAYER_LOSES);
		
		if(loss){
			playOutEnd.setImmediateLossDetected();
		}
		
		MctNode updateNode = playOutEnd;
		while (updateNode != null) {
			if(loss && !updateNode.canBeImmediateLoss() && updateNode.hasNonImmediateLossChildren()){
				// the node to be updated has better (non-losing) options, so from now on we're pretending the loss didn't happen
				loss = false;
				score += Globals.HUGE_ENDGAME_SCORE;
			}
			else if(loss){
				// the node to be updated has been proven to have a nonzero probability of leading to a loss with perfect play
				updateNode.setImmediateLossDetected();
			}
			
			updateNode.backpropagate(score);
			
			if(inescapableLossFound){
				updateNode.setInescapableLossFound();
			}
			
			if(updateNode.getParent() != null && collectActionStatistics){
				// we want to collect statistics for the played action
				final ActionLocation action = updateNode.getActionLocationFromParent();
				
				Score actionScore = actionStatistics.get(action);
				if(actionScore == null){
					actionScore = new Score();
					actionStatistics.put(action, actionScore);
				}
				
				actionScore.score += score;
				actionScore.timesVisited += 1.0;
				
				MIN_ACTION_SCORE = Math.min(MIN_ACTION_SCORE, score);
				MAX_ACTION_SCORE = Math.max(MAX_ACTION_SCORE, score);
				
				if(maxActionNGramSize > 1){
					// we also want to collect statistics for n-grams
					for(int n = 2; n <= maxActionNGramSize; ++n){
						// create n-gram of size n
						final ActionLocation[] nGram = new ActionLocation[n];
						MctNode currentActionNode = updateNode;
						boolean finishedNGram = true;
						
						for(int actionIdx = n - 1; actionIdx >= 0; --actionIdx){
							if(currentActionNode.getParent() == null){
								// tree is not deep enough for an n-gram of this size
								finishedNGram = false;
								break;
							}
							
							nGram[actionIdx] = currentActionNode.getActionLocationFromParent();
							currentActionNode = currentActionNode.getParent();
						}
						
						if(!finishedNGram){
							break;
						}
						
						final ActionNGram actionNGram = new ActionNGram(nGram);
						Score actionNGramScore = actionNGramStatistics.get(actionNGram);
						if(actionNGramScore == null){
							actionNGramScore = new Score();
							actionNGramStatistics.put(actionNGram, actionNGramScore);
						}
						
						actionNGramScore.score += score;
						actionNGramScore.timesVisited += 1.0;
					}
				}
			}
			
			updateNode.postBackup();
			updateNode = updateNode.getParent();
		}
	}
	
	// TODO don't re-use tree if the avatar type changed? or maybe only if the set of legal actions changed?
	// we only generate child nodes the first time currently when we need children of a node, so we don't correctly
	// handle changed action sets
	public void decayOldTree(MctNode root){
		// decay the old results in all nodes of the tree
		ArrayList<MctNode> nodes = new ArrayList<MctNode>();
		nodes.add(root);
		
		//int nodesDecayed = 0;
		
		while(!nodes.isEmpty()){
			//++nodesDecayed;
			
			// pop a node from the list of nodes to process
			MctNode node = nodes.remove(nodes.size() - 1);
			
			// we'll also have to process all children of the current node
			nodes.addAll(node.getChildren());
			
			// decay this node
			node.decay(treeDecayFactor);
			
			// we want to reset results from novelty tests close to root, since they might've been based on states
			// that are no longer reachable in non-deterministic games.
			if(node.getDepth() <= 1){
				node.resetNoveltyTestResults();
			}
		}
		
		//System.out.println("Decayed " + nodesDecayed + " nodes!");
	}
	
	public void decrementTreeDepth(MctNode root){
		ArrayList<MctNode> nodes = new ArrayList<MctNode>();
		nodes.add(root);
				
		while(!nodes.isEmpty()){
			// pop a node from the list of nodes to process
			MctNode node = nodes.remove(nodes.size() - 1);
			
			// we'll also have to process all children of the current node
			nodes.addAll(node.getChildren());
			
			// decrement depth of the node
			node.decrementDepth();
		}
	}
	
	public boolean allowsKnowledgeBasedEvaluation(){
		return knowledgeBasedEval;
	}
	
	public boolean allowsNoveltyBasedPruning(){
		return noveltyBasedPruning;
	}
	
	public boolean collectsActionStatistics(){
		return collectActionStatistics;
	}
	
	public void decayActionStatistics(double decayFactor){
		if(collectActionStatistics){
			if(decayFactor == 0.0){
				// this is probably faster than iterating through everything
				actionStatistics.clear();
				
				if(maxActionNGramSize > 1){
					actionNGramStatistics.clear();
				}
			}
			else{
				for(Score actionScore : actionStatistics.values()){
					actionScore.decay(decayFactor);
				}
				
				if(maxActionNGramSize > 1){
					for(Score actionNGramScore : actionNGramStatistics.values()){
						actionNGramScore.decay(decayFactor);
					}
				}
			}
		}
	}
	
	public void exploreLosingActionSequence(StateObservation rootStateObs, MctNode losingNode, ElapsedCpuTimer elapsedForSimulationTimer){
		MctNode node = root;
		StateObservation state = rootStateObs;
		StateObs stateObs = new StateObs(state, true);
		
		// execute all but the last action of the losing sequence
		for(int i = losingActionSequence.size() - 1; i > 0; --i){
			ACTIONS nextAction = losingActionSequence.get(i);
			node = node.getExpandedChildForAction(nextAction);
			stateObs = node.generateNewStateObs(stateObs, nextAction);
			state = stateObs.getStateObsNoCopy();
			
			if(state.isGameOver()){
				// already encountered a terminal state earlier this time, probably an unstable line of play
				// so we'll just backpropagate the loss
				
				// pretending that we found inescapable loss since this seems to be an unstable situation, probably want
				// to turn off NBP
				backup(losingNode, playoutEval.scorePlayout(losingNode.getStateObs()), elapsedForSimulationTimer, true);
				losingActionSequence = null;
				losingState = null;
				return;
			}
		}
		
		MctNode preLossNode = losingNode.getParent();
		ArrayList<ACTIONS> actions = state.getAvailableActions();
		StateObservation[] states = new StateObservation[actions.size()];
		MctNode[] nodes = new MctNode[actions.size()];
		double[] evals = new double[actions.size()];
		
		int bestIdx = -1;
		double maxEval = Double.NEGATIVE_INFINITY;
		ACTIONS losingAction = losingActionSequence.get(0);
		
		for(int i = 0; i < actions.size(); ++i){
			ACTIONS action = actions.get(i);
			
			if(action == losingAction){
				states[i] = losingState;
			}
			else{
				states[i] = state.copy();	// TODO can probably afford to do one less copy here? the last copy is redundant?
				states[i].advance(action);
			}
			
			MctNode child = preLossNode.getExpandedChildForAction(action);
			if(child != null){
				// the node already has an expanded child for this action
				nodes[i] = child;
				nodes[i].setStateObs(states[i]);
				nodes[i].cacheStateObservation(states[i]);
			}
			else{
				// the node did not yet have an expanded child for this action
				nodes[i] = new MctNode(preLossNode, action);
				nodes[i].setStateObs(states[i]);
				nodes[i].cacheStateObservation(states[i]);
				preLossNode.addChild(nodes[i]);
			}
			
			double eval = playoutEval.scorePlayout(states[i]);
			evals[i] = eval;
			MAX_SCORE = Math.max(MAX_SCORE, eval);
			MIN_SCORE = Math.min(MIN_SCORE, eval);
			
			if(eval > maxEval){
				maxEval = eval;
				bestIdx = i;
			}
		}
		
		preLossNode.getUnexpandedActions().clear();
		
		for(int i = 0; i < actions.size(); ++i){
			if(i == bestIdx){
				boolean inescapableLossFound = (states[i].isGameOver() && states[i].getGameWinner() == WINNER.PLAYER_LOSES);
				backup(nodes[i], evals[i], elapsedForSimulationTimer, inescapableLossFound);
			}
			else{
				nodes[i].backpropagate(evals[i], 1.0 / evals.length);
			}
		}
		
		losingActionSequence = null;
		losingState = null;
	}
	
	public MctNode getRoot(){
		return root;
	}
	
	/**
	 * Returns the score for playing the given action
	 * 
	 * @param action
	 * @return
	 */
	public Score getActionScore(ActionLocation action){
		Score score = actionStatistics.get(action);
		
		if(score == null){
			score = new Score();
			actionStatistics.put(action, score);
		}
		
		return score;
	}
	
	/**
	 * Returns the score for playing the given n-gram of actions
	 * 
	 * @param nGram
	 * @return
	 */
	public Score getActionNGramScore(ActionNGram nGram){
		Score score = actionNGramStatistics.get(nGram);
		
		if(score == null){
			score = new Score();
			actionNGramStatistics.put(nGram, score);
		}
		
		return score;
	}
	
	public double getRootEvaluation(){
		return rootScore;
	}
	
	private void generateBreadthFirstTree(StateObservation rootStateObs, ElapsedCpuTimer elapsedTimer){
		root.setStateObs(rootStateObs);
		
		// create list of children of the root node with safety prepruning
		ArrayList<ACTIONS> availableActions = rootStateObs.getAvailableActions();
		int numRootActions = availableActions.size();
		int[] numGameLosses = new int[numRootActions];
		
		// will keep sum of scores collected in this array so we can initialize MCTS node with an average score
		double[] scoreSums = new double[numRootActions];
		int numSafetyChecksCompleted = 0;
		
		// will keep generated state observations here so we can cache them in the nodes and re-use them later in MCTS
		@SuppressWarnings("unchecked")
		ArrayList<StateObservation>[] stateObservations = new ArrayList[numRootActions];
		for(int i = 0; i < numRootActions; ++i){
			stateObservations[i] = new ArrayList<StateObservation>(MAX_NUM_SAFETY_CHECKS);
		}
		
		long maxDurationSafetyCheckRound = 0L;
		for(int safetyCheckRound = 0; safetyCheckRound < MAX_NUM_SAFETY_CHECKS; ++safetyCheckRound){
			ElapsedCpuTimer roundTimer = new ElapsedCpuTimer();
			for(int actionIdx = 0; actionIdx < numRootActions; ++actionIdx){				
				StateObservation successor = rootStateObs.copy();
				successor.advance(availableActions.get(actionIdx));
				
				if(successor.isGameOver() && successor.getGameWinner() == WINNER.PLAYER_LOSES){
					numGameLosses[actionIdx] += 1;
				}
				
				scoreSums[actionIdx] += playoutEval.scorePlayout(successor);
				stateObservations[actionIdx].add(successor);
				
				if(safetyCheckRound == 0 && knowledgeBasedEval){
					// in the first round, also add KB eval
					scoreSums[actionIdx] += Globals.knowledgeBase.knowledgeBasedEval(successor);
				}
			}
			
			++numSafetyChecksCompleted;
			maxDurationSafetyCheckRound = Math.max(maxDurationSafetyCheckRound, roundTimer.elapsedMillis());
			long remainingMillis = elapsedTimer.remainingTimeMillis();
			
			if(remainingMillis < TIME_BUFFER_MILLISEC || remainingMillis < maxDurationSafetyCheckRound){
				break;
			}
		}
		
		int lowestNumGameLosses = MAX_NUM_SAFETY_CHECKS;
		TIntArrayList safeActions = new TIntArrayList(numRootActions, -1);
		for(int actionIdx = 0; actionIdx < numRootActions; ++actionIdx){
			if(numGameLosses[actionIdx] < lowestNumGameLosses){
				lowestNumGameLosses = numGameLosses[actionIdx];
				safeActions.resetQuick();
				safeActions.add(actionIdx);
			}
			else if(numGameLosses[actionIdx] == lowestNumGameLosses){
				safeActions.add(actionIdx);
			}
		}
		
		ArrayList<MctNode> newRootChildren = new ArrayList<MctNode>(safeActions.size());
		for(int i = 0; i < safeActions.size(); ++i){
			int safeActionIdx = safeActions.getQuick(i);
			ACTIONS action = availableActions.get(safeActionIdx);
			
			MctNode childNode = root.getExpandedChildForAction(action);
			if(childNode == null){
				childNode = new MctNode(root, action);
			}
			
			// we'll initialize the node with a single visit and the average score obtained among all safety checks
			double avgScore = scoreSums[safeActionIdx] / numSafetyChecksCompleted;
			childNode.backpropagate(avgScore);
			
			// for every time that we pretend a child of the root node was visited, we also pretend that the root was visited
			// (this keeps the counters consistent)
			root.backpropagate(avgScore);
			
			MAX_SCORE = Math.max(MAX_SCORE, avgScore);
			MIN_SCORE = Math.min(MIN_SCORE, avgScore);
			
			childNode.cacheStateObservations(stateObservations[safeActionIdx]);
			
			newRootChildren.add(childNode);
		}
		
		root.getUnexpandedActions().clear();
		root.getChildren().clear();
		root.getChildren().addAll(newRootChildren);
	} 
	
	/**
	 * Performs safety prepruning on the children of the given node. The node is expected to
	 * already be fully expanded
	 * 
	 * @param node
	 * @param state
	 */
	public void safetyPreprune(MctNode node, StateObservation state, ElapsedCpuTimer elapsedTimer){
		node.setStateObs(state);
		
		ArrayList<MctNode> children = node.getChildren();
		int numActions = children.size();
		int[] numGameLosses = new int[numActions];
		
		// will keep generated state observations here so we can cache them in the nodes and re-use them later in MCTS
		@SuppressWarnings("unchecked")
		ArrayList<StateObservation>[] stateObservations = new ArrayList[numActions];
		for(int i = 0; i < numActions; ++i){
			stateObservations[i] = new ArrayList<StateObservation>(MAX_NUM_SAFETY_CHECKS);
		}

		long maxDurationSafetyCheckRound = 0L;
		for(int safetyCheckRound = 0; safetyCheckRound < MAX_NUM_SAFETY_CHECKS; ++safetyCheckRound){
			ElapsedCpuTimer roundTimer = new ElapsedCpuTimer();
			for(int actionIdx = 0; actionIdx < numActions; ++actionIdx){				
				StateObservation successor = state.copy();
				successor.advance(children.get(actionIdx).getActionFromParent());	// TODO also use these states to update knowledge base?
				
				if(successor.isGameOver() && successor.getGameWinner() == WINNER.PLAYER_LOSES){
					numGameLosses[actionIdx] += 1;
				}
				
				stateObservations[actionIdx].add(successor);
			}
			
			maxDurationSafetyCheckRound = Math.max(maxDurationSafetyCheckRound, roundTimer.elapsedMillis());
			long remainingMillis = elapsedTimer.remainingTimeMillis();
			
			if(remainingMillis < TIME_BUFFER_MILLISEC || remainingMillis < maxDurationSafetyCheckRound){
				break;
			}
		}
		
		int lowestNumGameLosses = MAX_NUM_SAFETY_CHECKS;
		for(int actionIdx = 0; actionIdx < numActions; ++actionIdx){
			if(numGameLosses[actionIdx] < lowestNumGameLosses){
				lowestNumGameLosses = numGameLosses[actionIdx];
			}
		}
		
		// collect indices of children that should be removed
		TIntArrayList toRemove = new TIntArrayList(numActions, -1);
		for(int i = 0; i < numActions; ++i){
			if(numGameLosses[i] > lowestNumGameLosses){
				toRemove.add(i);
			}
			else{
				// the child at index i is not gonna be removed, so we can cache the generated states for that node
				children.get(i).cacheStateObservations(stateObservations[i]);
			}
		}
		
		// remove the children at the indices that we just collected, in reverse order because that's most efficient
		for(int i = toRemove.size() - 1; i >= 0; --i){
			children.remove(toRemove.getQuick(i));
		}
	}
	
	@Override
	public void result(StateObservation stateObservation, ElapsedCpuTimer elapsedCpuTimer){
		//System.out.println("avg iterations = " + (double)TOTAL_ITERATIONS / stateObservation.getGameTick());
	}
	
	public boolean hasTimeLeft(ElapsedCpuTimer timer, long maxTimeMillis, long elapsedMillis){
		long timeRemaining = maxTimeMillis - elapsedMillis;
		return (timeRemaining > 2.0 * avgSimulationTime 	&&
				timeRemaining > TIME_BUFFER_MILLISEC			);
	}
	
	public boolean hasTimeLeft(ElapsedCpuTimer timer){
		long timeRemaining = timer.remainingTimeMillis();
		return (timeRemaining > 2.0 * avgSimulationTime 	&&
				timeRemaining > TIME_BUFFER_MILLISEC			);
	}
	
	/**
	 * Calling this function from modified version of framework to do any expensive debugging outside of
	 * the normal processing time.
	 * 
	 * @param stateObservation
	 */
	public static void debugFunction(StateObservation stateObservation){
		if(stateObservation.getGameTick() % 10 == 0){
			//MctsVisualizer.generateGraphFile(((MctsController)Agent.controller).root);
		}
	}
}
