package newgui.trackView;

import java.awt.Graphics2D;

/**
 * A trackView is an object that can paint some representation of a Track
 * onto a graphics object. 
 * @author brendan
 *
 */
public interface TrackView {

	/**
	 * Paint some graphics representing given interval of the track onto the graphics object
	 * @param g Graphics object to paint on
	 * @param trackBegin First site of track to paint
	 * @param trackEnd Last site of track to paint
	 * @param x x offset at which to begin painting
	 * @param y y offset at which to begin painting
	 */
	public void paintTrack(Graphics2D g, int trackBegin, int trackEnd, int x, int y);
	
	/**
	 * Set the number of pixels occupied by each site. In general this will be much less than one
	 * @param pixPerSite
	 */
	public void setPixelsPerSite(double pixPerSite);
	
	/**
	 * Height of this view in pixels
	 * @return
	 */
	public int getTrackHeight();
	
	/**
	 * Set height of this view, in pixels
	 */
	public void setTrackHeight(int h);
	
	
}
