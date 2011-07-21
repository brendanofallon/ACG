package figure.series;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.JTextField;
import javax.swing.plaf.basic.BasicInternalFrameTitlePane.SystemMenuBar;

import topLevelGUI.SunFishFrame;

import figure.Figure;
import figure.FigureElement;

/**
 * An element that draws a series of points in x-y space. Different markers can be placed
 * on data points, or lines can be drawn through points, or the series can be represented by
 * 'boxes' column graph style. 
 * 
 * @author brendan
 *
 */
public class XYSeriesElement extends SeriesElement {
	
	protected XYSeries xySeries;
	GeneralPath pathShape;
	GeneralPath markerShape;
	
	AxesElement axes;
	
	SeriesConfigFrame configFrame;
	
	//Flag to indicate if we should recalculate data bounds
	//False indicates we should recalculate
	boolean dataBoundsSet = false;
	
	boolean scaleHasBeenSet = false;
	

	//Tracks current marker type and size
	String currentMarkerType = markerTypes[2];
	int markerSize = 6;
	
	Color boxOutlineColor = Color.DARK_GRAY;
	
	Rectangle2D lineRect; //Use to test if this series contains certain points
	
	//Ensure boxes are linked 
	boolean connectBoxes = true;
	
	//The transformation object that maps 'figure' points (in 0..1 scale) into pixel space
	//We keep track of it to avoid having to make a new one all the time, and 
	//so that, when new transforms are needed, we can call the .invert() method
	//on it to unapply the previous transform before a new one is applied
	AffineTransform currentTransform;
	
	//Draws fancier looking boxes
	protected boolean decorateBoxes = true;
	
	Rectangle2D boxRect = new Rectangle2D.Double(0, 0, 0, 0); //Used repeatedly to draw boxes
	
	//Some allocated space for marker polygon drawing
	int[] xvals;
	int[] yvals;
	
	public XYSeriesElement(XYSeries series, AxesElement axes, SeriesFigure parent) {
		super(parent, series);
		this.xySeries = series;
		this.axes = axes;
		this.xFactor = 1;
		this.yFactor = 1;
		dataBoundsSet = false; 
		
		currentTransform = new AffineTransform();
		currentTransform.setToIdentity();
		
		configFrame = new SeriesConfigFrame(this, parent);
		
		normalStroke = new BasicStroke(1.25f);
		highlightStroke = new BasicStroke(1.25f + highlightWidthIncrease);
		
		lineRect = new Rectangle.Double(0, 0, 0, 0);
		
		//Some buffers for drawing marker polygons
		xvals = new int[5];
		yvals = new int[5];
	}
	
	
	
	public XYSeries getSeries() {
		return xySeries;
	}
	
	public void setSeries(XYSeries newSeries) {
		this.xySeries = newSeries;
	}
	

	@Override
	public double getMaxY() {
		return xySeries.getMaxY();
	}

	@Override
	public double getMinY() {
		return xySeries.getMinY();
	}
	
	@Override
	public double getMaxX() {
		return xySeries.getMaxX();
	}

	@Override
	public double getMinX() {
		return xySeries.getMinX();
	}
	
	
	public int indexForMode() {
		for(int i=0; i<styleTypes.length; i++) {
			if (styleTypes[i].equals(currentMode)) {
				return i;
			}
		}
		
		SunFishFrame.getSunFishFrame().getLogger().warning("Unrecognized current mode in XYSeriesElement : " + currentMode);
		throw new IllegalStateException("Illegal current mode in XYSeries Element : " + currentMode);		
	}
	
	
	/**
	 * Returns the index of the current marker type in markerTypes. 
	 * @param markerType
	 * @return The index of the current marker type in markerTypes
	 */
	public int indexForMarkerType() {
		for(int i=0; i<markerTypes.length; i++) {
			if (currentMarkerType.equals(markerTypes[i])) {
				return i;
			}
		}
		
		//We should never get here
		SunFishFrame.getSunFishFrame().getLogger().warning("Unrecognized current marker type in XYSeriesElement : " + currentMarkerType);
		throw new IllegalStateException("Illegal current marker type in XYSeries Element : " + currentMarkerType);
	}
	
	
	/**
	 * Set the marker type for this series to the given type, which should be a member of markerTypes. This
	 * throws an IllegalArgumentException if the supplied type is not a valid type.
	 * @param markerType
	 */
	public void setMarker(String markerType) {
		boolean valid = false;
		for(int i=0; i<markerTypes.length; i++) {
			if (markerTypes[i].equals(markerType)) {
				currentMarkerType = markerType;
				valid = true;
			}
		}
		
		if (!valid) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Illegal marker type request from XYSeriesElement : " + markerType);
			throw new IllegalArgumentException("Cannot set marker type to : " + markerType);
		}
	}
	
	public void popupConfigureTool(java.awt.Point pos) {
		configFrame.display(getName(), currentMode, getLineColor(), round(((BasicStroke)normalStroke).getLineWidth()), markerSize, currentMarkerType);
	}
	
	/**
	 * Sets the various options (colors, linewidths, etc) of this series to those specified in ops
	 * @param ops Container object for various series options.
	 */
	public void setOptions(SeriesConfigFrame.SeriesOptions ops) {
		boolean resort = false;

		xySeries.setName( ops.name );
		setLineColor(ops.lineColor);
		normalStroke = new BasicStroke((float)ops.lineWidth);
		highlightStroke = new BasicStroke((float)ops.lineWidth+highlightWidthIncrease);
		currentMarkerType = ops.markerType;
		markerSize = ops.markerSize;
		//Make sure lines are painted on top of boxes
		if (currentMode != ops.type && parent instanceof XYSeriesFigure) {
			resort = true;
			if (ops.type == BOXES) 
				setZPosition(-5);
			else 
				setZPosition(0);
		}
		currentMode = ops.type;
		
		if (resort) {
			((XYSeriesFigure)parent).getElementList().resort();
		}
		
		parent.repaint();
	}
	
	/**
	 * This call informs this element of what the data x and y bounds are.  It must be called
	 * prior to any painting operation. Also, calls to this function are relatively expensive since
	 * they involve creating a bunch of new objects, and ideally this should only be called when the
	 * x or y boundaries are changed (but not, for instance, when the figure size has changed).
	 * 
	 * @param dBounds
	 */
	public void setDataBounds() {
		regenerateShape();
		dataBoundsSet = true;
	}
	
	private void regenerateShape() {
		if (xySeries.size()==0) {
			pathShape = new GeneralPath();
			return;
		}
		
		if (pathShape == null) {
			pathShape = new GeneralPath(new Line2D.Double(xySeries.get(0).x, xySeries.get(0).y, xySeries.get(1).x, xySeries.get(1).y) );
		}
		else 
			pathShape.reset();
		
		if (currentMode == LINES || currentMode == POINTS_AND_LINES || currentMode == POINTS) {
			if (xySeries.size()>1) {
				double x1 = axes.dataXtoBoundsX(xySeries.get(0).x  );
				double y1 = axes.dataYtoBoundsY(xySeries.get(0).y );
				double x2 = axes.dataXtoBoundsX( xySeries.get(1).x);
				double y2 = axes.dataYtoBoundsY( xySeries.get(1).y );
				pathShape = new GeneralPath(new Line2D.Double(x1, y1, x2, y2) );
				
				boolean connect = true;
				for(int i=1; i<xySeries.size(); i++) {
					x1 = axes.dataXtoBoundsX( xySeries.get(i).x);
					y1 = axes.dataYtoBoundsY( xySeries.get(i).y );
					
					
					//We've moved from a undrawn region into an OK one, so just move the 'pointer
					//to the new site
					if (!connect && !(Double.isNaN(y1))) {
						pathShape.moveTo(x1, y1);
						connect = true;
					}
					
					//Moving from a good region to an undrawn one
					if (connect && Double.isNaN(y1)) {
						connect = false;
					}
					
					
					if (connect)
						pathShape.lineTo(x1, y1);
				}
			}
		}
				
		
		if (currentMode == BOXES) {
			// Currently there is no pathShape that defines the boundaries for BOXES mode. We use getboxForIndex(...)
			// to calculate a rectangle corresponding to the box for a particular index in the seiries, and that same
			// rectangle is used in the contains(x, y) method. 
		}
		
	}
	
	/**
	 * Calculate the standard width of the rectangle used to draw a box, if we're drawing boxes. The
	 * default here is to pack the boxes tightly, but that can be controlled if the boxWidthDivisor
	 * variable is set.
	 * @return
	 */
	private double calculateBoxWidth() {
		double boxesShowing = series.size()*(axes.maxXVal-axes.minXVal)/(xySeries.getMaxX()-xySeries.getMinX()); 
		return axes.getGraphAreaBounds().width / boxesShowing / (double)boxWidthDivisor;
	}
	
	public boolean contains(double x, double y) {		
		
		if (currentMode == POINTS_AND_LINES || currentMode == LINES) {
			double dataX = axes.boundsXtoDataX(x);
			
			lineRect.setRect(x*xFactor-4, y*yFactor-4, 7, 7);
			element.Point[] line = xySeries.getLineForXVal(dataX);
//			System.out.println("Series " + getName() + " Testing point x: " + x + ", " + y);
//			System.out.println(" Point 0 : " + line[0]);
//			System.out.println(" Point 1 : " + line[1]);
			if (line==null || Double.isNaN(line[0].y) || Double.isNaN(line[1].y)) {
				//System.out.println("line is null or NaN, returning false");
				return false;
			}
			else {
				boolean contains = lineRect.intersectsLine(axes.dataXtoBoundsX(line[0].x)*xFactor, axes.dataYtoBoundsY(line[0].y)*yFactor, axes.dataXtoBoundsX(line[1].x)*xFactor, axes.dataYtoBoundsY(line[1].y)*yFactor);
				return contains;
			}
			
		}
		
		if (currentMode == BOXES) {
			double boxWidth = calculateBoxWidth();


			double yAxis = axes.dataYtoFigureY(0);
			
			double dataX = axes.boundsXtoDataX(x+(boxWidth*boxOffset+Math.ceil(boxWidth/2.0))/xFactor);
			int boxIndex = xySeries.getIndexForXVal(dataX);
			Rectangle2D rect = getBoxForIndex(boxIndex, yAxis); 
			//System.out.println( " click x: " + x*xFactor + " data x: " + dataX + "Box index: " + boxIndex + " x: " + rect.getX() + " width: " + rect.getWidth() );
			if (rect==null) {
				//System.out.println("Rect is null, returning false");
				return false;
			}
			Point2D pos = new Point2D.Double(x*xFactor, y*yFactor);
			return rect.contains(pos);
			
		}
		
		if (currentMode == POINTS) {
			return pathShape.intersects(x*xFactor-3, y*yFactor-3, 5, 5);
		}
		
		return false;
	}
	
	public void drawMarker(Graphics2D g, int x, int y) {
		if (currentMarkerType.equals("Circle")) {
			g.setColor(getLineColor());
			g.fillOval(round(x-markerSize/2.0), round(y-markerSize/2.0), markerSize, markerSize);
		}
		if (currentMarkerType.equals("Square")) {
			g.setColor(getLineColor());
			g.fillRect(round(x-markerSize/2.0), round(y-markerSize/2.0), markerSize, markerSize);			
		}
		if (currentMarkerType.equals("Diamond")) {
			g.setColor(getLineColor());
			xvals[0] = round(x-markerSize/2.0);
			xvals[1] = x;
			xvals[2] = round(x+markerSize/2.0);
			xvals[3] = x;
			xvals[4] = xvals[0];
			yvals[0] = y;
			yvals[1] = round(y-markerSize/2.0);
			yvals[2] = y;
			yvals[3] = round(y+markerSize/2.0);
			yvals[4] = y;
			g.fillPolygon(xvals, yvals, 5);
		}
		if (currentMarkerType.equals("Plus")) {
			g.setColor(getLineColor());
			g.drawLine(round(x-markerSize/2.0), y, round(x+markerSize/2.0), y);
			g.drawLine(x, round(y-markerSize/2.0), x, round(y+markerSize/2.0));
		}
		if (currentMarkerType.equals("X")) {
			g.setColor(getLineColor());
			g.drawLine(round(x-markerSize/2.0), round(y-markerSize/2.0), round(x+markerSize/2.0), round(y+markerSize/2.0));
			g.drawLine(round(x-markerSize/2.0), round(y+markerSize/2.0), round(x+markerSize/2.0), round(y-markerSize/2.0));
		}
	}
	
	private void emitPathShape() {
		AffineTransform transform = new AffineTransform();
		transform.setToIdentity();
		PathIterator pi = pathShape.getPathIterator(transform);
		
		double[] coords = new double[6];
		int index = 0;
		while (! pi.isDone()) {
			pi.currentSegment(coords);
			pi.next();
			index++;
		}
	}
	
	public void setScale(double xFactor, double yFactor, Graphics g) {
		setDataBounds();
				
		currentTransform.setToScale(xFactor, yFactor);
		pathShape.transform(currentTransform);
		
		this.xFactor = xFactor;
		this.yFactor = yFactor;	
		scaleHasBeenSet = true;
	}
	
	
	/**
	 * Returns the rectangular box shape in pixel coordinates associated with the index i in the 
	 * data series. Requires knowing what the box width is and where the y-axis is.
	 */
	protected Rectangle2D getBoxForIndex(int i, double yZero) {
		if (i>=xySeries.size()) {
			return null;
		}
		double boxWidth = calculateBoxWidth();
		
		double halfBox = Math.ceil(boxWidth/2.0);
		double dataY = axes.dataYtoFigureY(xySeries.get(i).y);
		double xOffset = boxOffset*boxWidth;
		if (xySeries.get(i).y>0) 
			boxRect.setRect(axes.dataXtoFigureX(xySeries.get(i).x)-halfBox-xOffset, dataY, boxWidth, yZero-dataY);
		else 
			boxRect.setRect(axes.dataXtoFigureX(xySeries.get(i).x)-halfBox-xOffset, yZero, boxWidth, dataY-yZero);

		return boxRect;
	}
	
	public void paint(Graphics2D g) {
		if (! scaleHasBeenSet) {
			System.out.println(" Calling paint, but scale has not been set! ");
		}
			
		if (! dataBoundsSet )
			setDataBounds();
	
		Rectangle clipBounds = axes.getGraphAreaBounds();
		clipBounds.x++;
		clipBounds.height++;
		
		g.setClip(clipBounds ); //Make sure we don't draw outside the lines
		
		if (isSelected) {
			g.setColor(highlightColor);
			g.setStroke(highlightStroke);
			if (currentMode == LINES || currentMode == POINTS_AND_LINES || currentMode == BOXES) 
				g.draw(pathShape);
		
		}
		
		g.setStroke(normalStroke);
		
		
		
		if (currentMode == LINES) {
			g.setColor(getLineColor());
			g.draw(pathShape);	
		}
		
		if (currentMode == BOXES) {
			g.setColor(getLineColor());
			//This code is currently duplicated in contains - if you change something here, change it there too 
			//double boxesShowing = series.size()*(axes.maxXVal-axes.minXVal)/(xySeries.getMaxX()-xySeries.getMinX()); 
			//double boxWidth = axes.getGraphAreaBounds().width / boxesShowing / (double)boxWidthDivisor;
			//double xOffset = boxOffset*boxWidth;
			//System.out.println("Box offset: " + boxOffset + " box divisor: "  + boxWidthDivisor + " boxwidth: " + boxWidth);
			double yAxis = axes.dataYtoFigureY(0);
			
			for(int i=0; i<xySeries.size(); i++) {
				Rectangle2D rect = getBoxForIndex(i, yAxis);
				drawBox(g, rect);
			}
		}

		
		if (currentMode == POINTS ) {
			g.setColor(getLineColor());
			for(int i=0; i<xySeries.size(); i++) {
				drawMarker(g, round(axes.dataXtoFigureX(xySeries.get(i).x)), round(axes.dataYtoFigureY(xySeries.get(i).y)));
			}
		}
		
		if (currentMode == POINTS_AND_LINES ) {
			g.setColor(getLineColor());
			g.draw(pathShape);
			
			g.setColor(getLineColor());
			for(int i=0; i<xySeries.size(); i++) {
				drawMarker(g, round(axes.dataXtoFigureX(xySeries.get(i).x)), round(axes.dataYtoFigureY(xySeries.get(i).y))); 
			}
		}	
		
		g.setStroke(normalStroke);
		g.setClip(0, 0, parent.getWidth(), parent.getHeight()); //return clip to usual bounds
	}



	/**
	 * Draws the rectangular box that corresponds to a particular point in this series  
	 * @param rect
	 */
	private void drawBox(Graphics2D g, Rectangle2D rect) {
		g.setColor(getLineColor());
		g.fill(rect);

		if (rect.getWidth()>4) {
			if (decorateBoxes) {
				int dwidth = (int)Math.round(rect.getWidth()/2.0);
				for(int i=0; i<dwidth; i++) {
					g.setColor(new Color(1.0f, 1.0f, 1.0f, (0.2f)*(1.0f-(float)i/(float)dwidth)));
					g.drawLine(round(rect.getX()+i), round(rect.getY()), round(rect.getX()+i), round(rect.getY()+rect.getHeight()));
				}

			}

			g.setColor(boxOutlineColor);
			g.draw(rect);
		}
		

	}



	
}
