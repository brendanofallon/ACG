package gui.inputPanels.loggerConfigs;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class LoggerItemRenderer extends JPanel {

	AddLoggerFrame frame;
	LoggerConfigurator config;
	JLabel nameLabel;
	JButton addButton;
	
	public LoggerItemRenderer(LoggerConfigurator config, AddLoggerFrame frame) {
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.frame = frame;
		this.config = config;
		nameLabel = new JLabel( config.getName() );
		nameLabel.setToolTipText( config.getDescription() );
		nameLabel.setMinimumSize(new Dimension(250, 10));
		nameLabel.setPreferredSize(new Dimension(250, 24));
		nameLabel.setMaximumSize(new Dimension(250, 500));
		add(nameLabel);
		
		addButton = new JButton("Add logger");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				addLogger();
			}
		});
		add(addButton);
	}

	public void addLogger() {
		frame.addLoggerToPanel(config);
		frame.setVisible(false);
	}
}

