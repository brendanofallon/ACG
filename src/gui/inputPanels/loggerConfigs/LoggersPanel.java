package gui.inputPanels.loggerConfigs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import gui.inputPanels.Configurator;
import gui.inputPanels.DLConfigurator;
import gui.widgets.BorderlessButton;
import gui.widgets.RoundedPanel;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;


public class LoggersPanel extends RoundedPanel implements Configurator {

	List<LoggerConfigurator> loggers = new ArrayList<LoggerConfigurator>();
	
	JButton addButton;
	
	static final ImageIcon removeIcon = getIcon("icons/removeButton.png");
	
	public LoggersPanel() {
		this.setOpaque(false);
		
		this.getMainPanel().setLayout(new BoxLayout(getMainPanel(), BoxLayout.Y_AXIS));
		this.getMainPanel().setOpaque(false);
		
		addFrame = new AddLoggerFrame(this);
		addFrame.setVisible(false);
		
		addButton = new JButton("Add logger...");
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showAddFrame();
			}
		});
		add(addButton);
		
		//Must come after above initialization
		addLogger( new StateLoggerConfig() );
	}
	
	protected void showAddFrame() {
		addFrame.setVisible(true);
		
	}
	
	public void addLogger(LoggerConfigurator logger) {
		loggers.add(logger);
		this.remove(addButton);
		add(new LoggerWrapper(logger));
		this.add(addButton);
		revalidate();
		repaint();
	}
	
	public void removeLogger(LoggerConfigurator which) {
		this.remove(which);
		loggers.remove(which);
		revalidate();
		repaint();
	}
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getParameters() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Element[] getLikelihoods() {
		// TODO Auto-generated method stub
		return null;
	}

	private static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			java.net.URL imageURL = LoggersPanel.class.getResource(url);
			System.err.println("URL is : " + imageURL);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			System.err.println("Could not load icon from url: " + url +"\n Message : " + ex);
		}
		return icon;
	}
	
	/**
	 * Something to wrap logger configurators and add a remove button to them
	 * @author brendano
	 *
	 */
	class LoggerWrapper extends JPanel {
		
		public LoggerWrapper(final LoggerConfigurator conf) {
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			setPreferredSize(new Dimension(600, 36));
			setMaximumSize(new Dimension(3200, 36));
			add(conf);
			add(Box.createHorizontalGlue());
			
			BorderlessButton remove = new BorderlessButton(removeIcon);
			remove.setMinimumSize(new Dimension(24, 30));
			remove.setPreferredSize(new Dimension(24, 30));
			
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					removeLogger(conf);
				}
			});
			add(remove);
		}
		
		
	}
	
	
	private AddLoggerFrame addFrame;
	
}
