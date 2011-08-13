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
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BuildPanel extends JPanel {

	public BuildPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		RoundedPanel pane = new RoundedPanel();
		pane.setLayout(new FlowLayout(FlowLayout.LEFT));
		//pane.setLayout(new BorderLayout());
		pane.setMaximumSize(new Dimension(1000, 55));
		
		JPanel interior = new JPanel();
		interior.setLayout(new BoxLayout(interior, BoxLayout.X_AXIS));
		interior.setMaximumSize(new Dimension(1000, 32));
		interior.setPreferredSize(new Dimension(500, 30));
		interior.add(Box.createGlue());
		interior.add(Box.createHorizontalStrut(20));
		interior.add(new JTextField("Aln info"));
		interior.setBackground(new Color(1.0f, 1.0f, 1.0f, 0f));
		interior.add(Box.createGlue());
		interior.add(Box.createHorizontalStrut(20));
		interior.add(new JComboBox(new Object[]{"First", "Second"}));
		interior.add(Box.createGlue());
		interior.add(Box.createHorizontalStrut(20));
		interior.add(new JComboBox(new Object[]{"Five", "Six"}));
		interior.add(Box.createGlue());
		interior.add(Box.createHorizontalStrut(20));
		interior.add(new JButton("Configure"));
		
		interior.add(Box.createGlue());
		interior.add(Box.createHorizontalStrut(20));
		interior.add(new JButton("Remove"));
		
		interior.add(Box.createGlue());
		pane.add(interior);
		this.add(Box.createVerticalStrut(10));
		this.add(pane);
		JButton newPaneButton = new JButton("Add alignment");
		interior.setAlignmentY(LEFT_ALIGNMENT);
		newPaneButton.setAlignmentY(LEFT_ALIGNMENT);
		this.setAlignmentY(LEFT_ALIGNMENT);
		this.add(newPaneButton);
	}
}
