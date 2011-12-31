package newgui.gui.widgets.fancyTabPane;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * The panel containing the tabs to draw at the top of a FancyTabPane. Pretty much everything in here
 * is only meant to be used by a FancyTabPane, so everything is protected 
 * @author brendan
 *
 */
public class FancyTabsPanel extends JPanel {

	private List<FancyTab> tabs = new ArrayList<FancyTab>();
	private FTabPane pane;
	private Component glueBox;
	private static final int leftPadding = 3;
	
	protected FancyTabsPanel(FTabPane pane) {
		this.pane = pane;
		this.setBackground(Color.white);
		glueBox = Box.createGlue();
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.add(Box.createHorizontalStrut(leftPadding));
		this.add(glueBox);
	}
	
	/**
	 * Add a new tab to the tabs panel. This isn't meant to be used by the outside
	 * @param tab
	 */
	protected void addTab(FancyTab tab) {
		this.remove(glueBox);
		tabs.add(tab);
		this.add(tab);
		this.add(glueBox);
		revalidate();
		repaint();
	}
	
	protected void removeTab(FancyTab tab) {
		tabs.remove(tab);
		this.remove(tab);
		revalidate();
		repaint();
	}
	
	protected void showTab(FancyTab tab) {
		if (! containsTab(tab)) {
			throw new IllegalArgumentException("Given tab is not in the list of tabs");
		}

		for(FancyTab aTab : tabs) {
			if (aTab == tab) {
				tab.setSelected(true);
			}
			else {
				aTab.setSelected(false);

			}
		}
		checkConsistency();
		repaint();
	}
	
	protected FancyTab getSelectedTab() {
		for(FancyTab tab : tabs) {
			if (tab.isSelected())
				return tab;
		}
		return null;
	}
	
	/**
	 * Make sure only one tab is selected
	 */
	private void checkConsistency() {
		int count = 0;
		for(FancyTab tab : tabs) {
			if (tab.isSelected())
				count++;
		}
		if (count > 1)
			throw new IllegalStateException("Multiple tabs are selected!");
		if (tabs.size()>0 && count==0) {
			throw new IllegalStateException("NO tabs are selected!");
		}
	}
	
	protected boolean containsTab(FancyTab tab) {
		return tabs.contains(tab);
	}
	
	/**
	 * We override this just so we can paint a nice-looking line at the right spot across 
	 * the bottom of the panel...
	 */
	public void paint(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		super.paintComponent(g);
		super.paint(g);
		
		int firstStart = 0;
		int firstEnd = 0;
		int secondStart = 0;
		int secondEnd = getWidth();
		int width = leftPadding;
		
		for(int i=0; i<tabs.size(); i++) {
			FancyTab tab = tabs.get(i);
			if (tab == getSelectedTab()) {
				firstEnd = width;
				secondStart = firstEnd + tab.getWidth();
			}
			width += tab.getWidth();
			
		}
		
		
		g2d.setColor(Color.LIGHT_GRAY);
		int lineHeight = getHeight()-1;
		g2d.drawLine(firstStart, lineHeight, firstEnd, lineHeight);
		g2d.drawLine(secondStart, lineHeight, secondEnd, lineHeight);
		
	}
}
