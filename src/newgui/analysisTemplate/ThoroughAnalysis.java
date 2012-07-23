package newgui.analysisTemplate;

import sequence.Alignment;
import gui.inputPanels.loggerConfigs.BPDensityModel;
import gui.inputPanels.loggerConfigs.ConsensusTreeModel;
import gui.inputPanels.loggerConfigs.PopSizeLoggerModel;
import gui.inputPanels.loggerConfigs.RootHeightModel;
import gui.modelElements.AnalysisModel;
import gui.modelElements.MCMCModelElement;
import gui.modelElements.SiteModelElement.MutModelType;

public class ThoroughAnalysis extends AnalysisTemplate {

	@Override
	public AnalysisModel getModel(Alignment aln) {
		AnalysisModel model = new AnalysisModel();
		
		MCMCModelElement mcModel = model.getMCModelElement();
		mcModel.setRunLength(100000000);
		mcModel.setUseMC3(true);
		mcModel.setChains(8);
		mcModel.setUseAdaptiveMC3(true);
		
		
		model.getSiteModel().setMutModelType(MutModelType.TN93);
		
		
		BPDensityModel bpLogger = new BPDensityModel();
		bpLogger.setBurnin(5000000);
		model.addLoggerModel( bpLogger );
		
		RootHeightModel rhLogger = new RootHeightModel();
		rhLogger.setBurnin(5000000);
		model.addLoggerModel( rhLogger);
		
		PopSizeLoggerModel popSizeLogger = new PopSizeLoggerModel();
		popSizeLogger.setBurnin(5000000);
		model.addLoggerModel(popSizeLogger);
		
		ConsensusTreeModel treeLogger0 = new ConsensusTreeModel();
		treeLogger0.setSite(1);
		model.addLoggerModel( treeLogger0 );

		
		ConsensusTreeModel treeLogger1 = new ConsensusTreeModel();
		treeLogger1.setSite(aln.getSequenceLength()/2);
		model.addLoggerModel( treeLogger1 );

		
		ConsensusTreeModel treeLogger2 = new ConsensusTreeModel();
		treeLogger2.setSite(aln.getSequenceLength()-2);
		model.addLoggerModel( treeLogger2 );

		
		return model;
	}

	@Override
	public String getDescription() {
		return "A longer analysis appropriate for larger alignments or those with many recombinations";
	}

	@Override
	public String getAnalysisName() {
		return "Thorough Analysis";
	}

}
