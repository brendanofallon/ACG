package newgui.gui.widgets.fileBlocks;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Stroke;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class AbstractBlock extends JPanel {

	private boolean isOpen = true;
	private JComponent content = null;
	protected BlocksPanel parentPanel;
	protected BlockHeader header;
	protected JScrollPane mainScrollPane;
	private JComponent mainComp;
	protected int maxBlockHeight = 100;
	
	public AbstractBlock(String label) {
		header = new BlockHeader(this, label);
		initComponents();
	}
	
	public void setParentPanel(BlocksPanel parentPanel) {
		this.parentPanel = parentPanel;
	}
	
	public void setMainComponent(JComponent comp) {
		this.mainComp = comp;
		mainScrollPane.setViewportView(mainComp);
	}
	
	protected void initComponents() {
		setLayout(new BorderLayout());
		setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder(5, 3, 2, 3));
		
		this.add(header, BorderLayout.NORTH);
		
		
		mainScrollPane = new JScrollPane();
		mainScrollPane.setOpaque(false);
		
		JPanel dummyPanel = new JPanel();
		dummyPanel.setLayout(new BorderLayout());
		dummyPanel.add(new JLabel("Main Panel"));
		dummyPanel.setOpaque(false);
		mainScrollPane.setViewportView(dummyPanel);
		mainScrollPane.setViewportBorder(null);
		mainScrollPane.setBorder(null);
		
		if (isOpen) {
			this.add(mainScrollPane, BorderLayout.CENTER);
		}

		this.setMaximumSize(new Dimension(500, maxBlockHeight));
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public void setOpen(boolean open) {
		this.isOpen = open;
		if (isOpen) {
			this.add(mainScrollPane, BorderLayout.CENTER);
			this.setMaximumSize(new Dimension(500, maxBlockHeight));
			parentPanel.blockOpened(this);
		}
		else {
			this.remove(mainScrollPane);
			this.setMaximumSize(new Dimension(header.getSize()));
			parentPanel.blockClosed(this);
		}
		revalidate();
		repaint();
	}	
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		
		g.setColor(shadowColor);
		((Graphics2D)g).setStroke(shadowStroke);
		g.drawRoundRect(4, 4, getWidth()-5, getHeight()-5, 8, 8);
		
		g.setColor(bgColor);
		((Graphics2D)g).setStroke(normalStroke);
		g.fillRoundRect(1, 1, getWidth()-3, getHeight()-2, 5, 5);
	
		//A gradient
		float gradMax = Math.min(200, Math.max( getHeight()/3f, 20));
		g.setColor(gray2);
		g.drawLine(3, 2, getWidth()-4, 2);
		g.setColor(dark1);
		g.drawLine(3, 3, getWidth()-4, 3);
		g.drawLine(2, 4, getWidth()-2, 4);
		for(float i=5; i<gradMax; i++) {
			float newVal = topDark + (0.99f-topDark)*(1-(gradMax-i)/gradMax );
			g.setColor( new Color(newVal, newVal, newVal));
			g.drawLine(1, (int)i, getWidth()-2, (int)i);
		}
		
		g.setColor(lineColor);
		g.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 5, 5);
	}
	
	

	
		final static Color bgColor = new Color(253, 253, 253);
		final 	static Color gray1 = Color.white;
		final static Color gray2 = new Color(250, 250, 250, 100);
		final static float topDark = 0.935f;
		final static Color dark1 = new Color(topDark, topDark, topDark);
		final static Color dark2 = new Color(220, 220, 220, 100);
		final static Color shadowColor = new Color(0f, 0f, 0f, 0.1f);
		final static Color lineColor = new Color(200, 200, 200);
		final static Stroke shadowStroke = new BasicStroke(1.6f);
		final static Stroke normalStroke = new BasicStroke(1.0f);
		
}
