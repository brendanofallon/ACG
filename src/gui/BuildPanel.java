package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import gui.document.ACGDocumentBuilder;
import gui.inputPanels.AlignmentConfigurator;
import gui.inputPanels.CoalescentConfigurator;
import gui.inputPanels.SiteModelConfigurator;
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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.w3c.dom.Node;

public class BuildPanel extends JPanel {

	public BuildPanel(ACGFrame acgParent) {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		add(Box.createVerticalStrut(5));
		alnPanel = new AlignmentConfigurator(acgParent);
		add(alnPanel);
		add(Box.createVerticalStrut(5));
		
		siteModelPanel = new SiteModelConfigurator();
		add(siteModelPanel);
		add(Box.createVerticalStrut(5));
		
		CoalescentConfigurator coalescentPanel = new CoalescentConfigurator();
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
		newPaneButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buildDocument();
			}
		});
		alnPanel.setAlignmentY(LEFT_ALIGNMENT);
		this.add(newPaneButton);
	}

	protected void buildDocument() {
		
		Node[] alnNodes;
		try {
			ACGDocumentBuilder docBuilder = new ACGDocumentBuilder();
			alnNodes = alnPanel.getXMLNodes(docBuilder.getDocument());
			
			for(int i=0; i<alnNodes.length; i++)
				docBuilder.appendNode(alnNodes[i]);	
			
			
			Node[] siteModelNodes = siteModelPanel.getXMLNodes(docBuilder.getDocument());
			for(int i=0; i<siteModelNodes.length; i++)
				docBuilder.appendNode(siteModelNodes[i]);
			
			
			String str = docBuilder.getString();
			System.out.println("String is : \n" + str);
		} catch (ParserConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransformerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	
	}
	
	
	AlignmentConfigurator alnPanel;
	SiteModelConfigurator siteModelPanel;
}
