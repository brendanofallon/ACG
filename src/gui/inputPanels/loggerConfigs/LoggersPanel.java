package gui.inputPanels.loggerConfigs;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import gui.document.ACGDocument;
import gui.inputPanels.ARGModelElement;
import gui.inputPanels.Configurator.InputConfigException;
import gui.widgets.BorderlessButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import org.w3c.dom.Element;
import org.w3c.dom.Node;


public class LoggersPanel extends JPanel {

	List<LoggerWrapper> loggers = new ArrayList<LoggerWrapper>();
	
	JButton addButton;
	
	//Reference to ARG object that may be used by loggers
	//This implementation sucks because what if different loggers want to reference different ARGs?
	//Maybe there should be one logger panel per ARG? per alignment? 
	ARGModelElement ARGref = null;
	
	static final ImageIcon removeIcon = getIcon("icons/removeButton.png");
	
	public LoggersPanel() {
		this.setOpaque(false);
		
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setOpaque(false);
		
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
		addLogger( new StateLoggerView(new StateLoggerModel()) );
	}
	
	protected void showAddFrame() {
		addFrame.setVisible(true);
		
	}
	
	public void setARGReference(ARGModelElement argRef) {
		this.ARGref = argRef;
	}
	
	public void addLogger(AbstractLoggerView logger) {
		LoggerWrapper wrapped = new LoggerWrapper(logger);
		loggers.add(wrapped);
		this.remove(addButton);
		add(wrapped);
		this.add(addButton);
		revalidate();
		repaint();
	}
	
	public void removeLogger(AbstractLoggerView which) {
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
	
	
	public List<Element> getLoggerNodes(ACGDocument doc) throws InputConfigException {
		List<Element> nodes = new ArrayList<Element>();
		
		for(LoggerWrapper logger : loggers) {
			AbstractLoggerView view = logger.config;
			view.updateFields();
			view.getModel().setArgRef( ARGref );
			Element loggerElement;
			loggerElement = view.getModel().getElement(doc);
			nodes.add( loggerElement );
		}
		
		return nodes;
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
	
	public void readNodesFromDocument(ACGDocument doc) {
		
	}
	
	/**
	 * Something to wrap logger views and add a remove button to them
	 * @author brendano
	 *
	 */
	class LoggerWrapper extends JPanel {
		
		AbstractLoggerView config;
		
		public LoggerWrapper(final AbstractLoggerView conf) {
			this.config = conf;
			setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
			setOpaque(false);
			setPreferredSize(conf.getPreferredDimensions());
			setMaximumSize(conf.getPreferredDimensions());
			conf.setAlignmentY(TOP_ALIGNMENT);
			add(conf);
			add(Box.createHorizontalGlue());
			
			BorderlessButton remove = new BorderlessButton(removeIcon);
			remove.setAlignmentY(TOP_ALIGNMENT);
			remove.setToolTipText("Remove " + conf.getModel().getModelLabel() );
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
