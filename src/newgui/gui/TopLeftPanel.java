package newgui.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;

import gui.widgets.BorderlessButton;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import newgui.UIConstants;

/**
 * Panel that holds a couple of buttons and the search text field
 * @author brendan
 *
 */
public class TopLeftPanel extends JPanel {
	
	BorderlessButton importButton;
	
	public TopLeftPanel() {
		this.setBackground(UIConstants.lightBackground);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setPreferredSize(new Dimension(200, 50));
		ImageIcon openIcon =  UIConstants.blueRightArrow;
		if (openIcon != null) {
			importButton = new BorderlessButton(null, openIcon);
			importButton.setYDif(-1);
		}
		else {
			importButton = new BorderlessButton("Open", null);
		}
		importButton.setPreferredSize(new Dimension(32, 28));
		this.add(importButton);
		
		
	}
}
