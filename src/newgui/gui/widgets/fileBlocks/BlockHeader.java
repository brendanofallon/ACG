package newgui.gui.widgets.fileBlocks;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

public class BlockHeader extends JPanel {

	private final AbstractBlock parentBlock;
	private String label;
	private final int headerHeight = 14;
	private Font font = UIConstants.sansFontBold.deriveFont(12f);
	
	public BlockHeader(AbstractBlock block, String label) {
		this.parentBlock = block;
		this.label = label;
		this.setMinimumSize(new Dimension(1, headerHeight));
		this.setMaximumSize(new Dimension(32000, headerHeight));
		this.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(Box.createRigidArea(new Dimension(50, headerHeight)));
		BorderlessButton closeButton = new BorderlessButton("Close");
		this.add(closeButton);
		closeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentBlock.setOpen(! parentBlock.isOpen());
			}
		});

	}
	
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		g.drawString(label, 5, 18);
	}
	
	
	
}
