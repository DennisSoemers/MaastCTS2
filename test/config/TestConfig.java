package test.config;

import core.game.StateObservation;
import core.player.AbstractPlayer;

public abstract class TestConfig {
	public abstract AbstractPlayer createAgent(String actionFile,
			StateObservation stateObs, int randomSeed);

	public abstract String getName();

	public String getAdditionalLogData(char c) {
		return "";
	}

}
