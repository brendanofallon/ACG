package newgui.gui.widgets.fancyTabPane;

import gui.figure.treeFigure.DrawableTree;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.MultipleGradientPaint.CycleMethod;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import newgui.UIConstants;
import newgui.gui.ViewerWindow;
import newgui.gui.widgets.BorderlessButton;


public class FancyTab extends JPanel {

	private String text;
	private boolean selected = false;
	private static final Font normalFont = ViewerWindow.sansFont.deriveFont(12f);
	//private static final Font selectedFont = ViewerWindow.sansFont.deriveFont(Font.BOLD);
	private List<ChangeListener> listeners = new ArrayList<ChangeListener>();

	//A few constants for painting
	private static final Color darkColor = new Color(0.85f, 0.85f, 0.85f);
	private static final Color lighterColor = new Color(0.95f, 0.95f, 0.99f);
	private static final Color lightColor = new Color(1f, 1f, 1f);
	
	private static final ImageIcon closeIcon = UIConstants.grayCloseButton;
	
	private FTabPane parentPane;
	
	public FancyTab(FTabPane parentPane, String label) {
		this.text = label;
		this.parentPane = parentPane;
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		int targetSize = Math.max(40, label.length()*7 + 35); //Preferred width of tab
		this.setPreferredSize(new Dimension(targetSize , 30));
		this.setMinimumSize(new Dimension(20, 30));
		this.setMaximumSize(new Dimension(targetSize, 40));
		setFont(normalFont);
		
		this.add(Box.createHorizontalStrut(20));
		this.add(Box.createHorizontalGlue());
		BorderlessButton closeButton = new BorderlessButton(closeIcon);
		closeButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
//		closeButton.setYDif(-2);
//		closeButton.setXDif(-2);
//		closeButton.setPreferredSize(new Dimension(20, 18));
		closeButton.setMaximumSize(new Dimension(24, 24));
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				closeThisTab();
			}
		});
		this.add(closeButton);
		this.add(Box.createHorizontalStrut(1));
		
		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				fireChangeEvent();
			}

			@Override
			public void mousePressed(MouseEvent e) { }

			@Override
			public void mouseReleased(MouseEvent e)  {	}

			@Override
			public void mouseEntered(MouseEvent e) { }

			public void mouseExited(MouseEvent e) { }
			
		});
	}
	
	protected void closeThisTab() {
		parentPane.removeComponentForTab(this);
	}

	public boolean isSelected() {
		return selected;
	}
	
	public void addListener(ChangeListener l) {
		this.listeners.add(l);
	}
	
	public void removeListener(ChangeListener l) {
		listeners.remove(l);
	}
	
	private void fireChangeEvent() {
		ChangeEvent evt = new ChangeEvent(this);
		for(ChangeListener l : listeners) {
			l.stateChanged(evt);
		}
	}
	
	public String getLabel() {
		return text;
	}
	
	public void setSelected(boolean selected) {
		this.selected = selected;
		
		if (selected) {
		//	setFont(selectedFont);
		}
		else {
			//setFont(normalFont);
		}
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	
	     GradientPaint gp;
	     if (selected)
	    	 gp = new GradientPaint(0, 0, lightColor, 0, getHeight(), UIConstants.componentBackground);
	     else
	    	 gp = new GradientPaint(0, 0, lighterColor, 0, getHeight(), darkColor);

	     g.setColor(FTabPane.shadowColor);
	     g2d.setStroke(FTabPane.shadowStroke);
	     g2d.drawRoundRect(5, 4, getWidth()-6, getHeight()+10, 8, 8);
	     
	     g2d.setPaint(gp);
	     g.fillRoundRect(0, 2, getWidth()-3, getHeight()+10, 8, 8);
	     

	     g2d.setStroke(FTabPane.normalStroke);
	     g2d.setColor(FTabPane.gray2);
	     g.drawRoundRect(0, 2, getWidth()-2, getHeight()+10, 10, 10);
	     
	     g2d.setColor(Color.LIGHT_GRAY);
		g.drawRoundRect(0, 2, getWidth()-2, getHeight()+10, 8, 8);
	
		g.setFont(getFont());
		int strWidth = g.getFontMetrics().stringWidth(text);
		
		g2d.setColor(new Color(1f, 1f, 1f, 0.5f));
		g.drawString(text, Math.max(3, getWidth()/2 - strWidth/2 - 7), getHeight()-5);
		
		g2d.setColor(Color.DARK_GRAY);
		g.drawString(text, Math.max(2, getWidth()/2 - strWidth/2 - 8), getHeight()-6);
		
		//g.drawImage(closeIcon.getImage(), getWidth()-closeIcon.getIconWidth()-10, getHeight()/2-closeIcon.getIconHeight()/2, null);
		
	}
}
