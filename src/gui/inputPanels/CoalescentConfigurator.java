package gui.inputPanels;

import gui.widgets.RoundedPanel;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.xml.parsers.ParserConfigurationException;

import modifier.ScaleModifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import parameter.DoubleParameter;

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
	public Element[] getXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		
		Element popSizeElement = doc.createElement("PopSize");
		Element recombElement = doc.createElement("RecombModel");
		
		//Constant size
		if (coalModelBox.getSelectedItem().toString().equals(coalModels[0])) {
			popSizeElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantPopSize.class.getCanonicalName());
			popSizeElement.setAttribute(DoubleParameter.XML_VALUE, "0.01");
			
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
			recombElement.setAttribute(DoubleParameter.XML_VALUE, "0.0");
			recombElement.setAttribute(DoubleParameter.XML_LOWERBOUND, "0.0");
			recombElement.setAttribute(DoubleParameter.XML_UPPERBOUND, "0.0");
		}
		
		if (recombModelBox.getSelectedItem().toString().equals(recombModels[1])) {
			recombElement.setAttribute(XMLLoader.CLASS_NAME_ATTR, ConstantRecombination.class.getCanonicalName());
			recombElement.setAttribute(DoubleParameter.XML_VALUE, "0.0");
			recombElement.setAttribute(DoubleParameter.XML_LOWERBOUND, "0.0");
			recombElement.setAttribute(DoubleParameter.XML_UPPERBOUND, "1e10");
			Element recMod = doc.createElement("RecombinationRateModifier");
			recMod.setAttribute(XMLLoader.CLASS_NAME_ATTR,  modifier.ScaleModifier.class.getCanonicalName());

			
			//TODO Add in exponential prior here
			recombElement.appendChild(recMod);
		}

			
		Element nodes[] = new Element[2];
		nodes[0] = popSizeElement;
		nodes[1] = recombElement;
		return nodes;
	}

	
	private static Element createDoubleParamElement(Document doc, String label, double value, double lowerBound, double upperBound) {
		Element el = doc.createElement(label);
		el.setAttribute(XMLLoader.CLASS_NAME_ATTR,  parameter.DoubleParameter.class.getCanonicalName());
		el.setAttribute(DoubleParameter.XML_VALUE, "" + value);
		el.setAttribute(DoubleParameter.XML_LOWERBOUND, "" + lowerBound);
		el.setAttribute(DoubleParameter.XML_UPPERBOUND, "" + upperBound);
		
		return el;
	}

}

