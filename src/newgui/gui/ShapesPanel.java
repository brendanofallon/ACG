package newgui.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.BoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A panel that 
 * @author brendan
 *
 */
public class ShapesPanel extends JPanel implements BoundedRangeModel {

	private AffineTransform trans;
	private RangeBlock rangeBlock;
	private Set<Range> currentRanges;
	private Set<RangeView> currentViews;
	private double pixelsPerSite = 1.0;
	private int prevxTranslate = 0;
	
	private double prevZoom = 1.0;
	
	//private int xTranslate = 0; //x-displacement of view window, in pixels
	private int yTranslate = 100;
	private int maxXTranslate; //Maximum amount that we can possibly translate in x direction
	private boolean rangesSet = false;
	
	public ShapesPanel(RangeBlock ranges) {
		this.rangeBlock = ranges;
		trans = new AffineTransform();
		trans.translate(0, 40);
		currentViews = new HashSet<RangeView>();
		currentRanges = new HashSet<Range>();

		
		resetRanges();
	}
	
	private void resetRanges() {
		currentRanges.clear();
		currentViews.clear();
		if (getWidth() == 0) {
			rangesSet = false;
			return;
		}
		int minSite = (int)translateAbsPixelToSite(-getXTranslation());
		int maxSite = (int)translateAbsPixelToSite(-getXTranslation() + getWidth());
		//System.out.println("Getting ranges from " + minSite + " to " + maxSite);
		rangeBlock.getRanges(minSite, maxSite, currentRanges);
		for(Range r : currentRanges) {
			currentViews.add(new RangeView(r));
		}
		rangesSet = true;
	}
	
	/**
	 * Returns the pixel closest to the given site, relative to the position of the window
	 * @param pos
	 * @return
	 */
	private double translateSiteToRelPixel(int pos) {
		return pos*pixelsPerSite - getXTranslation();
	}
	
	private double translateSiteToAbsPixel(int pos) {
		return pos*pixelsPerSite;
	}
	
	public double translateAbsPixelToSite(double pix) {
		return pix/(pixelsPerSite);
	}
	
	public double translateRelPixelToSite(double pix) {
		return (pix - getXTranslation())/(pixelsPerSite);
	}
	
	public double getXTranslation() {
		return trans.getTranslateX();
	}
	
	
	private void resetTransform() {
		maxXTranslate = (int)translateSiteToAbsPixel( rangeBlock.blockEnd );
		double prevTranslate = getXTranslation();
		trans.setToScale(pixelsPerSite, 2.0);
		trans.translate(prevTranslate, yTranslate/2.0);
		rangesSet = false;
	}
	
	public void setZoomLevel(double value) {
		double xDif = value - prevZoom;
		pixelsPerSite += xDif;
		prevZoom = value;
		rangesSet = false;
		resetTransform();
		repaint();
	}
	
	
	public int getFirstVisibleSite() {
		return (int)Math.round( translateRelPixelToSite(0) );
	}
	
	public int getLastVisibleSite() {
		return (int)Math.round( translateRelPixelToSite( getWidth()) );
	}
	
	/**
	 * Returns the length of the visible interval in sites
	 * @return
	 */
	public int getExtentSites() {
		return getLastVisibleSite() - getFirstVisibleSite();
	}
	
	/**
	 * Return the maximum site tracked by the underlying rangeblock model
	 * @return
	 */
	public int getSiteMax() {
		return rangeBlock.blockEnd;
	}
	
	/**
	 * Set number of *pixels* to shift shape in x direction.  
	 * @param x
	 */
	public void setXTranslatePixels(int dx) {
		trans.translate(-1*dx/pixelsPerSite, 0);
		rangesSet = false;
		repaint();
	}
	
	/**
	 * Set amount to shift this image in the y direction, in pixels
	 * @param y
	 */
	public void setYTranslate(int y) {
		//this.yTranslate = y;
		//Right now we ignore y-translation
		//repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		if (trans.isIdentity()) 
			resetTransform();
	
		if (!rangesSet)
			resetRanges();
		
		g2d.setTransform(trans);
		g2d.setColor(Color.blue);
		for(RangeView view : currentViews) {
			g2d.fill(view);
			g2d.drawString(view.getBegin() + "", view.getBegin(), 25);
			//System.out.println("Drawing rect at site: " + view.getBegin() + " px: " + translateSiteToRelPixel(view.getBegin()));
		}
		System.out.println("Drawing " + currentViews.size() + " views in range");
		System.out.println("First visible: " + getFirstVisibleSite() + " last visible:" + getLastVisibleSite());
		paintScaleBar(g2d);
	}

	private void paintScaleBar(Graphics2D g) {
		int barTop = getHeight()-20;
		int numTicks = 5;
		int barWidth = getWidth();
		g.setColor(Color.black);
		
		g.setTransform(identity);
		
		g.drawLine(0, barTop, getWidth(), barTop);
		double xStep = (double)barWidth / (double)numTicks;
		for(int i=0; i<=numTicks; i++) {
			int x = (int)Math.round(i*xStep);
			g.drawLine(x, barTop, x, barTop+10);
		}
		
	}

	/*************************** BoundedRangeModel implementation *********************************/
	/** This is pretty much so we can be the model underlying a a horizontal scroll bar. All 
	 * units used are in terms of *sites* not pixels, unlike some other functions  
	 * 
	 */
	
	@Override
	public int getMinimum() {
		return 0;
	}

	@Override
	public void setMinimum(int newMinimum) {
		throw new IllegalArgumentException("Cannot set the minimum of this model");
	}

	@Override
	public int getMaximum() {
		return rangeBlock.blockEnd;
	}

	@Override
	public void setMaximum(int newMaximum) {
		throw new IllegalArgumentException("Cannot set the maximum of this model");
	}

	@Override
	public int getValue() {
		return getFirstVisibleSite();
	}

	
	/**
	 * Fire an event to all listeners notifying them that this model has changed somehow. 
	 * Most often, this means telling the bottom scroll bar to update its state because
	 * we've changes either the value (through a mouse drag) or the extent (by changing
	 * the zoom level)
	 */
	public void fireChangeEvent() {
		ChangeEvent evt = new ChangeEvent(this);
		for(ChangeListener l : changeListeners) {
			l.stateChanged(evt);
		}
	}
	
	@Override
	public void setValue(int newValue) {
		//System.out.println("Setting value to: " + newValue + " = " + newValue*pixelsPerSite + " pixels, prev value is: " + getXTranslation());
		this.setXTranslatePixels((int)Math.round( (newValue*pixelsPerSite - (-1.0)*getXTranslation())));
		repaint();
	}

	@Override
	public void setValueIsAdjusting(boolean b) {
		this.valueIsAdjusting = b;
	}

	@Override
	public boolean getValueIsAdjusting() {
		return valueIsAdjusting;
	}

	@Override
	public int getExtent() {
		return getExtentSites();
	}

	@Override
	public void setExtent(int newExtent) {
		System.out.println("Setting extent? " + newExtent);
	}

	@Override
	public void setRangeProperties(int value, int extent, int min, int max,
			boolean adjusting) {
		this.setValue(value);
		this.setExtent(extent);
		this.setMinimum(min);
		this.setMaximum(max);
		this.setValueIsAdjusting(adjusting);
	}

	@Override
	public void addChangeListener(ChangeListener x) {
		changeListeners.add(x);
	}

	@Override
	public void removeChangeListener(ChangeListener x) {
		changeListeners.remove(x);
	}

	private List<ChangeListener> changeListeners = new ArrayList<ChangeListener>();
	private AffineTransform identity = new AffineTransform();
	private boolean valueIsAdjusting = false;
}
