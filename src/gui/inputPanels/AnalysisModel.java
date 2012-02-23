package gui.inputPanels;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.AbstractLoggerView;
import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.inputPanels.loggerConfigs.StateLoggerModel;

import java.awt.LayoutManager;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import logging.PropertyLogger;
import logging.StateLogger;

import newgui.gui.modelViews.loggerViews.AvailableLoggers;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sequence.BasicSequenceAlignment;

import xml.XMLLoader;

/**
 * An AnalysisModel stores all the information required to run a markov chain with ACG. It can 
 * both produce an ACGDocument and read settings from an ACG document.  
 * At minimum, this includes an Alignment, a substitution model, a coalescent model, and
 * some MCMC settings (run length, Metropolis-coupling options, etc). 
 * 
 *   The various 'model elements' contained herein may be used as the data behind different
 * 'views' which allow the user to see and manipulate the information. 
 * 
 * ...Would be nice at some point to specify a listener architecture where we fire events when
 * various model elements change, so views can instantly update whenever their underlying
 * model changes. Right now we do all updating manually through calls to updateView() 
 * or updateModelFromView() when needed... but this is a bit tedious and error-prone as
 * things become more complex
 * @author brendan
 *
 */
public class AnalysisModel {

	protected AlignmentElement alignmentEl;
	protected MCMCModelElement mcElement;
	protected ARGModelElement argModel;
	protected SiteModelElement siteModel;
	protected CoalescentModelElement coalModel;
	protected List<LoggerModel> loggerModels = new ArrayList<LoggerModel>();


	public AnalysisModel() {
		initializeElements();
	}

	protected void initializeElements() {
		alignmentEl = new AlignmentElement();
		mcElement = new MCMCModelElement();
		argModel = new ARGModelElement();
		siteModel = new SiteModelElement();
		coalModel = new CoalescentModelElement();
		loggerModels.add(new StateLoggerModel() );
	}

	/**
	 * Read all analysis information from the given acg document. All current settings will
	 * be overwritten
	 * @param doc
	 * @throws InputConfigException
	 */
	public void readFromDocument(ACGDocument doc) throws InputConfigException { 
		alignmentEl.readElement(doc);
		argModel.readElements(doc);
		siteModel.readElements(doc);
		coalModel.readElements(doc);
		
		loggerModels.clear();
		
		//Loggers handled slightly differently since there may be zero or more...
		List<String> docLoggers = doc.getLabelForClass(PropertyLogger.class);
		//docLoggers.addAll( doc.getLabelForClass(StateLogger.class));
		//Other things we should look for / add? Maybe all loggers should implement some interface
		//just so we can easily find them here? Right now PopSizeLogger is just an MCListener,
		//so it can't be loaded here
		
		for(String loggerLabel : docLoggers) {
			Element el = doc.getElementForLabel(loggerLabel);
			String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
			for(LoggerModel model : AvailableLoggers.getLoggers()) {
				if (model.getLoggerClass().getCanonicalName().equals( className )) {
					//Clone model since the one from availableLoggers is stored statically and we don't want to
					//change the default for everyone
					LoggerModel newModel = AvailableLoggers.createModel(model);
					newModel.readElements(doc);
					loggerModels.add( newModel );
				}
			}
			
		}
		
		//StateLogger is, for some reason, not a PropertyLogger, it won't be in the above list. Handle it separately...
		List<String> stateLoggerLabels = doc.getLabelForClass(StateLogger.class);
		if (stateLoggerLabels.size() > 0) {
			if (stateLoggerLabels.size() > 2) {
				throw new InputConfigException("Can't handle multiple State loggers right now");
			}
			
			StateLoggerModel stateLoggerModel = new StateLoggerModel();
			stateLoggerModel.readElements(doc);
			loggerModels.add(stateLoggerModel);
			
		}
		
		
		mcElement.readElements(doc);
	}
	
	
	
	/**
	 * Creates and returns an ACGDocumentBuilder with all analysis nodes added. This 
	 * will throw an InputConfigException if there are errors in any of the nodes
	 * @return
	 * @throws InputConfigException 
	 */
	public ACGDocumentBuilder getACGDocBuilder() throws InputConfigException {
		ACGDocumentBuilder docBuilder = null;
		try {	
			docBuilder = new ACGDocumentBuilder();

			docBuilder.appendHeader();
			docBuilder.appendTimeAndDateComment();

			docBuilder.addRandomSource();

			docBuilder.appendNodes( alignmentEl );

			argModel.setAlignmentRef(alignmentEl);
			docBuilder.appendNodes( argModel );			

			siteModel.setARGRef( argModel );
			docBuilder.appendNodes( siteModel );

			coalModel.setARGRef(argModel);
			docBuilder.appendNodes( coalModel );

			//Prior stuff comes last
			List<DoubleParamElement> paramModels = new ArrayList<DoubleParamElement>();
			paramModels.addAll(siteModel.getDoubleParameters());
			paramModels.addAll(coalModel.getDoubleParameters());

			for(DoubleParamElement paramElement : paramModels) {
				if (paramElement.getPriorModel() != null) {
					Element priorNode = paramElement.getPriorModel().getElement(docBuilder.getACGDocument());
					docBuilder.appendNode(priorNode);
				}
			}


			mcElement.clearReferences();

			for(LoggerModel loggerModel : loggerModels) {
				loggerModel.setArgRef(argModel);

				Element loggerNode = (Element) loggerModel.getElements(docBuilder.getACGDocument()).get(0);
				docBuilder.appendNode(loggerNode);
				mcElement.addListenerRef(loggerNode);
				
			}

			List<Element> params = docBuilder.getParameters();
			for(Element param : params) {
				mcElement.addParamRef(param);
			}

			List<Element> likelihoods = docBuilder.getLikelihoods();
			for(Element like : likelihoods) {
				mcElement.addLikelihoodRef(like);
			}


			docBuilder.appendNodes( mcElement );


		} catch (ParserConfigurationException e) {
			ErrorWindow.showErrorWindow(e);
		} 

		if (docBuilder != null)
			return docBuilder;
		else
			return null;
	}
	
	/**
	 * Returns an ACG document describing the current state of all models
	 * The contract with this class is that if we read in the document 
	 * via readFromDocument(some doc), all models should be restored to the
	 * exact state they were in when the document was created. 
	 * @return
	 * @throws InputConfigException 
	 */
	public ACGDocument getACGDocument() throws InputConfigException {
		ACGDocumentBuilder docBuilder = getACGDocBuilder();
		if (docBuilder != null)
			return docBuilder.getACGDocument();
		else
			return null;
	}

	/**
	 * Obtain the list of loggermodels specifying which loggers we're using
	 * @return
	 */
	public List<LoggerModel> getLoggerModels() {
		return loggerModels;
	}

	/**
	 * Obtain the model describing the MCMC settings
	 * @return
	 */
	public MCMCModelElement getMCModelElement() {
		return mcElement;
	}

	/**
	 * Obtain the model describing the (single) alignment
	 * @return
	 */
	public AlignmentElement getAlignmentModel() {
		return alignmentEl;
	}
	
	/**
	 * Add a new logger type to this analysis
	 * @param model
	 */
	public void addLoggerModel(LoggerModel model) {
		loggerModels.add(model);
	}

	/**
	 * Clear all current loggers and add all new ones from the list
	 * @param newModels
	 */
	public void setLoggerModels(List<LoggerModel> newModels) {
		loggerModels.clear();
		loggerModels.addAll(newModels);
	}
	
	/**
	 * Set the alignment backing the alignment element used in this analysis
	 * @param aln
	 */
	public void setAlignment(BasicSequenceAlignment aln) {
		alignmentEl.setElement(aln);
	}

	public ARGModelElement getARGModel() {
		return argModel;
	}

	public SiteModelElement getSiteModel() {
		return siteModel;
	}

	public CoalescentModelElement getCoalescentModel() {
		return coalModel;
	}

}
