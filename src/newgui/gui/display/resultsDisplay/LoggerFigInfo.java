package newgui.gui.display.resultsDisplay;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import math.Histogram;
import newgui.datafile.resultsfile.XYSeriesInfo;

import org.w3c.dom.Element;

/**
 * Container for information read from results file XML that is used to create a figure
 * and display some additional data regarding a saved results logger 
 * @author brendan
 *
 */
public class LoggerFigInfo {

	 List<XYSeriesInfo> seriesInfo = new ArrayList<XYSeriesInfo>();
	 Map<String, String> properties = new HashMap<String, String>();
	 Histogram histo = null;
	 String title;
	 String xAxisTitle = "x axis title";
	 String yAxisTitle = "y axis title";
	 
	 public List<XYSeriesInfo> getSeriesInfo() {
		 return seriesInfo;
	 }

	 public void setSeriesInfo(List<XYSeriesInfo> seriesInfo) {
		 this.seriesInfo = seriesInfo;
	 }

	 public Map<String, String> getProperties() {
		 return properties;
	 }

	 public void setProperties(Map<String, String> properties) {
		 this.properties = properties;
	 }

	 public Histogram getHisto() {
		 return histo;
	 }

	 public void setHisto(Histogram histo) {
		 this.histo = histo;
	 }

	 public String getTitle() {
		 return title;
	 }

	 public void setTitle(String title) {
		 this.title = title;
	 }

	 public String getxAxisTitle() {
		 return xAxisTitle;
	 }

	 public void setxAxisTitle(String xAxisTitle) {
		 this.xAxisTitle = xAxisTitle;
	 }

	 public String getyAxisTitle() {
		 return yAxisTitle;
	 }

	 public void setyAxisTitle(String yAxisTitle) {
		 this.yAxisTitle = yAxisTitle;
	 }

	 
}
