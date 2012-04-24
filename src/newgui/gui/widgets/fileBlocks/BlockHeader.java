package newgui.gui.widgets.fileBlocks;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

public class BlockHeader extends JPanel {

	private final AbstractBlock parentBlock;
	private String label;
	private final int headerHeight = 14;
	private Font font = UIConstants.sansFontBold.deriveFont(12f);
	static final ImageIcon minimizeIcon = UIConstants.getIcon("gui/icons/minimize.png");
	static final ImageIcon maximizeIcon = UIConstants.getIcon("gui/icons/maximize.png");
	static final ImageIcon closeIcon = UIConstants.getIcon("gui/icons/smallGrayClose.png");
	final BorderlessButton minimButton;
	final BorderlessButton closeButton;

	
	public BlockHeader(AbstractBlock block, String label) {
		this.parentBlock = block;
		this.label = label;
		this.setMinimumSize(new Dimension(1, headerHeight));
		this.setMaximumSize(new Dimension(32000, headerHeight));
		this.setLayout(new FlowLayout(FlowLayout.RIGHT));
		this.add(Box.createRigidArea(new Dimension(50, headerHeight)));
		minimButton = new BorderlessButton(minimizeIcon);
		this.add(minimButton);
		minimButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				parentBlock.setOpen(! parentBlock.isOpen());
				if ( parentBlock.isOpen() )
					minimButton.setIcon(minimizeIcon);
				else
					minimButton.setIcon(maximizeIcon);
			}
		});

		
		closeButton = new BorderlessButton(closeIcon);
		this.add(closeButton);

		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mousePressed(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseReleased(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// TODO Auto-generated method stub

			}

			@Override
			public void mouseExited(MouseEvent e) {
				redrawButtons();
			}

		});
	}
	
	public String getLabel() {
		return label;
	}

	protected void redrawButtons() {
		parentBlock.repaint();
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		g.drawString(label, 5, 18);
	}
	
	
}
