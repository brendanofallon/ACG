package gui.inputPanels;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.PopSizeModelElement.PopSizeModel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import coalescent.CoalescentLikelihood;

/**
 * Model implementation for Coalescent stuff - including pop size, recomb rate, etc. Mostly just a 
 * wrapper for a PopSizeModel and a recombination rate model, but also creates the coalescent likelihood
 * element. 
 * @author brendano
 *
 */
public class CoalescentModelElement extends ModelElement {
	
	private PopSizeModelElement popSizeModel;
	private RecombRateModel recRateModel;
	
	private ARGModelElement argRef;
	
	private String modelLabel = "CoalescentLikelihood";
	
	

	public CoalescentModelElement() {
		popSizeModel = new PopSizeModelElement();
		popSizeModel.setModelType(PopSizeModel.Constant);
		
		recRateModel = new RecombRateModel();
	}

	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> nodes = new ArrayList<Node>();
		params.clear();
		
		nodes.addAll(popSizeModel.getElements(doc));
		params.addAll(popSizeModel.getDoubleParameters());
		
		if (recRateModel != null)
			nodes.addAll(recRateModel.getElements(doc));
		
		Element coalEl = createElement(doc, modelLabel, CoalescentLikelihood.class);
		coalEl.appendChild( doc.createElement(popSizeModel.getModelLabel()));
		if (recRateModel != null) {
			coalEl.appendChild( doc.createElement(recRateModel.getModelLabel()));
			params.addAll(recRateModel.getDoubleParameters());
		}
		
		coalEl.appendChild( doc.createElement(argRef.getModelLabel()));
		
		nodes.add(coalEl);
		return nodes;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		Element coalEl = getSingleElementForClass(doc, CoalescentLikelihood.class);
		this.setModelLabel(coalEl.getNodeName());
		
		popSizeModel = new PopSizeModelElement();
		popSizeModel.readElements(doc);
		
		recRateModel = new RecombRateModel();
		recRateModel.readElements(doc);
	}
	
	public PopSizeModelElement getPopSizeModel() {
		return popSizeModel;
	}
	
	public RecombRateModel getRecombModel() {
		return recRateModel;
	}

	public void setARGRef(ARGModelElement argModel) {
		this.argRef = argModel;
	}
	
	public void setUseRecombination(boolean useRecomb) {
		if (! useRecomb) {
			recRateModel = null;
		}
		else {
			if (recRateModel == null) {
				recRateModel = new RecombRateModel();
			}
		}
	}
	
	public String getModelLabel() {
		return modelLabel;
	}

	public void setModelLabel(String modelLabel) {
		this.modelLabel = modelLabel;
	}

	public List<DoubleParamElement> getDoubleParameters() {
		return params;
	}
	
	//Maintains list of all double parameters we use
	private List<DoubleParamElement> params = new ArrayList<DoubleParamElement>();



}
