package newgui.analysisTemplate;

import gui.inputPanels.AnalysisModel;
import gui.inputPanels.loggerConfigs.BPDensityModel;
import gui.inputPanels.loggerConfigs.RootHeightModel;
import gui.inputPanels.loggerConfigs.StateLoggerModel;

/**
 * A simple analysis template that can operate on a single alignment. This does 
 * not use MC3 and includes just the StateLogger, BreakpointDensityLogger, and RootHeightDensity logger 
 * @author brendan
 *
 */
public class BasicAnalysis extends AnalysisTemplate {

	@Override
	public AnalysisModel getModel() {
		AnalysisModel model = new AnalysisModel();
		//model.addLoggerModel(new StateLoggerModel()); //State logger always in by default
		model.addLoggerModel(new BPDensityModel());
		model.addLoggerModel(new RootHeightModel() );
		
		return model;
	}

	@Override
	public String getDescription() {
		return "A first-pass analysis suitable for smaller alignments. This analysis identifies the locations of recombination breakpoints as well as the TMRCA along the length of the sequence. It assumes a simple model of constant population size";
	}

	@Override
	public String getAnalysisName() {
		return "Basic Analysis";
	}

}
