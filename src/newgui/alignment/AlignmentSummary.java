package newgui.alignment;


import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import newgui.gui.ViewerWindow;

public class AlignmentSummary extends JPanel {

	private Font font = ViewerWindow.sansFont.deriveFont(12f);
	private final CompressedAlignment aln;
	private String title;
	private static final Color darkColor = new Color(0.85f, 0.85f, 0.85f);
	private static final Color lightColor = new Color(1f, 1f, 1f);
	
	public AlignmentSummary(String title, Alignment aln) {
		this.title = title;
		if (aln instanceof CompressedAlignment) {
			this.aln = (CompressedAlignment)aln;
		}
		else {
			this.aln = new CompressedAlignment(aln);
		}
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		add(Box.createRigidArea(new Dimension(200, 70)));
		//this.setPreferredSize(new Dimension())
		this.setMaximumSize(new Dimension(300, 100));
		computeStats();
		
	}
	
	private void computeStats() {
		// TODO Auto-generated method stub
		
	}

	public Alignment getAlignment() {
		return aln;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

		 GradientPaint  gp = new GradientPaint(0, 0, lightColor, 0, getHeight(), darkColor);
		g2d.setPaint(gp);
		g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
		
		g2d.setColor(Color.DARK_GRAY);
		g2d.setFont(font.deriveFont(Font.BOLD));
		int titleLength = g2d.getFontMetrics().stringWidth(title);
		g2d.drawString(title, getWidth()/2 - titleLength/2, 18);
		
		g2d.setFont(font);
		g2d.drawString("Sequences: " + aln.getSequenceCount(), 6, 35);
		g2d.drawString("Length: " + aln.getSequenceLength(), 6, 53);
		
		g2d.setColor(Color.LIGHT_GRAY);
		g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 20, 20);
		
	}
	
	
}
