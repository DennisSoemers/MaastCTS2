package test.config;

import test.ExtendedArcadeMachine;
import core.game.StateObservation;
import core.player.AbstractPlayer;

/**
 * Config to run experiments with agents implemented by others (competition participants
 * of previous years)
 *
 * @author Dennis Soemers
 */
public class OtherTestConfig extends TestConfig {
	
	private String agentName;
	
	public OtherTestConfig(String agentName){
		this.agentName = agentName;
	}
	
	@Override
	public AbstractPlayer createAgent(String actionFile,
			StateObservation stateObs, int randomSeed) {
		return ExtendedArcadeMachine.createPlayer(this.getName(), actionFile,
				stateObs, randomSeed);
	}

	@Override
	public String getName() {
		return agentName;
	}

	@Override
	public String getAdditionalLogData(char c) {
		return "";
	}

}
