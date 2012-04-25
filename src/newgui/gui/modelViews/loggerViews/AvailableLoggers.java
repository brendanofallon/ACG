package newgui.gui.modelViews.loggerViews;

import gui.inputPanels.loggerConfigs.BPDensityModel;
import gui.inputPanels.loggerConfigs.BPLocationModel;
import gui.inputPanels.loggerConfigs.ConsensusTreeModel;
import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.inputPanels.loggerConfigs.MPEARGModel;
import gui.inputPanels.loggerConfigs.PopSizeLoggerModel;
import gui.inputPanels.loggerConfigs.RootHeightModel;
import gui.inputPanels.loggerConfigs.StateLoggerModel;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;


/**
 * Stores a list of all (user-ready) logger models that can be added to an analysis. This
 * thing also provides a static method for obtaining a DefaultLoggerView suitable
 * for rendering a given loggermodel (createDefaultViewForModel).
 * Like many classes in this package, there is an equivalent (though now outdated) 
 * version of this class in the gui.input panels 
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
		models.add(new PopSizeLoggerModel());
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
	public static DefaultLoggerView createDefaultViewForModel(LoggerModel model) {
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
		if (model instanceof PopSizeLoggerModel) {
			return new PopSizeView( (PopSizeLoggerModel)model);
		}
		
		throw new IllegalArgumentException("Could not find a suitable view for logger model: " + model.getModelLabel() );
	}
	
	/**
	 * Create a new model of the same class as the given model. Does not copy data stored in the model.
	 * @param model
	 * @return
	 */
	public static LoggerModel createModel(LoggerModel model) {
		if (model instanceof StateLoggerModel) {
			return new StateLoggerModel();
		}
		if (model instanceof BPDensityModel) {
			return new BPDensityModel();
		}
		if (model instanceof BPLocationModel) {
			return new BPLocationModel();
		}
		if (model instanceof RootHeightModel) {
			return new RootHeightModel();
		}
		if (model instanceof ConsensusTreeModel) {
			return new ConsensusTreeModel();
		}
		if (model instanceof MPEARGModel) {
			return new MPEARGModel();
		}
		if (model instanceof PopSizeLoggerModel) {
			return new PopSizeLoggerModel();
		}
		
		throw new IllegalArgumentException("Could not find a suitable view for logger model: " + model.getModelLabel() );
	}
}
