package newgui.app;


import java.awt.EventQueue;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.util.Properties;

import newgui.gui.ViewerWindow;

/**
 * The main application class. Right now all we do is try to load some properties and then 
 * show the 'ViewerWindow' by running it on the event dispatch thread 
 * @author brendan
 *
 */
public class ViewerApp {

	static ViewerApp acgApp;
	protected ViewerWindow window;
	protected static String defaultDataDir = ".acgdata";
	protected static String defaultPropsFilename = "acg_properties.dat"; 
	
	public static void showMainWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					loadProperties(); 
					final ViewerWindow window = new ViewerWindow();
					window.addWindowListener(new WindowAdapter() {
						@Override
						public void windowClosed(WindowEvent arg0) {
							shutdown();
						}
					});
					window.setVisible(true);
				} catch (Exception e) {
					System.err.println("Caught exception : " + e);
					e.printStackTrace();
				}
			}
		});
		
	}
	
	public static void shutdown() {
		
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
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		showMainWindow();
	}
	
}

