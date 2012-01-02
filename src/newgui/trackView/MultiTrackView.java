package newgui.trackView;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.event.ChangeListener;


/**
 * A component for displaying multiple 'tracks', arranged as rows. Each track 
 * is a list of shapes that all implement 'range', 
 * @author brendan
 *
 */
public class MultiTrackView extends JPanel {

	//List of trackviews that we will paint
	protected List<TrackView> views = new ArrayList<TrackView>();
	
	//Description of sub-interval to paint 
	BoundedRangeModel hInterval = new DefaultBoundedRangeModel(0, 1, 0, 1);
	BoundedRangeModel vInterval = new DefaultBoundedRangeModel(0, 1, 0, 1);
	
	//private int trackHeightSum = 0; //Sum of height of all tracks
	
	public MultiTrackView() {
		this.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		this.addComponentListener(new ComponentListener() {

			public void componentResized(ComponentEvent e) {
				vInterval.setExtent( getHeight() );
			}

			@Override
			public void componentMoved(ComponentEvent e) {	}

			@Override
			public void componentShown(ComponentEvent e) {	}

			public void componentHidden(ComponentEvent e) {	}
		});
	}
	
	/**
	 * Set initial values for visible interval, all units are in sites (not pixels, of course)
	 * @param min
	 * @param max
	 * @param value
	 * @param extent
	 */
	public void initializeHInterval(int min, int max, int value, int extent) {
		hInterval.setRangeProperties(value, extent, min, max, false); 
	}
	/**
	 * Add given view to list of views to paint, it will be at bottom of views
	 * @param view
	 */
	public void addTrackView(TrackView view) {
		views.add(view);
		fitHeightToTracks();
		repaint();
	}
	
	/**
	 * Remove given view from list of views to paint
	 * @param view
	 */
	public void removeTrackView(TrackView view) {
		views.remove(view);
		fitHeightToTracks();
		repaint();
	}
	
	public int fitHeightToTracks() {
		int newHeight = 0;
		for(TrackView view : views) {
			newHeight += view.getTrackHeight();
		}
		vInterval.setMaximum(newHeight);
		return newHeight;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		//System.out.println("Painting track view with offset : " + vInterval.getValue());
		g2d.translate(0, -vInterval.getValue());
		
		if (hInterval == null) {
			g2d.setColor(Color.DARK_GRAY);
			g2d.drawString("Interval not initialized", 5, 20);
			return;
		}
		
		int y = 1;
		if (pixelsPerSite < 0 || pixelsPerSite*(double)hInterval.getExtent() < (getWidth()))
			pixelsPerSite = (double)getWidth() / (double)hInterval.getExtent();
		
		for(TrackView view : views) {
			view.setPixelsPerSite(pixelsPerSite);
			if (y> vInterval.getValue())
				view.paintTrack(g2d, hInterval.getValue(), hInterval.getValue()+hInterval.getExtent(), 0, y);
			y += view.getTrackHeight();
			if (y >= getHeight()+vInterval.getValue() )
				break;
		}
	}
	
	
	/**
	 * Set the 'zoom' level of this view in pixels per site
	 * @param pxPerSite
	 */
	public void setPixelsPerSite(double pxPerSite) {
		this.pixelsPerSite = pxPerSite;
		int newExtent = (int)Math.round(getWidth() / pixelsPerSite);
		int newValue = getIntervalModel().getValue();
		if (newValue + newExtent > hInterval.getMaximum()) {
			newValue = hInterval.getMaximum() - newExtent;
		}
		if (newValue < 0) {
			newValue = 0;
			newExtent = hInterval.getMaximum();
		}
			
		getIntervalModel().setValue(newValue);
		getIntervalModel().setExtent(newExtent);
		repaint();
	}
	
	public double getPixelsPerSite() {
		return pixelsPerSite;
	}

	public void clearViews() {
		views.clear();
		hInterval.setExtent(1);
		hInterval.setMaximum(1);
		vInterval.setExtent(1);
		vInterval.setMaximum(1);
		repaint();
	}
	
	/**
	 * Get the model describing the location and size of the visible interval
	 * @return
	 */
	public BoundedRangeModel getIntervalModel() {
		return hInterval;
	}
	
	public BoundedRangeModel getVIntervalModel() {
		return vInterval;
	}
	
	private double pixelsPerSite = -1.0; //Will be overwritten on first call to paint


	
}
