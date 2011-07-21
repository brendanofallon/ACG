package figure.rowFigure;

import java.awt.Graphics2D;

import figure.Figure;
import figure.FigureElement;

/**
 * Row elements are used in RowFigures to display a single row, various subclasses
 * draw 
 * @author brendan
 *
 */
public abstract class RowElement extends FigureElement {

	
	public RowElement(Figure parent) {
		super(parent);
		bounds.x = 0;
		bounds.width = 1.0;
	}

}
