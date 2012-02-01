package gui.figure.treeFigure.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public class LineBranchPainter implements BranchPainter {

	public void paintBranch(Graphics2D g, int x1, int y1, int x2, int y2,
			Stroke stroke, Color color) {
		Stroke origStroke = g.getStroke();
		Color origColor = g.getColor();
		g.setStroke(stroke);
		g.setColor(color);
		
		g.drawLine(x1, y1, x2, y2);
		
		g.setStroke(origStroke);
		g.setColor(origColor);
	}

	@Override
	public void setOrientation(Direction orientation) {
		//Doesn't do anything. This is OK. 
	}

}
