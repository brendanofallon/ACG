package gui.figure.treeFigure.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class SquareBranchPainter implements BranchPainter {

	Direction orientation = Direction.YFIRST;

	public void paintBranch(Graphics2D g, int x1, int y1, int x2, int y2, Stroke stroke, Color color) {
		Stroke origStroke = g.getStroke();
		Color origColor = g.getColor();
		g.setStroke(stroke);
		g.setColor(color);
		
		if (orientation == Direction.YFIRST) {
			g.drawLine(x1, y1, x1, y2);
			g.drawLine(x1, y2, x2, y2);
		}
		else {
			g.drawLine(x1, y1, x2, y1);
			g.drawLine(x2, y1, x2, y2);
		}
		g.setStroke(origStroke);
		g.setColor(origColor);
	}


	public void setOrientation(Direction orientation) {
		this.orientation = orientation;
	}

}
