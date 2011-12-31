package newgui.app;


import java.awt.EventQueue;

import newgui.gui.ViewerWindow;

public class ViewerApp {

	static ViewerApp pipelineApp;
	
	protected ViewerWindow window;
	
	public static void showMainWindow() {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ViewerWindow window = new ViewerWindow();
					window.setVisible(true);
				} catch (Exception e) {
					System.err.println("Caught exception : " + e);
					e.printStackTrace();
				}
			}
		});
		
	}
	
	private void loadProperties() {
		
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		showMainWindow();
	}
	
}

