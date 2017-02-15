package test.lists;

import java.util.ArrayList;
import java.util.Collections;

public class GameList {
	// CIG 2014 Training Set Games
	public static final String[] games2014Training = new String[] { "aliens",
			"boulderdash", "butterflies", "chase", "frogs", "missilecommand",
			"portals", "sokoban", "survivezombies", "zelda" };

	// CIG 2014 Validation Set Games
	public static final String[] games2014Validation = new String[] {
			"camelRace", "digdug", "firestorms", "infection", "firecaster",
			"overload", "pacman", "seaquest", "whackamole", "eggomania" };

	// CIG 2015 Training Set Games
	public static final String[] games2015Training = new String[] { "bait",
			"boloadventures", "brainman", "chipschallenge", "modality",
			"painter", "realportals", "realsokoban", "thecitadel", "zenpuzzle" };

	// CIG 2014 TEST SET / GECCO 2015 VALIDATION SET
	public static final String[] games2015Validation = new String[] {
			"roguelike", "surround", "catapults", "plants", "plaqueattack",
			"jaws", "labyrinth", "boulderchase", "escape", "lemmings" };

	public static final String[] getAllGames() {
		ArrayList<String> allGames = new ArrayList<String>(
				games2014Training.length + games2014Validation.length
						+ games2015Training.length + games2015Validation.length);
		Collections.addAll(allGames, games2014Training);
		Collections.addAll(allGames, games2014Validation);
		Collections.addAll(allGames, games2015Training);
		Collections.addAll(allGames, games2015Validation);
		String[] games = new String[allGames.size()];
		games = allGames.toArray(games);
		return games;
	}

}
