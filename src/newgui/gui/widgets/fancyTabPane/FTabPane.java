package newgui.gui.widgets.fancyTabPane;

import java.awt.BorderLayout;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

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
	
	public FTabPane() {
		setLayout(new BorderLayout());
		add(tabsPanel, BorderLayout.NORTH);
		centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		add(centerPanel, BorderLayout.CENTER);
	}

	
	public void addComponent(String label, JComponent comp) {
		FancyTab tab = new FancyTab(label);
		tab.addListener(this);
		tabMap.put(tab, comp);
		tabsPanel.addTab(tab);
		showComponent(comp);
	}
	
	public void removeComponent(JComponent comp) {
		FancyTab tab = tabForComponent(comp);
		if (tab != null) {
			tabsPanel.removeTab(tab);
		}
		tabMap.remove(tab);
		tab.removeListener(this);
		
		System.out.println("Ahh! Component not actually removed from parent!");
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
			centerPanel.add(scrollPane, BorderLayout.CENTER);
		}
		else {
			centerPanel.add(comp, BorderLayout.CENTER);
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

	private JPanel centerPanel;
}

