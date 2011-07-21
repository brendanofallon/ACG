package gui;

import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class ACGFrame extends JFrame {
	
	public ACGFrame() {
		
        try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel
        	if (plaf.contains("metal")) {

        		UIManager.setLookAndFeel(gtkLookAndFeel);
        	}

        	UIManager.setLookAndFeel( plaf );
        }
        catch (Exception e) {
            System.err.println("Could not set look and feel, exception : " + e.toString());
        }
        
		initComponents();
		setPreferredSize(new Dimension(400, 400));
		setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		setLocationRelativeTo(null);
		pack();
		setVisible(true);
	}

}
