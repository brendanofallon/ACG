package newgui.gui.widgets.fileBlocks;

import java.awt.Color;
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

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;

import newgui.UIConstants;
import newgui.gui.widgets.BorderlessButton;

public class BlockHeader extends JPanel {

	private final AbstractBlock parentBlock;
	//private String label;
	private final int headerHeight = 14;
	private Font font = UIConstants.sansFontBold.deriveFont(12f);
	static final ImageIcon minimizeIcon = UIConstants.getIcon("gui/icons/minimize.png");
	static final ImageIcon maximizeIcon = UIConstants.getIcon("gui/icons/maximize.png");
	static final ImageIcon closeIcon = UIConstants.getIcon("gui/icons/smallGrayClose.png");
	final BorderlessButton minimButton;
	//final BorderlessButton closeButton;
	private JPopupMenu popup;
	
	public BlockHeader(AbstractBlock block) {
		this.parentBlock = block;
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

		initializePopupMenu();
		

		this.addMouseListener(new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {
				handleMouseClick(e);
			}

			@Override
			public void mouseExited(MouseEvent e) {
				redrawButtons();
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
			}

			@Override
			public void mouseReleased(MouseEvent e) {
			}

			@Override
			public void mouseEntered(MouseEvent e) {
			}

		});
	}
	
	/**
	 * Force this block into the 'minimized' state, in which the main
	 * content is hidden and only the label shows
	 */
	protected void minimize() {
		parentBlock.setOpen( false);
		minimButton.setIcon(maximizeIcon);
		minimButton.repaint();
	}
	
	public String getLabel() {
		return parentBlock.getLabel();
	}

	protected void redrawButtons() {
		parentBlock.repaint();
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		g.setFont(font);
		g.drawString(parentBlock.getLabel(), 7, 18);
	}
	
	private void initializePopupMenu() {
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.GRAY) );
		popup.setBackground(new Color(100,100,100) );

		JMenuItem minItem = new JMenuItem("Minimize");
		minItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				minimize();
			}
		});
		popup.add(minItem);
		
		JMenuItem renameItem = new JMenuItem("Rename folder");
		renameItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showRenamingDialog();
			}
		});
		popup.add(renameItem);
		
		JMenuItem deleteItem = new JMenuItem("Delete folder");
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteBlock();
			}
		});
		popup.add(deleteItem);
		
	}
	
	protected void showRenamingDialog() {
		BlockRenameFrame renameFrame = new BlockRenameFrame(parentBlock);
		renameFrame.setVisible(true);
	}

	protected void deleteBlock() {
		parentBlock.getManager().deleteBlock(parentBlock);
	}

	/**
	 * Called when there's a mouse click event in this component. We open the selected file (if there is one) if
	 * there's a double-click, or show the popup menu if there's a right-button or control-click event
	 * @param me
	 */
	protected void handleMouseClick(MouseEvent me) {
		if (me.isPopupTrigger() || (UIConstants.isMac() && me.isControlDown()) || (me.getButton()==MouseEvent.BUTTON3)) {
			popup.show(this, me.getX(), me.getY());
			return;
		}
		
	}

}
