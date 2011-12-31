package newgui.trackView;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.text.DecimalFormat;

public class AxisTrackView implements TrackView {

	private Color trackColor = Color.DARK_GRAY;
	private int numTicks = 5;
	private Font normalFont = new Font("Sans", Font.PLAIN, 11);

	
	@Override
	public void paintTrack(Graphics2D g, int trackBegin, int trackEnd, int x,
			int y) {
		
		g.setColor(trackColor);
		g.setFont(normalFont);
		//Horizontal line across top
		g.drawLine(x, y+2, (int)Math.round(trackEnd*pxPerSite), y+2);
		
		//Ticks and tick labels
		int xOffset=2;
		double tickStep = (trackEnd - trackBegin)*pxPerSite/(double)(numTicks+1);
		int tickX = x-(int)Math.round( (trackBegin % 10)*pxPerSite)+xOffset;
		for(int i=0; i<=numTicks; i++) {
			g.drawLine(tickX, y+2, tickX, y+8);
			int site = trackBegin + (int)Math.round( (tickX-xOffset)/pxPerSite)+1; //Converts from 0 to 1-based coords
			String siteStr = "" + site;
			int strWidth = g.getFontMetrics().stringWidth( siteStr );
			g.drawString(siteStr, tickX-strWidth/2, y+18 );
			tickX += tickStep;
		}
	}

	@Override
	public void setPixelsPerSite(double pixPerSite) {
		this.pxPerSite = pixPerSite;
	}

	@Override
	public int getTrackHeight() {
		return trackHeight;
	}

	@Override
	public void setTrackHeight(int h) {
		this.trackHeight = h;
	}

	private int trackHeight = 22;
	private double pxPerSite = -1;
}
