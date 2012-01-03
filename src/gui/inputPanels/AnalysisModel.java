package gui.inputPanels;

import gui.ErrorWindow;
import gui.document.ACGDocument;
import gui.document.ACGDocumentBuilder;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.AbstractLoggerView;
import gui.inputPanels.loggerConfigs.AvailableLoggers;
import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.inputPanels.loggerConfigs.StateLoggerModel;

import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;

import logging.PropertyLogger;
import logging.StateLogger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import sequence.Alignment;

import xml.XMLLoader;

/**
 * An AnalysisModel stores all the information required to run a markov chain with ACG.
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

	public void readFromDocument(ACGDocument doc) throws InputConfigException { 
		alignmentEl.readElement(doc);
		argModel.readElements(doc);
		siteModel.readElements(doc);
		coalModel.readElements(doc);
		
		//Loggers handled slightly differently since there may be zero or more...
		List<String> docLoggers = doc.getLabelForClass(PropertyLogger.class);
		docLoggers.addAll( doc.getLabelForClass(StateLogger.class));
		//Other things we should look for / add? Maybe all loggers should implement some interface
		//just so we can easily find them here? Right now PopSizeLogger is just an MCListener,
		//so it can't be loaded here
		
		for(String loggerLabel : docLoggers) {
			Element el = doc.getElementForLabel(loggerLabel);
			String className = el.getAttribute(XMLLoader.CLASS_NAME_ATTR);
			for(LoggerModel model : AvailableLoggers.getLoggers()) {
				if (model.getLoggerClass().equals( className )) {
					loggerModels.add(model);
				}
			}
			
		}
		
		mcElement.readElements(doc);
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

				Element loggerNode = loggerModel.getElement(docBuilder.getACGDocument());
				mcElement.addListenerRef(loggerNode);
				
			}

			//			loggersPanel.setARGReference(argModel);
			//			List<Element> loggers = loggersPanel.getLoggerNodes(docBuilder.getACGDocument());
			//			for(Element node : loggers) {
			//				docBuilder.appendNode(node);
			//				mcElement.addListenerRef(node);
			//			}

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
	 * Set the alignment backing the alignment element used in this analysis
	 * @param aln
	 */
	public void setAlignment(Alignment aln) {
		alignmentEl.setElement(aln);
	}

}
