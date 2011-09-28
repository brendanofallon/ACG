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


package gui.inputPanels.loggerConfigs;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;


import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import logging.StateLogger;

public class AddLoggerFrame extends JFrame {

	LoggersPanel parentPanel;
	List<AbstractLoggerView> loggerList = new ArrayList<AbstractLoggerView>();
	
	public AddLoggerFrame(LoggersPanel parentPanel) {
		super("Choose loggers");
		this.parentPanel = parentPanel;
		this.setLayout(new BorderLayout());
				
		//Build list of loggers
		importLoggers();
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(listPanel);
		
		for(AbstractLoggerView logger : loggerList) {
			LoggerItemRenderer renderer = new LoggerItemRenderer(logger, this);
			listPanel.add(renderer);
			listPanel.add(Box.createVerticalStrut(3));
		}
		
		add(scrollPane, BorderLayout.CENTER);
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));
		pack();
		setLocationRelativeTo(parentPanel);
		setVisible(false);
	}

	/**
	 * Build the list of available loggers to be represented in this frame
	 */
	private void importLoggers() {
		loggerList.add(new StateLoggerView() );
		loggerList.add(new BPDensityView() );
		loggerList.add(new BPLocationView() );
		loggerList.add(new RootHeightView() );
		loggerList.add(new ConsensusTreeView() );
		loggerList.add(new MPEARGView() );
	}
	
	/**
	 * Obtain the view associated with the given class, or null if there is not one
	 * @param clazz
	 * @return
	 */
	public AbstractLoggerView getViewForClass(String canonicalName) {
		for(AbstractLoggerView view : loggerList){
			if (view.getModel().getLoggerClass().getCanonicalName().equals( canonicalName )) {
				return view;
			}
		}
		return null;
	}

	/**
	 * Adds the given logger to the LoggersPanel associated with this frame
	 * @param config
	 */
	public void addLoggerToPanel(AbstractLoggerView config) {
		parentPanel.addLogger(config);
	}

	
}
