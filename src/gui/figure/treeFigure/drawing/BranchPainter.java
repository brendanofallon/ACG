package gui.figure.treeFigure.drawing;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Stroke;

public interface BranchPainter {

	enum Direction {XFIRST, YFIRST};
	
	public void setOrientation(Direction orientation);
	
	public void paintBranch(Graphics2D g, int x1, int y1, int x2, int y2, Stroke stroke, Color color);
	
}
