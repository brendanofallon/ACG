package gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

public class ACGFrame extends JFrame {
	
	public ACGFrame( /* might be nice to get some properties here */ ) {
		
        try {
        	String plaf = UIManager.getSystemLookAndFeelClassName();
        	String gtkLookAndFeel = "com.sun.java.swing.plaf.gtk.GTKLookAndFeel";
        	//Attempt to avoid metal look and feel if possible
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
		
	}
	
	
	private void initComponents() {
		BorderLayout layout = new BorderLayout();
		Container mainContainer = this.getContentPane();
		mainContainer.setLayout(layout);
		
		JPanel centerPanel = new StartFrame();
		mainContainer.add(centerPanel, BorderLayout.CENTER);
		
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.add(new JLabel("Bottom panel"));
		mainContainer.add(bottomPanel, BorderLayout.SOUTH);
	}

}
