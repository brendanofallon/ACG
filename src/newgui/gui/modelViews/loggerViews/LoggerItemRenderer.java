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

import gui.ErrorWindow;
import gui.widgets.RoundedPanel;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import newgui.gui.modelViews.AddLoggersFrame;
import newgui.gui.modelViews.DefaultLoggerView;

/**
 * This draws a representation of the given logger view in an AddLoggerFrame 
 * @author brendan
 *
 */
public class LoggerItemRenderer extends RoundedPanel {

	final AddLoggersFrame frame;
	final DefaultLoggerView config;
	JLabel nameLabel;
	JButton addButton;
	
	public LoggerItemRenderer(DefaultLoggerView config, AddLoggersFrame frame) {
		setLayout(new BoxLayout(this.getMainPanel(), BoxLayout.X_AXIS));
		this.getMainPanel().setOpaque(false);
		this.frame = frame;
		this.config = config;
		JPanel leftPanel = new JPanel();
		leftPanel.setLayout(new BoxLayout(leftPanel, BoxLayout.Y_AXIS));
		leftPanel.setOpaque(false);
		
		nameLabel = new JLabel( config.getName() );
		nameLabel.setToolTipText( config.getDescription() );
		nameLabel.setMinimumSize(new Dimension(250, 10));
		nameLabel.setPreferredSize(new Dimension(250, 24));
		nameLabel.setMaximumSize(new Dimension(250, 500));
		leftPanel.add(nameLabel);
		
		JLabel descLabel = new JLabel("<html><i>" + config.getDescription() + "</i></html>");
		leftPanel.add(descLabel);
		add(leftPanel);
		add(Box.createHorizontalGlue());
		addButton = new JButton("Add");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLogger();
			}
		});
		add(addButton);
	}

	/**
	 * Adds a CLONE of this type of logger to the logger panel
	 */
	public void addLogger() {
		Class<DefaultLoggerView> loggerClass = null;
		DefaultLoggerView newView = null;
		try {
			loggerClass = (Class<DefaultLoggerView>) config.getClass();
			newView = loggerClass.newInstance();
		}
		catch (Exception ex) {
			System.out.println("Whoa, couldn't do that cast : " + ex);
		}
		
		if (newView != null)
			frame.addLoggerToPanel(newView);
		else {
			ErrorWindow.showErrorWindow(new Exception("Could not create a logger of type : " + config.getModel().getLoggerClass()));
		}
		frame.setVisible(false);
	}
}

