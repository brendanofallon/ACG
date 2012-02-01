package gui.figure.treeFigure.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.QuadCurve2D;

/**
 * Paints a curved branch to connect two nodes on a tree
 * @author brendan
 *
 */
public class CurvedBranchPainter implements BranchPainter {

	CubicCurve2D curve;
	double param;
	Direction orientation = Direction.YFIRST;
	
	public CurvedBranchPainter(double param) {
		curve = new CubicCurve2D.Float();
		this.param = param;
	}
	
	public void setOrientation(Direction orientation) {
		this.orientation = orientation;
	}

	public void paintBranch(Graphics2D g, int x1, int y1, int x2, int y2,
			Stroke stroke, Color color) {
		
		Stroke origStroke = g.getStroke();
		Color origColor = g.getColor();
		g.setStroke(stroke);
		g.setColor(color);
		
		if (orientation == Direction.YFIRST) {
			int py = y1+(int)Math.round(param*(y2-y1)); //when param==1, this is y2
			int px = x2+(int)Math.round(param*(x1-x2)); //When param==1, this is x1

			curve.setCurve(x1, py, x1, (py+3*y2)/4.0, (px+x1)/2.0, y2, px, y2);

			g.drawLine(x1,y1,x1,py);
			g.draw(curve);
			g.drawLine(px, y2, x2, y2);
		}
		else {
			int py = y2+(int)Math.round(param*(y1-y2));
			int px = x1+(int)Math.round(param*param*(x2-x1));

			curve.setCurve(px, y1,   x2, y1,   x2, (py+y1)/2.0,   x2, py);
			
			g.drawLine(x1,y1, px,y1);
			g.draw(curve);
			g.drawLine(x2, py, x2, y2);			
		}
		
		g.setStroke(origStroke);
		g.setColor(origColor);
	}

}
