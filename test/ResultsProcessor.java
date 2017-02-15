package test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;

/**
 * Class to process a directory containing results in .csv files.
 * Will only process files immediately in a directory, and not recursively in subdirectories
 *
 * @author Dennis Soemers
 */
public class ResultsProcessor {
	
	/** Directory containing the results that should be processed */
	private static final String RESULTS_DIRECTORY = "D:/Apps/gvg-master-thesis/Results/";
	
	private static class PlayerResults implements Comparable{
		public String playerName;
		public HashMap<String, ArrayList<Double>> iterationsPerGame = new HashMap<String, ArrayList<Double>>();
		public HashMap<String, ArrayList<Double>> losingIterationsRatioPerGame = new HashMap<String, ArrayList<Double>>();
		public HashMap<String, ArrayList<Double>> scoresPerGame = new HashMap<String, ArrayList<Double>>();
		public HashMap<String, Double> avgIterationsPerGame = new HashMap<String, Double>();
		public HashMap<String, Double> avgIterationsPerGameStd = new HashMap<String, Double>();
		public HashMap<String, Double> avgLosingIterationsRatioPerGame = new HashMap<String, Double>();
		public HashMap<String, Double> avgScoresPerGame = new HashMap<String, Double>();
		public HashMap<String, Integer> winsPerGame = new HashMap<String, Integer>();
		public HashMap<String, Integer> lossesPerGame = new HashMap<String, Integer>();
		public HashMap<String, Integer> totalTicksInWins = new HashMap<String, Integer>();
		public HashMap<String, Integer> totalTicksInLosses = new HashMap<String, Integer>();
		public HashMap<String, Integer> totalNumTimeouts = new HashMap<String, Integer>();
		
		public PlayerResults(String playerName){
			this.playerName = playerName;
		}

		@Override
		public int compareTo(Object o) {
			return this.playerName.compareTo(((PlayerResults)o).playerName);
		}
	}
	
	private static final int NUM_GAME_SETS = 6;
	
	private static final boolean MERGE_ALL_SETS = true;
	private static final boolean INCLUDE_AVG_ITERATIONS = false;
	
	private static final boolean ONLY_DETERMINISTIC_GAMES = false;
	private static final boolean ONLY_NONDETERMINISTIC_GAMES = false;
	
	private static final String[] DETERMINISTIC_GAMES =
		{
			"bait",
			"boloadventures",
			"brainman",
			"catapults",
			"chipschallenge",
			"cookmepasta",
			"digdug",
			"escape",
			"factorymanager",
			"hungrybirds",
			"iceandfire",
			"labyrinth",
			"lasers",
			"lasers2",
			"modality",
			"painter",
			"racebet2",
			"realportals",
			"realsokoban",
			"sokoban",
			"tercio",
			"thecitadel",
			"zenpuzzle"
		};
	
	public static void main(String[] args){
		HashMap<String, String> playerNameMap = new HashMap<String, String>();
		playerNameMap.put("MaastCTS2", "MaastCTS2");
		playerNameMap.put("dennisNST", "MCTS");
		playerNameMap.put("dennisNST_ExploreLosses", "LA");
		playerNameMap.put("dennisNST_NoveltyPruning", "NBP");
		playerNameMap.put("dennisNST_NoveltyPruning_ExploreLosses", "LA+NBP");
		playerNameMap.put("controllers.sampleOLMCTS.Agent", "SOLMCTS");
		playerNameMap.put("acontrollers.sampleOLMCTS.Agent", "SOLMCTS");
		playerNameMap.put("IteratedWidth.Agent", "IW(1)");
		playerNameMap.put("cIteratedWidth.Agent", "IW(1)");
		playerNameMap.put("Return42.Agent", "Return42");
		playerNameMap.put("YBCriber.Agent", "YBCriber");
		playerNameMap.put("dYBCriber.Agent", "YBCriber");
		playerNameMap.put("YOLOBOT.Agent", "YOLOBOT");
		playerNameMap.put("dennisVanillaMCTS", "MCTS");
		playerNameMap.put("bdennisVanillaMCTS", "MCTS");
		playerNameMap.put("dennisBFTI", "BFTI");
		playerNameMap.put("dennisTreeReuse_0", "$\\gamma = 0$");
		playerNameMap.put("dennisTreeReuse_02", "$\\gamma = 0.2$");
		playerNameMap.put("dennisTreeReuse_04", "$\\gamma = 0.4$");
		playerNameMap.put("dennisTreeReuse_06", "$\\gamma = 0.6$");
		playerNameMap.put("dennisTreeReuse_08", "$\\gamma = 0.8$");
		playerNameMap.put("dennisTreeReuse_1", "$\\gamma = 1$");
		playerNameMap.put("dennisNST", "NST");
		playerNameMap.put("dennisProgHist", "PH");
		playerNameMap.put("dennisNSTProgHist", "NST+PH");
		playerNameMap.put("dennisKBE", "KBE");
		playerNameMap.put("dennisLA", "LA");
		playerNameMap.put("dennisNBP", "NBP");
		playerNameMap.put("dennisAllEnhanc", "Enh. (No TDTS)");
		//playerNameMap.put("dennisAllEnhanc", "AOE");
		playerNameMap.put("dennisAllEnhancPlusTD05", "Enh. TD(0.5)");
		playerNameMap.put("dennisAllEnhancPlusTD05AlwaysKB", "Always KBE (0.5)");
		playerNameMap.put("dennisAllEnhancPlusTD05NoTRBFTI", "No TR-BFTI (0.5)");
		playerNameMap.put("dennisAllEnhancPlusTD08", "Enh. TD(0.8)");
		playerNameMap.put("dennisAllEnhancPlusTD08AlwaysKB", "Always KBE (0.8)");
		playerNameMap.put("dennisAllEnhancPlusTD08NoTRBFTI", "No TR-BFTI (0.8)");
		playerNameMap.put("dennisAllEnhancNew", "AOE (New)");
		playerNameMap.put("dennisAllEnhancNoBFTI", "No BFTI");
		playerNameMap.put("dennisAllEnhancNoDGD", "No DGD");
		playerNameMap.put("dennisDGD", "DGD");
		playerNameMap.put("dennisAllEnhancPlusTD", "AOE+TD");
		playerNameMap.put("dennisAllEnhancPlusTDKB", "AOE+TDKB");
		playerNameMap.put("dennisAllEnhancPlusTDsqrt2", "AOE+TD($C=\\sqrt{2}$)");
		playerNameMap.put("dennisTD", "TD");
		playerNameMap.put("dennisTDKBE", "TDKB");
		playerNameMap.put("dennisTD_05", "TD(0.5)");
		playerNameMap.put("dennisTD_08", "TD(0.8)");
		playerNameMap.put("dennisTDKB_05", "TDKBE(0.5)");
		playerNameMap.put("dennisTDKB_08", "TDKBE(0.8)");
		playerNameMap.put("dennisTDMS_05", "TDMS(0.5)");
		playerNameMap.put("dennisTDMS_08", "TDMS(0.8)");
		playerNameMap.put("dennisTDIR_05", "TDIR(0.5)");
		playerNameMap.put("dennisTDIR_08", "TDIR(0.8)");
		
		playerNameMap.put("dennisBFTI_2", "$M = 2$");
		playerNameMap.put("dennisBFTI_3", "$M = 3$");
		playerNameMap.put("dennisBFTI_5", "$M = 5$");
		playerNameMap.put("dennisBFTI_10", "$M = 10$");
		
		playerNameMap.put("dennisAllEnhancC02", "$C = 0.2$");
		playerNameMap.put("dennisAllEnhancC04", "$C = 0.4$");
		playerNameMap.put("dennisAllEnhancC06", "$C = 0.6$");
		playerNameMap.put("dennisAllEnhancC08", "$C = 0.8$");
		playerNameMap.put("dennisAllEnhancC1", "$C = 1.0$");
		playerNameMap.put("dennisAllEnhancC12", "$C = 1.2$");
		playerNameMap.put("dennisAllEnhancC14", "$C = 1.4$");
		
		playerNameMap.put("adennisBFTI", "BFTI");
		playerNameMap.put("bdennisProgHist", "PH");
		playerNameMap.put("cdennisNST", "NST");
		playerNameMap.put("ddennisNSTProgHist", "NST+PH");
		
		playerNameMap.put("bdennisAllEnhancNoDGD", "No DGD");
		playerNameMap.put("cdennisAllEnhancNoBFTI", "No BFTI");
		playerNameMap.put("ddennisAllEnhanc", "All Enhanc.");
		
		File directory = new File(RESULTS_DIRECTORY);
		File[] files = directory.listFiles();
		HashMap<String, PlayerResults[]> playerResultsMap = new HashMap<String, PlayerResults[]>();
		
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		DecimalFormat scoreFormatter = (DecimalFormat)nf;
		scoreFormatter.applyPattern("#0.0");
		
		int[][] allGameSetsWins = null;
		int[][] allGameSetsLosses = null;
		
		if(files != null){
			for(File file : files){
				if(file.getName().endsWith(".csv")){
					//System.out.println("File = " + file.getName());
					String filename = file.getName();
					filename = filename.substring(0, filename.length() - ".csv".length());	// get rid of .csv
					String[] parts = filename.split("_");
					int numParts = parts.length;
					String filenameDateAndTime = parts[numParts - 2] + " " + parts[numParts - 1];
					int gameSet = Integer.parseInt(parts[numParts - 3]);
					
					// merge all results
					if(MERGE_ALL_SETS){
						gameSet = 0;
					}
					
					String controllerName = "";
					for(int i = 0; i < parts.length - 3; ++i){
						controllerName += parts[i];
						
						if(i < parts.length - 4){
							controllerName += "_";
						}
					}
					
					PlayerResults[] resultsArray = playerResultsMap.get(controllerName);
					PlayerResults results = null;
					if(resultsArray == null){
						resultsArray = new PlayerResults[NUM_GAME_SETS];
						results = new PlayerResults(controllerName);
						resultsArray[gameSet] = results;
						playerResultsMap.put(controllerName, resultsArray);
					}
					else if(resultsArray[gameSet] == null){
						results = new PlayerResults(controllerName);
						resultsArray[gameSet] = results;
					}
					else{
						results = resultsArray[gameSet];
					}
					
					try(BufferedReader reader = new BufferedReader(new FileReader(file))){
						for(String line; (line = reader.readLine()) != null; /**/){
							String[] lineParts = line.split("\t");
							
							if(lineParts.length == 0){
								continue;
							}
							
							String agentName = lineParts[0];
							String gameFileName = lineParts[1];
							String levelFileName = lineParts[2];
							String levelRepetitionIndex = lineParts[3];
							String gameOutcome = lineParts[4];
							String score = lineParts[5];
							String gameDuration = lineParts[6];
							String actionThinkTime = lineParts[7];
							String disqualifyTime = lineParts[8];
							String initTime = lineParts[9];
							String timerType = lineParts[10];
							
							String gameName = gameFilenameToGameName(gameFileName);
							if(ONLY_DETERMINISTIC_GAMES){
								boolean found = false;
								
								for(String name : DETERMINISTIC_GAMES){
									if(name.equals(gameName)){
										found = true;
										break;
									}
								}
								
								if(!found){
									continue;
								}
							}
							else if(ONLY_NONDETERMINISTIC_GAMES){
								boolean found = false;
								
								for(String name : DETERMINISTIC_GAMES){
									if(name.equals(gameName)){
										found = true;
										break;
									}
								}
								
								if(found){
									continue;
								}
							}
							
							int numTicks = Integer.parseInt(gameDuration);
							double gameScore = Double.parseDouble(score);
							
							if(!results.iterationsPerGame.containsKey(gameFileName)){
								results.iterationsPerGame.put(gameFileName, new ArrayList<Double>());
							}
							
							if(!results.losingIterationsRatioPerGame.containsKey(gameFileName)){
								results.losingIterationsRatioPerGame.put(gameFileName, new ArrayList<Double>());
							}
							
							if(!results.scoresPerGame.containsKey(gameFileName)){
								results.scoresPerGame.put(gameFileName, new ArrayList<Double>());
							}
							
							if(!results.winsPerGame.containsKey(gameFileName)){
								results.winsPerGame.put(gameFileName, 0);
							}
							
							if(!results.lossesPerGame.containsKey(gameFileName)){
								results.lossesPerGame.put(gameFileName, 0);
							}
							
							if(!results.totalTicksInWins.containsKey(gameFileName)){
								results.totalTicksInWins.put(gameFileName, 0);
							}
							
							if(!results.totalTicksInLosses.containsKey(gameFileName)){
								results.totalTicksInLosses.put(gameFileName, 0);
							}
							
							if(!results.totalNumTimeouts.containsKey(gameFileName)){
								results.totalNumTimeouts.put(gameFileName, 0);
							}
							
							results.scoresPerGame.get(gameFileName).add(gameScore);
							
							if(gameOutcome.equals("PLAYER_WINS")){
								results.winsPerGame.put(gameFileName, results.winsPerGame.get(gameFileName) + 1);
								results.totalTicksInWins.put(gameFileName, results.totalTicksInWins.get(gameFileName) + numTicks);
							}
							else{
								results.lossesPerGame.put(gameFileName, results.lossesPerGame.get(gameFileName) + 1);
								results.totalTicksInLosses.put(gameFileName, results.totalTicksInLosses.get(gameFileName) + numTicks);
								
								if(numTicks == 2000){
									results.totalNumTimeouts.put(gameFileName, results.totalNumTimeouts.get(gameFileName) + 1);
								}
							}

							int totalIterations = 0;
							for(int i = 11; i < lineParts.length; ++i){
								String linePart = lineParts[i];
								
								if(linePart.startsWith("Total_Iterations=")){
									totalIterations = Integer.parseInt(linePart.substring("Total_Iterations=".length()));
									double averageIterations = (double)totalIterations / Math.max(1, numTicks);
									results.iterationsPerGame.get(gameFileName).add(averageIterations);
								}
								else if(linePart.startsWith("Total_Loss_Iterations=")){
									int totalLossIterations = Integer.parseInt(linePart.substring("Total_Loss_Iterations=".length()));
									
									if(totalIterations == 0){
										results.losingIterationsRatioPerGame.get(gameFileName).add(0.0);
									}
									else{
										double lossIterationsRatio = (double)totalLossIterations / totalIterations;
										results.losingIterationsRatioPerGame.get(gameFileName).add(lossIterationsRatio);
									}
								}
							}
						}
						
						for(String gameName : results.iterationsPerGame.keySet()){
							ArrayList<Double> averageIts = results.iterationsPerGame.get(gameName);
							double sumOfAvgIts = 0.0;
							for(Double avg : averageIts){
								sumOfAvgIts += avg;
							}
							results.avgIterationsPerGame.put(gameName, (sumOfAvgIts / averageIts.size()));
							//System.out.println("Average MCTS iterations per game tick for game (" + gameName + ") = " + (sumOfAvgIts / averageIts.size()));
							
							ArrayList<Double> averageScores = results.scoresPerGame.get(gameName);
							double sumOfAvgScores = 0.0;
							for(Double avg: averageScores){
								sumOfAvgScores += avg;
							}
							results.avgScoresPerGame.put(gameName, (sumOfAvgScores / averageScores.size()));
							//System.out.println("Average score for game (" + gameName + ") = " + (sumOfAvgScores / averageScores.size()));
							
							//System.out.println("Num wins for game (" + gameName + ") = " + results.winsPerGame.get(gameName));
							
							ArrayList<Double> lossItsRatio = results.losingIterationsRatioPerGame.get(gameName);
							double sumOfRatios = 0.0;
							for(Double ratio : lossItsRatio){
								sumOfRatios += ratio;
							}
							results.avgLosingIterationsRatioPerGame.put(gameName, (sumOfRatios / lossItsRatio.size()));
							//System.out.println("Average ratio of losing iterations for game (" + gameName + ") = " + (sumOfRatios / lossItsRatio.size()));
						}
						//System.out.println();
						
						for(String gameName : results.iterationsPerGame.keySet()){
							double mean = results.avgIterationsPerGame.get(gameName);
							
							ArrayList<Double> averageIts = results.iterationsPerGame.get(gameName);
							double sumSquares = 0.0;
							for(Double avg : averageIts){
								sumSquares += Math.pow((avg - mean), 2.0);
							}
							
							int N = averageIts.size();
							double std = Math.sqrt((1.0 / (N - 1)) * sumSquares);
							
							results.avgIterationsPerGameStd.put(gameName, std);
						}
					} 
					catch (IOException exception) {
						exception.printStackTrace();
					}
					
					//System.out.println("");
				}
			}
			
			int numPlayers = playerResultsMap.size();
			PlayerResults[][] allPlayerResults = new PlayerResults[numPlayers][NUM_GAME_SETS];
			playerResultsMap.values().toArray(allPlayerResults);
			
			allGameSetsWins = new int[NUM_GAME_SETS][numPlayers];
			allGameSetsLosses = new int[NUM_GAME_SETS][numPlayers];
			
			for(int gameSet = 0; gameSet < NUM_GAME_SETS; ++gameSet){
				// generate table for latex
				String textPlacements = "";
				String columnHeaders = "";
				
				PlayerResults[] playerResults = new PlayerResults[numPlayers];
				boolean foundNull = false;

				for(int i = 0; i < numPlayers; ++i){
					playerResults[i] = allPlayerResults[i][gameSet];
					
					if(playerResults[i] == null){
						foundNull = true;
						break;
					}
				}
				
				if(foundNull){
					continue;
				}
				
				Arrays.sort(playerResults);
				for(int i = 0; i < numPlayers; ++i){
					textPlacements += "c";
					columnHeaders += playerNameMap.get(playerResults[i].playerName) + " ";
					
					if(i < numPlayers - 1 || INCLUDE_AVG_ITERATIONS){
						columnHeaders += "& ";
					}
					else{
						columnHeaders += "\\\\";
					}
				}
				
				if(INCLUDE_AVG_ITERATIONS){
					textPlacements += "|";
					for(int i = 0; i < numPlayers; ++i){
						textPlacements += "c";
						columnHeaders += playerNameMap.get(playerResults[i].playerName) + " ";
						
						if(i < numPlayers - 1){
							columnHeaders += "& ";
						}
						else{
							columnHeaders += "\\\\";
						}
					}
				}
				
				String[] games = new String[playerResults[0].winsPerGame.size()];
				playerResults[0].winsPerGame.keySet().toArray(games);
				Arrays.sort(games);
				
				int runsPerGame = 0;
				runsPerGame += playerResults[0].winsPerGame.get(games[0]);
				runsPerGame += playerResults[0].lossesPerGame.get(games[0]);
				
				System.out.println("\\begin{table}[H]");
				if(MERGE_ALL_SETS){
					System.out.println("\\scriptsize");
				}
				else{
					System.out.println("\\footnotesize");
				}
				System.out.println("\\renewcommand{\\arraystretch}{1.2}");
				System.out.println("\\caption{\\textcolor{red}{ENTER DESCRIPTION} (" + runsPerGame + " runs per game / " + (runsPerGame / 5) + " runs per level)}");
				System.out.println("\\label{TableLabel}");
				System.out.println("\\centering");
				System.out.println("\\begin{tabular}{r|" + textPlacements + "}");
				
				if(INCLUDE_AVG_ITERATIONS){
					System.out.println("& \\multicolumn{" + numPlayers + "}{c}{Win Percentage (\\%)} \\vline& \\multicolumn{" + numPlayers + "}{c}{Avg. Simulations per Tick} \\\\");
				}
				else{
					System.out.println("& \\multicolumn{" + numPlayers + "}{c}{Win Percentage (\\%)} \\\\");
				}
				
				System.out.println("Games & " + columnHeaders);
				System.out.println("\\hline");
				
				int[] totalWinsPerPlayer = new int[playerResults.length];
				int[] totalLossesPerPlayer = new int[playerResults.length];
				
				for(int i = 0; i < games.length; ++i){
					String row = "";
					
					String[] gameFilepathSplit = games[i].split("/");
					String gameName = gameFilepathSplit[gameFilepathSplit.length - 1];
					gameName = gameName.substring(0, gameName.length() - ".txt".length());
					row += gameName + " & ";
					
					double[] winRatios = new double[playerResults.length];
					double[] errorMargins = new double[playerResults.length];
					double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
					int runs = 0;
					for(int j = 0; j < playerResults.length; ++j){
						PlayerResults results = playerResults[j];
						if(results == null){
							results = new PlayerResults("");
							playerResults[j] = results;
							
						}
						if(results.winsPerGame.get(games[i]) == null){
							results.winsPerGame.put(games[i], 0);
						}
						if(results.lossesPerGame.get(games[i]) == null){
							results.lossesPerGame.put(games[i], 0);
						}
						
						int numWins = results.winsPerGame.get(games[i]);
						int numLosses = results.lossesPerGame.get(games[i]);
						runs = numWins + numLosses;
						
						double winRatio = ((double)numWins / runs);
						double errorMargin = 1.96 * Math.sqrt((winRatio * (1.0 - winRatio)) / runs);
						winRatios[j] = winRatio;
						errorMargins[j] = errorMargin;
						
						potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (winRatio - errorMargin));
						
						totalWinsPerPlayer[j] += numWins;
						totalLossesPerPlayer[j] += numLosses;
					}
					
					for(int j = 0; j < playerResults.length; ++j){
						double winRatio = winRatios[j];
						double errorMargin = errorMargins[j];
						
						//if(winRatio + errorMargin >= potentiallyBestThreshold){
						//	row += "\\textbf{" + scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
						//}
						//else{
							row += scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
						//}
						
						if(j < playerResults.length - 1 || INCLUDE_AVG_ITERATIONS){
							row += "& ";
						}
						else{
							row += "\\\\";
						}
					}
					
					if(INCLUDE_AVG_ITERATIONS){
						for(int j = 0; j < playerResults.length; ++j){
							PlayerResults results = playerResults[j];
							double avgIts = results.avgIterationsPerGame.get(games[i]);
							double avgItsStd = results.avgIterationsPerGameStd.get(games[i]);
							double errorMargin = 2.0 * (avgItsStd / Math.sqrt(runs));
							
							//if(winRatio + errorMargin >= potentiallyBestThreshold){
							//	row += "\\textbf{" + scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
							//}
							//else{
								row += scoreFormatter.format(avgIts) + " $\\pm$ " + scoreFormatter.format(errorMargin) + " ";
							//}
							
							if(j < playerResults.length - 1){
								row += "& ";
							}
							else{
								row += "\\\\";
							}
						}
					}
					
					System.out.println(row);
				}
				
				System.out.println("\\hline");
				String totalRow = "Total & ";

				double[] totalWinRatios = new double[playerResults.length];
				double[] totalErrorMargins = new double[playerResults.length];
				double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
				
				for(int i = 0; i < playerResults.length; ++i){
					int totalWins = totalWinsPerPlayer[i];
					int totalLosses = totalLossesPerPlayer[i];
					int totalRuns = totalWins + totalLosses;
					
					double winRatio = ((double)totalWins / totalRuns);
					double errorMargin = 1.96 * Math.sqrt((winRatio * (1.0 - winRatio)) / totalRuns);
					totalWinRatios[i] = winRatio;
					totalErrorMargins[i] = errorMargin;
					
					potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (winRatio - errorMargin));
					
					allGameSetsWins[gameSet][i] += totalWins;
					allGameSetsLosses[gameSet][i] += totalLosses;
				}
				
				for(int i = 0; i < playerResults.length; ++i){
					double winRatio = totalWinRatios[i];
					double errorMargin = totalErrorMargins[i];
					
					//if(winRatio + errorMargin >= potentiallyBestThreshold){
					//	totalRow += "\\textbf{" + scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
					//}
					//else{
						totalRow += scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
					//}
					
					if(i < totalWinsPerPlayer.length - 1 || INCLUDE_AVG_ITERATIONS){
						totalRow += "& ";
					}
				}
				
				if(INCLUDE_AVG_ITERATIONS){
					for(int i = 0; i < playerResults.length; ++i){
						PlayerResults results = playerResults[i];
						
						double sumOfAvgs = 0.0;
						for(Double avg : results.avgIterationsPerGame.values()){
							sumOfAvgs += avg;
						}
						double mean = sumOfAvgs / results.avgIterationsPerGame.size();
						
						double sumSquares = 0.0;
						for(Double avg : results.avgIterationsPerGame.values()){
							sumSquares += Math.pow((avg - mean), 2.0);
						}
						
						int N = results.avgIterationsPerGame.size();
						double std = Math.sqrt((1.0 / (N - 1)) * sumSquares);
						double errorMargin = 2.0 * (std / Math.sqrt(N));
								
						totalRow += scoreFormatter.format(mean) + " $\\pm$ " + scoreFormatter.format(errorMargin) + " ";
						
						if(i < playerResults.length - 1){
							totalRow += "& ";
						}
					}
				}

				System.out.println(totalRow + "\\\\");
				
				System.out.println("\\end{tabular}");
				System.out.println("\\end{table}");
				
				if(!MERGE_ALL_SETS){
					System.out.println();
				}
			}
			
			// print table for all game sets together
			if(MERGE_ALL_SETS){
				return;
			}
			
			PlayerResults[] playerResults = new PlayerResults[numPlayers];

			for(int i = 0; i < numPlayers; ++i){
				playerResults[i] = allPlayerResults[i][0];
			}
			Arrays.sort(playerResults);
			
			String textPlacements = "";
			String winsColumnHeaders = "";
			for(int i = 0; i < numPlayers; ++i){
				textPlacements += "c";
				winsColumnHeaders += playerNameMap.get(playerResults[i].playerName) + " ";
				
				if(i < numPlayers - 1){
					winsColumnHeaders += "& ";
				}
				else{
					winsColumnHeaders += "\\\\";
				}
			}
			
			System.out.println("\\begin{table}[h]");
			System.out.println("\\footnotesize");
			System.out.println("\\renewcommand{\\arraystretch}{1.2}");
			System.out.println("\\caption{Win Percentages (All Training Sets, " + (allGameSetsWins[0][0] + allGameSetsLosses[0][0]) + " runs per set)}");
			System.out.println("\\label{TableLabelAll}");
			System.out.println("\\centering");
			System.out.println("\\begin{tabular}{|c|" + textPlacements + "|}");
			System.out.println("\\hline");
			System.out.println("Sets & " + winsColumnHeaders);
			System.out.println("\\hline");
			
			int[] totalWinsPerPlayer = new int[numPlayers];
			int[] totalLossesPerPlayer = new int[numPlayers];
			
			for(int i = 0; i < NUM_GAME_SETS; ++i){
				String row = "";
				row += "Set " + (i + 1) + " & ";
				
				double[] winRatios = new double[numPlayers];
				double[] errorMargins = new double[numPlayers];
				double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
				for(int j = 0; j < numPlayers; ++j){
					int numWins = allGameSetsWins[i][j];
					int numLosses = allGameSetsLosses[i][j];
					int runs = numWins + numLosses;
					
					double winRatio = ((double)numWins / runs);
					double errorMargin = 1.96 * Math.sqrt((winRatio * (1.0 - winRatio)) / runs);
					winRatios[j] = winRatio;
					errorMargins[j] = errorMargin;
					
					potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (winRatio - errorMargin));
					
					totalWinsPerPlayer[j] += numWins;
					totalLossesPerPlayer[j] += numLosses;
				}
				
				for(int j = 0; j < numPlayers; ++j){
					double winRatio = winRatios[j];
					double errorMargin = errorMargins[j];
					
					if(winRatio + errorMargin >= potentiallyBestThreshold){
						row += "\\textbf{" + scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
					}
					else{
						row += scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
					}
					
					if(j < numPlayers - 1){
						row += "& ";
					}
					else{
						row += "\\\\";
					}
				}
				
				System.out.println(row);
			}
			
			System.out.println("\\hline");
			System.out.println("\\hline");
			String totalRow = "Total & ";

			double[] totalWinRatios = new double[numPlayers];
			double[] totalErrorMargins = new double[numPlayers];
			double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
			
			for(int i = 0; i < numPlayers; ++i){
				int totalWins = totalWinsPerPlayer[i];
				int totalLosses = totalLossesPerPlayer[i];
				int totalRuns = totalWins + totalLosses;
				
				double winRatio = ((double)totalWins / totalRuns);
				double errorMargin = 1.96 * Math.sqrt((winRatio * (1.0 - winRatio)) / totalRuns);
				totalWinRatios[i] = winRatio;
				totalErrorMargins[i] = errorMargin;
				
				potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (winRatio - errorMargin));
			}
			
			for(int i = 0; i < numPlayers; ++i){
				double winRatio = totalWinRatios[i];
				double errorMargin = totalErrorMargins[i];
				
				if(winRatio + errorMargin >= potentiallyBestThreshold){
					totalRow += "\\textbf{" + scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
				}
				else{
					totalRow += scoreFormatter.format(winRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
				}
				
				if(i < totalWinsPerPlayer.length - 1){
					totalRow += "& ";
				}
			}
			
			System.out.println(totalRow + "\\\\");
			
			System.out.println("\\hline");
			System.out.println("\\end{tabular}");
			System.out.println("\\end{table}");
			System.out.println();
		}
		
		// create table here of change in game durations for wins and losses
		int numPlayers = playerResultsMap.size();
		String[] playerNames = new String[numPlayers];
		playerResultsMap.keySet().toArray(playerNames);
		Arrays.sort(playerNames);
		
		String textPlacements = "";
		String columnHeaders = "";
		for(int i = 0; i < numPlayers; ++i){
			textPlacements += "c";
			columnHeaders += playerNameMap.get(playerNames[i]) + " ";
			
			if(i < numPlayers - 1){
				columnHeaders += "& ";
			}
			else{
				columnHeaders += "\\\\";
			}
		}
		/*
		System.out.println("\\begin{table}[h]");
		System.out.println("\\footnotesize");
		System.out.println("\\renewcommand{\\arraystretch}{1.2}");
		System.out.println("\\caption{95\\% Confidence Intervals for Timeout \\% Among Losses (All Training Sets)}");
		System.out.println("\\label{TableLabelTimeoutPercentages}");
		System.out.println("\\centering");
		System.out.println("\\begin{tabular}{|c|" + textPlacements + "|}");
		System.out.println("\\hline");
		System.out.println("Sets & " + columnHeaders);
		System.out.println("\\hline");
		
		int[] totalLossesPerPlayer = new int[numPlayers];
		int[] totalTimeoutsPerPlayer = new int[numPlayers];
		
		for(int i = 0; i < NUM_GAME_SETS; ++i){
			String row = "";
			row += "Set " + (i + 1) + " & ";
			
			double[] timeoutRatios = new double[numPlayers];
			double[] errorMargins = new double[numPlayers];
			double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
			for(int j = 0; j < numPlayers; ++j){
				int numLosses = allGameSetsLosses[i][j];
				
				HashMap<String, Integer> numTimeoutsPerGame = playerResultsMap.get(playerNames[j])[i].totalNumTimeouts;
				int numTimeouts = 0;
				for(String game : numTimeoutsPerGame.keySet()){
					numTimeouts += numTimeoutsPerGame.get(game);
				}
				
				double timeoutRatio = ((double)numTimeouts / numLosses);
				double errorMargin = 1.96 * Math.sqrt((timeoutRatio * (1.0 - timeoutRatio)) / numLosses);
				timeoutRatios[j] = timeoutRatio;
				errorMargins[j] = errorMargin;
				
				potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (timeoutRatio - errorMargin));
				
				totalTimeoutsPerPlayer[j] += numTimeouts;
				totalLossesPerPlayer[j] += numLosses;
			}
			
			for(int j = 0; j < numPlayers; ++j){
				double timeoutRatio = timeoutRatios[j];
				double errorMargin = errorMargins[j];
				
				if(timeoutRatio + errorMargin >= potentiallyBestThreshold){
					row += "\\textbf{" + scoreFormatter.format(timeoutRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
				}
				else{
					row += scoreFormatter.format(timeoutRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
				}
				
				if(j < numPlayers - 1){
					row += "& ";
				}
				else{
					row += "\\\\";
				}
			}
			
			System.out.println(row);
		}
		
		System.out.println("\\hline");
		System.out.println("\\hline");
		String totalRow = "Total & ";

		double[] totalTimeoutRatios = new double[numPlayers];
		double[] totalErrorMargins = new double[numPlayers];
		double potentiallyBestThreshold = Double.NEGATIVE_INFINITY;
		
		for(int i = 0; i < numPlayers; ++i){
			int totalTimeouts = totalTimeoutsPerPlayer[i];
			int totalLosses = totalLossesPerPlayer[i];
			
			double timeoutRatio = ((double)totalTimeouts / totalLosses);
			double errorMargin = 1.96 * Math.sqrt((timeoutRatio * (1.0 - timeoutRatio)) / totalLosses);
			totalTimeoutRatios[i] = timeoutRatio;
			totalErrorMargins[i] = errorMargin;
			
			potentiallyBestThreshold = Math.max(potentiallyBestThreshold, (timeoutRatio - errorMargin));
		}
		
		for(int i = 0; i < numPlayers; ++i){
			double timeoutRatio = totalTimeoutRatios[i];
			double errorMargin = totalErrorMargins[i];
			
			if(timeoutRatio + errorMargin >= potentiallyBestThreshold){
				totalRow += "\\textbf{" + scoreFormatter.format(timeoutRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + "} ";
			}
			else{
				totalRow += scoreFormatter.format(timeoutRatio * 100.0) + " $\\pm$ " + scoreFormatter.format(errorMargin * 100.0) + " ";
			}
			
			if(i < totalTimeoutsPerPlayer.length - 1){
				totalRow += "& ";
			}
		}
		
		System.out.println(totalRow + "\\\\");
		
		System.out.println("\\hline");
		System.out.println("\\end{tabular}");
		System.out.println("\\end{table}");
		System.out.println();*/
	}
	
	private static String gameFilenameToGameName(String gameFilename){
		String[] gameFilepathSplit = gameFilename.split("/");
		String gameName = gameFilepathSplit[gameFilepathSplit.length - 1];
		gameName = gameName.substring(0, gameName.length() - ".txt".length());
		return gameName;
	}

}
