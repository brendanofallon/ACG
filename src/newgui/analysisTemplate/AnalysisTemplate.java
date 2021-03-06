package newgui.analysisTemplate;

import newgui.gui.modelElements.AnalysisModel;
import sequence.Alignment;

public abstract class AnalysisTemplate {

	/**
	 * Obtain the analysis model with all of the default settings of this analysis type
	 * @return
	 */
	public abstract AnalysisModel getModel(Alignment aln);
	
	/**
	 * Obtain a long-form description of this analysis type
	 * @return
	 */
	public abstract String getDescription();
	
	/**
	 * Obtain short-form name for this analysis (recombination analysis, demographic analysis, etc...)
	 * @return
	 */
	public abstract String getAnalysisName();
	
	
}
