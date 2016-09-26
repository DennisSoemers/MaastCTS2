package MaastCTS2;

import java.awt.Graphics2D;

import MaastCTS2.controller.IController;
import MaastCTS2.controller.MctsController;
import MaastCTS2.heuristics.states.GvgAiEvaluation;
import MaastCTS2.move_selection.MaxAvgScore;
import MaastCTS2.playout.NstPlayout;
import MaastCTS2.selection.ol.ProgressiveHistory;
import core.game.StateObservationMulti;
import core.player.AbstractMultiPlayer;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

public class Agent extends AbstractMultiPlayer {
	public static IController controller;
	
	public static int myID;
	public static int otherID;
	public static int numPlayers;

	/**
	 * constructor for competition
	 * 
	 * @param so
	 * @param elapsedTimer
	 */
	public Agent(StateObservationMulti so, ElapsedCpuTimer elapsedTimer, int playerId) {
		MctsController.TIME_BUFFER_MILLISEC = 10;
		myID = playerId;
		numPlayers = so.getNoPlayers();
		otherID = (myID + 1) % numPlayers;
		
		// turned off Loss Avoidance and Novelty-Based Pruning since I doubt they'd work well in adversarial games
		// they might be nice in cooperative games though, but dont have time to test that before submission deadline
		controller = new MctsController(new ProgressiveHistory(0.6, 1.0), new NstPlayout(10, 0.5, 7.0, 3), 
				new MaxAvgScore(), new GvgAiEvaluation(), true, false, false, true, true, 0.6, 3, true, false);
		controller.init(so, elapsedTimer);
	}

	@Override
	public ACTIONS act(StateObservationMulti stateObs, ElapsedCpuTimer elapsedTimer) {
		Globals.knowledgeBase.update(stateObs);
		return controller.chooseAction(stateObs, elapsedTimer);
	}
	
	@Override
	public void draw(Graphics2D g){
		if(Globals.DEBUG_DRAW){
			Globals.knowledgeBase.draw(g);
		}
	}

}
