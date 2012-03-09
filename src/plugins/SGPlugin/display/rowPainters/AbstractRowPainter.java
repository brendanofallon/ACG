package plugins.SGPlugin.display.rowPainters;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import topLevelGUI.SunFishFrame;


import element.sequence.*;
import element.codon.GeneticCode;
import element.codon.GeneticCode.AbstractGeneticCode;
import element.codon.GeneticCode.AminoAcid;
import element.sequence.Sequence;
import element.sequence.SequenceGroupChangeListener.ChangeType;

/**
 * Abstract base class for many of the row painter classes. This class houses code for rapid drawing
 * of contiguous sequences of bases. The basic idea is that we divide sequences into blocks of
 * hashBlockSize bases, then compute a hash value unique to a particular sub-sequence. We compute and
 * store all of these for the entire group of sequences whenever we load a new sequenceGroup. 
 * Finally, when it comes time to paint, we store a bunch of images of the drawn bases in a map that 
 * is keyed by hash values. So painting is the fairly fast - load the (already computed) hash value
 * for the block of bases we want to paint, use that hash value to get the (already drawn) image, and
 * just paint that image to the desired graphics object. The downside is memory overhead - we end 
 * up storing a possibly big array of int's for all of the hash values, and a map of a bunch of images.
 * The current implementation is smart enough to only store images for patterns we've already seen,
 * and we keep the block size relatively small (at 4), so there's only 256 possible combinations
 * (not counting ambiguity codes and gap characters).
 * 
 * 
 *  
 * @author brendan
 *
 */
public abstract class AbstractRowPainter implements SGRowPainter, SequenceGroupChangeListener {

	protected char[] base = new char[1];
	protected static Color shadowColor =  new Color(200, 200, 200,  155);
	
	//Codings for different letter-drawing schemes
	public static final int ALL_LETTERS = 0;
	public static final int NO_LETTERS = 1;
	public static final int DIF_LETTERS = 2;
	
	int letterMode = 0;
	
	protected Font font = new Font("Sans", Font.PLAIN, 11);
	float translationFontSizeMod = 4f;
	
	protected boolean hasCalculatedBaseline = false;
	protected int textBaseline = 0;
	protected static Rectangle2D workingRect;
	
	private int prevCellWidth = 0;
	private int prevRowHeight = 0;
	
	//Used for the DIF_LETTERS mode, this is the sequence to which others are compared
	//to identify differences
	Sequence referenceSeq = null;
	
	SequenceGroup currentSG = null;
	
	static final int hashBlockSize = 4; //Means we hash four bases together to make a block
	//Total number of images we draw will then be hashBlockSize^4 
	
	protected static SGHashValsManager sequenceHashes = new SGHashValsManager(hashBlockSize);
	
	public static final int A = 0;
	public static final int C = 1;
	public static final int G = 2;
	public static final int T = 3;
	public static final int S = 4;
	public static final int R = 5;
	public static final int Y = 6;
	public static final int M = 7;
	public static final int W = 8;
	public static final int GAP = 9;
	
	//Stores a mapping from a base character to an integer
	protected Map<Character, Integer> baseIntMap = new HashMap<Character, Integer>();
	
	//Stores a mapping from integer to base characters
	protected Map<Integer, Character> intBaseMap = new HashMap<Integer, Character>();
	
	//Stores images indexed by hash values. These are built on an as-needed basis
	//by calls from drawBaseGroup to createBaseImage
	protected Map<Integer, BufferedImage> hashImageMap = new HashMap<Integer, BufferedImage>();
	
	protected BufferedImage dotsImage;
	
	//Whether or not we should translate the sequences. Dubious if this works right now.
	boolean translate = false;
	
	int frame = 0; //Corresponds to offset form seq.at(0), so frame will either be 0, 1, 2. Frames > 2 will be interpreted as mod%3
	boolean revComp = false; 
	AbstractGeneticCode translator = null;
	
	public AbstractRowPainter(SequenceGroup sg) {
		currentSG = sg;
		currentSG.addSGChangeListener(this);
		constructBaseIntMap();
	}
	
	
	public void setSequenceGroup(SequenceGroup sg) {
		if (currentSG != sg) {
			currentSG.removeSGChangeListener(this);
		}
		currentSG = sg;
		currentSG.addSGChangeListener(this);

	}
	
	public void sgChanged(SequenceGroup source, ChangeType type) {
		//Hmm..not entirely clear if we care about this anymore. The new hash value indexing mechanism
		//listens for individual sequence changes, and stores everything by sequence, so if a sequence
		//is lost or something I don't think we care. 
		//Frequency row painter definitely cares about this, however, and overrides this method
	}

	
	/**
	 * Draws a new image of the string of bases, and stores the image in a map indexed by the given 
	 * hashVal
	 * @param width Width of a single base, image width will be width*hashBlockSize
	 * @param height Height of row, which will be the height of the image computed
	 * @return A bufferedImage representing the sequence of bases 
	 */
	protected BufferedImage createBaseImage(char[] bases, int hashVal, int width, int height) {
		BufferedImage newImage = SunFishFrame.getSunFishFrame().getGraphicsConfiguration().createCompatibleImage(width*hashBlockSize, height, Transparency.TRANSLUCENT );
		drawImageForBases(newImage.createGraphics(), width, height, bases);
		hashImageMap.put(hashVal, newImage);
		//System.out.println("Creating base image for " + String.valueOf(bases) + " map size is now: " + hashImageMap.size());
		return newImage;
	}
	
	
	/**
	 * Paint the image used to display the given sequence of bases. This image is displayed anytime
	 * we see a sequence of bases matching those used here. This method is also easily overridden 
	 * so subclasses can draw them however they want to. 
	 * @param g2d
	 * @param colWidth
	 * @param height
	 * @param bases
	 */
	protected void drawImageForBases(Graphics2D g2d, int colWidth, int height, char[] bases) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setFont(font);
		int x = 0;
		for(int i=0; i<bases.length; i++) {
			g2d.setColor(shadowColor);
			g2d.drawChars(new char[]{bases[i]}, 0, 1, x+1, height-3);
			g2d.setColor(Color.black);
			g2d.drawChars(new char[]{bases[i]}, 0, 1, x, height-4);
			x += colWidth;
		}
	}
	
	/**
	 * Turn on/off translation. If on, use the specified genetic code. If off, code is ignored. 
	 * @param trans
	 * @param code
	 */
	public void setTranslate(boolean trans, AbstractGeneticCode code, int frame, boolean revComp) {
		translate = trans;
		translator = code;
		this.revComp = revComp;
		this.frame = frame %3;
	}
	
	
	/**
	 * Sets the font size based on the new cell size and redraws the base images array. This checks
	 * to see if anything has changed since the previous call to setCellSize and does nothing if
	 * the values have not changed, so it's safe to cell frequently
	 * @param cellWidth
	 * @param rowHeight
	 */
	protected void setCellSize(int cellWidth, int rowHeight) {
		if (prevCellWidth != cellWidth || prevRowHeight != rowHeight) {
			float fontSize = cellWidth+1;
			if (translate)
				fontSize += translationFontSizeMod;
			font = font.deriveFont( fontSize );
			prevCellWidth = cellWidth;
			prevRowHeight = rowHeight;
			
			hashImageMap.clear();
		}
	}
	
	
	/**
	 * Set the mode in which letters (bases or amino acid symbols) are drawn.
	 * @param mode
	 */
	public void setLetterMode(int mode) {
		if (mode>2) {
			throw new IllegalArgumentException("Letter mode must be in 0..2");
		}
		else
			letterMode = mode;
			
	}
	
	public void setFont(Font font) {
		this.font = font;
	} 
	
	public static String getIdentifier() {
		throw new IllegalStateException("getIdentifier was called on the AbstractRowPainter...this should be overridden in every subclass");
	}
	
	/**
	 * Paint the current used using the specified graphics object and other params, and the 
	 * sequenceGroup associated with this rowPainter. The given sequenceGroup is ignored
	 */	
	public abstract void paintRow(Graphics2D g2d, 
			int row, 
			int firstCol,
			int lastCol, 
			int x, 
			int y, 
			int cellWidth,
			int rowHeight);
	
	protected int getTextBaseline(Graphics g, int rowHeight) {
		if (hasCalculatedBaseline)
			return textBaseline;
		else {
			g.setFont( font );
			workingRect = g.getFontMetrics().getStringBounds("X", g);
			textBaseline =  rowHeight/2 + (int)Math.round(workingRect.getHeight()/2.0)-2;
			hasCalculatedBaseline = true;
			return textBaseline;
		}
	}
	
	/**
	 * Set the sequence to which others are compared in the DIF_LETTERS mode. Currently unused
	 * (we always use currentSG.get(0) as a reference, but this could be handy if we wanted to
	 * compare the sequences to the consensus, for example. 
	 * @param ref
	 */
	public void setReferenceSeq(Sequence ref) {
		referenceSeq = ref;
	}
	
	
	/**
	 * A 'hook' for various subclasses to draw the correct background for a single base. The
	 * default is to do nothing. 
	 * @param g2d
	 * @param x
	 * @param y2
	 * @param colWidth
	 * @param row
	 * @param site
	 * @param seq
	 */
	protected void drawBackground(Graphics2D g2d, int x, int y2, int colWidth, int height,
			int row, int site, Sequence seq) {
	}
	
	/**
	 * Draw the amino acid symbol associated with the codon that ENDS with the given site. This does
	 * nothing when we call it with a site for which (site-frame) % 2 != 0. This is because the background
	 * and the letters are typically drawn at the same time, and since the letters may be big they could
	 * extend into the next cell's background, leading to the letter being partially drawn over. However,
	 * if we postpone drawing the letters until the last codon position is reached this issue is ameliorated. 
	 * @param g2d
	 * @param x
	 * @param y
	 * @param colWidth
	 * @param row
	 * @param site
	 */
	protected void drawChar(Graphics2D g2d, int x, int y, int colWidth, int row, char c) {
		base[0] = c;
		g2d.setFont(font);
		FontMetrics fm = g2d.getFontMetrics();
		y -= 4;
		x -= fm.charWidth(base[0])/2+colWidth-2;
		//System.out.println("Drawing base with font size: " + font.getSize());
		g2d.setColor(shadowColor);
		g2d.drawChars(base, 0, 1, x+1, y+1);
		g2d.setColor(Color.black);
		g2d.drawChars(base, 0, 1, x, y);
	}
	
	protected void drawBaseNoShadow(Graphics2D g2d, int x, int y, int colWidth, int row, int site, Sequence seq) {
		if (site>=seq.length())
			base[0] = ' ';
		else
			base[0] = seq.at(site);
		
		if (letterMode == NO_LETTERS) {
			return;
		}
		if (letterMode == DIF_LETTERS) {
			if (referenceSeq == null)
				referenceSeq = currentSG.get(0);
			
			if (seq != referenceSeq && base[0] == referenceSeq.at(site))
				base[0] = '.';
			
		}
		
		g2d.setFont(font);
		y -= 4;
		
		if (base[0]=='G') {
			g2d.setColor(Color.black);
			g2d.drawChars(base, 0, base.length, x, y);
		}
		else {
			g2d.setColor(Color.black);
			g2d.drawChars(base, 0, 1, x, y);
		}
	}
	
	/**
	 * Draw a series of bases starting from site 'site' and extending for hashBlockSize. This 
	 * looks up a hash value for the block of bases by querying baseHashMap, and if it finds a 
	 * good hash value, we attempt to load an image from the hashImageMap to paint.  
	 * If we can't find a good hash value (probably because the block contains ambiguity codes,
	 * gaps, or other unknown characters), we just draw the bases one at a time. Also,
	 * if we're at the end of a sequence, we just draw the bases one at a time.  
	 *  
	 * @param g2d
	 * @param x
	 * @param y
	 * @param colWidth
	 * @param rowHeight
	 * @param row
	 * @param site
	 * @param seq
	 */
	protected void drawBaseGroup(Graphics2D g2d, int x, int y, int colWidth, int rowHeight, int row, int site, Sequence seq) {
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		if (letterMode == NO_LETTERS) {
			for(int i=site; i<Math.min(site+hashBlockSize, seq.length()); i++)
				drawBackground(g2d, x+i*colWidth, y, colWidth, rowHeight, row, i, seq);
			return;
		}
		
		if (letterMode == DIF_LETTERS) {
			if (referenceSeq == null)
				referenceSeq = currentSG.get(0);
		
			if (seq==referenceSeq) {
				int hashVal = sequenceHashes.getHashForSequence(seq)[site/hashBlockSize];
				drawBasesFromHashMap(g2d, x, y, hashVal, colWidth, rowHeight, site, seq);
				return;
			}
			
			int xPos = x+1;
			if (dotsImage == null) {
				char[] dotsBases = new char[hashBlockSize];
				for(int i=0; i<hashBlockSize; i++)
					dotsBases[i] = '.';
				dotsImage = createBaseImage(dotsBases, -1, colWidth, rowHeight);
			}
			
			if ( sequenceHashes.getHashForSequence(seq)==sequenceHashes.getHashForSequence(referenceSeq)) {
				g2d.drawImage(dotsImage, x, y, null);
				return;
			}
			
			for(int i=site; i<Math.min(seq.length(), site+hashBlockSize); i++) {
				int xMod = 0;
				base[0] = seq.at(i);
				if (seq != referenceSeq && base[0] == referenceSeq.at(i)) { 
					base[0] = '.';
					xMod = 2;
				}
				else {
					base[0] = seq.at(i);
					xMod = 0;
				}

				drawBackground(g2d, x, y, colWidth, rowHeight, row, i, seq);
				g2d.setFont(font);
				g2d.setColor(Color.black);
				g2d.drawChars(base, 0, 1, xPos+xMod, y+rowHeight-4);
				xPos += colWidth;
			}
				
			return;
		}
	
		
		//If we're at the end of the sequence then just draw the last few bases individually
		if (site >= (seq.length() - hashBlockSize)) {
			int xPos = x+1;
			for(int i=site; i<seq.length(); i++) { 
				drawBase(g2d, xPos, y+rowHeight, colWidth, rowHeight, row, i, seq);
				xPos += colWidth;
			}
			return;
		}
		
		int[] hashes = sequenceHashes.getHashForSequence(seq);
		int hashVal = -1;
		if (hashes == null)
			sequenceHashes.addSequence(seq);
		
		hashes = sequenceHashes.getHashForSequence(seq);
		
		hashVal = hashes[site/hashBlockSize];
		
		//Bad hash value, resort to drawing each base individually. 
		if (hashVal == -1) {
			drawBasesIndividually(g2d, x, y, colWidth, rowHeight, row, site, site+hashBlockSize, seq);
			return;
		}
		drawBasesFromHashMap(g2d, x, y, hashVal, colWidth, rowHeight, site, seq);
	}
	
	private void drawBasesFromHashMap(Graphics2D g2d, int x, int y, int hashVal, int colWidth, int rowHeight, int site, Sequence seq) {
		BufferedImage image = hashImageMap.get(hashVal);
		if (image == null) {
			image = createBaseImage(seq.toCharArray(site, site+hashBlockSize), hashVal, colWidth, rowHeight );			
		}
		g2d.drawImage(image, x, y, null);
	}
	
	/**
	 * Draw a series of bases from the given sequence starting at startSite and proceeding until end
	 * This checks to make sure we don't draw off the end of the sequence, but doesn't do any hash acceleration
	 * @param g2d
	 * @param x
	 * @param y
	 * @param colWidth
	 * @param rowHeight
	 * @param row
	 * @param startSite
	 * @param end
	 * @param seq
	 */
	private void drawBasesIndividually(Graphics2D g2d, int x, int y, int colWidth, int rowHeight, int row, int startSite, int end, Sequence seq) {
		int xPos = x+1;
		for(int i=startSite; i<Math.min(end, seq.length()); i++) { 
			drawBase(g2d, xPos, y+rowHeight, colWidth, rowHeight, row, i, seq);
			xPos += colWidth;
		}
	}
	
	/**
	 * Draw a given base using the given graphics object
	 * @param g2d
	 * @param x
	 * @param y
	 * @param colWidth
	 * @param row
	 * @param site
	 * @param seq
	 */
	protected void drawBase(Graphics2D g2d, int x, int y, int colWidth, int rowHeight, int row, int site, Sequence seq) {
		if (translate) {
			throw new IllegalStateException("Not sure drawBase is supposed to get called for a translated sequence..?");
		}
		
		if (site>=seq.length())
			base[0] = ' ';
		else
			base[0] = seq.at(site);
		
		if (letterMode == NO_LETTERS) {
			return;
		}
		if (letterMode == DIF_LETTERS) {
			if (referenceSeq == null)
				referenceSeq = currentSG.get(0);
			
			if (seq != referenceSeq && base[0] == referenceSeq.at(site))
				base[0] = '.';
			
		}
		
		drawBackground(g2d, x, y, colWidth, rowHeight, row, site, seq);
		
		g2d.setFont(font);
		y -= 4;
		
		if (base[0]=='G') {
			g2d.setColor(shadowColor);
			g2d.drawChars(base, 0, 1, x+1, y+1);
			g2d.setColor(Color.black);
			g2d.drawChars(base, 0, base.length, x, y);
		}
		else {
			g2d.setColor(shadowColor);
			g2d.drawChars(base, 0, 1, x+1, y+1);
			g2d.setColor(Color.black);
			g2d.drawChars(base, 0, 1, x, y);
		}
		
	}
	

	/**
	 * Construct a couple of useful maps to handle going back and forth between base characters
	 * and integers
	 */
	private void constructBaseIntMap() {
		baseIntMap.clear();
		baseIntMap.put('A', A);
		baseIntMap.put('C', C);
		baseIntMap.put('G', G);
		baseIntMap.put('T', T);
		baseIntMap.put('S', S);
		baseIntMap.put('R', R);
		baseIntMap.put('Y', Y);
		baseIntMap.put('M', M);
		baseIntMap.put('W', W);
		baseIntMap.put('-', GAP);
		baseIntMap.put(' ', GAP);
		baseIntMap.put('?', GAP);
		baseIntMap.put('N', GAP);
		
		intBaseMap.put(A, 'A');
		intBaseMap.put(C, 'C');
		intBaseMap.put(G, 'G');
		intBaseMap.put(T, 'T');
		intBaseMap.put(S, 'S');
		intBaseMap.put(R, 'R');
		intBaseMap.put(Y, 'Y');
		intBaseMap.put(M, 'M');
		intBaseMap.put(W, 'W');
		intBaseMap.put(GAP, '-');
		
	}
	
	
	
	public abstract static class Instantiator {
		
		public abstract AbstractRowPainter getNewRowPainter(SequenceGroup sg);
		
	}

	
}
