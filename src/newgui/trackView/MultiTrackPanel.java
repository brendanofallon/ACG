package newgui.trackView;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.BoundedRangeModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

/**
 * A container for a MultiTrackView, a horizontal scroll bar, and potentially
 * a few other widgets that allow for adjusting the view
 * @author brendan
 *
 */
public class MultiTrackPanel extends JPanel {
	
	private MultiTrackView trackView;
	protected JScrollBar hScrollBar;
	protected JScrollBar vScrollBar;
	
	public MultiTrackPanel(BoundedRangeModel intervalModel) {
		trackView = new MultiTrackView();
		trackView.initializeHInterval(intervalModel.getMinimum(), intervalModel.getMaximum(), intervalModel.getValue(), intervalModel.getExtent());
		initializeComponents();
	}
	
	public MultiTrackPanel(int min, int max, int value, int extent) {
		trackView = new MultiTrackView();
		trackView.initializeHInterval(min, max, value, extent);
		initializeComponents();
	}
	
	public void initializeHInterval(int min, int max, int value, int extent) {
		trackView.initializeHInterval(min, max, value, extent);
	}

	/**
	 * Remove all views from this panel
	 */
	public void clearViews() {
		trackView.clearViews();
	}
	
	public void fitHeightToTracks() {
		int newHeight = trackView.fitHeightToTracks();
		this.setMinimumSize(new Dimension(trackView.getWidth(), newHeight));
		//this.setPreferredSize(new Dimension(500, newHeight));
		revalidate();
		repaint();
	}
	
	public void addTrackView(TrackView view) {
		trackView.addTrackView(view);
	}
	
	public void removeTrackView(TrackView view) {
		trackView.removeTrackView(view);
	}
	
	public void setPixelsPerSite(double pxPerSite) {
		trackView.setPixelsPerSite(pxPerSite);
		repaint();
	}
	
	
	
	private void initializeComponents() {
		setLayout(new BorderLayout());
		
		this.add(trackView, BorderLayout.CENTER);
		
		hScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		hScrollBar.setModel(trackView.getIntervalModel());
		hScrollBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				trackView.getIntervalModel().setValue(e.getValue());
				trackView.repaint();
			}
		});
		this.add(hScrollBar, BorderLayout.SOUTH);
		
		vScrollBar = new JScrollBar(JScrollBar.VERTICAL);
		vScrollBar.setModel(trackView.getVIntervalModel());
		vScrollBar.addAdjustmentListener(new AdjustmentListener() {

			@Override
			public void adjustmentValueChanged(AdjustmentEvent e) {
				//System.out.println("Changed to value: " + e.getValue());
				trackView.getVIntervalModel().setValue(e.getValue());
				trackView.repaint();
			}
			
		});
		this.add(vScrollBar, BorderLayout.EAST);
	}
	
	
}
