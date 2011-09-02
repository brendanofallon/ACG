package gui.inputPanels;

import gui.widgets.RoundedPanel;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;

import modifier.ScaleModifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import coalescent.ConstantPopSize;
import coalescent.ConstantRecombination;
import coalescent.ExponentialGrowth;

import sequence.Alignment;

import xml.XMLLoader;

public class CoalescentConfigurator extends RoundedPanel implements Configurator {
	
	private JComboBox coalModelBox;
	private String[] coalModels = new String[]{"Constant size", "Exponential growth"};
	
	private JComboBox recombModelBox;
	private String[] recombModels = new String[]{"No recombination", "Constant recombination"};
	
	public CoalescentConfigurator() {
		setMaximumSize(new Dimension(1000, 50));
		setPreferredSize(new Dimension(500, 50));
		add(new JLabel("Coalescent model:"));
		coalModelBox = new JComboBox(coalModels);
		add(coalModelBox);
		add(new JLabel("Recombination rate:"));
		recombModelBox = new JComboBox(recombModels);
		add(recombModelBox);
	}

	@Override
	public Node[] getXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		Element popSizeElement = doc.createElement("PopSize");
		Element recombElement = doc.createElement("RecombRate");
		
		//Constant size
		if (coalModelBox.getSelectedItem().toString().equals(coalModels[0])) {
			popSizeElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantPopSize.class.getCanonicalName());
			Element popSizeScaler = doc.createElement("popSizeModifier");
			popSizeScaler.setAttribute(XMLLoader.CLASS_NAME_ATTR, ScaleModifier.class.getCanonicalName());
			popSizeElement.appendChild(popSizeScaler);
		}
		
		//Exponential growth
		if (coalModelBox.getSelectedItem().toString().equals(coalModels[1])) {
			popSizeElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ExponentialGrowth.class.getCanonicalName());
			Element baseSize = createDoubleParamElement(doc, "BaseSize", 0.01, 1e-10, 1e20);
			Element baseSizeMod = doc.createElement("BaseSizeModifier");
			baseSizeMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.ScaleModifier.class.getCanonicalName());
			baseSize.appendChild(baseSizeMod);
			popSizeElement.appendChild(baseSize);
			
			Element growthRate = createDoubleParamElement(doc, "GrowthRate", 0, -1e12, 1e12);
			Element growthRateMod = doc.createElement("GrowthRateModifier");
			growthRateMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.SimpleModifier.class.getCanonicalName());
			growthRate.appendChild(growthRateMod);
			popSizeElement.appendChild(growthRate);
			
		}
		

		if (recombModelBox.getSelectedItem().toString().equals(recombModels[0])) {
			recombElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantRecombination.class.getCanonicalName());
			Element recRate = createDoubleParamElement(doc, "RecombRate", 0, 0, 0);
			recombElement.appendChild(recRate);
		}
		
		if (recombModelBox.getSelectedItem().toString().equals(recombModels[1])) {
			recombElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantRecombination.class.getCanonicalName());
			Element recRate = createDoubleParamElement(doc, "RecombRate", 1.0, 0, 1e20);
			Element recMod = doc.createElement("RecombinationRateModifier");
			recMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.ScaleModifier.class.getCanonicalName());
			recRate.appendChild(recMod);
			recombElement.appendChild(recRate);
		}

			
		Element nodes[] = new Element[2];
		nodes[0] = popSizeElement;
		nodes[1] = recombElement;
		return nodes;
	}

	
	private static Element createDoubleParamElement(Document doc, String label, double value, double lowerBound, double upperBound) {
		Element el = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  parameter.DoubleParameter.class.getCanonicalName());
		el.setAttribute("value", "" + value);
		el.setAttribute("lowerBound", "" + lowerBound);
		el.setAttribute("upperBound", "" + upperBound);
		
		return el;
	}

}
