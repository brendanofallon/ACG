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
import newgui.datafile.AlignmentFile;
import newgui.gui.alnGen.AlnGenFrame;
import newgui.gui.filepanel.BlockChooser;
import newgui.gui.widgets.HighlightButton;

/**
 * Panel that holds a couple of buttons and the search text field, typically at upper left of
 * main window
 * @author brendan
 *
 */
public class TopLeftPanel extends JPanel {
	
	BorderlessButton importButton;
	ImageIcon addFastaIcon = UIConstants.getIcon("gui/icons/addFASFile.png");
	ImageIcon addVCFIcon = UIConstants.getIcon("gui/icons/addVCFFile.png");
	
	public TopLeftPanel() {
		this.setBackground(UIConstants.lightBackground);
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		this.setPreferredSize(new Dimension(200, 50));
		importButton = new BorderlessButton(addFastaIcon);
		importButton.setToolTipText("Import an alignment");
		importButton.setXDif(-2);
		importButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openImportDialog();
			}
		});

		
		BorderlessButton buildFromVCFButton= new BorderlessButton(addVCFIcon);
		buildFromVCFButton.setToolTipText("Build an alignment from a VCF file");
		buildFromVCFButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				showAlnGenFrame();
			}
		});
		
		this.add(importButton);	
		this.add(buildFromVCFButton);
	}

	protected void showAlnGenFrame() {
		AlnGenFrame alnGenFrame = new AlnGenFrame();
		alnGenFrame.setVisible(true);
		
	}

	protected void openImportDialog() {
		File fileToImport = browseForFile();
		if (fileToImport != null) {
			String filename = fileToImport.getName();
			if (filename.endsWith(".fasta") || filename.endsWith(".fa") || filename.endsWith(".fas") ) {
				try {
					Alignment aln = FastaImporter.getAlignment(fileToImport);
					AlignmentFile alnFile = new AlignmentFile(aln);
					String newFilename = filename;
					if (filename.contains("."))
						newFilename = filename.substring(0, filename.lastIndexOf("."));
					ViewerWindow.getViewer().getFileManager().showSaveDialog(alnFile, newFilename);
					
				} catch (Exception e) {
					ErrorWindow.showErrorWindow(e, "Could not import file");
					e.printStackTrace();
				} 
			}
			else {
				JOptionPane.showMessageDialog(this, "Currently, only fasta-formatted files can be imported");
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
