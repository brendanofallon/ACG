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

	List<LoggerWrapper> loggers = new ArrayList<LoggerWrapper>();
	
	JButton addButton;
	
	//Reference to ARG object that may be used by loggers
	//This implementation sucks because what if different loggers want to reference different ARGs?
	//Maybe there should be one logger panel per ARG? per alignment? 
	Element ARGref = null;
	
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
	
	public void setARGReference(Element ARGref) {
		this.ARGref = ARGref;
	}
	
	public void addLogger(LoggerConfigurator logger) {
		LoggerWrapper wrapped = new LoggerWrapper(logger);
		loggers.add(wrapped);
		this.remove(addButton);
		add(wrapped);
		this.add(addButton);
		revalidate();
		repaint();
	}
	
	public void removeLogger(LoggerConfigurator which) {
		System.out.println("Removing logger : " + which.getName());
		LoggerWrapper toRemove = null;
		for(LoggerWrapper logger : loggers) {
			if (logger.config == which) {
				toRemove = logger;
			}
		}
		
		if (toRemove != null) {
			remove(toRemove);
			loggers.remove(toRemove);				
		}
		revalidate();
		repaint();
	}
	
	
	@Override
	public Element[] getRootXMLNodes(Document doc)
			throws ParserConfigurationException, InputConfigException {
		List<Element> elements = new ArrayList<Element>();
		
		for(LoggerWrapper logger : loggers) {
			LoggerConfigurator conf = logger.config;
			conf.setARG( ARGref );
			Element[] nodes = conf.getRootXMLNodes(doc);
			if (nodes != null) {
				for(int i=0; i<nodes.length; i++) {
					elements.add(nodes[i]);
				}
			}
		}
		
		Element[] els = new Element[elements.size()];
		els = elements.toArray(els);
		return els;
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
		
		LoggerConfigurator config;
		
		public LoggerWrapper(final LoggerConfigurator conf) {
			this.config = conf;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			setPreferredSize(new Dimension(600, 36));
			setMaximumSize(new Dimension(3200, 36));
			add(conf);
			add(Box.createHorizontalGlue());
			
			BorderlessButton remove = new BorderlessButton(removeIcon);
			remove.setToolTipText("Remove logger");
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
