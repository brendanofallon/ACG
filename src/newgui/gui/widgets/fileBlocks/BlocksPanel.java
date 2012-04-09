package newgui.gui.widgets.fileBlocks;

import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A container for a bunch of Blocks 
 * @author brendan
 *
 */
public class BlocksPanel extends JPanel {

	private List<AbstractBlock> blocks = new ArrayList<AbstractBlock>();
	private int blockPadding = 8; //Vertical space between blocks
	
	public BlocksPanel() {
		initComponents();
	}
	
	public void setBlockPadding(int padding) {
		this.blockPadding = padding;
		layoutBlocks();
	}
	
	private void layoutBlocks() {
		this.removeAll();
		this.add(Box.createVerticalGlue());
		for(AbstractBlock block : blocks) {
			this.add(block);
			this.add(Box.createVerticalStrut(blockPadding));
		}
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
		blocks.remove(whichBlock);
		layoutBlocks();
	}
	
	public void addBlock(AbstractBlock newBlock) {
		blocks.add(newBlock);
		newBlock.setParentPanel(this);
		layoutBlocks();
	}
	
	private void initComponents() {
		this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.setBorder(BorderFactory.createEmptyBorder(10, 4, 10, 4));
		layoutBlocks();
	}
}
