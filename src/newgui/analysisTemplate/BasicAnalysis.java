package newgui.analysisTemplate;

import newgui.gui.modelElements.AnalysisModel;
import sequence.Alignment;
import gui.loggerConfigs.BPDensityModel;
import gui.loggerConfigs.ConsensusTreeModel;
import gui.loggerConfigs.RootHeightModel;
import gui.loggerConfigs.StateLoggerModel;

/**
 * A simple analysis template that can operate on a single alignment. This does 
 * not use MC3 and includes just the StateLogger, BreakpointDensityLogger, and RootHeightDensity logger 
 * @author brendan
 *
 */
public class BasicAnalysis extends AnalysisTemplate {

	@Override
	public AnalysisModel getModel(Alignment aln) {
		AnalysisModel model = new AnalysisModel();
		//model.addLoggerModel(new StateLoggerModel()); //State logger always in by default
		model.addLoggerModel(new BPDensityModel());
		model.addLoggerModel(new RootHeightModel() );
		
		ConsensusTreeModel treeLogger = new ConsensusTreeModel();
		treeLogger.setSite(aln.getSequenceLength()/2);
		treeLogger.setLogFrequency(5000);
		model.addLoggerModel( treeLogger );
		
		return model;
	}

	@Override
	public String getDescription() {
		return "A smaller analysis suitable for small to medium-sized alignments. This analysis identifies the locations of recombination breakpoints as well as the TMRCA along the length of the sequence, and the consensus tree ancestral to a position along the sequence.";
	}

	@Override
	public String getAnalysisName() {
		return "Basic Analysis";
	}

}
