package test;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import MaastCTS2.controller.MctsController;
import core.competition.CompetitionParameters;
import test.config.TestConfig;
import test.lists.ConfigList;

public class Main {

	//public static final String gamesPath = "./gridphysics/";

	public static String gamesPath = "D:/Apps/gvg-master-thesis/gvgai-master/examples/gridphysics/";
	public static String resultsDir = "D:/Apps/gvg-master-thesis/Results/";
	
	public static int numLevelRepeats = 5;

	public static void main(String[] args) {
		
		CompetitionParameters.IMG_PATH = "D:/Apps/gvg-master-thesis/gvgai-master/sprites/";

		//String games[] = GameList.getAllGames();
		
		//String[] games = {/*"aliens", */"boulderdash"/*, "butterflies", "chase", "missilecommand", "frogs", "portals", 
		//					"sokoban", "survivezombies", "zelda"*/};
		
		String[] games = {"boulderdash"};

		SimpleDateFormat fileNameFormat = new SimpleDateFormat(
				"yyyy-MM-dd_HH-mm-ss'.csv'");
		String dateString = fileNameFormat.format(new Date());

		ConfigList configList = new ConfigList();
		
		String[] configs = {
				/*"dennisBfs",
				"dennisIW",
				"dennisVanillaMCTS",
				"dennisTreeReuse_02",
				"dennisKBE",*/
				"MaastCTS2"/*,
				"dennisMAST*,
				"dennisOlUctSelection",
				"competitionOlUctSelection", 
				"sampleController.sampleOLMCTS.Agent",
				"NovTea.Agent"*/
				};
		
		int gameSet = 0;
		if(args.length > 0){
			try{
				gameSet = Integer.parseInt(args[0]);
				configs = Arrays.copyOfRange(args, 1, args.length);
			}
			catch(NumberFormatException exception){
				gameSet = 0;
				configs = args;
			}			
			
			// will assume that we're running on the DKE linux cluster if args are given
			gamesPath = "./examples/gridphysics/";
			resultsDir = "./results/";
			CompetitionParameters.IMG_PATH = "./sprites/";
			MctsController.TIME_BUFFER_MILLISEC = 10;
			games = allGames[gameSet];
			numLevelRepeats = 15;
		}

		ArrayList<TestConfig> tests = configList.getTests(configs);
		if (args != null && tests.size() != configs.length) {
			System.err.println("not all configs for \"" + Arrays.toString(configs)
					+ "\" found.");
			return;
		}
		
		// useful to wait a little bit so that VisualVM has time to boot up for profiling
		boolean delayedStart = false;
		if(delayedStart){
			long startTime = System.currentTimeMillis();
			long timeWaited = System.currentTimeMillis() - startTime;
			System.out.println("Waiting to start games...");
			
			while(timeWaited < 15000){
				timeWaited = System.currentTimeMillis() - startTime;
			}
			
			System.out.println("Ready to start games!");
			System.out.println();
		}

		for (TestConfig testConfig : tests) {
			runTournament(testConfig, games, gameSet, dateString);
		}

	}

	private static void runTournament(TestConfig testConfig, String[] games,
										int gameSet, String dateString) {
		String fileName = testConfig.getName() + "_" + gameSet + "_" + dateString;
		System.out.println(fileName);
		int numberOfLevels = 5;
		int N = games.length;
		String game;
		String[] levels = new String[numberOfLevels];
		for (int i = 0; i < N; ++i) {
			game = gamesPath + games[i] + ".txt";
			for (int j = 0; j < numberOfLevels; ++j) {
				levels[j] = gamesPath + games[i] + "_lvl" + j + ".txt";
			}
			ExtendedArcadeMachine.runGames(game, levels, numLevelRepeats,
					testConfig, null, fileName, resultsDir);
		}
	}
	
	private static String[][] allGames =
		{		{"aliens", "boulderdash", "butterflies", "chase", "frogs", "missilecommand", 
				"portals", "sokoban", "survivezombies", "zelda"},
			
				{"camelRace", "digdug", "firestorms", "infection", "firecaster", "overload",
				"pacman", "seaquest", "whackamole", "eggomania"},
			
				{"bait", "boloadventures", "brainman", "chipschallenge", "modality", "painter",
				"realportals", "realsokoban", "thecitadel", "zenpuzzle"},
				
				{"roguelike", "surround", "catapults", "plants", "plaqueattack", "jaws",
				"labyrinth", "boulderchase", "escape", "lemmings"},
				
				{"solarfox", "defender", "enemycitadel", "crossfire", "lasers", "sheriff",
				"chopper", "superman", "waitforbreakfast", "cakybaky"},
				
				{"lasers2", "hungrybirds", "cookmepasta", "factorymanager", "racebet2",
				"intersection", "blacksmoke", "iceandfire", "gymkhana", "tercio"},
				
				{"colourescape", "labyrinthdual", "shipwreck", "bomber", "fireman",
				"rivers", "chainreaction", "islands", "clusters", "dungeon"}
		};

}
