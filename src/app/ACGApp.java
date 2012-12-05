package app;


import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import newgui.gui.ViewerWindow;

/**
 * The main application class. Right now all we do is try to load some properties and then 
 * show the 'ViewerWindow' by running it on the event dispatch thread 
 * @author brendan
 *
 */
public class ACGApp extends ACGApplication {

	public static final Integer VERSION_NUM = 6;
	public static final String VERSION = "0.9-" + VERSION_NUM;
	
	
	public static final String defaultDataDir = ".acgdata";
	public static final String defaultPropsFilename = "acg_properties.dat";
	public static final String defaultLogFilename = "acglog.txt";
	public static final Logger logger = Logger.getAnonymousLogger();
	
	static ACGApp acgApp;
	protected static ViewerWindow viewerWindow;

	public ACGApp() {
		//Must have explicit no-arg constructor
		acgApp = this;
	}
	
	public static ACGApp getApplication() {
		return acgApp;
	}
	
	public static ViewerWindow getViewerWindow() {
		return viewerWindow;
	}
	
	public static void showMainWindow() {
		
		if (System.getProperty("os.name").contains("Mac")) {
				logger.info("Detected macintosh operating system, adding mac-specific application stuff");
			try {
				new MacAdapter();
			}
			catch (Exception ex){
				logger.warning("Error creating Mac-specific application handlers : " + ex);
			}
			
		}
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					loadProperties(); 
					final ViewerWindow window = new ViewerWindow();
					viewerWindow = window; 
					window.addWindowListener(new WindowAdapter() {
						
						public void windowClosing(WindowEvent arg0) {
							shutdown();
						}

					});
					window.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	/**
	 * Right now this just adds a FileHandler to the main logger 
	 */
	public static void initializeLoggers() {
		String baseDir = System.getProperty("user.dir");
		String fileSep = System.getProperty("file.separator");

		File logFile = new File(baseDir + fileSep + defaultDataDir + fileSep + defaultLogFilename);
		try {
			FileHandler handler = new FileHandler(logFile.getAbsolutePath());
			logger.addHandler(handler);

			logger.info("Starting up version " + VERSION_NUM);
		} catch (SecurityException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	public static void shutdown() {
		try {
			ACGProperties.addProperty(ViewerWindow.WINDOW_WIDTH, ViewerWindow.getViewer().getWidth() + "");
			ACGProperties.addProperty(ViewerWindow.WINDOW_HEIGHT, ViewerWindow.getViewer().getHeight() + "");
			ACGProperties.writeToLastFileRead();
			logger.info("Shutdown");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Load some basic properties from a file...
	 * @return
	 */
	private static void loadProperties() {
		//Look for properties in the current directory, then one dir up, then in the users home dir
		String path = System.getProperty("user.dir");
		String fileSep = System.getProperty("file.separator");
		File propsFile = new File(path + fileSep + defaultDataDir + fileSep + defaultPropsFilename);
		if (propsFile.exists()) {
			try {
				new ACGProperties(propsFile);
				return;
			} catch (IOException e) {
				//Hmm, error reading usual properties file... try another
				e.printStackTrace();
			}
		}
		
		path = propsFile.getParentFile().getParent();
		propsFile = new File(path + fileSep + defaultDataDir + fileSep + defaultPropsFilename);

		if (propsFile.exists()) {
			try {
				new ACGProperties(propsFile);
				return;
			} catch (IOException e) {
				//Hmm, error reading usual properties file... try another
				e.printStackTrace();
			}
		}
		
		
		//Hmm, try the users home dir
		path = System.getProperty("user.home");
		propsFile = new File(path + fileSep + defaultDataDir + fileSep + defaultPropsFilename);

		if (propsFile.exists()) {
			try {
				new ACGProperties(propsFile);
				return;
			} catch (IOException e) {
				//Hmm, error reading usual properties file... try another
				e.printStackTrace();
			}
		}
		
		System.err.println("Warning: Could not read properties file");
	}
	
	public void startup(String[] args) {
		initializeLoggers();
		showMainWindow();	
	}
	
	public Integer getVersionNum() {
		return VERSION_NUM;
	}

	public static void main(String[] args) {
		(new ACGApp()).startup(args);
	}


}

