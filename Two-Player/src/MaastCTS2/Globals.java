package MaastCTS2;

import java.util.ArrayList;
import java.util.EnumMap;

import MaastCTS2.controller.MctsController;
import MaastCTS2.libs.it.unimi.dsi.util.XorShift64StarRandom;
import MaastCTS2.model.MctNode;
import MaastCTS2.utils.OrderedIntPair;
import core.game.StateObservationMulti;
import ontology.Types.ACTIONS;

/**
 * Some globals used throughout the entire agent's code
 *
 * @author Dennis Soemers
 */
public class Globals {
	
	/** A large constant score added to winning game states and subtracted from losing game states */
	public static final double HUGE_ENDGAME_SCORE = 10000000.0;
	
	/** Random Number Generator */
	public static final XorShift64StarRandom RNG = new XorShift64StarRandom();
	
	/** Knowledge Base for the current game/level */
	public static KnowledgeBase knowledgeBase;
	
	/** If true, we'll draw some stuff for debugging */
	public static boolean DEBUG_DRAW = false;
	
	/**
	 * Generates an array of actions for all players. We will play the given action,
	 * and the other player selects an action based on the statistics in the given EnumMaps
	 * 
	 * @param myAction
	 * @param state
	 * @return
	 */
	public static ACTIONS[] generateActionArray(ACTIONS myAction, StateObservationMulti state,
												EnumMap<ACTIONS, Double> otherPlayerActionScores, 
												EnumMap<ACTIONS, Double> otherPlayerNumVisits, double parentNumVisits, MctNode node){
		ACTIONS[] actions = new ACTIONS[Agent.numPlayers];
		
		for(int i = 0; i < actions.length; ++i){
			if(i == Agent.myID){
				actions[i] = myAction;
			}
			else{
				ArrayList<ACTIONS> otherAvailableActions = state.getAvailableActions(i);
				if(otherAvailableActions.size() > 1){
					otherAvailableActions.remove(ACTIONS.ACTION_NIL);
				}
				
				// hardcoding some UCT for modelling opponent's action selection in here
				// it's not very clean, but it works, and competition deadline is getting close,
				// so I don't care too much about clean coding
				if(otherPlayerActionScores == null || otherPlayerNumVisits == null){
					// random action
					ACTIONS randomAction = otherAvailableActions.get(RNG.nextInt(otherAvailableActions.size()));
					node.lastSimmedActionOtherPlayer = randomAction;
					actions[i] = randomAction;
				}
				else{
					MctsController controller = (MctsController) Agent.controller;
					final double MIN_SCORE = controller.MIN_SCORE;
					final double MAX_SCORE = controller.MAX_SCORE;
					
					// epsilon-greedy opponent model
					ACTIONS bestAction = ACTIONS.ACTION_NIL;
					if(RNG.nextDouble() < 0.5){
						bestAction = otherAvailableActions.get(RNG.nextInt(otherAvailableActions.size()));
					}
					else{
						double bestActionScore = Double.NEGATIVE_INFINITY;
						
						for(int idx = 0; idx < otherAvailableActions.size(); ++idx){
							ACTIONS action = otherAvailableActions.get(idx);
							double n_i = Math.max(otherPlayerNumVisits.get(action), 0.00001);
							
							double avgScore = otherPlayerActionScores.get(action) / n_i;
							avgScore = Globals.normalise(avgScore, MIN_SCORE, MAX_SCORE);
							avgScore += Globals.smallNoise();
							
							if(avgScore > bestActionScore){
								bestAction = action;
								bestActionScore = avgScore;
							}
						}
					}
					
					// UCT opponent model
					/*
					double log_n = Math.max(0.0, Math.log(parentNumVisits));
					
					ACTIONS bestAction = ACTIONS.ACTION_NIL;
					double bestUctVal = Double.NEGATIVE_INFINITY;
					
					for(int idx = 0; idx < otherAvailableActions.size(); ++idx){
						ACTIONS action = otherAvailableActions.get(idx);
						double n_i = Math.max(otherPlayerNumVisits.get(action), 0.00001);
						
						double avgScore = otherPlayerActionScores.get(action) / n_i;
						avgScore = Globals.normalise(avgScore, MIN_SCORE, MAX_SCORE);
						
						double uctVal = avgScore + 1.4 * Math.sqrt(log_n / n_i);
						uctVal += Globals.smallNoise();

						if (uctVal > bestUctVal) {
							bestUctVal = uctVal;
							bestAction = action;
						}
					}*/
					
					node.lastSimmedActionOtherPlayer = bestAction;
					actions[i] = bestAction;
				}				
			}
		}
		
		return actions;
	}
	
	public static boolean isMovementAction(ACTIONS action){
		return (action != ACTIONS.ACTION_ESCAPE &&
				action != ACTIONS.ACTION_NIL &&
				action != ACTIONS.ACTION_USE);
	}
	
	public static boolean isOppositeMovement(ACTIONS a1, ACTIONS a2){
		if(a1 == ACTIONS.ACTION_DOWN){
			return (a2 == ACTIONS.ACTION_UP);
		}
		else if(a1 == ACTIONS.ACTION_LEFT){
			return (a2 == ACTIONS.ACTION_RIGHT);
		}
		else if(a1 == ACTIONS.ACTION_RIGHT){
			return (a2 == ACTIONS.ACTION_LEFT);
		}
		else if(a1 == ACTIONS.ACTION_UP){
			return (a2 == ACTIONS.ACTION_DOWN);
		}
		
		return false;
	}
	
	/**
	 * Computes the Manhattan distance between two given cells
	 * 
	 * @param cell1
	 * @param cell2
	 * @return
	 */
	public static int manhattanDistance(OrderedIntPair cell1, OrderedIntPair cell2){
		return (Math.abs(cell1.first - cell2.first) + Math.abs(cell1.second - cell2.second));
	}
	
	public static int minHorizontalOrVerticalDist(OrderedIntPair cell1, OrderedIntPair cell2){
		return Math.min(Math.abs(cell1.first - cell2.first), Math.abs(cell1.second - cell2.second));
	}
	
	/**
	 * Normalises the given value to lie in [0.0, 1.0] given the provided
	 * 'min' and 'max' bounds.
	 * 
	 * <p> Different from Utils::normalise() implementation of the GVG framework
	 * in that this method returns a value of 0.5 if min >= max, instead of returning
	 * the given value.
	 * 
	 * @param value
	 * @param min
	 * @param max
	 * @return
	 */
	public static double normalise(double value, double min, double max){
		if(min < max){
			return (value - min) / (max - min);
		}
		else{
			return 0.5;
		}
	}
	
	/**
	 * Returns a small number in [-0.0000005, 0.0000005] for noise
	 * 
	 * @return
	 */
	public static double smallNoise(){
		return (RNG.nextDouble() - 0.5) * 0.000001;
	}

}
