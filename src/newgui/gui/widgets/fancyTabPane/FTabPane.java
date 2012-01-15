package newgui.gui.widgets.fancyTabPane;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
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
	private FancyTabsPanel tabsPanel = new FancyTabsPanel(this);
	
	//If true, show a tab even if there's only one panel attached. If false, don't
	private boolean showTabIfOne = true;
	
	public FTabPane() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(0, 5, 4, 5));
		setBackground(UIConstants.lightBackground);
		add(tabsPanel, BorderLayout.NORTH);
		centerPanel = new JPanel();
		centerPanel.setBackground(getBackground());
		centerPanel.setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
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
		g.setColor(getBackground());
		g.fillRect(1, 1, getWidth(), getHeight());
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (tabMap.size()>1 || (tabMap.size()==1 && showTabIfOne)) {
			g2d.setColor(Color.LIGHT_GRAY);
			g2d.drawRoundRect(3, tabsPanel.getHeight()-2, getWidth()-6, getHeight()-tabsPanel.getHeight(), 12, 12);
		}
	}
	
	private JPanel centerPanel;
}

