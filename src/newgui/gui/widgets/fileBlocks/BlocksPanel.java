package newgui.gui.widgets.fileBlocks;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import newgui.UIConstants;
import newgui.gui.filepanel.DirectoryListener;
import newgui.gui.widgets.BorderlessButton;
import newgui.gui.widgets.ToolbarPanel;

/**
 * A JPanel that displays a collection of 'AbstractBlocks' contained in a BlocksManager 
 * @author brendan
 *
 */
public class BlocksPanel extends JPanel implements PropertyChangeListener {

	private int blockPadding = 8; //Vertical space between blocks
	protected BlocksManager manager; //Contains list of AbstractBlocks and controls their contents
	private JPanel centerPanel;
	
	public BlocksPanel(BlocksManager manager) {
		this.manager = manager;
		manager.addBlockListener(this);
		initComponents();
	}
	
	public void setBlockPadding(int padding) {
		this.blockPadding = padding;
		layoutBlocks();
	}
	
	private void layoutBlocks() {
		centerPanel.removeAll();
		centerPanel.add(Box.createVerticalGlue());
		for(int i=0; i<manager.getBlockCount(); i++) {
			AbstractBlock block = manager.getBlockByNumber(i);
			//System.out.println("Adding block " + i + " with name : " + block.getLabel());
			centerPanel.add(block);
			centerPanel.add(Box.createVerticalStrut(blockPadding));
		}

		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		centerPanel.add(Box.createVerticalGlue());
		revalidate();
		repaint();
	}
	
	public void blockOpened(AbstractBlock whichBlock) {
		layoutBlocks();
	}
	
	public void blockClosed(AbstractBlock whichBlock) {
		layoutBlocks();
	}
	
	public void removeBlock(AbstractBlock whichBlock) {
		manager.removeBlock(whichBlock);
		layoutBlocks();
	}
	
	
	private void initComponents() {
		this.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
		this.setBackground(UIConstants.lightBackground);
		this.setLayout(new BorderLayout());
		
		
		centerPanel = new JPanel();
		centerPanel.setBackground(UIConstants.lightBackground);
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
		
		JScrollPane centerSP = new JScrollPane(centerPanel);
		centerSP.setBorder(BorderFactory.createEmptyBorder());
		centerSP.setViewportBorder(BorderFactory.createEmptyBorder());
		centerSP.setOpaque(false);
		centerSP.getViewport().setOpaque(false);
		this.add(centerSP, BorderLayout.CENTER);
				
		layoutBlocks();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == manager)
			layoutBlocks();
	}

	
}
