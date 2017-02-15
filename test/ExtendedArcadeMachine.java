package test;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Random;

import core.VGDLFactory;
import core.VGDLParser;
import core.VGDLRegistry;
import core.competition.CompetitionParameters;
import core.game.Game;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import core.player.Player;
import ontology.Types;
import ontology.avatar.MovingAvatar;
import test.config.TestConfig;
import tools.ElapsedCpuTimer;
import tools.StatSummary;

/**
 * Created with IntelliJ IDEA. User: Diego Date: 06/11/13 Time: 11:24 This is a
 * Java port from Tom Schaul's VGDL - https://github.com/schaul/py-vgdl
 */
public class ExtendedArcadeMachine {
	public static final boolean VERBOSE = false;

	/**
	 * Reads and launches a game for a bot to be played. It specifies which
	 * levels to play and how many times. Filenames for saving actions can be
	 * specified. Graphics always off.
	 * 
	 * @param game_file
	 *            game description file.
	 * @param level_files
	 *            array of level file names to play.
	 * @param level_times
	 *            how many times each level has to be played.
	 * @param actionFiles
	 *            names of the files where the actions of this player, for this
	 *            game, should be recorded. Accepts null if no recording is
	 *            desired. If not null, this array must contain as much String
	 *            objects as level_files.length*level_times.
	 * @param fileName2
	 */
	public static void runGames(String game_file, String[] level_files,
			int level_times, TestConfig config, String[] actionFiles,
			String outFileName, String resultsDir) {
		
		File outFile = new File(resultsDir + outFileName);
		
		try(PrintWriter writer = new PrintWriter(new FileOutputStream(outFile, true))){			
			VGDLFactory.GetInstance().init(); // This always first thing to do.
			VGDLRegistry.GetInstance().init();

			boolean recordActions = false;
			if (actionFiles != null) {
				recordActions = true;
				assert actionFiles.length >= level_files.length * level_times : "runGames (actionFiles.length<level_files.length*level_times): "
						+ "you must supply an action file for each game instance to be played, or null.";
			}

			StatSummary scores = new StatSummary();

			Game toPlay = new VGDLParser().parseGame(game_file);
			for (int levelIdx = 0; levelIdx < level_files.length; ++levelIdx) {
				String level_file = level_files[levelIdx];

				for (int i = 0; i < level_times; ++i) {
					System.out.println(" ** Playing game " + game_file
							+ ", level " + level_file + " (" + (i + 1) + "/"
							+ level_times + ") **");
					
					//Determine the random seed, different for each game to be played.
	                int randomSeed = new Random().nextInt();

					// build the level in the game.
					toPlay.buildLevel(level_file, randomSeed);

					String filename = recordActions ? actionFiles[levelIdx
							* level_times + i] : null;

					// Warm the game up.
					warmUp(toPlay, CompetitionParameters.WARMUP_TIME);

					// Create the player.
					AbstractPlayer player = config.createAgent(filename,
							toPlay.getObservation(), randomSeed);
					// createPlayer(agentName, filename,toPlay.getObservation(),
					// randomSeed);

					double score = -1;
					if (player == null) {
						// Something went wrong in the constructor, controller
						// disqualified
						toPlay.disqualify();

						// Get the score for the result.
						score = toPlay.handleResult()[0];

					} else {
						
						boolean visuals = true;

						// Then, play the game.
						if(visuals){
							score = toPlay.playGame(new Player[]{player}, randomSeed, false, 0)[0];
						}
						else{
							score = toPlay.runGame(new Player[]{player}, randomSeed)[0];
						}
						
						char sep = '\t';
						String result = config.getName() + sep + game_file
								+ sep + level_file + sep + i + sep
								+ toPlay.getWinner() + sep + score + sep
								+ toPlay.getGameTick() + sep
								+ CompetitionParameters.ACTION_TIME + sep
								+ CompetitionParameters.ACTION_TIME_DISQ + sep
								+ CompetitionParameters.INITIALIZATION_TIME
								+ sep + CompetitionParameters.TIMER_TYPE.name()
								+ sep + config.getAdditionalLogData(sep);
						writer.println(result);
						System.out.println(result);
					}

					scores.add(score);

					// Finally, when the game is over, we need to tear the
					// player down.
					if (player != null)
						tearPlayerDown(toPlay, player);

					// reset the game.
					toPlay.reset();
				}
			}

			System.out.println(" *** Results in game " + game_file + " *** ");
			System.out.println(scores);
			System.out.println(" *********");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Creates a player given its name with package. This class calls the
	 * constructor of the agent and initializes the action recording procedure.
	 * 
	 * @param playerName
	 *            name of the agent to create. It must be of the type
	 *            "<agentPackage>.Agent".
	 * @param actionFile
	 *            filename of the file where the actions of this player, for
	 *            this game, should be recorded.
	 * @param so
	 *            Initial state of the game to be played by the agent.
	 * @param randomSeed
	 *            Seed for the sampleRandom generator of the game to be played.
	 * @return the player, created and initialized, ready to start playing the
	 *         game.
	 */
	public static AbstractPlayer createPlayer(String playerName,
			String actionFile, StateObservation so, int randomSeed) {
		AbstractPlayer player = null;

		try {
			// create the controller.
			player = createController(playerName, so);
			if (player != null)
				player.setup(actionFile, randomSeed, false);

		} catch (Exception e) {
			// This probably happens because controller took too much time to be
			// created.
			e.printStackTrace();
			System.exit(1);
		}

		return player;
	}

	/**
	 * Creates and initializes a new controller with the given name. Takes into
	 * account the initialization time, calling the appropriate constructor with
	 * the state observation and time due parameters.
	 * 
	 * @param playerName
	 *            Name of the controller to instantiate.
	 * @param so
	 *            Initial state of the game to be played by the agent.
	 * @return the player if it could be created, null otherwise.
	 */
	protected static AbstractPlayer createController(String playerName,
			StateObservation so) throws RuntimeException {
		AbstractPlayer player = null;
		try {
			// Get the class and the constructor with arguments
			// (StateObservation, long).
			Class<? extends AbstractPlayer> controllerClass = Class.forName(
					playerName).asSubclass(AbstractPlayer.class);
			@SuppressWarnings("rawtypes")
			Class[] gameArgClass = new Class[] { StateObservation.class,
					ElapsedCpuTimer.class };
			@SuppressWarnings("rawtypes")
			Constructor controllerArgsConstructor = controllerClass
					.getConstructor(gameArgClass);

			// Determine the time due for the controller creation.
			ElapsedCpuTimer ect = new ElapsedCpuTimer(
					CompetitionParameters.TIMER_TYPE);
			ect.setMaxTimeMillis(CompetitionParameters.INITIALIZATION_TIME);

			// Call the constructor with the appropriate parameters.
			Object[] constructorArgs = new Object[] { so, ect.copy() };
			player = (AbstractPlayer) controllerArgsConstructor
					.newInstance(constructorArgs);

			// Check if we returned on time, and act in consequence.
			long timeTaken = ect.elapsedMillis();
			if (ect.exceededMaxTime()) {
				long exceeded = -ect.remainingTimeMillis();
				System.out.println("Controller initialization time out ("
						+ exceeded + ").");

				return null;
			} else {
				System.out.println("Controller initialization time: "
						+ timeTaken + " ms.");
			}

			// This code can throw many exceptions (no time related):

		} catch (NoSuchMethodException e) {
			e.printStackTrace();
			System.err.println("Constructor " + playerName
					+ "(StateObservation,long) not found in controller class:");
			System.exit(1);

		} catch (ClassNotFoundException e) {
			System.err.println("Class " + playerName
					+ " not found for the controller:");
			e.printStackTrace();
			System.exit(1);

		} catch (InstantiationException e) {
			System.err.println("Exception instantiating " + playerName + ":");
			e.printStackTrace();
			System.exit(1);

		} catch (IllegalAccessException e) {
			System.err.println("Illegal access exception when instantiating "
					+ playerName + ":");
			e.printStackTrace();
			System.exit(1);
		} catch (InvocationTargetException e) {
			System.err.println("Exception calling the constructor "
					+ playerName + "(StateObservation,long):");
			e.printStackTrace();
			System.exit(1);
		}

		return player;
	}

	/**
	 * This methods takes the game and warms it up. This allows Java to finish
	 * the runtime compilation process and optimize the code before the proper
	 * game starts.
	 * 
	 * @param toPlay
	 *            game to be warmed up.
	 * @param howLong
	 *            for how long the warming up process must last (in
	 *            milliseconds).
	 */
	public static void warmUp(Game toPlay, long howLong) {
		StateObservation stateObs = toPlay.getObservation();
		ElapsedCpuTimer ect = new ElapsedCpuTimer(
				CompetitionParameters.TIMER_TYPE);
		ect.setMaxTimeMillis(howLong);

		int playoutLength = 10;
		ArrayList<Types.ACTIONS> actions = stateObs.getAvailableActions();
		int copyStats = 0;
		int advStats = 0;

		StatSummary ss1 = new StatSummary();
		StatSummary ss2 = new StatSummary();

		boolean finish = ect.exceededMaxTime()
				|| (copyStats > CompetitionParameters.WARMUP_CP && advStats > CompetitionParameters.WARMUP_ADV);

		// while(!ect.exceededMaxTime())
		while (!finish) {
			for (Types.ACTIONS action : actions) {
				StateObservation stCopy = stateObs.copy();
				ElapsedCpuTimer ectAdv = new ElapsedCpuTimer();
				stCopy.advance(action);
				copyStats++;
				advStats++;

				if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
					ss1.add(ectAdv.elapsedNanos());
				}

				for (int i = 0; i < playoutLength; i++) {

					int index = new Random().nextInt(actions.size());
					Types.ACTIONS actionPO = actions.get(index);

					ectAdv = new ElapsedCpuTimer();
					stCopy.advance(actionPO);
					advStats++;

					if (ect.remainingTimeMillis() < CompetitionParameters.WARMUP_TIME * 0.5) {
						ss2.add(ectAdv.elapsedNanos());
					}
				}
			}

			finish = ect.exceededMaxTime()
					|| (copyStats > CompetitionParameters.WARMUP_CP && advStats > CompetitionParameters.WARMUP_ADV);

			// if(VERBOSE)
			// System.out.println("[WARM-UP] Remaining time: " +
			// ect.remainingTimeMillis() +
			// " ms, copy() calls: " + copyStats + ", advance() calls: " +
			// advStats);
		}

		if (VERBOSE) {
			System.out.println("[WARM-UP] Finished, copy() calls: " + copyStats
					+ ", advance() calls: " + advStats + ", time (s): "
					+ ect.elapsedSeconds());
			// System.out.println(ss1);
			// System.out.println(ss2);
		}

		//Reset input to delete warm-up effects.
        MovingAvatar[] avatars = toPlay.getAvatars();
        for (int i = 0; i < toPlay.getNoPlayers(); i++) {
           avatars[i].getKeyHandler().resetAll();
        }
	}

	/**
     * Tears the player down. This initiates the saving of actions to file.
     * It should be called when the game played is over.
     * @param player player to be closed.
     * @return false if there was a timeout from the palyer. true otherwise.
     */
    private static boolean tearPlayerDown(Game toPlay, AbstractPlayer player)
    {
        //This is finished, no more actions, close the writer.
        player.teardown(toPlay);

        //Determine the time due for the controller close up.
        ElapsedCpuTimer ect = new ElapsedCpuTimer(CompetitionParameters.TIMER_TYPE);
        ect.setMaxTimeMillis(CompetitionParameters.TEAR_DOWN_TIME);

        //Inform about the result and the final game state.
        player.result(toPlay.getObservation(), ect);

        //Check if we returned on time, and act in consequence.
        long timeTaken = ect.elapsedMillis();
        if(ect.exceededMaxTime())
        {
            long exceeded =  - ect.remainingTimeMillis();
            System.out.println("Controller tear down time out (" + exceeded + ").");

            toPlay.disqualify();
            return false;
        }

        System.out.println("Controller tear down time: " + timeTaken + " ms.");
        return true;
    }

}
