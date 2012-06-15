package newgui.gui.display.resultsDisplay;

import logging.BreakpointDensity;
import logging.BreakpointLocation;
import logging.ConsensusTreeLogger;
import logging.PopSizeLogger;
import logging.PropertyLogger;
import logging.RootHeightDensity;

/**
 * Creates LoggerElementConverters that can convert PropertyLoggers of various types
 * @author brendan
 *
 */
public class LoggerConverterFactory {

	public static LoggerElementConverter getConverter(Class<? extends PropertyLogger> clz) {

		if (clz.equals(BreakpointDensity.class)) {
			return new BPDensityConverter();
		}

		if (clz.equals(BreakpointLocation.class)) {
			return new BPLocationConverter();
		}

		if (clz.equals(PopSizeLogger.class)) {
			return new PopSizeConverter();
		}
		
		if (clz.equals(ConsensusTreeLogger.class)) {
			return new ConsensusTreeConverter();
		}

		if (clz.equals(RootHeightDensity.class)) {
			return new RootHeightDensityConverter();
		}

		return null;
	}
}
