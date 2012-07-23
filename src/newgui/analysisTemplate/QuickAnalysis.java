package newgui.analysisTemplate;

import sequence.Alignment;
import gui.inputPanels.loggerConfigs.BPDensityModel;
import gui.inputPanels.loggerConfigs.RootHeightModel;
import gui.modelElements.AnalysisModel;
import gui.modelElements.MCMCModelElement;

public class QuickAnalysis extends AnalysisTemplate {

	@Override
	public AnalysisModel getModel(Alignment aln) {
		AnalysisModel model = new AnalysisModel();
		
		MCMCModelElement mcModel = model.getMCModelElement();
		mcModel.setRunLength(1000000);
		
		BPDensityModel bpLogger = new BPDensityModel();
		bpLogger.setBurnin(100000);
		model.addLoggerModel( bpLogger );
		
		RootHeightModel rhLogger = new RootHeightModel();
		rhLogger.setBurnin(100000);
		model.addLoggerModel( rhLogger);
		
		return model;
	}

	@Override
	public String getDescription() {
		return "A short analysis for assessing run speed and approximate values of parameters. Suitable for smaller alignments only.";
	}

	@Override
	public String getAnalysisName() {
		return "Quick Analysis";
	}

}
