package gui;

import gui.widgets.BorderlessButton;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;

/**
 * The toolbar at the top of the main application panel with the load / new / save/ run buttons
 * @author brendano
 *
 */
public class TopToolBar extends JPanel {
	
	static final ImageIcon acgImage = ACGFrame.getIcon("icons/acgImage.png");
	static final Color topColor = new Color(0.99f, 0.99f, 0.99f);
	static final Color bottomColor = new Color(0.80f, 0.80f, 0.80f);
	
	private ACGFrame acgParent;
	
	public TopToolBar(ACGFrame acgParent) {
		this.acgParent = acgParent;
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		makeButtons();
	}
	
	
	
	private void makeButtons() {

		newButton = new BorderlessButton("New", ACGFrame.getIcon("icons/addIcon.png"));
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				newButtonPressed();
			}
		});
		newButton.setToolTipText("Create a new analysis");
		
		loadButton = new BorderlessButton("Load", ACGFrame.getIcon("icons/upArrow.png"));
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				loadButtonPressed();
			}
		});
		loadButton.setToolTipText("Load from file");
		
		saveButton = new BorderlessButton("Save", ACGFrame.getIcon("icons/downArrow.png"));
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				saveButtonPressed();
			}
		});
		saveButton.setToolTipText("Save this analysis");
		
		runButton = new BorderlessButton("Run", ACGFrame.getIcon("icons/rightArrow.png"));
		runButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				runButtonPressed();
			}
		});
		runButton.setToolTipText("Run with current settings");
		
		add(Box.createRigidArea(new Dimension(20, 40)));
		add(newButton);
		add(loadButton);
		add(saveButton);
		add(runButton);
		add(Box.createHorizontalGlue());
	
	}
	
	protected void runButtonPressed() {
	//	acgParent.startNewRun();
	}



	protected void saveButtonPressed() {
		// TODO Auto-generated method stub
	}



	protected void loadButtonPressed() {
		acgParent.loadDocumentFromFile();
	}



	protected void newButtonPressed() {
		acgParent.clearAndMakeNew();
	}



	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		GradientPaint gp = new GradientPaint(0, 0, topColor, 0, getHeight(), bottomColor);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setPaint(gp);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		
		if (acgImage != null) {
			g.drawImage(acgImage.getImage(), Math.max(100, getWidth()-acgImage.getIconWidth()-10), 5, null);
		}
	}
	
	private BorderlessButton runButton;
	private BorderlessButton saveButton;
	private BorderlessButton loadButton;
	private BorderlessButton newButton;
	

}
