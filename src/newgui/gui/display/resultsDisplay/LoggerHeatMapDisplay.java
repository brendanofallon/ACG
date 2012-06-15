package newgui.gui.display.resultsDisplay;

import gui.figure.TextElement;
import gui.figure.VerticalTextElement;
import gui.figure.heatMapFigure.ColorBarElement;
import gui.figure.heatMapFigure.HeatMapElement;
import gui.figure.heatMapFigure.HeatMapFigure;
import newgui.UIConstants;

public class LoggerHeatMapDisplay extends LoggerResultDisplay {

	@Override
	public void initializeFigure() {
		fig = new HeatMapFigure();
		HeatMapFigure heatMapFig = (HeatMapFigure)fig;

		heatMapFig.setYAxisLabel("Time in past (subs./ site)");
		heatMapFig.setXAxisLabel("Site");	
	}
	
	public void setData(double[][] matrix, double xMin, double xMax, double yMin, double yMax, double maxDensity) {

		HeatMapFigure heatMapFig = (HeatMapFigure)fig;
		
		heatMapFig.setXMax(xMax);
		heatMapFig.setXMin(xMin);
		heatMapFig.setYMax(yMax);
		heatMapFig.setYMin(yMin);
		heatMapFig.setData(matrix);
		heatMapFig.setHeatMax(maxDensity);
		heatMapFig.setHeatMin(0);
		repaint();
	}

	@Override
	public void showConfigFrame() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected String getDataString() {
		StringBuilder str = new StringBuilder();
		double[][] matrix = ((HeatMapFigure)fig).getData();
		
		for(int i=0; i<matrix.length; i++) {
			for(int j=0; j<matrix[i].length; j++) {
				str.append(matrix[i] + ",");	
			}
			str.append(System.getProperty("line.separator"));
			
		}
		return str.toString();
	}

}
