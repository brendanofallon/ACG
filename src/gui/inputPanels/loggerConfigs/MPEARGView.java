package gui.inputPanels.loggerConfigs;

import gui.inputPanels.Configurator.InputConfigException;

public class MPEARGView extends AbstractLoggerView {

	
	public MPEARGView() {
		this(new MPEARGModel());
	}
	
	public MPEARGView(LoggerModel model) {
		super(model);
		filenameField.setText("MPEARG.xml");
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		
	}

	@Override
	public String getName() {
		return "Most likely ARG";
	}

	@Override
	public String getDescription() {
		return "ARG found with greatest probability given data";
	}

	
	
}
