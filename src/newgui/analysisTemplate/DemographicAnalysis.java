package newgui.analysisTemplate;

import gui.loggerConfigs.BPDensityModel;
import gui.loggerConfigs.ConsensusTreeModel;
import gui.loggerConfigs.PopSizeLoggerModel;
import gui.loggerConfigs.RootHeightModel;
import newgui.gui.modelElements.AnalysisModel;
import newgui.gui.modelElements.CoalescentModelElement;
import newgui.gui.modelElements.PopSizeModelElement.PopSizeModel;
import sequence.Alignment;

public class DemographicAnalysis extends AnalysisTemplate {

	@Override
	public AnalysisModel getModel(Alignment aln) {
		AnalysisModel model = new AnalysisModel();
		//model.addLoggerModel(new StateLoggerModel()); //State logger always in by default
		CoalescentModelElement coalModel = model.getCoalescentModel();
		coalModel.getPopSizeModel().setModelType(PopSizeModel.ExpGrowth);
		model.addLoggerModel(new BPDensityModel());
		model.addLoggerModel(new RootHeightModel() );
		model.addLoggerModel(new PopSizeLoggerModel() );
		
		ConsensusTreeModel treeLogger = new ConsensusTreeModel();
		treeLogger.setSite(aln.getSequenceLength()/2);
		model.addLoggerModel( treeLogger );
		
		return model;
	}

	@Override
	public String getDescription() {
		return "A analysis that fits an model of exponential growth (or decay) of population size over time. This model also logs recombination breakpoints and TMRCA along the sequence.";
	}

	@Override
	public String getAnalysisName() {
		return "Demographic Analysis";
	}
}
