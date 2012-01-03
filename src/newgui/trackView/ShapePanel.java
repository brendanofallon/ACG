package newgui.trackView;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;

import javax.swing.JPanel;

public class ShapePanel extends JPanel {

	AffineTransform trans;
	GeneralPath path;
	private boolean first = true;
	private int prevWidth = 1;
	private int prevHeight = 1;
	private double prevZoom = 1.0;
	private double currentZoom = 1.0;
	private int xTranslate = 0; //x shift of image in pixels
	private int yTranslate = 0; //y shift of image, in pixels
	
	public ShapePanel() {
		makePath();
	}
	
	public void makePath() {
		trans = new AffineTransform();
		
		path = new GeneralPath(GeneralPath.WIND_EVEN_ODD);
		path.moveTo(0f, 0f);
//		for(int i=0; i<100000; i++) {
//			path.lineTo((float)i, (float)Math.random());
//		}
		path.lineTo(50, 0);
		path.lineTo(50, 100);
		path.lineTo(0, 100);
		path.lineTo(0, 0);
	}
	
	/**
	 * Set number of pixels to shift shape in x direction
	 * @param x
	 */
	public void setXTranslate(int x) {
		this.xTranslate = x;
		repaint();
	}
	
	/**
	 * Set amount to shift this image in the y direction, in pixels
	 * @param y
	 */
	public void setYTranslate(int y) {
		this.yTranslate = y;
		repaint();
	}
	
	private void rescaleShape() {
		if (first) {
			trans.setToScale(getWidth() / 100000.0, getHeight());	
		}
		else {
			trans.setToScale(currentZoom/prevZoom*(double)getWidth()/(double)prevWidth, (double)getHeight()/(double)prevHeight);
		}
		
		prevZoom = currentZoom;
		path.transform(trans);
		prevWidth = getWidth();
		prevHeight = getHeight();
		first = false;
	}
	
	public void paintComponent(Graphics g) {
		if (first || prevWidth != getWidth() || prevHeight != getHeight()) {
			rescaleShape();
		}
		
		Graphics2D g2d = (Graphics2D)g;
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
	
		PathIterator pit = path.getPathIterator(null);
		float[] begin = new float[2];
		float[] end = new float[2];
		pit.currentSegment(begin);
		while(! pit.isDone()) {
			pit.currentSegment(end);
			pit.next();
		}
		
		g.setColor(Color.blue);
		g2d.translate(xTranslate, yTranslate);
		g2d.draw(path);
	}

	public void setZoomLevel(double value) {
		prevZoom = currentZoom;
		this.currentZoom = value;
		rescaleShape();
		repaint();
	}
}
