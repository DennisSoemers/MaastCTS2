package MaastCTS2.heuristics.states;

import MaastCTS2.test.IPrintableConfig;
import core.game.StateObservation;

public interface IPlayoutEvaluation extends IPrintableConfig {
	public double scorePlayout(StateObservation stateObs);
}
