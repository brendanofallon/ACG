package newgui.gui.widgets.panelPile;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

import newgui.UIConstants;

/**
 * A single panel in a PanelPile
 * @author brendan
 *
 */
public class PPanel extends JPanel {

	private boolean showing = false;
	private PPanelHeader header = null;
	private PanelPile pileParent = null;
	
	public PPanel(PanelPile pileParent, String headerText) {
		header = new PPanelHeader(headerText);
		header.setAlignmentX(Component.LEFT_ALIGNMENT);
		this.setAlignmentX(Component.LEFT_ALIGNMENT);
		setBackground(UIConstants.componentBackground);
		header.setBackground(UIConstants.componentBackground);
		this.pileParent = pileParent;
		header.addMouseListener(new ClickListener(this));
		this.setBackground(Color.white);
	}
	
	public PPanelHeader getHeader() {
		return header;
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.setColor(Color.LIGHT_GRAY);
		g.drawLine(getWidth()-1, -1, getWidth()-1, getHeight());
	}
	
	/**
	 * Listens for 
	 * @author brendan
	 *
	 */
	class ClickListener extends MouseAdapter {
		
		final PPanel owner;
		
		public ClickListener(PPanel panel) {
			this.owner = panel;
		}
		
		public void mouseClicked(MouseEvent me) {
			if (me.getClickCount()>1) {
				pileParent.showPanel(owner, true);
			}
		}
	}
}
