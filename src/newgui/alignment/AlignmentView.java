package newgui.alignment;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import sequence.Alignment;
import sequence.Sequence;

import newgui.trackView.AxisTrackView;
import newgui.trackView.MultiTrackPanel;


/**
 * Simple viewer panel for alignments. This is just a MultiTrackView
 * that contains SequenceTrackViews for each Sequence in the alignment
 * @author brendan
 *
 */
public class AlignmentView extends MultiTrackPanel {

	protected JPanel topPanel;
	protected Alignment aln;
	protected AlignmentRowHeader rowHeader; //Paints sequence labels at left
	protected List<SequenceTrackView> seqViews = new ArrayList<SequenceTrackView>();
	protected int seqTrackHeight = 20;
	
	
	public AlignmentView() {
		super(0, 100, 0, 100);
		this.aln = new BasicAlignment();
		initComponents();
	}
	
	public AlignmentView(Alignment aln) {
		super(0, aln.getSequenceLength(), 0, aln.getSequenceLength());
		
		this.aln = aln;
		fitHeightToTracks();
		initComponents();
	}

	/**
	 * Set the Aligment represented by this view
	 * @param aln
	 */
	public void setAlignment(Alignment aln) {
		this.aln = aln;
		
		
		initializeTracks();
		initializeHInterval(0, aln.getSequenceLength(), 0, aln.getSequenceLength());
	}
	
	private void initComponents() {
		this.setBackground(Color.white);
		topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
		topPanel.setOpaque(false);
		final JSlider slider = new JSlider(1, 1000, 1); //We start completely zoomed out
		slider.setMaximumSize(new Dimension(100, 50));
		slider.setPreferredSize(new Dimension(100, 30));
		slider.setFont(new Font("Sans", Font.PLAIN, 0)); //Only way to make dumb values thing disappear
		slider.setPaintLabels(false);
		slider.setPaintTicks(false);
		slider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				setPixelsPerSite(10.0*slider.getValue()/(double)slider.getMaximum());
			}
		});
		topPanel.add(slider);
		this.add(topPanel, BorderLayout.NORTH);
		
		rowHeader = new AlignmentRowHeader();
		rowHeader.setBackground(this.getBackground());
		rowHeader.setRowHeight(seqTrackHeight);
		initializeTracks();
		this.add(rowHeader, BorderLayout.WEST);
		
		DragListener headerListener = new DragListener();
		rowHeader.addMouseListener( headerListener );
		rowHeader.addMouseMotionListener( headerListener );	
		
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				rowHeader.setYTranslate(e.getValue());
			}
		});
		
		topPanel.addMouseListener(new MouseListener() {
			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
			
			@Override
			public void mouseClicked(MouseEvent e) { }

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) {	}

		});
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseEntered(MouseEvent e) {
				setCursor(Cursor.getDefaultCursor());
			}
			
			@Override
			public void mouseClicked(MouseEvent e) { }

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e) { }

			@Override
			public void mouseExited(MouseEvent e) {	}

		});
	}
	/**
	 * Attempt to adjust the width of the row header so it makes sense for the 
	 * length of the labels
	 */
	public void fitRowHeaderWidthToLabels() {
		//Find longest label
		int max = 0;
		
		for(int i=0; i<aln.getSequenceCount(); i++) {
			int width = 8+7*aln.getSequence(i).getLabel().length();
			if (width > max)
				max = width;
		}
		
		int headerWidth = Math.min(max, 200);
		if (headerWidth < 20)
			headerWidth = 20;
		setRowHeaderWidth(headerWidth);
	}
	
	
	private void addSequenceTrack(SequenceTrackView seqView) {
		seqViews.add(seqView);
		seqView.setTrackHeight(seqTrackHeight);
		rowHeader.addRowLabel(seqView.getSequence().getLabel());
		super.addTrackView(seqView);
	}
	
	private void removeSequenceTrack(SequenceTrackView seqView) {
		seqViews.remove(seqView);
		super.removeTrackView(seqView);
	}
	
	private void addSequenceTrack(Sequence seq) {
		SequenceTrackView view = new SequenceTrackView(seq);
		addSequenceTrack(view);
	}
	
	private void initializeTracks() {
		clearTracks();
		for(int i=0; i<aln.getSequenceCount(); i++) {
			addSequenceTrack(aln.getSequence(i));
		}
		AxisTrackView axis = new AxisTrackView();
		super.addTrackView( axis );
		fitRowHeaderWidthToLabels();
		fitHeightToTracks();
	}
	
	public void clearTracks() {
		seqViews.clear();
		rowHeader.clearLabels();
		super.clearViews();
	}
	
	public void setRowHeaderWidth(int width) {
		rowHeader.setMinimumSize(new Dimension(width, 1));
		rowHeader.setPreferredSize(new Dimension(width, 100));
		rowHeader.setMaximumSize(new Dimension(width, 10000));
		rowHeader.revalidate();
		revalidate();
		repaint();
	}
	
	/**
	 * Small class to listen for drag events near edge of row header so it can be resized
	 * @author brendan
	 *
	 */
	class DragListener extends MouseAdapter {
		
		private int dragBegin = -1;
		private int dragRegionWidth = 15;
		
		public void mousePressed(MouseEvent me) {
			this.dragBegin = me.getX();
		}
		
		public void mouseDragged(MouseEvent me) {
			if (dragBegin > (rowHeader.getWidth()-dragRegionWidth)) {
				int dragDif = me.getX() - dragBegin;
				setRowHeaderWidth(rowHeader.getWidth() + dragDif);
				dragBegin = me.getX();
			}
		}
		
		public void mouseReleased(MouseEvent me) {
			dragBegin = -1;
		}
		
		public void mouseMoved(MouseEvent arg0) {
			Point mousePos = arg0.getPoint();

			if (mousePos.x>(rowHeader.getWidth()-dragRegionWidth) && mousePos.x < (getWidth()-2)) {
				setCursor(AlignmentRowHeader.edgeAdjustCursor);
			}
			
			if (mousePos.x<(rowHeader.getWidth()-dragRegionWidth)) {
				setCursor(Cursor.getDefaultCursor());
			}
		}

		
	}


	
}
