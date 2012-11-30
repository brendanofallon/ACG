package newgui.gui.filepanel;


import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import newgui.ErrorWindow;
import newgui.alignment.FileParseException;
import newgui.datafile.AlignmentFile;
import newgui.datafile.DataFileFactory;
import newgui.datafile.XMLDataFile;

import sequence.Alignment;

/**
 * A dialog that allows the user to select an alignment from the input files
 * @author brendan
 *
 */
public class ChooseAlignmentPanel extends JDialog {

	final ChooseAlignmentListener listener;
	
	public ChooseAlignmentPanel(ChooseAlignmentListener listener) {
		this.setTitle("Choose an alignment");
		this.listener = listener;
		initComponents();
		pack();
		setLocationRelativeTo(null);
		setVisible(false);
	}

	private void initComponents() {
		this.getContentPane().setLayout(new BorderLayout());
		this.getRootPane().setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		
		fileTree = new FileTree(InputFilesManager.getManager().getRootDirectory());
		fileTree.setOpenFilesOnDoubleClick(false);
		fileTree.setPopupEnabled(false);
		scrollPane = new JScrollPane(fileTree);
		this.add(scrollPane, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		
		JButton okButton = new JButton("Choose");
		
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				okButtonPressed();
			}
		});
		
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				cancelButtonPressed();
			}
		});	

		bottomPanel.add(okButton);
		this.add(bottomPanel, BorderLayout.SOUTH);
		
		this.setMinimumSize(new Dimension(300, 300));
		this.setPreferredSize(new Dimension(300, 300));
	}

	protected void cancelButtonPressed() {
		this.setVisible(false);
		if (listener != null)
			listener.alignmentChosen(null);
		this.dispose();		
	}

	/**
	 * Called when user clicks OK button. We read the selected file to obtain the
	 * Alignment contained in it, then call alignmentChosen on the listener 
	 */
	protected void okButtonPressed() {
		this.setVisible(false);
		File selectedFile = fileTree.getSelectedFile();
		if (selectedFile == null && listener != null) {
			listener.alignmentChosen(null);
		}
		else {
			try {
				XMLDataFile	dataFile = DataFileFactory.createDataFile(selectedFile);
				if (dataFile instanceof AlignmentFile) {
					Alignment aln = ((AlignmentFile)dataFile).getAlignment();
					if (listener != null)
						listener.alignmentChosen( aln );
				}

			} catch (IOException e) {
				ErrorWindow.showErrorWindow(e);
				e.printStackTrace();
			} catch (FileParseException e) {
				ErrorWindow.showErrorWindow(e);
				e.printStackTrace();
			}
		}
		this.dispose();
	}
	
	/**
	 * Displays a ChooseAlignmentPanel that allows the user to select an alignment from among
	 * the input files
	 * @param listener
	 */
	public static void chooseAlignment(ChooseAlignmentListener listener) {
		panel = new ChooseAlignmentPanel(listener);			
		panel.setVisible(true);
	}
	
	protected static ChooseAlignmentPanel panel = null;
	
	private JScrollPane scrollPane;
	private FileTree fileTree;
}
