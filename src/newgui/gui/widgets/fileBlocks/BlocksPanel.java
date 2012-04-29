package newgui.gui.widgets.fileBlocks;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

import newgui.gui.filepanel.DirectoryListener;

/**
 * A JPanel that displays a collection of 'AbstractBlocks' contained in a BlocksManager 
 * @author brendan
 *
 */
public class BlocksPanel extends JPanel implements PropertyChangeListener {

	private int blockPadding = 8; //Vertical space between blocks
	protected BlocksManager manager; //Contains list of AbstractBlocks and controls their contents
	
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
		this.removeAll();
		this.add(Box.createVerticalGlue());
		for(int i=0; i<manager.getBlockCount(); i++) {
			AbstractBlock block = manager.getBlockByNumber(i);
			this.add(block);
			this.add(Box.createVerticalStrut(blockPadding));
		}

		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
		this.add(Box.createVerticalGlue());
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
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
		layoutBlocks();
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getSource() == manager)
			layoutBlocks();
	}

	
}
