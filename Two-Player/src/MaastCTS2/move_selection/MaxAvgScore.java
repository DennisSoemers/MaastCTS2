package MaastCTS2.move_selection;

import java.util.ArrayList;

import MaastCTS2.Globals;
import MaastCTS2.model.MctNode;
import ontology.Types.ACTIONS;

/**
 * Strategy that recommends the agent to play the move corresponding to the node with
 * the highest average score.
 * 
 * <p> Ties are broken by selecting actions with the highest number of visits, because
 * we are more certain about those scores being correct, and it allows for better tree reuse.
 * 
 * <p> This class actually is no longer a clean implementation that only maximizes the average score,
 * but also has some other special cases.
 *
 * @author Dennis Soemers
 */
public class MaxAvgScore implements IMoveSelectionStrategy {

	@Override
	public ACTIONS selectMove(MctNode root){
		ArrayList<MctNode> children = root.getChildren();	
		
		ACTIONS bestAction = ACTIONS.ACTION_NIL;
		double bestAvgScore = Double.NEGATIVE_INFINITY;
		double bestActionNumVisits = Double.NEGATIVE_INFINITY;
		double bestActionRandomTieBreaker = Double.NEGATIVE_INFINITY;
		int numChildren = children.size();
		
		/*NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat scoreFormatter = (DecimalFormat)nf;
		scoreFormatter.applyPattern("#000.000000000");
		
		String scores = "Avg. Scores	= [";
		String visits = "Visits		= [";
		String actions = "Actions		= [";
		MctsController controller = (MctsController)Agent.controller;
		for(int i = 0; i < numChildren; ++i){
			MctNode child = children.get(i);
			double numVisits = child.getNumVisits();
			double avgScore = child.getTotalScore() / numVisits;
			avgScore = Globals.normalise(avgScore, controller.MIN_SCORE, controller.MAX_SCORE);
			
			if(i < numChildren - 1){
				scores += scoreFormatter.format(avgScore) + ", ";
				visits += scoreFormatter.format(numVisits) + ", ";
				actions += child.getActionFromParent() + ", ";
			}
			else{
				scores += scoreFormatter.format(avgScore);
				visits += scoreFormatter.format(numVisits);
				actions += child.getActionFromParent();
			}
		}
		scores += "]";
		visits += "]";
		actions += "]";
		System.out.println(scores);
		System.out.println(visits);
		System.out.println(actions);*/
		//System.out.println();
		
		for(int i = 0; i < numChildren; ++i){
			MctNode child = children.get(i);
			double numVisits = child.getNumVisits();
			double avgScore;
			
			//if(Globals.knowledgeBase.isGameDeterministic()){
			//	avgScore = child.getMaxScore();		// can simply go for max score ever observed in deterministic games
			//}
			//else{
				avgScore = child.getTotalScore() / numVisits;
			//}
			
			if(avgScore > bestAvgScore){	// new best score
				//System.out.println("New best child because score: " + i + ". " + avgScore + " > " + (bestAvgScore));
				bestAvgScore = avgScore;
				bestActionNumVisits = numVisits;
				bestActionRandomTieBreaker = Globals.smallNoise();
				bestAction = child.getActionFromParent();
			}
			else if(avgScore >= bestAvgScore){
				if(numVisits > bestActionNumVisits){	// num visits as tie-breaker
					//System.out.println("New best child because visits: " + i + ". " + numVisits + " > " + (bestActionNumVisits));
					bestAvgScore = Math.max(avgScore, bestAvgScore);
					bestActionNumVisits = numVisits;
					bestActionRandomTieBreaker = Globals.smallNoise();
					bestAction = child.getActionFromParent();
				}
				else if(avgScore >= bestAvgScore && numVisits == bestActionNumVisits){
					double randomTieBreaker = Globals.smallNoise();
					if(randomTieBreaker > bestActionRandomTieBreaker){	// randomly break ties
						//System.out.println("New best child because random tiebreaker: " + i);
						bestAvgScore = Math.max(avgScore, bestAvgScore);
						bestActionNumVisits = numVisits;
						bestActionRandomTieBreaker = randomTieBreaker;
						bestAction = child.getActionFromParent();
					}
				}
			}
		}
		
		//System.out.println("Best action = " + bestAction);
		//System.out.println();
		
		return bestAction;
	}
	
	@Override
	public String getName() {
		return "MaxAvgScore";
	}

	@Override
	public String getConfigDataString() {
		return "";
	}
}
