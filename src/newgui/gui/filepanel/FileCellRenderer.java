package newgui.gui.filepanel;


import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JTree;
import javax.swing.tree.DefaultTreeCellRenderer;

import newgui.UIConstants;
import newgui.gui.ViewerWindow;

public class FileCellRenderer extends DefaultTreeCellRenderer {

	private Font font = ViewerWindow.sansFont.deriveFont(12f);
	//public static Color selectionColor = javax.swing.UIManager.getColor( "Tree.selectionBackground" );
	private static Color topColor = new Color(0.98f, 0.98f, 0.5f);
	private static Color bottomColor = new Color(1.0f, 1.0f, 0.0f);
	private GradientPaint gradient = new GradientPaint(0, 0, topColor, 0, 30, bottomColor);
	private boolean selected = false;
	
	public FileCellRenderer() {
		setMinimumSize(new Dimension(10, 40));
		setBackgroundNonSelectionColor(new Color(0, 0, 0, 0)); //Make this thing transparent
		//this.setOpaque(false);
		this.setBackground(new Color(0, 0, 0, 0));
	}
	
//	public Component getTreeCellRendererComponent(JTree tree,
//            Object value,
//            boolean selected,
//            boolean expanded,
//            boolean leaf,
//            int row,
//            boolean hasFocus) {
//	
//		this.selected = selected;
//		this.setText(value.toString().replace(".xml", ""));
//		return super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
//	}
	
	public void paintComponent(Graphics g) {
		super.paintComponent(g);
//		Graphics2D g2d = (Graphics2D)g;
//		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
//				RenderingHints.VALUE_ANTIALIAS_ON);
//		
//		g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
//				RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
//		
//		if (UIConstants.isMac() && selected) {
//			GradientPaint gp;
//				gp = new GradientPaint(1, 0, new Color(0f, 0f, 1f, 0.1f), 3, getHeight(), new Color(0f, 0f, 1f, 0.5f));
//			g2d.setPaint(gp);
//			g.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 5, 5);
//			
//			g.setColor(new Color(0f, 0f, 1f, 0.5f));
//			g.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 5, 5);
//		}
//		
//		g.setColor(new Color(1.0f, 1.0f, 1.0f, 0.5f));
//		g.drawString(getText(), 2, getHeight()-2);
//		g.setColor(Color.DARK_GRAY);
//		g.drawString(getText(), 1, getHeight()-3);
		
	}
}
