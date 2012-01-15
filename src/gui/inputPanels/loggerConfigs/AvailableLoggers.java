package gui.inputPanels.loggerConfigs;

import java.util.ArrayList;
import java.util.List;

import newgui.gui.modelViews.loggerViews.DefaultLoggerView;

/**
 * Stores a list of all (user-ready) logger models that can be added to an analysis 
 * @author brendan
 *
 */
public class AvailableLoggers {

	private static List<LoggerModel> models = new ArrayList<LoggerModel>();
	
	/**
	 * Right now available loggers are just hardcoded in here.. might be nice
	 * to do something more modular at some point (read 'em from a file?)
	 */
	static {
		models.add(new StateLoggerModel());
		models.add(new BPDensityModel());
		models.add(new BPLocationModel());
		models.add(new RootHeightModel());
		models.add(new ConsensusTreeModel());
		models.add(new MPEARGModel());
	}
	
	public static List<LoggerModel> getLoggers() {
		return models;
	}
	
	/**
	 * Create a NEW view suitable for the given logger model.
	 * Once again, this is just hardcoded in here for now... probably a more
	 * elegant way somehow 
	 * @param model
	 * @return
	 */
	public static AbstractLoggerView createViewForModel(LoggerModel model) {
		if (model instanceof StateLoggerModel) {
			return new StateLoggerView( (StateLoggerModel)model);
		}
		if (model instanceof BPDensityModel) {
			return new BPDensityView( (BPDensityModel)model);
		}
		if (model instanceof BPLocationModel) {
			return new BPLocationView( (BPLocationModel)model);
		}
		if (model instanceof RootHeightModel) {
			return new RootHeightView( (RootHeightModel)model);
		}
		if (model instanceof ConsensusTreeModel) {
			return new ConsensusTreeView( (ConsensusTreeModel)model);
		}
		if (model instanceof MPEARGModel) {
			return new MPEARGView( (MPEARGModel)model);
		}
		
		throw new IllegalArgumentException("Could not find a suitable view for logger model: " + model.getModelLabel() );
	}
	

}
