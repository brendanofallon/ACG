package newgui.alignment;


import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics2D;

import sequence.BaseMap;

import newgui.gui.ViewerWindow;


/**
 * A simple track view for a sequence. This is likely to be slow, especially if the extent of the
 * range is large
 * @author brendan
 *
 */
public class SequenceTrackView implements TrackView {

	private BaseMap baseMap = new BaseMap(); //Translate nucs from int to char
	private Font font = ViewerWindow.sansFont.deriveFont(10.0f);
	private final Sequence seq;
	
	//Mapping from base-integer to colors so we can colorize things
	private static final Color[] baseColors = new Color[]{ new Color(0.1f, 0.4f, 0.9f, 0.6f), 
														   Color.yellow,
														   Color.cyan,
														   Color.orange,
														   Color.gray,
														   Color.magenta,
														   Color.pink,
														   Color.white };
														   
	
	
	public SequenceTrackView(Sequence seq) {
		this.seq = seq;
	}
	
	@Override
	public void paintTrack(Graphics2D g, int trackBegin, int trackEnd, int x,
			int y) {
		updateBases = updateBases || trackBegin != prevBegin || trackEnd != prevEnd;
		
		
		boolean showLetters = pixPerSite > 2;
			
		if (updateBases) {
			updateVisibleBases(trackBegin, trackEnd);
			updateBases = false;
		}
		
		//drawBackground(g, trackBegin, trackEnd, x,  y);
		
		g.setColor(Color.darkGray);
		g.setFont(font);
		float newFontSize = Math.min(12f,(float)Math.round(pixPerSite+2));
		font = font.deriveFont(newFontSize);
		g.setFont(font);
		double dubX = x+0.5;
		int yDif = g.getFontMetrics().getHeight()/2+1;
		
		
		for(int i=0; i<visibleBases.length; i++) {
			//g.setColor(baseColors[seq.baseAt(i+trackBegin)]);
			//g.fillRect((int)Math.round(dubX), y+trackHeight-12, (int)Math.round(pixPerSite), 12);

			g.setColor(Color.gray);
			if (colorBases[i])
				g.fillRect((int)Math.round(dubX), y, (int)Math.round(pixPerSite), trackHeight);

			if (showLetters) {
				if (newFontSize > 6) {
					g.setColor(Color.DARK_GRAY);
					g.drawChars(visibleBases, i, 1, (int)Math.round(dubX), y+trackHeight/2+yDif);
				}
				else {
					g.setColor(Color.black);
					g.drawChars(visibleBases, i, 1, (int)Math.round(dubX), y+trackHeight/2+yDif);
				}
			}
			else {
				g.drawLine((int)Math.round(dubX), y+trackHeight/2+yDif-2, (int)Math.round(dubX+1), y+trackHeight/2+yDif-2);
			}
			dubX += pixPerSite;
		}
		
		prevBegin = trackBegin;
		prevEnd = trackEnd;
	}

	private void drawBackground(Graphics2D g, int trackBegin, int trackEnd,
			int x, int y) {	
		GradientPaint gp = new GradientPaint(0, y+trackHeight, Color.gray, 0, y, Color.white);
		g.setPaint(gp);
		g.fillRoundRect(-4, y+1, (int)((seq.getLength()-trackBegin)*pixPerSite), trackHeight-2, 10, 10);
		
		g.setColor(Color.gray);
		g.drawRoundRect(-4, y+1, (int)((seq.getLength()-trackBegin)*pixPerSite), trackHeight-2, 10, 10);
	}

	/**
	 * Fill the visibleBases array with those bases we're going to paint
	 * @param first
	 * @param last
	 */
	private void updateVisibleBases(int first, int last) {
		final int lastPos = Math.min(seq.getLength(), last)-1;
		final int length = lastPos-first+1;
		
		if ( (visibleBases == null) || length != visibleBases.length) {
			visibleBases = new char[length];
			colorBases = new boolean[length];
		}
		
		
		int index = 0;
		for(int i=first; i<lastPos; i++) {
			visibleBases[index] = baseMap.baseForVal(seq.baseAt(i)); 
			index++;
		}
		
		index = 0;
		if (seq.getReference() != null) {
			for(int i=first; i<lastPos; i++) {
				colorBases[index] = seq.differsFromReference(i);  
				index++;
			}
		}
		else {
			System.out.println("No reference for sequence");
		}
		prevLength = length;
	}

	@Override
	public void setPixelsPerSite(double pxPerSite) {
		this.pixPerSite = pxPerSite;
	}

	@Override
	public int getTrackHeight() {
		return trackHeight;
	}

	@Override
	public void setTrackHeight(int h) {
		this.trackHeight = h;
	}

	public Sequence getSequence() {
		return seq;
	}
	
	private boolean updateBases = true; //True if we need to rebuild visible bases list
	private int prevBegin = -1;
	private int prevEnd = -1;
	private int prevLength = -1;
	private double pixPerSite = 1.0;
	private int trackHeight = 20;
	private char[] visibleBases = null;
	private boolean[] colorBases = null;

	
}
