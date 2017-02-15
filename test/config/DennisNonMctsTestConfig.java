package test.config;

import tools.ElapsedCpuTimer;
import MaastCTS2.Agent;
import MaastCTS2.controller.IController;
import core.competition.CompetitionParameters;
import core.game.StateObservation;
import core.player.AbstractPlayer;

/**
 * Config for testing non-MCTS agents
 *
 * @author Dennis Soemers
 */
public class DennisNonMctsTestConfig extends TestConfig {
	
	private IController controller;
	private String name;
	
	public DennisNonMctsTestConfig(String name, IController controller){
		this.controller = controller;
		this.name = name;
	}
	
	@Override
	public AbstractPlayer createAgent(String actionFile, StateObservation stateObs, int randomSeed) {
		AbstractPlayer player = null;

		try {

			// Determine the time due for the controller creation.
			ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
			ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

			player = new Agent(stateObs, ect.copy(), controller);

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
		return "";
	}

}
