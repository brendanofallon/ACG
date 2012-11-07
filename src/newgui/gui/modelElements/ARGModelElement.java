/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package newgui.gui.modelElements;


import java.util.ArrayList;
import java.util.List;

import modifier.AbstractModifier;
import newgui.gui.modelElements.Configurator.InputConfigException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import dlCalculation.substitutionModels.F84Matrix;
import document.ACGDocument;

import parameter.AbstractParameter;
import sequence.BasicSequenceAlignment;

import arg.ARG;
import arg.Newick;

import xml.XMLLoader;

/**
 * Model stuff for ARGs
 * @author brendano
 *
 */
public class ARGModelElement extends ModelElement {

	
	@Override
	public List<Node> getElements(ACGDocument doc) throws InputConfigException {
		List<Node> elements = new ArrayList<Node>();
		
		Element argEl = doc.createElement(argLabel);
		argEl.setAttribute(XMLLoader.CLASS_NAME_ATTR, ARG.class.getCanonicalName());
		argEl.setAttribute(AbstractParameter.XML_PARAM_FREQUENCY, "" + frequency);
		
		if (startingNewick != null) {
			Element newickEl = doc.createElement("StartingNewick");
			Node newickText = doc.createTextNode(startingNewick.getNewick());
			newickEl.appendChild(newickText);
			argEl.appendChild(newickEl);
		}
		
		if (startingFilePath != null) {
			if (startingNewick != null)
				throw new InputConfigException("Both newick and starting ARG file path specified, please choose only one");
			argEl.setAttribute(ARG.XML_FILENAME, startingFilePath);
		}
		
		if (alignmentRef != null) {
			Element alnRef = doc.createElement(alignmentRef.getNodeLabel());
			argEl.appendChild(alnRef);
		}
		
		if (useAllModifiers) {
			Element modList = doc.createElement("ARGModifiers");
			modList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
			appendModifier(modList, doc, modifier.RecombAddRemove.class.getCanonicalName(), "RecombAddRemove", 5);
			appendModifier(modList, doc, modifier.RootHeightModifier.class.getCanonicalName(), "RootHeight", 1);
			appendModifier(modList, doc, modifier.NodeHeightModifier.class.getCanonicalName(), "NodeHeight", 25);
			appendModifier(modList, doc, modifier.SubtreeSwap.class.getCanonicalName(), "SubTreeSwapper", 5);
			appendModifier(modList, doc, modifier.WideSwap.class.getCanonicalName(), "WideSwapper", 5);
			appendModifier(modList, doc, modifier.BreakpointShifter.class.getCanonicalName(), "BPShifter", 5);
			appendModifier(modList, doc, modifier.BreakpointSwapper.class.getCanonicalName(), "BPSwapper", 5);
			argEl.appendChild(modList);
		}
		else {
			if (argMods.size()>0) {
				Element modList = doc.createElement("ARGModifiers");
				modList.setAttribute(XMLLoader.CLASS_NAME_ATTR, XMLLoader.LIST_ATTR);
				for(ARGModifierElement argModEl : argMods) {
					appendModifier(modList, doc, argModEl.className, argModEl.label, argModEl.frequency);
				}
				argEl.appendChild(modList);
			}
		}

		elements.add(argEl);
		return elements;
	}

	@Override
	public void readElements(ACGDocument doc) throws InputConfigException {
		List<String> argLabels = doc.getLabelForClass(ARG.class);
		argMods.clear();
		startingNewick = null;
		startingFilePath = null;
		alignmentRef = null;
		
		if (argLabels.size()==0)
			throw new InputConfigException("No ARGs found");
		
		if (argLabels.size()>1) 
			throw new InputConfigException("Multiple ARGs found, for now only one ARG is supported");
		
		this.argLabel = argLabels.get(0);
		
		Element argEl = doc.getElementForLabel(argLabel);
		String startingFile = argEl.getAttribute(ARG.XML_FILENAME);
		if (startingFile != null && startingFile.length() > 0) {
			startingFilePath = startingFile;
		}
		
		String argFreq = argEl.getAttribute(ARG.XML_PARAM_FREQUENCY);
		if (argFreq != null && argFreq.length()>0) {
			try {
				Double freq = Double.parseDouble(argFreq);
				this.frequency = freq;
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse frequency from ARG element with label: " + argEl.getNodeName());
			}
		}
		
		List<String> childRefs = doc.getChildrenForLabel(argLabel);
		for(String ref : childRefs) {
			Element child = doc.getElementForLabel(ref);
			String childClass = child.getAttribute(XMLLoader.CLASS_NAME_ATTR);
			if (childClass == null)
				throw new InputConfigException("Node with label : " + ref + " has no class attribute?");
			
			if (childClass.equals( Newick.class.getCanonicalName())) {
				Node newickText = child.getFirstChild();
				String newickStr = null;
				if (newickText.getNodeType() == Node.TEXT_NODE)
					newickStr = newickText.getTextContent();
				
				if (newickStr == null)
					throw new InputConfigException("Could not find starting newick string, but found a newick element with label: " + ref);
				startingNewick = new Newick( newickStr );
				
				continue;
			}
			

			if (childClass.equals( XMLLoader.LIST_ATTR)) {
				//Load all the modifiers
				List<String> modRefs = doc.getChildrenForLabel(ref);
				for(String modRef : modRefs) {
					Element modEl = doc.getElementForLabel(modRef);
					addModifierForElement(modEl); //Adds an entry to list of argMods
				}
				continue;
			}
			
			String alnClassName = BasicSequenceAlignment.class.getCanonicalName();
			if (childClass.equals( alnClassName )) {
				alignmentRef = new AlignmentElement();
				alignmentRef.setNodeLabel( child.getNodeName() );
			}
		}
		
		
	}

	private void addModifierForElement(Element modEl) throws InputConfigException {
		String modClass = modEl.getAttribute(XMLLoader.CLASS_NAME_ATTR);
		if (modClass == null)
			throw new InputConfigException("No class found for object with label: " + modEl.getNodeName());
		
		
		ARGModifierElement argModEl = new ARGModifierElement();
		argModEl.className = modClass;
		argModEl.label = modEl.getNodeName();
		String freqStr = modEl.getAttribute(AbstractModifier.XML_FREQUENCY);
		if (freqStr != null && freqStr.length()>0) {
			try {
				Double freq = Double.parseDouble(freqStr);
				argModEl.frequency = freq;
			}
			catch (NumberFormatException nfe) {
				throw new InputConfigException("Could not parse frequency for ARG modifier with label " + modEl.getNodeName() + ", got : " + freqStr);
			}
		}
		
		argMods.add(argModEl);
	}
	
	private void appendModifier(Element mods, ACGDocument doc, String className, String elementName, double frequency) { 
		Element mod = doc.createElement(elementName);
		mod.setAttribute(XMLLoader.CLASS_NAME_ATTR, className);
		mod.setAttribute(AbstractModifier.XML_FREQUENCY, "" + frequency);
		mods.appendChild(mod);
	}
	
	public Newick getStartingNewick() {
		return startingNewick;
	}

	public void setStartingNewick(String startingNewick) {
		this.startingNewick = new Newick(startingNewick);
		startingFilePath = null;
	}

	public String getStartingFilePath() {
		return startingFilePath;
	}

	public void setStartingFilePath(String startingFilePath) {
		this.startingFilePath = startingFilePath;
		startingNewick = null;
	}

	public AlignmentElement getAlignmentRef() {
		return alignmentRef;
	}

	public void setAlignmentRef(AlignmentElement alignmentRef) {
		this.alignmentRef = alignmentRef;
	}

	public Double getFrequency() {
		return frequency;
	}

	public void setFrequency(Double frequency) {
		this.frequency = frequency;
	}

	public boolean isUseAllModifiers() {
		return useAllModifiers;
	}

	public void setUseAllModifiers(boolean useAllModifiers) {
		this.useAllModifiers = useAllModifiers;
	}

	public String getModelLabel() {
		return argLabel;
	}

	public void setModelLabel(String label) {
		this.argLabel = label;
	}
	
	@Override
	public List<DoubleParamElement> getDoubleParameters() {
		return new ArrayList<DoubleParamElement>();
	}
	
	public boolean isUseUPGMA() {
		return useUPGMA;
	}

	public void setUseUPGMA(boolean useUPGMA) {
		this.useUPGMA = useUPGMA;
	}
	
	/**
	 * Little wrapper for ARG modifiers
	 * @author brendano
	 *
	 */
	class ARGModifierElement {
		String label;
		String className;
		Double frequency = 1.0;
	}

	
	
	private List<ARGModifierElement> argMods = new ArrayList<ARGModifierElement>();
	
	private String startingFilePath = null;
	private AlignmentElement alignmentRef = null;
	private Double frequency = 10.0;
	private boolean useAllModifiers = true;
	private boolean useUPGMA = true;
	private String argLabel = "ARG";
	private Newick startingNewick = null;
	
}
