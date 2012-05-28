package newgui.gui.widgets.sideTabPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JPanel;

import newgui.UIConstants;

/**
 * A panel containing a list of tabs along the left side which allows
 * the user to switch to different components in the center. Like a tabpane, but
 * tabs are bigger and on the left
 * @author brendano
 *
 */
public class SideTabPane extends JPanel {

	private JPanel sidePanel;
	private JPanel centerPanel;
	private List<TabCompPair> compList = new ArrayList<TabCompPair>();
	public static final Color sidePanelBackground = UIConstants.componentBackground;
	
	private int sidePanelWidth = 120;
	public static final int topPadding = 50; //Space between top of component and first SideTab
	public static final int rightPadding = 4; //Space to right of main area of component, used for drawing a shadow
	public static final int tabHeight = 65;
	
	public SideTabPane() {
		initComponents();
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent me) {
				handleMouseClick(me);
			}

			@Override
			public void mouseEntered(MouseEvent arg0) {
			}

			public void mouseExited(MouseEvent arg0) {
			}

			public void mousePressed(MouseEvent arg0) {
			}

			public void mouseReleased(MouseEvent arg0) {
			}
			
		});
	}
	
	protected void handleMouseClick(MouseEvent me) {
		SideTab tab = tabForPosition(me.getPoint());
		if (tab != null) {
			selectTab(tab);
		}
	}
	
	/**
	 * Causes the given tab to be selected, thus showing the associated component. 
	 * All other tabs will be unselected
	 * @param tab
	 */
	public void selectTab(SideTab tab) {
		for(TabCompPair pair : compList) {
			if (pair.tab == tab) {
				tab.setSelected(true);
				showComponent(pair.comp);
			}
			else {
				pair.tab.setSelected(false);
			}
		}
		repaint();
	}
	
	/**
	 * Get the index of the selected component, or -1 if there's somehow no component selected
	 * @return
	 */
	public int getSelectedIndex() {
		for(int i=0; i<compList.size(); i++)
			if (compList.get(i).tab.isSelected())
				return i;
		return -1;
	}
	
	/**
	 * Cause the tab at the given index to be selected
	 * @param tabIndex
	 */
	public void selectTab(int tabIndex) {
		SideTab tab = compList.get(tabIndex).tab;
		selectTab(tab);
	}
	
	/**
	 * Cause the tab associated with the given component to be selected
	 * @param comp
	 */
	public void selectTabForComponent(Component comp) {
		for(TabCompPair pair : compList) {
			if (pair.comp == comp) {
				selectTab(pair.tab);
			}
		}
	}
	
	/**
	 * Get the number of tabs (/components) added to this panel
	 * @return
	 */
	public int getTabCount() {
		return compList.size();
	}
	
	
	private void showComponent(Component comp) {
		centerPanel.removeAll();
		centerPanel.add(comp, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	/**
	 * Return the tab underneath the given point, where the point is relative to the entire
	 * SideTabPane
	 * @param point
	 * @return
	 */
	protected SideTab tabForPosition(Point2D point) {
		//Click not on side panel, return null
		//System.out.println("Position is : " + point.getX() + ", " + point.getY());
		if (point.getX() > sidePanel.getWidth()) {
			return null;
		}
		
		for(TabCompPair pair : compList) {
			SideTab tab = pair.tab;
			//System.out.println("Tab y:" + tab.getY() + " height: " + tab.getHeight());
			if (point.getY() > tab.getY() && point.getY() < (tab.getY() + tab.getHeight())) {
				return tab;
			}
		}
		return null;
	}

	public void addTab(String label, ImageIcon icon, JComponent comp) {
		SideTab tab = new SideTab(label, icon);
		sidePanel.add(tab);
		
		TabCompPair pair = new TabCompPair();
		pair.tab = tab;
		pair.comp = comp;
	
		compList.add(pair);
		selectTab(tab);
	}
	
	public SideTab getSelectedTab() {
		for(TabCompPair pair : compList) {
			if (pair.tab.isSelected()) {
				return pair.tab;
			}
		}
		
		return null;
	}
	
	private void initComponents() {
		this.setLayout(new BorderLayout());
		
		sidePanel = new SidePanel(this);
		//sidePanel.setBackground(sidePanelBackground);
		sidePanel.setMaximumSize(new Dimension(sidePanelWidth, 32000));
		sidePanel.setPreferredSize(new Dimension(sidePanelWidth, 400));
		sidePanel.setMinimumSize(new Dimension(sidePanelWidth, 1));
		//sidePanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, rightPadding));
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		sidePanel.add(Box.createVerticalStrut(topPadding));
		add(sidePanel, BorderLayout.WEST);
		
		centerPanel = new JPanel();
		centerPanel.setOpaque(false);
		centerPanel.setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	
		int topHalfBottom =0;
		int bottomHalfTop = 0;
		int height = topPadding;
		for(int i=0; i<compList.size(); i++) {
			SideTab tab = compList.get(i).tab;
			if (tab.isSelected()) {
				topHalfBottom = height;
				bottomHalfTop = height + tab.getHeight();
			}
			height += tab.getHeight();
		}
		
		

//		g.setColor(Color.LIGHT_GRAY);
//		g.drawLine(sidePanelWidth-1, 0, sidePanelWidth-1, topHalfBottom);
//		g.drawLine(sidePanelWidth, bottomHalfTop, sidePanelWidth, getHeight());
//		
//		
//		g.setColor(new Color(1f, 1f, 1f, 0.5f));
//		g.drawLine(sidePanelWidth+1, 0, sidePanelWidth+1, topHalfBottom);
//		g.drawLine(sidePanelWidth+1, bottomHalfTop, sidePanelWidth+1, getHeight());
		
		
	}
	
	class TabCompPair {
		SideTab tab;
		Component comp;
	}
}
