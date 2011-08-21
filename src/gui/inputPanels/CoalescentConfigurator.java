package gui.inputPanels;

import gui.widgets.RoundedPanel;

import java.awt.Dimension;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class CoalescentConfigurator extends RoundedPanel implements Configurator {

	public CoalescentConfigurator() {
		setMaximumSize(new Dimension(1000, 50));
		setPreferredSize(new Dimension(500, 50));
		add(new JLabel("Coalescent model:"));
		add(new JComboBox(new String[]{"Constant size", "Exponential growth"}));
		add(new JLabel("Recombination rate:"));
		add(new JComboBox(new String[]{"Constant rate"}));
	}

	@Override
	public Node getXMLNode(Document doc) {
		// TODO Auto-generated method stub
		return null;
	}
}
