package newgui.trackView;

import java.awt.BorderLayout;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

import javax.swing.JPanel;
import javax.swing.JScrollBar;


public class SlidingShapePanel extends JPanel {

	protected JScrollBar bottomScrollBar;
	protected ShapesPanel shapesPanel;
	
	public SlidingShapePanel(RangeBlock ranges) {
		setLayout(new BorderLayout());
		
		shapesPanel = new ShapesPanel(ranges);
		

		new ShapeDragListener(shapesPanel);
		
		this.add(shapesPanel, BorderLayout.CENTER);
		//scrollBarModel =  //new DefaultBoundedRangeModel(0, ranges.blockEnd, 0, ranges.blockEnd);
		bottomScrollBar = new JScrollBar(JScrollBar.HORIZONTAL);
		bottomScrollBar.setModel(shapesPanel);
		
		this.add(bottomScrollBar, BorderLayout.SOUTH);
		bottomScrollBar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				//Scroll bar has units of sites, which is what setValue takes 
				shapesPanel.setValue(e.getValue());
			}
		});
	}
	
	/**
	 * Set the new amount to zoom by. This directly translates into setting the pixelsPerSite
	 * field in the shapesPanel
	 * @param d
	 */
	public void setZoomLevel(double d) {
		int prevMidSite = (shapesPanel.getFirstVisibleSite() + shapesPanel.getLastVisibleSite())/2;
		//int prevValue = shapesPanel.getValue();
		shapesPanel.setZoomLevel(d);
		shapesPanel.fireChangeEvent();
		int newMidSite = (shapesPanel.getFirstVisibleSite() + shapesPanel.getLastVisibleSite())/2;
		int newValue = shapesPanel.getValue();
		shapesPanel.setValue(newValue + (prevMidSite-newMidSite));
		
		shapesPanel.repaint();
	}
}
