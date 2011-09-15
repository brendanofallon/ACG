package gui.inputPanels.loggerConfigs;

import gui.inputPanels.Configurator;

import javax.swing.JPanel;

import org.w3c.dom.Element;

/**
 * Base class for individual logger configurators that appear in the loggers panel. Each one of these
 * corresponds to a single type of logger - StateLogger, Breakpoint Density logger, etc.
 * @author brendano
 *
 */
public abstract class LoggerConfigurator extends JPanel implements Configurator {
	
	//Reference to ARG object that may be used by this logger
	String ARGref = null;
	
	//Do we need references to other model elements?
	
	/**
	 * Set the ARG element that this logger may (optionally) reference
	 * @param ARGref
	 */
	public void setARG(Element ARGref) {
		this.ARGref = ARGref.getNodeName();
	}
	
	/**
	 * Return the name of the logger (eg "State Logger" or "TMRCA logger")
	 */
	public abstract String getName();
	
	/**
	 * A brief description of the logger, suitable for use in a tool-tip  description
	 */
	public abstract String getDescription();
	
	
	
	/// Loggers don't have any parameters or likelihoods associated with them, so these are implemented as no-ops here
	
	@Override
	public Element[] getParameters() {
		return new Element[]{};
	}

	@Override
	public Element[] getLikelihoods() {
		return new Element[]{};
	}
	
}
