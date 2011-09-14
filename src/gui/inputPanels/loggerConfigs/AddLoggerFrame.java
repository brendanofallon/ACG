package gui.inputPanels.loggerConfigs;

import java.awt.BorderLayout;
import java.util.ArrayList;
import java.util.List;


import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AddLoggerFrame extends JFrame {

	LoggersPanel parentPanel;
	List<LoggerConfigurator> loggerList = new ArrayList<LoggerConfigurator>();
	
	
	public AddLoggerFrame(LoggersPanel parentPanel) {
		this.parentPanel = parentPanel;
		this.setLayout(new BorderLayout());
				
		//Build list of loggers
		importLoggers();
		
		JPanel listPanel = new JPanel();
		listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane(listPanel);
		
		for(LoggerConfigurator logger : loggerList) {
			LoggerItemRenderer renderer = new LoggerItemRenderer(logger, this);
			listPanel.add(renderer);
		}
		
		add(scrollPane, BorderLayout.CENTER);
		pack();
		setLocationRelativeTo(parentPanel);
		setVisible(false);
	}

	/**
	 * Build the list of available loggers to be represented in this frame
	 */
	private void importLoggers() {
		loggerList.add(new StateLoggerConfig() );
		loggerList.add(new BPDensityConfig() );
		loggerList.add(new BPLocationConfig() );
	}

	/**
	 * Adds the given logger to the LoggersPanel associated with this frame
	 * @param config
	 */
	public void addLoggerToPanel(LoggerConfigurator config) {
		parentPanel.addLogger(config);
	}
	
	
}
