/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package newgui.gui.modelViews.loggerViews;

import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.loggerConfigs.LoggerModel;
import gui.inputPanels.loggerConfigs.MPEARGModel;

public class MPEARGView extends DefaultLoggerView {

	
	public MPEARGView() {
		this(new MPEARGModel());
	}
	
	public MPEARGView(LoggerModel model) {
		super(model);
		loggerLabelField.setText("MPEARG.xml");
		updateView();
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
