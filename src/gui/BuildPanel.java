package gui;

import java.awt.Dimension;
import java.awt.FlowLayout;

import gui.widgets.RoundedPanel;

import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class BuildPanel extends JPanel {

	public BuildPanel() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		RoundedPanel pane = new RoundedPanel();
		pane.setLayout(new FlowLayout(FlowLayout.LEFT));
		pane.add(new JTextField("Some info here"));
		pane.setMaximumSize(new Dimension(1000, 75));
		this.add(pane);
	}
}
