package newgui.app;


import java.awt.EventQueue;
import java.util.Properties;

import newgui.gui.ViewerWindow;

public class ViewerApp {

	static ViewerApp pipelineApp;
	
	protected ViewerWindow window;
	
	public static void showMainWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Properties props = loadProperties(); 
					ViewerWindow window = new ViewerWindow();
					window.setVisible(true);
				} catch (Exception e) {
					System.err.println("Caught exception : " + e);
					e.printStackTrace();
				}
			}
		});
		
	}
	
	/**
	 * Load some basic properties from a file...
	 * @return
	 */
	private static Properties loadProperties() {
		return null;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		showMainWindow();
	}
	
}

