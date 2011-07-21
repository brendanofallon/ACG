package figure.series;

import java.util.ArrayList;
import java.util.List;

import figure.categorySeries.CatSeriesElement;

/**
 * A figure that displays category data. Currently the handling of multiple data series is very crude, and although
 * each element in the series' provided has an identifier, the id's are actually ignored and we just place
 * each element in the input order. Hence, when multiple series are added, they may not end up with corresponding
 * elements in the same bin ...
 * @author brendan
 *
 */
public class CategoryFigure extends XYSeriesFigure {

	
	public CategoryFigure() {
		setYLabel("");
		setDrawLegend(false);
		axes.setAllowMouseDragSelection(false);
		removeElement(dataPosElement);
	}
	
	public XYSeriesElement addDataSeries(XYSeries newSeries) {
		throw new IllegalArgumentException("Can't add an XYSeries to a CategoryFigure (just category series)");
	}
	
	
	/**
	 * Recalculate box widths and offsets for all series to ensure nothing's overlapping.
	 * This overrides XYSeriesFigure.placeBoxSeries to make the boxes a bit smaller.
	 */
	public void placeBoxSeries() {
		int count = 0;
		for(SeriesElement series : getSeriesElements()) {
			if (series.getType().equals(  XYSeriesElement.BOXES)) {
				count++;
			}
		}
		
		int index = 0;
		for(SeriesElement series : getSeriesElements()) {
			if (series.getType().equals(  XYSeriesElement.BOXES)) {
				series.setBoxWidthAndOffset(count*2, (double)(index-(count-1.0)/2.0));
				index++;
			}
		}
		
		repaint();
	}
	
	/**
	 * Add a new series to this this figure. A new XYSeriesElement is generated and added to both the list of
	 * FigureElements tracked by the Figure, as well as the list of SeriesElements maintained in SeriesFigure.
	 * @param newSeries
	 * @return newElement The newly created element
	 */
	public XYSeriesElement addDataSeries(CategorySeries newSeries) {
		XYSeriesElement newElement = new CatSeriesElement(this, axes, newSeries);
		
		newElement.setMode(SeriesElement.BOXES);
		newElement.setLineColor( colorList[ seriesElements.size() % colorList.length] );
		seriesElements.add(newElement);
		addElement(newElement);
		newElement.setCanConfigure(true);
		
		//System.out.println("Setting color index to: " + seriesElements.size() % colorList.length + " which is color: " + colorList[ seriesElements.size() % colorList.length]);
		placeBoxSeries();
		inferBoundsFromCurrentSeries();
		setXMin(-0.5);
		setXMax( Math.max( newSeries.size()-0.5, 0));
		axes.setXTickSpacing(1.0); //Required for proper painting of x label list
		
		if (seriesElements.size()>1) 
			setDrawLegend(true);
		
		return newElement;
	}
	
	/**
	 * Provides a list of x-labels to use instead of numbers for the x-axis. Suppresses drawing all numbers and forces
	 * the x tick interval to be 1.0.
	 * @param labels
	 */
	public void setXLabelList(List<String> labels) {
		axes.setXLabels(labels);
	}
	
}
