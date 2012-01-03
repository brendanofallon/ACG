package newgui.trackView;

import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


public class ShapeDragListener extends MouseAdapter {
	
	private Point dragLast = null;
	private Point dragNow = null;
	private ShapesPanel panel;
	
	public ShapeDragListener(ShapesPanel panel) {
		this.panel = panel;
		panel.addMouseListener(this);
		panel.addMouseMotionListener(this);
		
	}
	
	@Override
	public void mouseClicked(MouseEvent e) {
		dragLast = null;
		dragNow = null;
	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragLast = e.getPoint();
		dragNow = e.getPoint();
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		dragNow = e.getPoint();
	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void mouseDragged(MouseEvent e) {
		dragNow = e.getPoint();
		if (dragLast != null) {
			int dx = (int)Math.round(dragNow.getX() - dragLast.getX());
			int dy = (int)Math.round(dragNow.getY() - dragLast.getY());
			panel.setValue(panel.getValue()-dx);
			panel.fireChangeEvent();
		}
		
		dragLast = dragNow;
	}

	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

}
