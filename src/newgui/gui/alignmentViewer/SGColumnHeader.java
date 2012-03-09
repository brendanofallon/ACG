package newgui.gui.alignmentViewer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;

import plugins.SGPlugin.analyzer.SequenceGroupCalculator;
import plugins.SGPlugin.display.SGContentPanel.Selection;
import element.sequence.PartitionChangeListener;
import element.sequence.Partitionable;
import element.sequence.Sequence;
import element.sequence.SequenceGroup;

/**
 * Handles the drawing of the image used to display the column header information for the sgContentPanel. Note
 * that this does not handle selection region oriented stuff, just the numbers, tick marks, and partition
 * labels that are not likely to get updated very rapidly.
 * Note that this image is drawn by a component that extends JPanel, defined in a class that lives in 
 * SGContentPanel and which is named ColumnHeader... watch out for confusing nomelclature. A more descriptive
 * name for this class may be columnHeaderImagePainter or something. 
 * better be called 
 * @author brendan
 *
 */
public class SGColumnHeader extends JPanel implements ZeroColumnListener, PartitionChangeListener {

	private boolean drawPartitions = false;
	private boolean drawNumbers = true;
	private boolean drawConsensus = false;
	
	JPopupMenu popup;
	
	private int maxRulerHeight = 13;
	private int consensusHeight = 15;
	private int partitionHeight = 14;
	
	private int totalHeight = maxRulerHeight+5; //Last bit is a buffer
	
	int majorTick = 10;
	int minorTick = 5;
	
	int prevCellWidth = 0;
	
	//This 'shadows' a field of the same name in SGContentPanel; it indicates which column is numbered zero
	int zeroColumn = -1;
	
	Font font = new Font("Sans", Font.PLAIN, 10);
	Font parFont = new Font("Sans", Font.PLAIN, 10); //Used for partition label drawing
	Font conFont = new Font("Sans", Font.PLAIN, 9); //Used for consensus seq. drawing
	
	private char[] partitionLetters = {'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'W', 'X', 'Y', 'Z'};
	
	Sequence consensus = null;
	
	SGContentPanel contentPanel;
	
	BufferedImage columnHeaderImage;
	
	int prevColHeaderWidth = -1;
	
	//Set to true if we will redraw the header image on the next call to paint
	private boolean redrawImage = true;
	
	SequenceGroup currentSG = null;
	
	public SGColumnHeader(SGContentPanel contentPanel) {
		this.contentPanel = contentPanel;
		setBackground(Color.white);
		
		constructPopup();
		
		PopupListener popupListener = new PopupListener();
		this.addMouseListener(popupListener);
	}
	
	
	public void setShowConsensus(boolean show) {
		drawConsensus = show;
	}
	
	public void setShowPartitions(boolean show) {
		drawPartitions = show;
	}
	

	@Override
	public void partitionStateChanged(Partitionable source,
			PartitionChangeType type) {
		redrawImage = true;
		repaint();
	}
	
	/**
	 * This component keeps track of how high it 'should' be via the totalHeight field, which
	 * should generally (but in practice doesn't always) reflect the height of the component.
	 * @return Get the 
	 */
	public int getTotalHeight() {
		return totalHeight;
	}
	
	/**
	 * Set this component's height to totalHeight via calls to setMinimumSize... etc. 
	 * This also calls revalidate and repaint
	 */
	private void setHeight() {
		setMinimumSize(new Dimension(10, totalHeight));
		setPreferredSize(new Dimension(getWidth(), totalHeight));
		setMaximumSize(new Dimension(Integer.MAX_VALUE, totalHeight));
		revalidate();
		repaint();
	}
	
	public void setShowRuler(boolean show) {
		drawNumbers = show;
	}
	
	
	/**
	 * Re-construct the column header image, creating a new BufferedImage if necessary
	 */
	public void drawColumnHeaderImage() {
		
		if (columnHeaderImage==null || prevColHeaderWidth != getWidth() || totalHeight != getHeight()) {
			prevColHeaderWidth = getWidth();
			columnHeaderImage = this.getGraphicsConfiguration().createCompatibleImage(getWidth(), totalHeight);
			//System.out.println("Reallocating column header image with width: " + getWidth());
		}
		Graphics2D g2d = columnHeaderImage.createGraphics();
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		g2d.setColor(getBackground());
		g2d.fillRect(0, 0, columnHeaderImage.getWidth(), columnHeaderImage.getHeight());
		paintColumnHeader(g2d, contentPanel.columnWidth, totalHeight, contentPanel.seqs);
		redrawImage = false;
	}
	
	/**
	 * Paint the current column header image using the specific graphics object, sequence group, etc. 
	 * @param g2d
	 * @param cellWidth
	 * @param height
	 * @param sg
	 */
	public void paintColumnHeader(Graphics2D g2d, 
									int cellWidth,
									int height, 
									SequenceGroup sg) {
	
		g2d.setColor(Color.GRAY);
		g2d.setFont(font);
		
		majorTick = 10;
		minorTick = 5;
		
		int sumHeight = 0;
		
		if (cellWidth < 5) {
			majorTick = 50;
			minorTick = 10;
		}
		if (cellWidth < 3) {
			majorTick = 100;
			minorTick = 50;
		}

		if (drawNumbers) {
			int max = sg.getMaxSeqLength();
			int rulerHeight = Math.min(maxRulerHeight, height);

			
			
			g2d.drawLine(0, rulerHeight-1, max*cellWidth, rulerHeight-1); //Bottom horizontal line
			int offSet = 0;
			
			if (zeroColumn != 0)
				offSet = zeroColumn % majorTick;
			
			//Draw minor ticks first so col. numbers are on top
			for(int i=offSet; i<max; i+=minorTick) {
				g2d.drawLine(i*cellWidth, 2, i*cellWidth, rulerHeight-1);
			}
			
			//Now draw major ticks and 
			for(int i=offSet; i<max; i+=majorTick) {
				g2d.drawLine(i*cellWidth, 0, i*cellWidth, rulerHeight-1);
				if (i>offSet) {
					String numStr = String.valueOf(i-zeroColumn);
					g2d.setColor(Color.white);
					g2d.fillRect(i*cellWidth+2, 1, g2d.getFontMetrics().stringWidth(numStr), 10);
					g2d.setColor(Color.DARK_GRAY);
					g2d.drawString(numStr, i*cellWidth+2, rulerHeight-4);
				}
			}


			
			Color[] partitionColors = ColorDefaults.partitionColors;
			for (int i=0; i<=sg.getMaxSeqLength(); i++) {
				Integer part = sg.getPartitionNumForSite(i);
				if (part!= null && part>0) {
					g2d.setColor( partitionColors[(part)%partitionColors.length]);
					g2d.fillRect(i*(cellWidth)+1 , rulerHeight, cellWidth, 2);
				}
				
			}

			sumHeight += rulerHeight;
		}
		
		if (drawConsensus) {
			char[] base = new char[1];
			if (consensus==null) {
				SequenceGroupCalculator calc = new SequenceGroupCalculator(sg);
				consensus = calc.getConsensusSequence(true);
			}
			g2d.setColor(Color.black);
			
			sumHeight += consensusHeight;
			float fontSize = cellWidth+2;
			conFont = conFont.deriveFont( fontSize );
			g2d.setFont(conFont);
			
			for(int i=0; i<consensus.length(); i++) {
				base[0] = consensus.at(i);
				if (base[0] == 'G' || base[0] == 'C')
					g2d.drawChars(base, 0, 1, i*cellWidth-1, sumHeight-3);
				else
					g2d.drawChars(base, 0, 1, i*cellWidth, sumHeight-3);
			}
		}
		
		
		if (drawPartitions) {
			if (prevCellWidth != cellWidth) {
				float fontSize = cellWidth;
				parFont = parFont.deriveFont( fontSize );
				prevCellWidth = cellWidth;
			}
			sumHeight += partitionHeight;
			Color[] partitionColors = ColorDefaults.partitionColors;
			g2d.setFont(parFont);
			for (int i=0; i<=sg.getMaxSeqLength(); i++) {
				Integer part = sg.getPartitionNumForSite(i);
				if (part!= null && part>0) {
					g2d.setColor(Color.white);
					g2d.fillRect(i*(cellWidth)+1 , sumHeight-partitionHeight+2, cellWidth+1, partitionHeight-2);
					g2d.setColor( partitionColors[(part)%partitionColors.length]);
					g2d.drawString(String.valueOf(partitionLetters[(part-1)%partitionLetters.length]), i*(cellWidth)+2, sumHeight-3);
				}
				
			}
		}
	}

	@Override
	public void zeroColumnChanged(int newCol) {
		zeroColumn = newCol;
	}
		
	public void paintComponent(Graphics g) {
		int columnWidth = contentPanel.columnWidth;
		int dragStart = contentPanel.dragStart;
		int dragEnd = contentPanel.dragEnd;
		
		g.setColor(Color.white);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		//We must listen for partition change events so we know when to redraw the image
		if (currentSG == null) {
			currentSG = contentPanel.seqs;
			currentSG.addPartitionListener(this);
		}
		
		if (redrawImage)
			drawColumnHeaderImage();
		
		((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,   RenderingHints.VALUE_ANTIALIAS_ON);		
		g.drawImage(columnHeaderImage, 0, 0, null);

		
		//Draw line indicating column selection region
		if (contentPanel.selectionMode == Selection.COLUMNS && dragStart != dragEnd && drawNumbers) {

			int rulerHeight = Math.min(maxRulerHeight, getHeight());
			int dMin = Math.min(dragStart, dragEnd);
			int dMax = Math.max(dragStart, dragEnd);

			g.setColor(new Color(255, 255, 255, 200));
			g.fillRect(dMin*columnWidth, 0, (dMax-dMin)*columnWidth, rulerHeight-1);
			g.setColor(Color.gray);
			((Graphics2D) g).setStroke(new BasicStroke(1.25f));
			int lineY = rulerHeight/2-1;
			g.drawLine(dMin*columnWidth+1, lineY, dMax*columnWidth-1, lineY);
			
			g.drawLine(dMin*columnWidth, lineY, (dMin)*columnWidth+5, 0);
			g.drawLine(dMin*columnWidth, lineY, (dMin)*columnWidth+5, rulerHeight-3);

			g.drawLine(dMax*columnWidth, lineY, (dMax)*columnWidth-5, 0);
			g.drawLine(dMax*columnWidth, lineY, (dMax)*columnWidth-5, rulerHeight-3);

			g.setColor(Color.white);
			String label = String.valueOf(dMax-dMin);
			g.setFont(font.deriveFont(Font.BOLD));
			FontMetrics fm = g.getFontMetrics();
			int labWidth = fm.stringWidth(label);
			g.fillRect((dMax+dMin)*columnWidth/2-labWidth/2, 0, labWidth, rulerHeight-1);
			g.setColor(Color.gray);
			g.drawString(label, (dMax+dMin)*columnWidth/2-labWidth/2, rulerHeight-3);
		}
	}


	
	private void constructPopup() {
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
		popup.setBackground(new Color(100,100,100) );
		hideNumbers = new JMenuItem("Hide ruler");
		hideNumbers.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if (drawNumbers) {
            		totalHeight -= maxRulerHeight;
            	}
            	else
            		totalHeight += maxRulerHeight;
            	
            	drawNumbers = !drawNumbers;
            	setHeight();
            	drawColumnHeaderImage();
            	repaint();
            }
        });
		popup.add(hideNumbers);
		
		
		hideConsensus = new JMenuItem("Draw consensus");
		hideConsensus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if (drawConsensus)
            		totalHeight -= consensusHeight;
            	else
            		totalHeight += consensusHeight;
            	drawConsensus = !drawConsensus;
            	setHeight();
            	drawColumnHeaderImage();
            	repaint();
            }
        });
		popup.add(hideConsensus);
		
		hidePartitions = new JMenuItem("Draw partitions");
		hidePartitions.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	if (drawPartitions)
            		totalHeight -= partitionHeight;
            	else 
            		totalHeight += partitionHeight;
            	drawPartitions = !drawPartitions;
            	setHeight();
            	drawColumnHeaderImage();
            	repaint();
            }
        });
		popup.add(hidePartitions);
	}

	
	
	private class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        showPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        showPopup(e);
	    }

	    private void showPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	if (drawNumbers) {
	        		hideNumbers.setText("Hide ruler");
	        	}
	        	else {
	        		hideNumbers.setText("Show ruler");
	        	}
	        	
	        	if (drawPartitions) {
	        		hidePartitions.setText("Hide partitions");
	        	}
	        	else {
	        		hidePartitions.setText("Show partitions");
	        	}
	        	
	        	if (drawConsensus) {
	        		hideConsensus.setText("Hide consensus");
	        	}
	        	else {
	        		hideConsensus.setText("Show consensus");
	        	}
	            
	        	popup.show(e.getComponent(), e.getX(), e.getY());
	        }
	    }
	}


	JMenuItem hidePartitions;
	JMenuItem hideConsensus;
	JMenuItem hideNumbers;

	

	
}
