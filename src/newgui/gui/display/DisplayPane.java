package newgui.gui.display;


import java.awt.BorderLayout;

import javax.swing.JPanel;

import newgui.gui.widgets.fancyTabPane.FTabPane;

/**
 * This is the panel that houses and manages zero or more Displays.
 * @author brendan
 *
 */
public class DisplayPane extends JPanel {

	private FTabPane tabPane = new FTabPane();
	
	public DisplayPane() {
		setOpaque(false);
		setLayout(new BorderLayout());
		add(tabPane, BorderLayout.CENTER);
	}
	
	public void addDisplay(Display display) {
		tabPane.addComponent(display.getTitle(), display);
	}
	
	public void removeDisplay(Display display) {
		tabPane.removeComponent(display);
	}
}
