package gui.widgets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;
import javax.swing.event.MouseInputAdapter;

/**
 * A label with button-like functionality but no border
 * @author brendano
 *
 */
public class BorderlessButton extends JLabel {

	ImageIcon icon = null;
	
	List<ActionListener> actionListeners = new ArrayList<ActionListener>();
	
	public BorderlessButton(ImageIcon icon) {
		super(icon);
		super.setHorizontalAlignment(SwingConstants.LEFT);
		super.setVerticalAlignment(SwingConstants.TOP);
		setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
		if (icon == null)
			this.setText("NULL");
		else
			setPreferredSize(new Dimension(icon.getIconWidth()+2, icon.getIconHeight()+2));
		Listener listener = new Listener();
		addMouseListener(listener);
		addMouseMotionListener(listener);
	}
	
	public void fireActionEvent(MouseEvent me) {
		ActionEvent evt = new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "Button pressed");
		for(ActionListener listener : actionListeners ) {
			listener.actionPerformed(evt);
		}
	}
	
	public void addActionListener(ActionListener listener) {
		if (!actionListeners.contains(listener))
			actionListeners.add(listener);
	}
	
	public void removeActionListener(ActionListener listener) {
		actionListeners.remove(listener);
	}
	
	public void setDrawBorder(boolean drawIt) {
		if (drawIt) {
			this.setBorder(BorderFactory.createEtchedBorder());
		}
		else {
			this.setBorder(BorderFactory.createEmptyBorder(2, 2, 1, 1));
		}
		repaint();
	}
	
	class Listener extends MouseInputAdapter {
		
		public void mouseClicked(MouseEvent me) {
			fireActionEvent(me);
		}
		
		public void mouseEntered(MouseEvent me) {
			setDrawBorder(true);
		}
		
		public void mouseExited(MouseEvent me) {
			setDrawBorder(false);
		}
		
	}


	
	
}
