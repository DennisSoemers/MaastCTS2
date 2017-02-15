package test.config;

import MaastCTS2.Agent;
import MaastCTS2.controller.MctsController;
import MaastCTS2.heuristics.states.IPlayoutEvaluation;
import MaastCTS2.move_selection.IMoveSelectionStrategy;
import MaastCTS2.playout.IPlayoutStrategy;
import MaastCTS2.selection.ISelectionStrategy;
import core.competition.CompetitionParameters;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import tools.ElapsedCpuTimer;

/**
 * Copy of the MctsTestConfig class written by Torsten Schuster, but changed to use implementations
 * from the DennisSoemers package (instead of the MaasCTS package)
 *
 * @author Dennis Soemers
 */
public class DennisMctsTestConfig extends TestConfig {
	private ISelectionStrategy selectionStrategy;
	private IPlayoutStrategy playoutStrategy;
	private IMoveSelectionStrategy moveSelectionStrategy;
	private IPlayoutEvaluation playoutEval;
	private boolean initBreadthFirst;
	private boolean noveltyBasedPruning;
	private boolean exploreLosses;
	private boolean knowledgeBasedEval;
	private boolean detectDeterministicGames;
	private boolean treeReuse;
	private String name;
	private double treeReuseGamma;
	private int maxNumSafetyChecks;
	private boolean alwaysKB;
	private boolean noTreeReuseBFTI;
	
	private AbstractPlayer player = null;

	public DennisMctsTestConfig(String name, ISelectionStrategy selectionStrategy,
								IPlayoutStrategy playoutStrategy, IMoveSelectionStrategy moveSelectionStrategy, 
								IPlayoutEvaluation playoutEval, boolean initBreadthFirst, boolean noveltyBasedPruning,
								boolean exploreLosses, boolean knowledgeBasedEval, boolean detectDeterministicGames,
								boolean treeReuse, double treeReuseGamma, int maxNumSafetyChecks, boolean alwaysKB, boolean noTreeReuseBFTI) {
		this.name = name;
		this.selectionStrategy = selectionStrategy;
		this.playoutStrategy = playoutStrategy;
		this.moveSelectionStrategy = moveSelectionStrategy;
		this.playoutEval = playoutEval;
		this.initBreadthFirst = initBreadthFirst;
		this.noveltyBasedPruning = noveltyBasedPruning;
		this.exploreLosses = exploreLosses;
		this.knowledgeBasedEval = knowledgeBasedEval;
		this.detectDeterministicGames = detectDeterministicGames;
		this.treeReuse = treeReuse;
		this.treeReuseGamma = treeReuseGamma;
		this.maxNumSafetyChecks = maxNumSafetyChecks;
		this.alwaysKB = alwaysKB;
		this.noTreeReuseBFTI = noTreeReuseBFTI;
	}

	@Override
	public AbstractPlayer createAgent(String actionFile, StateObservation stateObs, int randomSeed) {
		try {

			// Determine the time due for the controller creation.
			ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
			ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

			player = new Agent(stateObs, ect.copy(), selectionStrategy, playoutStrategy, 
								moveSelectionStrategy, playoutEval, initBreadthFirst,
								noveltyBasedPruning, exploreLosses, knowledgeBasedEval,
								detectDeterministicGames, treeReuse, treeReuseGamma,
								maxNumSafetyChecks, alwaysKB, noTreeReuseBFTI);

			// Check if we returned on time, and act in consequence.
			long timeTaken = ect.elapsedMillis();
			if (ect.exceededMaxTime()) {
				long exceeded = -ect.remainingTimeMillis();
				System.out.println("Controller initialization time out ("
						+ exceeded + ").");

				return null;
			} 
			else {
				System.out.println("Controller initialization time: "
						+ timeTaken + " ms.");
			}

			// This code can throw many exceptions (no time related):

			if (player != null){
				player.setup(actionFile, randomSeed, false);
			}
		} 
		catch (Exception e) {
			// This probably happens because controller took too much time to be
			// created.
			e.printStackTrace();
			System.exit(1);
		}

		return player;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getAdditionalLogData(char c) {
		return this.selectionStrategy.getName() + c
				+ this.selectionStrategy.getConfigDataString() + c
				+ this.playoutStrategy.getName() + c
				+ this.playoutStrategy.getConfigDataString() + c
				+ this.moveSelectionStrategy.getName() + c
				+ this.moveSelectionStrategy.getConfigDataString() + c
				+ this.playoutEval.getName() + c
				+ this.playoutEval.getConfigDataString() + c
				+ "Total_Iterations=" + MctsController.TOTAL_ITERATIONS + c
				+ "Min_Iterations=" + MctsController.MIN_ITERATIONS_PER_GAME + c
				+ "Max_Iterations=" + MctsController.MAX_ITERATIONS_PER_GAME;/* + c
				+ "Total_Loss_Iterations=" + MctsController.TOTAL_LOSS_ITERATIONS;*/
	}
}
