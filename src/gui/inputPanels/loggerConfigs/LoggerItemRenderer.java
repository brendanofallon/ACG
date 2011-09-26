package gui.inputPanels.loggerConfigs;

import gui.widgets.RoundedPanel;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

public class LoggerItemRenderer extends RoundedPanel {

	AddLoggerFrame frame;
	AbstractLoggerView config;
	JLabel nameLabel;
	JButton addButton;
	
	public LoggerItemRenderer(AbstractLoggerView config, AddLoggerFrame frame) {
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

	public void addLogger() {
		frame.addLoggerToPanel(config);
		frame.setVisible(false);
	}
}

