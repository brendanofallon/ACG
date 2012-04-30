package newgui.gui.widgets.fileBlocks;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import net.miginfocom.swing.MigLayout;
import newgui.gui.ViewerWindow;

/**
 * A tiny frame that allows a user to enter a new name for an AbstractBlock 
 * @author brendan
 *
 */
public class BlockRenameFrame extends JDialog {
	
	private final BlocksManager manager;
	private AbstractBlock block;
	
	public BlockRenameFrame(AbstractBlock blockToRename) {
		super(ViewerWindow.getViewer(), "Rename folder", true);
		this.manager = blockToRename.getManager();
		this.block = blockToRename;
		initComponents();
		setLocationRelativeTo(ViewerWindow.getViewer());
		this.getRootPane().setDefaultButton(okButton);
		setPreferredSize(new Dimension(300, 150));
		pack();
	}

	private void initComponents() {
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new MigLayout());
		
		JLabel lab = new JLabel("Enter a new name for " + block.getLabel() + " :");
		centerPanel.add(lab, "wrap");
		blockNameField = new JTextField("new name");
		blockNameField.setPreferredSize(new Dimension(150, 40));
		centerPanel.add(blockNameField);
		
		mainPanel.add(centerPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButtonPressed();
			}
		});
		
		okButton = new JButton("OK");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonPressed();
			}
		});
		
		bottomPanel.add(cancelButton);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(okButton);
		mainPanel.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setContentPane(mainPanel);
	}

	protected void okButtonPressed() {
		String newBlockName = blockNameField.getText();
		if (newBlockName.length()>0) {
			block.renameTo(newBlockName);
			//manager.renameBlock(block, newBlockName);
			manager.fireBlockStateChangedEvent(block); //forces a re-layout of the blocks
		}
		
		this.setVisible(false);
		this.dispose();
	}

	protected void cancelButtonPressed() {
		this.setVisible(false);
		this.dispose();
	}

	private JButton okButton;
	private JTextField blockNameField;
}
