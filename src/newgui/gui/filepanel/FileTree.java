package newgui.gui.filepanel;


import gui.ErrorWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;


import newgui.UIConstants;
import newgui.alignment.FileParseException;
import newgui.datafile.*;
import newgui.gui.ViewerWindow;


/**
 * The file tree displays a tree of files in a jpanel, using a jtree. 
 * 
 * @author brendan
 *
 */
public class FileTree extends JPanel implements DirectoryListener {

	private JTree tree;
	final File rootDir;
	private JPopupMenu popup;
	private JPopupMenu renamerPopup; //Holds renaming field
	private JTextField renamer;
	final static String fileSeparator = System.getProperty("file.separator");
	
	public FileTree(File rootDir) {
		this.rootDir = rootDir;
		setOpaque(false);
		this.setLayout(new BorderLayout());
		
		tree = new JTree();
		tree.addMouseListener(new TreeMouseListener());
		tree.setAlignmentX(Component.LEFT_ALIGNMENT);
		createTreeNodes();
		this.add(tree, BorderLayout.CENTER);
		
		initializePopupMenu();
	}

	/**
	 * Create the popup menu and add a bunch of menu items to it
	 */
	private void initializePopupMenu() {
		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.GRAY) );
		popup.setBackground(new Color(100,100,100) );

		JMenuItem openItem = new JMenuItem("Open");
		openItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openSelectedFile();
			}
		});
		popup.add(openItem);
		
		
		
		JMenuItem renameFile = new JMenuItem("Rename");
		renameFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openRenamingField();
			}
		});
		popup.add(renameFile);

		//Create file-renaming text field.. it lives in a popup menu so we can easily draw it on top
		//of other components
		renamerPopup = new JPopupMenu();
		renamer = new JTextField();
		renamer.setBorder(null);
		renamerPopup.add(renamer);
		
		renamer.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				renameSelectedFileTo(renamer.getText());				
				renamerPopup.setVisible(false);
				repaint();
			}
		});
		
		
		JMenuItem deleteItem = new JMenuItem("Delete");
		deleteItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				deleteSelectedFile();
			}
		});
		popup.add(deleteItem);
		
	}

	/**
	 * Actually perform renaming operation, currently selected file is renamed
	 * to the given string (parent directory is not affected)
	 * @param text
	 */
	protected void renameSelectedFileTo(String text) {
		if (! text.endsWith(".xml"))
			text = text + ".xml";
		
		File file = getSelectedFile();
		File renamedFile = new File(file.getParentFile().getAbsolutePath() + fileSeparator + text);
		System.out.println("Renaming file : " + file.getAbsolutePath() + " to: " + renamedFile.getAbsolutePath());
		file.renameTo( renamedFile );
		filesChanged(rootDir);
	}

	/**
	 * Traverse tree of files in rootDir and create FileNode objects for the files encountered, and add them
	 * to the JTree
	 */
	private void createTreeNodes() {
		if (dataUtils == null)
			dataUtils = new DataFileUtils();
		
		DirectoryNode rootNode = new DirectoryNode(rootDir);
		TreeModel treeModel = new DefaultTreeModel(rootNode);
		tree.setCellRenderer(new FileCellRenderer());
		tree.setRootVisible(false);
		buildTreeNodes(rootNode);
		tree.setModel(treeModel);
	}
	
	@Override
	public void filesChanged(File root) {
		//Right now we just re-scan all files
		createTreeNodes();
		revalidate();
		repaint();
	}
	
	/**
	 * Create the tree of nodes
	 * @param dirNode
	 */
	private void buildTreeNodes(DirectoryNode dirNode) {

		
		File[] files = dirNode.getFiles();
		
		for(int i=0; i<files.length; i++) {
			if (dataUtils.isDataFile(files[i])) {
				FileNode fNode = new FileNode(files[i]);
				dirNode.add(fNode);
			}
			
			if (files[i].exists() && files[i].isDirectory()) {
				DirectoryNode dirChild = new DirectoryNode(files[i]);
				buildTreeNodes(dirChild);
				dirNode.add(dirChild);
			}
		}
	}
	
	/**
	 * Get the File that is currently selected in this FileTree, or null if there is no selection 
	 * @return
	 */
	public File getSelectedFile() {
		TreePath selPath = tree.getSelectionPath();
		if (selPath == null) //Some double-clicks aren't on anything and selPath will be null
			return null;
		Object obj = selPath.getLastPathComponent();
		if (obj instanceof FileNode) {
			File file = ((FileNode)obj).getFile();
			return file;
		}
		return null;
	}
	
	/**
	 * Cause the file associated with the given node to be opened in the viewer
	 * @param node
	 */
	protected void openSelectedFile() {
		File file = getSelectedFile();
		XMLDataFile dataFile;
		try {
			dataFile = DataFileFactory.createDataFile(file);
			String title = dataFile.getSourceFile().getName();
			if (title.endsWith(".xml"))
				title = title.replace(".xml", "");

			ViewerWindow.getViewer().displayDataFile(dataFile, title);

		} catch (IOException e) {
			ErrorWindow.showErrorWindow(e, "Could not open file: " + file.getName());
			e.printStackTrace();
		} catch (FileParseException e) {
			ErrorWindow.showErrorWindow(e, "Could parse data from file: " + file.getName());
			e.printStackTrace();
		}
		
	}
	
	protected void openRenamingField() {
		File file = getSelectedFile();
		
		TreePath selPath = tree.getSelectionPath();
		if (selPath == null) //Some double-clicks aren't on anything and selPath will be null
			return;
		
		int[] selectedRows = tree.getSelectionRows();
		if (selectedRows.length != 1) {
			return;
		}
		
		int selectedRow = selectedRows[0];

		renamer.setText(file.getName());
		renamerPopup.show(tree, tree.getRowBounds(selectedRow).x, tree.getRowBounds(selectedRow).y);
		renamer.selectAll();
	}

	/**
	 * Permanently remove the selected file from the file system
	 */
	protected void deleteSelectedFile() {
		File file = getSelectedFile();
		if (file == null)
			return;
		
		int op = JOptionPane.showConfirmDialog(this, "Permanently delete " + file.getName() + " ? ");
		if (op == JOptionPane.OK_OPTION) {
			file.delete();
		}
		filesChanged(rootDir);
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

		if (me.getClickCount()>1) {
			openSelectedFile();
		}
	}
	
	class TreeMouseListener implements MouseListener {

		@Override
		public void mouseClicked(MouseEvent e) {
			handleMouseClick(e);
		}

		@Override
		public void mousePressed(MouseEvent e) {	}

		@Override
		public void mouseReleased(MouseEvent e) {	}

		@Override
		public void mouseEntered(MouseEvent e) {	}

		@Override
		public void mouseExited(MouseEvent e) { 	}
		
	}

	private DataFileUtils dataUtils; //Used for data file format checking


}
