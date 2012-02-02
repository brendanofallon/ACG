package newgui.gui.widgets;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;
import javax.swing.Timer;

import newgui.UIConstants;


/**
 * Class to handle fading in + out of regions of a jframe
 * @author brendan
 *
 */
public class RegionFader implements ActionListener {

	JFrame parent = null;
	final long msToFade = 1000; //Time taken to complete fade 
	Timer timer;
	private long startTime = -1;
	private Rectangle regionToFade = null;
	private BufferedImage backgroundImage = null; //Image of full component for rapid drawing. Frequently updated

	
	public RegionFader(JFrame parent) {
		this.parent = parent;
		timer = new Timer(10, this);
	}
	
	/**
	 * Experimental fading-in of region
	 * @param region
	 */
	public void fadeInRegion(Rectangle region) {
		System.out.println("Fading region " + region.x + ", " + region.y + " - " + region.width + ", " + region.height);
		regionToFade = region;
		GraphicsConfiguration gc = parent.getGraphicsConfiguration();
		if (gc == null || parent.getWidth()==0 || parent.getHeight()==0) {
			//This happens a few times when this component is being initialized, no big deal
			return;
		}
		backgroundImage = gc.createCompatibleImage((int)region.getWidth(), (int)region.getHeight());
		Graphics g = backgroundImage.createGraphics();
		parent.paint(g);
		
		startTime = System.currentTimeMillis();
		parent.getGlassPane().setVisible(true);
		timer.start();
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		long timeNow = System.currentTimeMillis();
		long elapsed = timeNow - startTime;
		float fraction = (float)elapsed / (float)msToFade;
		System.out.println("Fraction : " + fraction);
		
		if (fraction > 0.999) {
			finishFade();
			return;
		}
		
		Graphics g = parent.getRootPane().getGlassPane().getGraphics();
		g.drawImage(backgroundImage, regionToFade.x, regionToFade.y, null);
		
		Color backgroundColor = UIConstants.componentBackground;
		
		System.out.println("Fraction : " + fraction + " alpha: " + (int)(255*(1.0f-fraction)));
		Color c = new Color(backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), (int)(255*(1.0f-fraction)));
		g.setColor(c);
		
		g.fillRect(regionToFade.x, regionToFade.y, regionToFade.width, regionToFade.height);

		System.out.println("Fading to fraction " + fraction);
	}

	private void finishFade() {
		timer.stop();
		parent.getRootPane().getGlassPane().setVisible(false);
	}
}
