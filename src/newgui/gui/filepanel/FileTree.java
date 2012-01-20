package newgui.gui.filepanel;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

import newgui.alignment.FileParseException;
import newgui.datafile.*;
import newgui.gui.ViewerWindow;


/**
 * The file tree displays a tree of files in a jpanel, using a jtree
 * @author brendan
 *
 */
public class FileTree extends JPanel implements DirectoryListener {

	private JTree tree;
	final File rootDir;
	
	public FileTree(File rootDir) {
		this.rootDir = rootDir;
		setOpaque(false);
		this.setLayout(new BorderLayout());
		
		tree = new JTree();
		tree.addMouseListener(new TreeMouseListener());
		tree.setAlignmentX(Component.LEFT_ALIGNMENT);
		createTreeNodes();
		this.add(tree, BorderLayout.CENTER);
	}

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
	
	
	protected void handleMouseClick(MouseEvent me) {
		if (me.getClickCount()>1) {
			TreePath selPath = tree.getSelectionPath();
			if (selPath == null) //Some double-clicks aren't on anything and selPath will be null
				return;
			Object obj = selPath.getLastPathComponent();
			if (obj instanceof FileNode) {
				File file = ((FileNode)obj).getFile();
				XMLDataFile dataFile;
				try {
					
					dataFile = DataFileFactory.createDataFile(file);
					String title = dataFile.getSourceFile().getName();
					if (title.endsWith(".xml"))
						title = title.replace(".xml", "");
					
					ViewerWindow.getViewer().displayDataFile(dataFile, title);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (FileParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
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
