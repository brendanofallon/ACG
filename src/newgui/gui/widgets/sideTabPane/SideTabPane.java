package newgui.gui.widgets.sideTabPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
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
	public static final Color sidePanelBackground = UIConstants.darkBackground;
	
	private int sidePanelWidth = 100;
	
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
	
	private void initComponents() {
		this.setLayout(new BorderLayout());
		
		sidePanel = new JPanel();
		sidePanel.setBackground(sidePanelBackground);
		sidePanel.setMaximumSize(new Dimension(sidePanelWidth, 32000));
		sidePanel.setPreferredSize(new Dimension(sidePanelWidth, 400));
		sidePanel.setMinimumSize(new Dimension(sidePanelWidth, 1));
		sidePanel.setLayout(new BoxLayout(sidePanel, BoxLayout.Y_AXIS));
		sidePanel.add(Box.createVerticalStrut(10));
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
	
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(sidePanelWidth, 0, sidePanelWidth, getHeight());
		
		g.setColor(new Color(1f, 1f, 1f, 0.5f));
		g.drawLine(sidePanelWidth+1, 0, sidePanelWidth+1, getHeight());
	
		
	}
	
	class TabCompPair {
		SideTab tab;
		Component comp;
	}
}
