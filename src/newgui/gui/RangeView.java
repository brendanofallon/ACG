package newgui.gui;

import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;

public class RangeView extends RoundRectangle2D.Float {

	private final Range range;
	
	public RangeView(Range range) {
		super(range.getBegin(), 0, range.getEnd()-range.getBegin(), 10, 20, 80);
		this.range = range;
	}
	
	public int getBegin() {
		return range.getBegin();
	}
	
	public int getEnd() {
		return range.getEnd();
	}
	
	public String toString() {
		return "RangeView " + range;
	}
	
}
