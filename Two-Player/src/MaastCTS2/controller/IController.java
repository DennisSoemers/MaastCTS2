package MaastCTS2.controller;

import core.game.StateObservationMulti;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;

/**
 * Interface for controllers to play GVGAI
 *
 * @author Dennis Soemers
 */
public interface IController {
	
	/**
	 * Method called upon construction of the agent, to perform any necessary initialization
	 * 
	 * @param so
	 * @param elapsedTimer
	 */
	public void init(StateObservationMulti so, ElapsedCpuTimer elapsedTimer);
	
	/**
	 * Method called whenever an action needs to be taken. Should return the action to play in-game.
	 * 
	 * @param currentStateObs
	 * @param elapsedTimer
	 * @return
	 */
	public ACTIONS chooseAction(StateObservationMulti currentStateObs, ElapsedCpuTimer elapsedTimer);

}
