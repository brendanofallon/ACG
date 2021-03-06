package newgui.analysisTemplate;

import newgui.gui.modelElements.AnalysisModel;
import newgui.gui.modelElements.MCMCModelElement;
import newgui.gui.modelElements.SiteModelElement.MutModelType;
import sequence.Alignment;
import gui.loggerConfigs.BPDensityModel;
import gui.loggerConfigs.ConsensusTreeModel;
import gui.loggerConfigs.PopSizeLoggerModel;
import gui.loggerConfigs.RootHeightModel;

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
		treeLogger0.setLogFrequency(20000);
		model.addLoggerModel( treeLogger0 );

		
		ConsensusTreeModel treeLogger1 = new ConsensusTreeModel();
		treeLogger1.setSite(aln.getSequenceLength()/2);
		treeLogger1.setLogFrequency(20000);
		model.addLoggerModel( treeLogger1 );

		
		ConsensusTreeModel treeLogger2 = new ConsensusTreeModel();
		treeLogger2.setSite(aln.getSequenceLength()-2);
		treeLogger2.setLogFrequency(20000);
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
