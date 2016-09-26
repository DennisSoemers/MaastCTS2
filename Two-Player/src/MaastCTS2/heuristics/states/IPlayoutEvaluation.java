package MaastCTS2.heuristics.states;

import MaastCTS2.test.IPrintableConfig;
import core.game.StateObservationMulti;

public interface IPlayoutEvaluation extends IPrintableConfig {
	public double[] scorePlayout(StateObservationMulti stateObs);
}
