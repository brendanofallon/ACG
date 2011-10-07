package gui.inputPanels;

import gui.inputPanels.Configurator.InputConfigException;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/**
 * Displays and allows configuration of options for ARGs. Mostly just starting ARG and whether or not to modify ARG
 * @author brendano
 *
 */
public class ARGModelView extends JPanel {

	private final ARGModelElement argModel;
	private JCheckBox modifyARGBox;
	
	final String[] startARGChoices = {"Random", "UPGMA", "From file"};
	private JComboBox startARGBox;
	private JTextField startARGFileField;
	private JButton browseButton;
	private File selectedFile = null;
	
	public ARGModelView(ARGModelElement argModel) {
		this.argModel = argModel;
		this.setOpaque(false);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
	
		modifyARGBox = new JCheckBox("Modify ARG");
		modifyARGBox.setSelected(true);
		
		startARGBox = new JComboBox(startARGChoices);
		startARGBox.setSelectedIndex(1);
		startARGBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				argBoxChanged();
			}
		});
		this.add(new JLabel("Starting ARG :"));
		this.add(startARGBox);
		
		startARGFileField = new JTextField("");
		startARGFileField.setMinimumSize(new Dimension(100, 3));
		startARGFileField.setPreferredSize(new Dimension(100, 30));
		startARGFileField.setMinimumSize(new Dimension(100, 300));
		startARGFileField.setEditable(false);
		startARGFileField.setEnabled(false);
		this.add(startARGFileField);
		
		browseButton = new JButton("Choose");
		browseButton.setEnabled(false);
		this.add(browseButton);
		
		this.add(modifyARGBox);
		updateViewFromModel();
	}

	protected void argBoxChanged() {		
		if (startARGBox.getSelectedIndex()==0) {
			argModel.setUseUPGMA(false);
			argModel.setStartingFilePath(null);
			argModel.setStartingNewick(null);
		}
		if (startARGBox.getSelectedIndex()==1) {
			argModel.setUseUPGMA(true);
			argModel.setStartingFilePath(null);
			argModel.setStartingNewick(null);
		}
		
		if (startARGBox.getSelectedIndex()!= 2) {
			startARGFileField.setEnabled(false);
			browseButton.setEnabled(false);
		}
		else {
			startARGFileField.setEnabled(true);
			browseButton.setEnabled(true);
		}
	}
	
	/**
	 * Update all settings in model to reflect options chosen in this view
	 */
	public void updateModelFromView() throws InputConfigException {
		if (startARGBox.getSelectedIndex()==0) {
			argModel.setUseUPGMA(false);
			argModel.setStartingFilePath(null);
			argModel.setStartingNewick(null);
		}
		if (startARGBox.getSelectedIndex()==1) {
			argModel.setUseUPGMA(true);
			argModel.setStartingFilePath(null);
			argModel.setStartingNewick(null);
		}
		if (startARGBox.getSelectedIndex()==2) {
			if (selectedFile == null || (! selectedFile.exists())) {
				throw new InputConfigException("Please select a file to use a the starting ARG");
			}
			argModel.setStartingFilePath(selectedFile.getAbsolutePath());
		}
		
		argModel.setUseAllModifiers( modifyARGBox.isSelected() );
		
		
	}
	
	/**
	 * Update all settigs / widgets in this view to reflect options in model
	 */
	public void updateViewFromModel() {
		if (argModel.getStartingFilePath() != null) {
			startARGBox.setSelectedIndex(2);
			selectedFile = new File( argModel.getStartingFilePath() );
			startARGFileField.setText( selectedFile.getName() );
			browseButton.setEnabled(true);
			startARGFileField.setEnabled(true);
		}
		else {
			browseButton.setEnabled(false);
			startARGFileField.setEnabled(false);
			
			if (argModel.isUseUPGMA()) {
				startARGBox.setSelectedIndex(1);
			}
			else {
				startARGBox.setSelectedIndex(0);
			}
		}
		
		modifyARGBox.setSelected( argModel.isUseAllModifiers() );
		
		revalidate();
		repaint();
	}
	
	
}
