package newgui.trackView;

import java.awt.Color;
import java.awt.Graphics2D;
import java.util.HashSet;
import java.util.Set;


/**
 * A class to draw a series of ranges in fairly generic fashion, mostly for testing 
 * purposes 
 * @author brendan
 *
 */
public class GenericRangeView implements TrackView {

	private int height = 40; 
	private RangeBlock ranges;
	private Set<Range> visibleRanges = new HashSet<Range>();
	private double pixPerSite;
	
	public GenericRangeView(RangeBlock ranges) {
		this.ranges = ranges;
	}
	
	
	private void updateRanges(int begin, int end) {
		if (begin != prevBegin || end != prevEnd) {
			visibleRanges.clear();
			ranges.getRanges(begin, end, visibleRanges);
			prevBegin = begin;
			prevEnd = end;
		}
	}
	
	
	
	@Override
	public void paintTrack(Graphics2D g, int trackBegin, int trackEnd, int x,
			int y) {

		updateRanges(trackBegin, trackEnd); //Update info about which ranges are visible
		g.setColor(Color.blue);
		int xSlide = (int)(trackBegin*pixPerSite);
		for(Range r : visibleRanges) {
			drawRange(g, r, x - xSlide, y);
			
		}
		
	}

	/**
	 * Draw a single range using the given graphics obj and x and y offset
	 * @param g
	 * @param r
	 * @param x
	 * @param y
	 */
	private void drawRange(Graphics2D g, Range r, int x, int y) {
		g.fillRect( (int)Math.round(x+r.getBegin()*pixPerSite), y, (int)Math.round(pixPerSite*(r.getEnd()-r.getBegin())), 25);
	}


	@Override
	public int getTrackHeight() {
		return height;
	}

	@Override
	public void setTrackHeight(int h) {
		this.height = h;
	}

	@Override
	public void setPixelsPerSite(double pixPerSite) {
		this.pixPerSite = pixPerSite;
	}
	
	private int prevBegin = 0; //Track previous values of trackBegin and end so we know if they have changed
	private int prevEnd = 0;

	
}
