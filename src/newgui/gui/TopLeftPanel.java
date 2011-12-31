package newgui.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import gui.widgets.BorderlessButton;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

/**
 * Panel that holds a couple of buttons and the search text field
 * @author brendan
 *
 */
public class TopLeftPanel extends JPanel {
	
	BorderlessButton importButton;
	
	public TopLeftPanel() {
		this.setBackground(Color.white);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setPreferredSize(new Dimension(200, 50));
		ImageIcon openIcon = ViewerWindow.getIcon("icons/openFile.png");
		importButton = new BorderlessButton(null, openIcon);
		importButton.setPreferredSize(new Dimension(30, 30));
		this.add(importButton);
		
	}
}
