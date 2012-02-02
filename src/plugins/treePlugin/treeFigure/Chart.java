package plugins.treePlugin.treeFigure;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

import javax.swing.JPanel;

import figure.FigureElement;

/**
 * A Chart is a form of JPanel that has a padded boundary and whose image
 * can be easily saved. It's been largely superseded by Figure, which
 * is much more functional. This is still around since MultiTreeChart uses it. One day
 * I'll get around to redoing MultiTreeChart as a Figure. 
 * @author brendan
 *
 */
public abstract class Chart extends JPanel {
	
	int leftPadding = 50;
	int bottomPadding = 20;
	int topPadding = 10;
	int rightPadding = 15;
	int tickSize = 3;
	int minWidth = 100;
	int minHeight = 100;
	ChartMouseListener mouseListener;

	
	public Chart() {
		setMinimumSize(new Dimension(minWidth, minHeight));
		mouseListener = new ChartMouseListener();
		addMouseListener(mouseListener);
	}
	
	public BufferedImage getImage() {
		Image img = this.createImage(getWidth(), getHeight());
		BufferedImage bi;
		if (img instanceof BufferedImage) {
			bi = (BufferedImage)img;
			Graphics graphics = bi.getGraphics();
			paintComponent(graphics);
			return bi;
		}
		else {
			System.out.println("Uh-oh, createImage didn't return a buffered image... dang \n");
			return null;
		}

	}
	
	
	class ChartMouseListener implements MouseListener {

		public void mouseClicked(MouseEvent evt) {
			
		}

		public void mouseEntered(MouseEvent arg0) {
			
		}

		public void mouseExited(MouseEvent arg0) {
			
		}

		public void mousePressed(MouseEvent arg0) {
			
		}

		public void mouseReleased(MouseEvent arg0) {
		
		}

	}
	
}
