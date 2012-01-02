package newgui.gui.widgets.panelPile;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

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
		this.pileParent = pileParent;
		header.addMouseListener(new ClickListener(this));
		this.setBackground(Color.white);
	}
	
	public PPanelHeader getHeader() {
		return header;
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
