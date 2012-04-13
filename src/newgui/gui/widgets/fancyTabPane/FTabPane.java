package newgui.gui.widgets.fancyTabPane;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import newgui.UIConstants;

/**
 * A fancy tab pane is a better-looking type of jtabbed pane. All components displayed are associated
 * with a labelled tab, and the user switches back and forth between components by clicking on
 * tabs that are visibe in a panel at the top of the screen. New components/ labelled tabs are 
 * created via the addComponent(label, component) method
 * @author brendan
 *
 */
public class FTabPane extends JPanel implements ChangeListener {
	
	private Map<FancyTab, JComponent> tabMap = new HashMap<FancyTab, JComponent>();
	private FancyTabsPanel tabsPanel = null;
	
	//If true, show a tab even if there's only one panel attached. If false, don't
	private boolean showTabIfOne = true;
	
	public FTabPane() {
		setLayout(new BorderLayout());
		setOpaque(false);
		setBorder(BorderFactory.createEmptyBorder(0, leftPadding, 4, rightPadding));
		tabsPanel = new FancyTabsPanel(this);
		add(tabsPanel, BorderLayout.NORTH);
		centerPanel = new JPanel();
		centerPanel.setBackground(getBackground());
		centerPanel.setLayout(new BorderLayout());
		centerPanel.setOpaque(false);
		add(centerPanel, BorderLayout.CENTER);
		revalidate();
		repaint();
	}

	
	/**
	 * Add a new component with the given label to this panel
	 * @param label
	 * @param comp
	 */
	public void addComponent(String label, JComponent comp) {
		FancyTab tab = new FancyTab(this, label);
		tab.addListener(this);
		tabMap.put(tab, comp);
		tabsPanel.addTab(tab);
		showComponent(comp);
	}
	
	public void removeComponent(JComponent comp) {
		FancyTab tab = tabForComponent(comp);
		removeComponentForTab(tab);
	}
	
	/**
	 * Remove the component associated with the given tab (as well as the tab) from everything
	 * @param tab
	 */
	public void removeComponentForTab(FancyTab tab) {
		tabsPanel.removeTab(tab);
		tabMap.remove(tab);
		tab.removeListener(this);
		
		if (tabMap.size() > 0) {
			Collection<FancyTab> tabs = tabMap.keySet();
			Iterator<FancyTab> fit = tabs.iterator();
			FancyTab newTab = fit.next();
			if (newTab != null)
				showComponent( tabMap.get(newTab));
		}
		else {
			centerPanel.removeAll();
			revalidate();
			repaint();
		}
	}
	
	/**
	 * Returns the tab associated with the given component
	 * @param comp
	 * @return
	 */
	private FancyTab tabForComponent(JComponent comp) {
		for(FancyTab tab : tabMap.keySet()) {
			if (comp == tabMap.get(tab)) {
				return tab;
			}
		}
		return null;
	}
	
	/**
	 * Returns true if this contains the given component
	 * @param comp
	 * @return
	 */
	public boolean contains(JComponent comp) {
		for(JComponent c : tabMap.values()) {
			if (c == comp) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Obtain the total number of components added to this pane
	 */
	public int getTabCount() {
		return tabMap.size();
	}
	
	/**
	 * Return the component at index i
	 * @param i
	 * @return
	 */
	public JComponent getComponentAtIndex(int i) {
		FancyTab tab= tabsPanel.getTabAt(i);
		return tabMap.get(tab);
	}
	
	public void showComponent(JComponent comp) {
		FancyTab tab = tabForComponent(comp);
		if (tab == null) {
			throw new IllegalArgumentException("No tab associated with component");
		}
		tabsPanel.showTab(tab);
		centerPanel.removeAll();
		if (! (comp instanceof JScrollPane)) {
			JScrollPane scrollPane = new JScrollPane(comp);
			scrollPane.setViewportBorder(BorderFactory.createEmptyBorder());
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			scrollPane.getViewport().setOpaque(false);
			scrollPane.setOpaque(false);
			//scrollPane.getViewport().setBackground(Color.RED);
			centerPanel.add(scrollPane, BorderLayout.CENTER);
		}
		else {
			centerPanel.add(comp, BorderLayout.CENTER);
		}
		
		if (tabMap.size()<2 && (!showTabIfOne)) {
			this.remove(tabsPanel);
		}
		if (showTabIfOne || tabMap.size()>1) {
			add(tabsPanel, BorderLayout.NORTH);
		}
		revalidate();
		repaint();
	}

	@Override
	public void stateChanged(ChangeEvent e) {
		if (e.getSource()  instanceof FancyTab) {
			FancyTab tab = (FancyTab)e.getSource();
			JComponent comp = tabMap.get(tab);
			showComponent(comp);
		}
	}
	
	

	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		

		if (tabMap.size()>1 || (tabMap.size()==1 && showTabIfOne)) {
			//Outer shadow..goes behind everything else
			g.setColor(shadowColor);
			((Graphics2D)g).setStroke(shadowStroke);
			g.drawRoundRect(leftPadding+3, tabsPanel.getHeight()+2, getWidth()-leftPadding-rightPadding-2, getHeight()-tabsPanel.getHeight()-7, 8, 8);
			
			GradientPaint gp = new GradientPaint(1, tabsPanel.getHeight(), dark1, 1, this.getHeight()/4f, UIConstants.lightBackground);
			g2d.setPaint(gp);
			g2d.fillRoundRect(leftPadding, tabsPanel.getHeight(), getWidth()-leftPadding-rightPadding, getHeight()-tabsPanel.getHeight()-6, 4, 4);
			
			g2d.setColor(gray2);
			g2d.drawRoundRect(leftPadding-1, tabsPanel.getHeight()+1, getWidth()-leftPadding-rightPadding+2, getHeight(), 4,4);

			//Paint over part where selected tab is...
			g2d.setColor(dark1);
			g.drawLine(tabsPanel.getSelectedTabLeftX(), tabsPanel.getHeight()+1, tabsPanel.getSelectedTabRightX(), tabsPanel.getHeight()+1);
			
			g2d.setStroke(normalStroke);
			g.setColor(lineColor);
			g.drawRoundRect(leftPadding-1, tabsPanel.getHeight()-1, getWidth()-leftPadding-rightPadding+1, getHeight()-tabsPanel.getHeight()-5, 8, 8);
		}
	}
	

	final int leftPadding = 3;
	final int rightPadding = 8;
	final static Color bgColor = new Color(253, 253, 253);
	final static Color gray1 = Color.white;
	final static Color gray2 = new Color(250, 250, 250, 100);
	final static float topDark = 0.925f;
	final static Color dark1 = new Color(topDark, topDark, topDark);
	final static Color dark2 = new Color(220, 220, 220, 100);
	final static Color shadowColor = new Color(0f, 0f, 0f, 0.2f);
	final static Color lineColor = new Color(200, 200, 200);
	final static Stroke shadowStroke = new BasicStroke(1.8f);
	final static Stroke normalStroke = new BasicStroke(1.0f);
	private JPanel centerPanel;
}

