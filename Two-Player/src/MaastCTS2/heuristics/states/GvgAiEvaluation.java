package MaastCTS2.heuristics.states;

import MaastCTS2.Agent;
import MaastCTS2.Globals;
import core.competition.CompetitionParameters;
import core.game.StateObservationMulti;
import ontology.Types.WINNER;

/**
 * Same evaluation function as used by the sample MCTS controller of GVGAI
 * 
 * @author Dennis Soemers
 */
public class GvgAiEvaluation implements IPlayoutEvaluation {
	
	public static double[] evaluate(StateObservationMulti stateObs){
		double scores[] = new double[Agent.numPlayers];
		
		for(int i = 0; i < Agent.numPlayers; ++i){
			scores[i] = stateObs.getGameScore(i);
			
			if (stateObs.isGameOver()) {		// game over
				if (stateObs.getMultiGameWinner()[i] == WINNER.PLAYER_WINS) {
					// game won
					scores[i] += Globals.HUGE_ENDGAME_SCORE;
				} 
				else if (stateObs.getMultiGameWinner()[i] == WINNER.PLAYER_LOSES) {
					// game lost
					if(stateObs.getGameTick() == CompetitionParameters.MAX_TIMESTEPS){
						// loss based on time is preferable over an early loss
						scores[i] -= Globals.HUGE_ENDGAME_SCORE * 0.8;
					}
					else{
						scores[i] -= Globals.HUGE_ENDGAME_SCORE;
					}
				}
			}
		}
		
		if(stateObs.getMultiGameWinner()[Agent.myID] == WINNER.PLAYER_LOSES &&
				stateObs.getMultiGameWinner()[Agent.otherID] == WINNER.PLAYER_LOSES){
			// losing is a bit less bad if our opponent also loses
			scores[Agent.myID] += 0.8 * Globals.HUGE_ENDGAME_SCORE;
			
			// we don't think our opponent will expect us to do this, so we'll pretend this is a good evaluation for opponent
			scores[Agent.otherID] += 1.0 * Globals.HUGE_ENDGAME_SCORE + 1; 
		}
		else if(stateObs.getMultiGameWinner()[Agent.myID] == WINNER.PLAYER_WINS &&
				stateObs.getMultiGameWinner()[Agent.otherID] == WINNER.PLAYER_WINS){
			// and winning is a bit less good if our opponent also wins
			scores[Agent.myID] -= 0.8 * Globals.HUGE_ENDGAME_SCORE; 
		}

		return scores;
	}

	@Override
	public double[] scorePlayout(StateObservationMulti stateObs) {
		return GvgAiEvaluation.evaluate(stateObs);
	}

	@Override
	public String getConfigDataString() {
		return "";
	}

	@Override
	public String getName() {
		return "GvgAiEvaluation";
	}

}
