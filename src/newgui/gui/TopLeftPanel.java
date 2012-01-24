package newgui.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import gui.ErrorWindow;
import gui.widgets.BorderlessButton;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;

import newgui.UIConstants;
import newgui.alignment.FastaImporter;
import newgui.alignment.FileParseException;
import newgui.alignment.UnrecognizedBaseException;
import newgui.gui.filepanel.InputFilesManager;

/**
 * Panel that holds a couple of buttons and the search text field
 * @author brendan
 *
 */
public class TopLeftPanel extends JPanel {
	
	BorderlessButton importButton;
	
	public TopLeftPanel() {
		this.setBackground(UIConstants.lightBackground);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setPreferredSize(new Dimension(200, 50));
		ImageIcon openIcon =  UIConstants.blueRightArrow;
		if (openIcon != null) {
			importButton = new BorderlessButton(null, openIcon);
			importButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					openImportDialog();
				}
			});
			importButton.setYDif(-1);
		}
		else {
			importButton = new BorderlessButton("Open", null);
		}
		importButton.setPreferredSize(new Dimension(32, 28));
		this.add(importButton);
	
	}

	protected void openImportDialog() {
		File fileToImport = browseForFile();
		if (fileToImport != null) {
			String filename = fileToImport.getName();
			if (filename.endsWith(".fasta") || filename.endsWith(".fa") || filename.endsWith(".fas") ) {
				try {
					Alignment aln = FastaImporter.getAlignment(fileToImport);
					InputFilesManager inputManager = InputFilesManager.getManager();
					String newFilename = filename.substring(0, filename.lastIndexOf("."));
					inputManager.addAlignment(aln, newFilename + ".xml");
				} catch (Exception e) {
					ErrorWindow.showErrorWindow(e, "Could not import file");
					e.printStackTrace();
				} 
			}
			else {
				JOptionPane.showMessageDialog(this, "Currently, we can only import fasta-formatted files");
			}
		}
	}
	
	/**
	 * Open a file browser that allows the user to select a file to import
	 * @return
	 */
	private File browseForFile() {
		File selectedFile = null;
        
		//If we're on a mac then a FileDialog looks better and supports a few mac-specific options 
		if (UIConstants.isMac()) {
			FileDialog fileDialog = new FileDialog(ViewerWindow.getViewer(), "Choose a file");
			fileDialog.setMode(FileDialog.LOAD);
			String userDir = System.getProperty("user.dir");
			if (userDir != null)
				fileDialog.setDirectory(userDir);
			
			fileDialog.setVisible(true);
			
			String filename = fileDialog.getFile();
			String path = fileDialog.getDirectory();
			selectedFile = new File(path + filename);
		}
		else {
			//Not on a mac, use a JFileChooser instead of a FileDialog
			
			//Construct a new file choose whose default path is the path to this executable, which 
			//is returned by System.getProperty("user.dir")
			JFileChooser fileChooser = new JFileChooser( System.getProperty("user.dir"));
			int option = fileChooser.showOpenDialog(ViewerWindow.getViewer());
			if (option == JFileChooser.APPROVE_OPTION) {
				selectedFile = fileChooser.getSelectedFile();
			}
		}
		
		return selectedFile;
	}
	
	
}
