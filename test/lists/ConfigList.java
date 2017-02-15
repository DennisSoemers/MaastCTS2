package test.lists;

import java.util.ArrayList;
import java.util.HashMap;

import MaastCTS2.heuristics.states.GvgAiEvaluation;
import MaastCTS2.move_selection.MaxAvgScore;
import MaastCTS2.playout.NstPlayout;
import MaastCTS2.playout.RandomPlayout;
import MaastCTS2.selection.ol.OlUctSelection;
import MaastCTS2.selection.ol.ProgressiveHistory;
import test.config.DennisMctsTestConfig;
import test.config.OtherTestConfig;
import test.config.TestConfig;

public class ConfigList {

	private HashMap<String, TestConfig> _allTests;

	public ConfigList() {
		this._allTests = new HashMap<String, TestConfig>();
		this.addDennisImplementations();
		this.addOtherCompetitionParticipants();
	}

	private void addConfig(TestConfig config) {
		if (this._allTests.containsKey(config.getName())) {
			throw new IllegalArgumentException("Duplicate key \""
					+ config.getName() + "\"");
		}
		this._allTests.put(config.getName(), config);
	}
	
	/**
	 * Adds agents implemented in 2016 by Dennis Soemers (all others are 2015 implementations by Torsten Schuster)
	 */
	private void addDennisImplementations(){
		this.addConfig(new DennisMctsTestConfig("dennisOlUctSelection",
				new MaastCTS2.selection.ol.OlUctSelection(Math.sqrt(2.0)), 
				new MaastCTS2.playout.RandomPlayout(10),
				new MaxAvgScore(),
				new MaastCTS2.heuristics.states.GvgAiEvaluation(),
				false, false, false, false, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisVanillaMCTS",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				false, false, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisBFTI",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisBFTI_2",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 2, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisBFTI_3",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisBFTI_5",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 5, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisBFTI_10",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 10, false, false));
		/*
		this.addConfig(new DennisMctsTestConfig("dennisTD_05",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, true, false, false, 0.5, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTD_08",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, true, false, false, 0.8, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTDMS_05",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, true, false, true, 0.5, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTDMS_08",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, true, false, true, 0.8, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTDKB_05",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, true, false, false, 0.6, true, true, false, 0.5, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTDKB_08",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, true, false, false, 0.6, true, true, false, 0.8, 3, false, false));*/
		
		this.addConfig(new DennisMctsTestConfig("dennisKBE",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, true, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisKBE_LA",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, true, true, false, false, 0.6, 3, false, false));
		/*
		this.addConfig(new DennisMctsTestConfig("dennisTDKBE",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, true, false, false, 0.6, true, true, false, 0.8, 3, false, false));*/
		
		this.addConfig(new DennisMctsTestConfig("dennisLA",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, true, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisNBP",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisDGD",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancNoBFTI",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				false, true, true, true, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancNoDGD",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, false, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhanc",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("MaastCTS2",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, false));
		/*
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.8, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD05",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.5, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD08",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.8, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD05AlwaysKB",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.5, 3, true, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD08AlwaysKB",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.8, 3, true, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD05NoTRBFTI",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.5, 3, false, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTD08NoTRBFTI",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, true, false, false, 0.8, 3, false, true));*/
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC02",
				new ProgressiveHistory(0.2, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC04",
				new ProgressiveHistory(0.4, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC06",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC08",
				new ProgressiveHistory(0.8, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC1",
				new ProgressiveHistory(1.0, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC12",
				new ProgressiveHistory(1.2, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancC14",
				new ProgressiveHistory(1.4, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, true, true));
		
		this.addConfig(new DennisMctsTestConfig("dennisAllEnhancPlusTDsqrt2",
				new ProgressiveHistory(Math.sqrt(2.0), 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, true, true, true, true, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisProgHist",
				new ProgressiveHistory(0.6, 1.0),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisNST",
				new OlUctSelection(0.6),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisNSTProgHist",
				new ProgressiveHistory(0.6, 1.0),
				new NstPlayout(10, 0.5, 7.0, 3),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, false, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_0",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 0.0, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_02",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 0.2, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_04",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 0.4, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_06",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 0.6, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_08",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 0.8, 3, false, false));
		
		this.addConfig(new DennisMctsTestConfig("dennisTreeReuse_1",
				new OlUctSelection(0.6),
				new RandomPlayout(10),
				new MaxAvgScore(),
				new GvgAiEvaluation(),
				true, false, false, false, false, true, 1.0, 3, false, false));
		
		/*this.addConfig(new DennisNonMctsTestConfig("dennisBfs", 
				new BreadthFirstSearchController(new DennisSoemers.heuristics.states.GvgAiEvaluation())));
		
		this.addConfig(new DennisNonMctsTestConfig("dennisIW",
				new IteratedWidthController(new DennisSoemers.heuristics.states.GvgAiEvaluation(), 1)));*/
	}
	
	/**
	 * Adds agents that participated in GVG-AI Competitions in previous years (downloaded from gvgai.net)
	 */
	private void addOtherCompetitionParticipants(){
		//this.addConfig(new OtherTestConfig("NovTea.Agent"));
		//this.addConfig(new OtherTestConfig("Return42.Agent"));
		this.addConfig(new OtherTestConfig("YBCriber.Agent"));
		this.addConfig(new OtherTestConfig("IteratedWidth.Agent"));
		//this.addConfig(new OtherTestConfig("YOLOBOT.Agent"));
		this.addConfig(new OtherTestConfig("controllers.singlePlayer.sampleOLMCTS.Agent"));
	}

	// TODO: idea: parse configuration string instead of checking in list
	public ArrayList<TestConfig> getTests(String[] configArgs) {
		ArrayList<TestConfig> tests = new ArrayList<TestConfig>();
		for (String config : configArgs) {
			TestConfig testConfig = this._allTests.get(config);
			if (testConfig != null) {
				tests.add(testConfig);
			} else {
				throw new IllegalArgumentException("Config \"" + config
						+ "\" not found");
			}
		}
		return tests;
	}
}
