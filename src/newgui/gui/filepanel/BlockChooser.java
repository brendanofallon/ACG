package newgui.gui.filepanel;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import net.miginfocom.swing.MigLayout;
import newgui.datafile.XMLDataFile;
import newgui.gui.ViewerWindow;
import newgui.gui.widgets.fileBlocks.BlocksManager;

/**
 * A frame that allows a user to choose a block to save a file into
 * @author brendan
 *
 */
public class BlockChooser extends JDialog {

	private BlocksManager manager ;
	private XMLDataFile file;
	
	public BlockChooser(BlocksManager manager, XMLDataFile file, String suggestedName) {
		super(ViewerWindow.getViewer(), "Save file", true);
		this.manager = manager;
		this.file = file;
		initComponents();
		initializeBlockList();
		setLocationRelativeTo(ViewerWindow.getViewer());
		setPreferredSize(new Dimension(300, 300));
		this.setMinimumSize(new Dimension(300, 300));
		filenameField.setText(suggestedName);
		this.getRootPane().setDefaultButton(chooseButton);
		pack();
	}
	

	private void initializeBlockList() {
		DefaultListModel model = new DefaultListModel();
		for(String name : manager.getBlockNames()) {
			model.addElement(name);
		}
		
		blocksList.setModel(model);
	}
	
	private void initComponents() {
		JPanel rootPane = new JPanel();
		rootPane.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		JLabel topLabel = new JLabel("Please select a folder:");
		topPanel.add(topLabel);
		topPanel.setOpaque(false);
		topPanel.setBorder(BorderFactory.createEmptyBorder(4, 6, 0, 6));
		rootPane.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
		centerPanel.setLayout(new BorderLayout());
		rootPane.add(centerPanel, BorderLayout.CENTER);
		
		blocksList = new JList();
		blocksList.setCellRenderer(new ChooserRenderer());
		blocksList.setOpaque(false);
		blocksList.setBorder(BorderFactory.createEmptyBorder());
		blocksList.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				listSelectionChanged();
			}
		});
		JScrollPane sp = new JScrollPane(blocksList);
		sp.setViewportBorder(BorderFactory.createEmptyBorder());
		sp.setBorder(BorderFactory.createEmptyBorder());
		sp.setOpaque(false);
		sp.getViewport().setOpaque(false);
		centerPanel.add(sp, BorderLayout.CENTER);
	
		JPanel selectedPanel = new JPanel();
		selectedPanel.setLayout(new MigLayout());
		
		selectedBlockLabel = new JLabel("Selected folder :  none");
		selectedPanel.add(selectedBlockLabel, "wrap");
		
		JLabel lab = new JLabel("File name :");
		String initialName = "";
		if (file.getSourceFile() != null)
			initialName = file.getSourceFile().getName().replace(".xml", "");
		filenameField = new JTextField( initialName );
		selectedPanel.add(lab, "split 2");
		selectedPanel.add(filenameField);
		filenameField.setMinimumSize(new Dimension(150, 10));
		filenameField.setPreferredSize(new Dimension(150, 36));
		filenameField.setMaximumSize(new Dimension(150, 1000));
		centerPanel.add(selectedPanel, BorderLayout.SOUTH);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButtonPressed();
			}
		});
		
		chooseButton = new JButton("Choose");
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				chooseButtonPressed();
			}
		});
		
		bottomPanel.add(Box.createHorizontalStrut(10));
		bottomPanel.add(cancelButton);
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(chooseButton);
		bottomPanel.add(Box.createHorizontalStrut(10));
		rootPane.add(bottomPanel, BorderLayout.SOUTH);
		setContentPane(rootPane);
	}
	
	protected void listSelectionChanged() {
		Object item = blocksList.getSelectedValue();
		if (item==null) 
			selectedBlockName = null;
		else
			selectedBlockName = item.toString();
		
		if (selectedBlockName == null)
			selectedBlockLabel.setText("Selected folder : none");
		else
			selectedBlockLabel.setText("Selected folder : " + item.toString());
		selectedBlockLabel.revalidate();
		selectedBlockLabel.repaint();
	}


	protected void chooseButtonPressed() {
		if (selectedBlockName == null) {
			JOptionPane.showMessageDialog(this, "Please select a folder");
			return;
		}
		if (filenameField.getText().length()==0) {
			JOptionPane.showMessageDialog(this, "Please enter a name for the file");
			return;
		}
		
		manager.saveFile(selectedBlockName, filenameField.getText(), file);
		
		this.setVisible(false);
		this.dispose();
	}


	protected void cancelButtonPressed() {
		selectedBlockName = null;
		this.setVisible(false);
		this.dispose();
	}

	public String getSelectedBlockName() {
		return selectedBlockName;
	}
	
	public String getSelectedFileName() {
		return filenameField.getText();
	}
	
	private JLabel selectedBlockLabel;
	private JButton chooseButton;
	private JTextField filenameField;
	private JList blocksList;
	private String selectedBlockName = null;
}
