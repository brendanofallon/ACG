package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import gui.widgets.RoundedPanel;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BuildPanel extends JPanel {

	public BuildPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		RoundedPanel alnPanel = new RoundedPanel();
		alnPanel.setMaximumSize(new Dimension(1000, 50));
		alnPanel.setPreferredSize(new Dimension(500, 50));
		alnPanel.add(Box.createGlue());
		alnPanel.add(Box.createHorizontalStrut(20));
		alnPanel.add(new JLabel("Alignment :"));
		alnPanel.add(new JTextField("Choose"));
		JButton chooseAlnButton = new JButton("Browse");		
		alnPanel.add(chooseAlnButton);
		add(alnPanel);
		add(Box.createVerticalStrut(5));
		
		RoundedPanel siteModelPanel = new RoundedPanel();
		siteModelPanel.setMaximumSize(new Dimension(1000, 50));
		siteModelPanel.setPreferredSize(new Dimension(500, 50));
		siteModelPanel.add(new JLabel("Mutation model: "));
		siteModelPanel.add(new JComboBox(new Object[]{"JC69", "K2P", "F84", "TN93"}));
		siteModelPanel.add(new JLabel("Rate model: "));
		siteModelPanel.add(new JComboBox(new Object[]{"1 rate", "Gamma rates", "Custom rates"}));
		add(siteModelPanel);
		add(Box.createVerticalStrut(5));
		
		RoundedPanel coalescentPanel = new RoundedPanel();
		coalescentPanel.setMaximumSize(new Dimension(1000, 50));
		coalescentPanel.setPreferredSize(new Dimension(500, 50));
		coalescentPanel.add(new JLabel("Coalescent model:"));
		coalescentPanel.add(new JComboBox(new String[]{"Constant size", "Exponential growth"}));
		coalescentPanel.add(new JLabel("Recombination rate:"));
		coalescentPanel.add(new JComboBox(new String[]{"Constant rate"}));
		add(coalescentPanel);
		add(Box.createVerticalStrut(5));
		
		RoundedPanel loggingPanel = new RoundedPanel();
		coalescentPanel.setMaximumSize(new Dimension(1000, 50));
		coalescentPanel.setPreferredSize(new Dimension(500, 50));
		loggingPanel.add(new JLabel("Logging :"));
		loggingPanel.add(new JCheckBox("Parameter values"));
		loggingPanel.add(new JCheckBox("Recomb. position"));
		loggingPanel.add(new JCheckBox("TMRCA"));
		loggingPanel.add(new JCheckBox("Marginal trees"));
		add(loggingPanel);
		
		
		add(Box.createVerticalGlue());
		JButton newPaneButton = new JButton("Add alignment");
		alnPanel.setAlignmentY(LEFT_ALIGNMENT);
		this.add(newPaneButton);
	}
}
