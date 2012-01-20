package newgui.gui.display;

import java.awt.BorderLayout;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

import newgui.UIConstants;
import newgui.gui.widgets.sideTabPane.SideTabPane;

public class TestDisplay extends Display {

	
	public TestDisplay() {
		setLayout(new BorderLayout());
		
		SideTabPane sidePane = new SideTabPane();
		
		this.add(sidePane, BorderLayout.CENTER);
		
		ImageIcon icon = UIConstants.getIcon("gui/icons/openFile.png");
		JLabel label1 = new JLabel("Component for tab 1");
		
		sidePane.addTab("Tab number 1", icon, label1);
		
		ImageIcon icon2 = UIConstants.getIcon("gui/icons/scaledBlueArrow.png");
		JLabel label2 = new JLabel("Component for tab 2");
		sidePane.addTab("Tab number dos", icon2, label2);
		
		
	}
}
