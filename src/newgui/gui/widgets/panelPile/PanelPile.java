package newgui.gui.widgets.panelPile;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.Timer;

/**
 * A 'pile' of overlapping panels which can be expanded to show one sub-panel at a time.
 * @author brendan
 *
 */
public class PanelPile extends JPanel {

	private List<PPanel> allPanels = new ArrayList<PPanel>(); //List of all panels
	private List<BufferedImage> images = null; //Images of all headers for rapid drawing
	private BufferedImage backgroundImage = null; //Image of full component for rapid drawing. Frequently updated
	private int openIndex = -1; //Index of the currently 'open' or showing panel
	
	public PanelPile() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));		
	}
	
	/**
	 * Add a new panel to the pile of panels
	 * @param panel
	 */
	public void addPanel(PPanel panel) {
		allPanels.add(panel);
	}

	/**
	 * Create images for all panel headers for faster drawing
	 */
	private void makeImages() {
		images = new ArrayList<BufferedImage>();
		for(PPanel panel : allPanels) {
			PPanelHeader header = panel.getHeader();
			GraphicsConfiguration gc = this.getGraphicsConfiguration();
			if (gc == null) {
				System.out.println("Graphics config is null");
			}
			BufferedImage image = gc.createCompatibleImage(header.getWidth(), header.getHeight());
			Graphics g = image.createGraphics();
			g.setColor(Color.white);
			g.fillRect(0, 0, image.getWidth(), image.getHeight());
			panel.getHeader().paint(g);
			images.add(image);
		}
	}
	
	private synchronized void makeBackgroundImage() {
		GraphicsConfiguration gc = this.getGraphicsConfiguration();
		if (gc == null || getWidth()==0 || getHeight()==0) {
			//This happens a few times when this component is being initialized, no big deal
			return;
		}
		backgroundImage = gc.createCompatibleImage(this.getWidth(), this.getHeight());
		Graphics g = backgroundImage.createGraphics();
		this.paint(g);
	}
	
	/**
	 * Return the currently open panel
	 * @return
	 */
	public PPanel getCurrentPanel() {
		return allPanels.get(openIndex);
	}
	
	public void showPanel(int which, boolean animate) {
		showPanel(allPanels.get(which), animate);
	}
	
	/**
	 * Make the given panel visible, animating the transition
	 * @param which
	 */
	public void showPanel(int which) {
		showPanel(allPanels.get(which), true);
		makeBackgroundImage();
	}
	
	/**
	 * Make the given panel visible 
	 * @param panel
	 * @param animate
	 */
	public void showPanel(PPanel panel, boolean animate) {
		PPanelHeader header;
		int newIndex = allPanels.indexOf(panel);
		if (newIndex == openIndex) {
			return;
		}
		
		if (animate) {
			if (images == null)
				makeImages();
			int newY;
			int movingIndex = newIndex;
			if (newIndex < openIndex) { //New panel is above old panel, panels above will move down
				movingIndex++;
				header = allPanels.get(movingIndex).getHeader();
				newY = (int) (panel.getHeight() + (newIndex+2.5) * header.getHeight());
			}
			else {
				header = allPanels.get(movingIndex).getHeader();
				newY = (int) ((newIndex+2.5) * header.getHeight());
			}
			
			this.getRootPane().getGlassPane().setVisible(true);
			Point startPoint = header.getLocation();
			Point endPoint = new Point((int)startPoint.getX(), newY);

			ImageMover mover = new ImageMover(this, newIndex, panel, images.get(movingIndex), startPoint, endPoint);
			mover.move();
		}
		
		this.removeAll();
		for(int i=0; i<newIndex; i++) {
			this.add(allPanels.get(i).getHeader());
			if (panel == allPanels.get(i))
				this.add(panel);
		}

		for(int i=newIndex; i<allPanels.size(); i++) {
			this.add(allPanels.get(i).getHeader());
			if (panel == allPanels.get(i))
				this.add(panel);
		}

		revalidate();
		if (animate)
			this.getRootPane().getGlassPane().setVisible(false);
		repaint();
		
		openIndex = newIndex;
		//makeBackgroundImage();
	}
	
	public int getPanelX() {
		return (int) this.getLocation().getX();
	}
	
	public int getPanelY() {
		return (int) this.getLocation().getY();
	}
	
	class ImageMover implements ActionListener {
		
		final BufferedImage image;
		final Point start;
		final Point end;
		final long msToMove = 250; //Time taken to move object, 
		Timer timer;
		long startTime;
		long endTime;
		final PanelPile parent;
		private boolean done = false;
		private int count = 0;
		private double fracElapsed = 0;
		private PPanel panel;
		final Rectangle paintRect;
		private int prevY = 0;
		private final double dx;
		private final double dy;
		
		public ImageMover(PanelPile parent, int index, PPanel panel, BufferedImage image, Point start, Point end) {
			this.image = image;
			this.start = start;
			this.end = end;
			this.parent = parent;
			this.panel = panel;
			this.dx = end.getX() - start.getX();
			this.dy = end.getY() - start.getY();
			int h = (int)Math.abs(start.getY() - end.getY() )+2*image.getHeight();
			this.paintRect = new Rectangle(0, 0, image.getWidth()+1, image.getHeight()+10);
			makeBackgroundImage();
		}
		
		protected Object move() {
			startTime = System.currentTimeMillis();
			endTime = startTime + msToMove;
			timer = new Timer(20, this);
			timer.start();
			
			return null;
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			count++;
			
			long now = System.currentTimeMillis();
			long elapsedMs = now - startTime;
			
			fracElapsed =  (double)(elapsedMs) / (double)msToMove;
			
			int x = (int)(start.getX() +  fracElapsed*dx);
			int y = (int)(start.getY() +  fracElapsed*dy);
			Graphics g = parent.getRootPane().getGlassPane().getGraphics();
			prevY = y;
			
			if (count%2 == 0)
				g.drawImage(backgroundImage, getPanelX() ,  getPanelY(), null);
			g.drawImage(image, x, y, null);
			
			if (fracElapsed >= 0.99) {
				timer.stop();
				done = true;
				parent.getRootPane().getGlassPane().setVisible(false);
				System.out.println("Total calls : " + count + " elapsed ms: " + (now-startTime) + " time per call:" + (now-startTime)/(double)count);
				repaint();
			}
		}
		
		public boolean isFinished() {
			return done;
		}
	}

	
}
