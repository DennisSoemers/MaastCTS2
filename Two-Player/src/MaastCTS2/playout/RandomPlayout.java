package MaastCTS2.playout;

import java.util.ArrayList;
import java.util.HashMap;

import MaastCTS2.Agent;
import MaastCTS2.Globals;
import MaastCTS2.controller.MctsController;
import MaastCTS2.model.MctNode;
import MaastCTS2.model.StateObs;
import core.game.StateObservationMulti;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

/**
 * Random play-out strategy
 * 
 * <p> NOTE: This class  has not been used for a long time, and may not include all the special cases
 * and/or heuristics that have been added to NstPlayout in the meantime
 * 
 * @author Dennis Soemers
 *
 */
public class RandomPlayout implements IPlayoutStrategy {
	private int maxPlayoutDepth;

	public RandomPlayout(int maxPlayoutDepth) {
		this.maxPlayoutDepth = maxPlayoutDepth;
	}

	@Override
	public MctNode runPlayout(MctNode node, ElapsedCpuTimer elapsedTimer) {
		StateObservationMulti state = node.getStateObs();
		StateObs stateObs;
		if(state == node.getSavedStateObs()){
			stateObs = new StateObs(state, true);	// last node of selection step has a closed-loop-style saved state
		}
		else{	// doing normal open-loop stuff
			stateObs = new StateObs(state, false);
		}
		
		int depth = 0;
		
		HashMap<Integer, Integer> previousResources = state.getAvatarResources(Agent.myID);
		HashMap<Integer, Integer> nextResources;
		
		for (/**/; depth < maxPlayoutDepth; ++depth) {
			if (state.isGameOver()) {
				break;
			}
			
			double previousScore = state.getGameScore(Agent.myID);
			int previousNumEvents = state.getEventsHistory().size();
			Vector2d previousAvatarPos = state.getAvatarPosition(Agent.myID);
			Vector2d previousAvatarOrientation = state.getAvatarOrientation(Agent.myID);
			
			ArrayList<ACTIONS> unexpandedActions = node.getUnexpandedActions();
			ACTIONS randomAction = unexpandedActions.remove(Globals.RNG.nextInt(unexpandedActions.size()));

			MctNode newNode = new MctNode(node, randomAction);
			stateObs = newNode.generateNewStateObs(stateObs, randomAction);
			state = stateObs.getStateObsNoCopy();
			
			node.addChild(newNode);
			node = newNode;
			
			nextResources = state.getAvatarResources(Agent.myID);
			Globals.knowledgeBase.addEventKnowledge(previousScore, previousNumEvents, previousAvatarPos, 
													previousAvatarOrientation, randomAction, state, 
													previousResources, nextResources, true);
			previousResources = nextResources;
		}
		
		return node;
	}
	
	@Override
	public int getDesiredActionNGramSize() {
		return -1;
	}

	@Override
	public String getName() {
		return "RandomPlayout";
	}

	@Override
	public String getConfigDataString() {
		return "maxPlayoutDepth=" + this.maxPlayoutDepth;
	}

	@Override
	public void init(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, MctsController mctsController) {
		// no initialization necessary
	}

	@Override
	public boolean wantsActionStatistics(){
		return false;
	}
}
