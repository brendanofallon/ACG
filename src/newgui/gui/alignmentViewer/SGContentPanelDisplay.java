package newgui.gui.alignmentViewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import plugins.SGPlugin.SGPlugin;
import plugins.SGPlugin.analyzer.DotPlotAnalyzer;
import plugins.SGPlugin.analyzer.PairwiseDifChart;
import plugins.SGPlugin.analyzer.SequenceBarChart;
import plugins.SGPlugin.analyzer.SequenceGroupSummary;
import plugins.SGPlugin.analyzer.SequenceLineChart;
import plugins.SGPlugin.analyzer.UsageLogoChart;
import plugins.SGPlugin.analyzer.haplotype.HapNetworkAnalyzer;
import plugins.SGPlugin.analyzer.haplotype.HaplotypeNetwork;
import plugins.SGPlugin.display.SGContentPanel.Selection;

import topLevelGUI.FileParser;
import topLevelGUI.SunFishFrame;
import topLevelGUI.analyzer.Analyzable;
import undoRedo.UndoableAction;
import undoRedo.UndoableActionSource;

import display.*;
import displayPane.SaveCancelledException;
import plugins.SGPlugin.display.rowPainters.*;
import plugins.SGPlugin.parsers.FastaParser;
import plugins.SGPlugin.parsers.IMAParser;
import plugins.SGPlugin.parsers.PhyParser;

import element.IntegerRange;
import element.sequence.SequenceGroup;
import element.codon.GeneticCodeFactory;
import element.codon.GeneticCodeFactory.GeneticCodes;
import element.sequence.Sequence;
import element.sequence.SimpleORFAnnotator;
import element.sequence.StringSequence;
import errorHandling.ErrorWindow;
import fileTree.FileTreePanel;
import guiWidgets.glassDropPane.GlassDropPane;
import guiWidgets.glassDropPane.GlassPaneThing;
import guiWidgets.CFButton;
import guiWidgets.GTKFixSeparator;

public class SGContentPanelDisplay extends Display implements UndoableActionSource {

	static final double VERSION = 1.1;
	static final String iconPath = "icons/"; //We search for icon resources here first
	
	JScrollPane scrollPane;
	SGContentPanel contentPane;
	JPanel topPanel;
	SequenceGroup currentSG;
	
	List<String> rowPainters;
	
	ArrayList<String> aaRowPainters; //Paints amino acid rows
	Map<String, AbstractRowPainter.Instantiator> rowPainterMap = new HashMap<String, AbstractRowPainter.Instantiator>();
	
	//Tracks what appearance mode we were in previously, for undo purposes
	int prevAppearenceMode = -1;
	
	int prevTranslationMode = -1;
	
	
	public SGContentPanelDisplay(SunFishFrame sunfishParent) {
		super(sunfishParent);
	}
	
	@Override
	public String getName() {
		return "Sequence group display";
	}

	@Override
	public String getDescription() {
		return "Visualize and edit groups of sequences";
	}
	
	public double getVersionNumber() {
		return VERSION;
	}
	
	protected void zoomValueChanged() {
		int val = (int)Math.round(zoomSlider.getValue()/10.0)+1;
		contentPane.setColumnWidth(val);
		scrollPane.getHorizontalScrollBar().setUnitIncrement(Math.max(5, contentPane.getColumnWidth()));
		SunFishFrame.getSunFishFrame().setInfoLabelText("Column width: " + val);
	}

	
	/**
	 * Obtain an icon from the given url. This looks in the icons package by default (meaning, if the url contains no 
	 * slashes, we look in 
	 * @param url
	 * @return
	 */
	public static ImageIcon getIcon(String url) {
		ImageIcon icon = null;
		try {
			if (! (url.contains("/")))
				url = iconPath + url;
			java.net.URL imageURL = SGPlugin.class.getResource(url);
			icon = new ImageIcon(imageURL);
		}
		catch (Exception ex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Error loading icon image " + url + " from resouce. " + ex);
		}
		return icon;
	}
	
	@Override
	public String getFileName() {
		if (sourceFile == null) {
			return "(unknown)";
		}
		return sourceFile.getName();
	}

	@Override
	public Display getNew() {
		return new SGContentPanelDisplay(sunfishParent);
	}
	
	
	/**
	 * Initialize the rowPainterMap by filling it with identifier, Instantiator pairs.
	 */
	private void constructRowPainters() {
		rowPainters = new ArrayList<String>(10);
		aaRowPainters =  new ArrayList<String>(5);
		
		if (rowPainterMap==null)
			rowPainterMap = new HashMap<String, AbstractRowPainter.Instantiator>();
		rowPainterMap.clear();
		
		rowPainterMap.put(GC_AT_RowPainter.getIdentifier(), new GC_AT_RowPainter.Instantiator());
		rowPainterMap.put(AG_CT_RowPainter.getIdentifier(), new AG_CT_RowPainter.Instantiator());
		rowPainterMap.put(FrequencyRowPainter.getIdentifier(), new FrequencyRowPainter.Instantiator());
		rowPainterMap.put(HaplotypeRowPainter.getIdentifier() , new HaplotypeRowPainter.Instantiator());
		rowPainterMap.put(PartitionRowPainter.getIdentifier(), new PartitionRowPainter.Instantiator());
		rowPainterMap.put(ORFRowPainter.getIdentifier(), new ORFRowPainter.Instantiator());
		
		for(String id : rowPainterMap.keySet()) {
			rowPainters.add(id);
		}
		
		rowPainterMap.put(AminoAcidRowPainter.getIdentifier(), new AminoAcidRowPainter.Instantiator());
		rowPainterMap.put(AAFreqRowPainter.getIdentifier(), new AAFreqRowPainter.Instantiator());
		
	
		//OK, bad design here, there should be an aaRowPainters map just like the normal row painter one...
		aaRowPainters.add(AminoAcidRowPainter.getIdentifier());
		aaRowPainters.add(AAFreqRowPainter.getIdentifier());
	}

	
	
	/**
	 * Returns true if this display can accept drops (as in Drag and Drop) of the specified
	 * file. Returning false (the default) means that this display will ignore the file
	 * 
	 * @param file
	 * @return True if we can handle having this file dropped on us.
	 */
	public boolean acceptDrop(File file) {
		return true;
	}
	
	/**
	 * This is called if a file has been dropped on us and we returned 'true' to acceptDrop
	 * @param file
	 */
	public void fileDropped(File file) {
		FileParser parser = SunFishFrame.getSunFishFrame().getParserForFileAndClass(file, SequenceGroup.class);
		if (parser != null) {
			try {
				
				Object obj = parser.readFile(file);
				if (obj instanceof SequenceGroup) {
					SequenceGroup sg = (SequenceGroup)obj;
					Object[] options = {"Ignore",	"Add sequences"};
		    		int n = JOptionPane.showOptionDialog(SunFishFrame.getSunFishFrame(),
		    				"Add " + sg.size() + " sequences to this display?",
		    				"Add sequences",
		    				JOptionPane.YES_NO_OPTION,
		    				JOptionPane.QUESTION_MESSAGE,
		    				null,
		    				options,
		    				options[1]);
		    		
		    		if (n==1) {
		    			addSequences(sg);
		    		}
				}
			} catch (Exception ex) {
				ErrorWindow.showErrorWindow(ex, SunFishFrame.getSunFishFrame().getLogger());
			}
			
		}
	}
	
	/**
	 * Add the given sequences to the current SG. This also correctly handles undo actions, etc. 
	 * @param seqsToAdd The sequences to add
	 */
	public void addSequences(SequenceGroup seqsToAdd) {
		SequenceGroup sgBefore = currentSG.clone();
		for(Sequence seq : seqsToAdd.getSequences()) {
			currentSG.add(seq);
		}
		contentPane.setSequenceGroup(currentSG);
		
		
		SGUndoableAction insertAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Add sequences");
		setHasUnsavedChanges(true);
		undoManager.postNewAction(insertAction);
	}
	
	/**
	 * Attempts to add ORF annotations to all the sequences in this group
	 */
	private void addORFAnnotations() {
		SimpleORFAnnotator orfAnnotator = new SimpleORFAnnotator();
		for(int i=0; i<currentSG.size(); i++) {
			Sequence seq = currentSG.get(i);
			if (! seq.hasAnnotationOfType(SimpleORFAnnotator.annotationType))
					orfAnnotator.annotate(seq);
		}
	}
	
	/**
	 * Clear the current selection and select the columns in start (inclusive) to end (exclusive)
	 * @param start
	 * @param end
	 */
	public void selectColumns(int start, int end) {
		contentPane.clearSelection();
		contentPane.setColumnDragInterval(start, end);
	}
	
	
	public void selectColumns(ArrayList<IntegerRange> ranges, boolean posOne,
			boolean posTwo, boolean posThree) {
		
		contentPane.clearSelection(); //Using this clear selection clears it in both the table and the row header
		int sum = 0;
		for(IntegerRange range : ranges) {
			if (posOne && posTwo && posThree) {
				contentPane.addColumnSelectionInterval(Math.max(0, range.start), Math.min(contentPane.getColumnCount()-1, range.end));	
			}
			else {
				if (posOne) {
					for(int i=Math.max(0, range.start);  i<Math.min(contentPane.getColumnCount(), range.end); i+=3) {
						contentPane.addColumnSelectionInterval(i, i);
						sum++;
					}
				}
				if (posTwo) {
					for(int i=Math.max(0, range.start)+1;  i<Math.min(contentPane.getColumnCount(), range.end); i+=3) {
						contentPane.addColumnSelectionInterval(i, i);
						sum++;
					}
				}
				if (posThree) {
					for(int i=Math.max(0, range.start)+2;  i<Math.min(contentPane.getColumnCount(), range.end); i+=3) {
						contentPane.addColumnSelectionInterval(i, i);
						sum++;
					}
				}
				SunFishFrame.getSunFishFrame().setInfoLabelText("Selected " + sum + " columns");
			}
			
		}
		
	}
	
	public int getNumSelectedRows() {
		return contentPane.getNumSelectedRows();
	}
	
	public int getNumSelectedColumns() {
		return contentPane.getNumSelectedColumns();
	}
	
	public int getZeroColumn() {
		return contentPane.getZeroColumn();
	}
	
	@Override
	public Class[] getDisplayableClasses() {
		return new Class[]{SequenceGroup.class};
	}

	/**
	 * Saves the data in this sg display to the given file. The file type is currently guessed
	 * from the suffix, but this should be improved to allow the user to select from a list
	 * of possible parser / writers.
	 */
	public void saveToFile(File file) {
		String suffix = file.getAbsolutePath().substring( file.getAbsolutePath().lastIndexOf(".")+1);
		boolean saved = false;
		if (suffix.equalsIgnoreCase("fasta") || suffix.equalsIgnoreCase("fas") ) {
			FastaParser parser = new FastaParser(sunfishParent);
			parser.writeData(file, currentSG);
			setHasUnsavedChanges(false);
			saved = true;
		}
		
		if (suffix.equalsIgnoreCase("phy") || suffix.equalsIgnoreCase("ph") || suffix.equalsIgnoreCase("phylip")) {
			PhyParser parser = new PhyParser(sunfishParent);
			parser.writeData(file, currentSG);
			setHasUnsavedChanges(false);
			saved = true;
		}
		
//		if (suffix.equalsIgnoreCase("nex") || suffix.equalsIgnoreCase("nexus")) {
//			NexusParser parser = new NexusParser(sunfishParent);
//			parser.writeData(file, currentSG);	
//			setHasUnsavedChanges(false);
//			saved = true;
//		}
		
		if (suffix.equalsIgnoreCase("im") || suffix.equalsIgnoreCase("ima")) {
			IMAParser parser = new IMAParser(sunfishParent);
			parser.writeData(file, currentSG);	
			setHasUnsavedChanges(false);
			saved = true;
		}
		
		if (saved) {
			sunfishParent.getLogger().info("Saved SG file to : " + file.getAbsolutePath());
			sourceFile = file;
			SunFishFrame.getSunFishFrame().getDisplayPane().setTitleForDisplay(this, file.getName());
			SunFishFrame.getSunFishFrame().setInfoLabelText("Saved file " + file.getName());
		}
		else 
			sunfishParent.getLogger().warning("Error, did not save SG file to : " + file.getAbsolutePath());

	}
	

	
	@Override
	public UndoableAction undoAction(UndoableAction action) {
		if (action.getSource() != this) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Undo action not from this source"));
			return null;
		}

		FinalState finalState = null;
		
		if (action instanceof FinalState) {
			ErrorWindow.showErrorWindow(new IllegalStateException("Hmmm, got a final state object for undo. I don't think this should happen"));
		}
		
		if (action instanceof SGUndoableAction) {
			finalState = new FinalState(this, currentSG.clone(), hasUnsavedChanges(), formatBox.getSelectedIndex());
			SGUndoableAction sgAction = (SGUndoableAction)action;
			currentSG.setSequences( sgAction.sgBeforeRemoval );
			contentPane.setSequenceGroup( currentSG );
			//////AAAAAHHHHHH! Totally confusing! What happens if we save in a previous state, then the
			// hasUnsavedChanges info is invalidated! Do we need to compare states to know if there
			//are unsaved changes, or what? 
			this.setHasUnsavedChanges( sgAction.hasUnsavedChanges);
			repaint();
		}
		
		return finalState;
	}

	@Override
	public void redoAction(UndoableAction action) {
		
		if (action instanceof FinalState) {
			SequenceGroup sg = ((FinalState)action).finalSG;
			currentSG.setSequences(sg);
			contentPane.setSequenceGroup(currentSG);
			setHasUnsavedChanges( ((FinalState)action).hasUnsavedChanges);
		//	AbstractRowPainter painter = rowPainters.get(((FinalState)action).formatBoxIndex);
		//	contentPane.setRowPainter(painter);
			System.out.println("Reloading final state, setting appearence index back to " + ((FinalState)action).formatBoxIndex);
			contentPane.repaint();
			repaint();
			return;
		}
		
		if (action instanceof SGUndoableAction) {
			SGUndoableAction sgAction = (SGUndoableAction)action;
			currentSG.setSequences(sgAction.sgBeforeRemoval);
			contentPane.setSequenceGroup( currentSG );
			this.setHasUnsavedChanges( sgAction.hasUnsavedChanges);
			contentPane.repaint();
			repaint();
		}
		
		if (action instanceof AppeareanceAction) {
			AppeareanceAction aAction = (AppeareanceAction)action;
			//AbstractRowPainter painter = rowPainters.get(aAction.prevSelIndex);
			//System.out.println("Redoing appearence setting index back to " + aAction.prevSelIndex);
			//contentPane.setRowPainter(painter);
			repaint();
		}
		
	}
	
	/**
	 * When we are no longer focussed we need to be sure to remove anything on the glass pane
	 */
	public void lostFocus() {
		super.lostFocus(); //Some important stuff happens in the base class as well
		dropPane.closeAllTabs();
	}
	
	/**
	 * Reattach this popped display to the main tabbed pane
	 * @param evt
	 */
	protected void reattachButtonPressed(ActionEvent evt) {
		dropPane.closeAllTabs();
		reattach();		
		sunfishParent.getDisplayPane().setSelectedComponent(this);
	}
		
	
	private void letterBoxActionPerformed(java.awt.event.ActionEvent evt) {
		contentPane.setLetterMode(letterBox.getSelectedIndex());
		contentPane.repaint();
	}

	private void displaySummary() {
		SequenceGroupSummary summary = new SequenceGroupSummary(this);
		summary.analyze(title, new DisplayData(sourceFile, currentSG));
		sunfishParent.displayOutput(summary);
	}
	
	private void displayWindowChart() {
		SequenceLineChart lineChart = new SequenceLineChart(this);
		lineChart.analyze(filename, currentSG);
		sunfishParent.displayOutput(lineChart);
	}
	
	private void displayAlleleFreqChart() {
		SequenceBarChart barChart = new SequenceBarChart(this);
		barChart.analyze(filename, currentSG);
		sunfishParent.displayOutput(barChart);
	}
	
	private void displayPairwiseDifsChart() {
		PairwiseDifChart chart = new PairwiseDifChart(this);
		chart.analyze(filename, currentSG);
		sunfishParent.displayOutput(chart);
	}
	
    /**
     * Open a new DotPlot analyzer
     */
    protected void displayDotPlot() {
		DotPlotAnalyzer dotPlot = new DotPlotAnalyzer(this);
		dotPlot.analyze(title, currentSG);
		sunfishParent.displayOutput(dotPlot);		
	}

    /**
     * Show the haplotype network analyzer
     */
	protected void showHaplotypeNetwork() {
		HaplotypeNetwork hapNet = new HaplotypeNetwork(currentSG);
		HapNetworkAnalyzer hapAnalyzer = new HapNetworkAnalyzer(this);
		hapAnalyzer.analyze(title, hapNet);
		sunfishParent.displayOutput(hapAnalyzer);
	}

	/**
	 * Show the usage logo analyzer
	 */
	protected void displayUsageLogo() {
		UsageLogoChart logo = new UsageLogoChart(this);
		logo.analyze(title, currentSG);
		sunfishParent.displayOutput(logo);
	}

	/**
	 * Remove all columns that contain any sequence with a gap character
	 * This could be improved to just remove gaps from selected rows/columns, and
	 * to remove gaps from individual sites, but not entire columns....
	 * @param evt
	 */
	protected void removeGapsActionPerformed(ActionEvent evt) {
		ArrayList<Integer> gapCols = new ArrayList<Integer>();
		
		for(int i=0; i<currentSG.getMaxSeqLength(); i++) {
			if (currentSG.hasGap(i))
				gapCols.add(i);
		}

		SequenceGroup sgBefore = currentSG.clone();
		SGUndoableAction removeAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Remove gaps");

		int[] gaps = new int[gapCols.size()];
		for(int i=0; i<gaps.length; i++)
			gaps[i] = (int)gapCols.get(i);
		currentSG.removeCols(gaps);
		
		undoManager.postNewAction(removeAction);
		contentPane.setSequenceGroup(currentSG);
	}

	
	/**
	 * Remove all columns that contain any sequence with a gap character
	 * This could be improved to just remove gaps from selected rows/columns, and
	 * to remove gaps from individual sites, but not entire columns....
	 * @param evt
	 */
	protected void removeUnknownsActionPerformed(ActionEvent evt) {
		ArrayList<Integer> cols = new ArrayList<Integer>();
		
		for(int i=0; i<currentSG.getMaxSeqLength(); i++) {
			if (currentSG.hasUnknown(i))
				cols.add(i);
		}

		SequenceGroup sgBefore = currentSG.clone();
		SGUndoableAction removeAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Remove unknowns");

		int[] gaps = new int[cols.size()];
		for(int i=0; i<gaps.length; i++)
			gaps[i] = (int)cols.get(i);
		currentSG.removeCols(gaps);
		
		undoManager.postNewAction(removeAction);
		contentPane.setSequenceGroup(currentSG);
	}
	
	protected void maskColumns(ActionEvent evt) {
		if (getNumSelectedColumns()==0) {
			SunFishFrame.getSunFishFrame().setInfoLabelText("No columns selected");
			return;
		}
		
		SequenceGroup sgBefore = currentSG.clone();
		SGUndoableAction maskAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Mask columns");
		
		int[] cols = contentPane.getSelectedColumns();
		currentSG.maskColumns(cols);

		
		undoManager.postNewAction(maskAction);
		contentPane.setSequenceGroup(currentSG);
	}
	
	
	public void selectRows(String rowSelectionType, String rowMatchingType, String pattern) {
    	contentPane.clearSelection();
		//System.out.println("Got type : " + rowSelectionType + " matching: " + rowMatchingType + " pattern: " + pattern);
    	int sum = 0;
    	if (rowSelectionType.equalsIgnoreCase(ROW_SELECTION_NAME)) {
    		if (rowMatchingType.equalsIgnoreCase( ROW_SELECTION_CONTAINS)) {
    			for(int i=0; i<contentPane.getRowCount(); i++) {	
    				if ( contentPane.getSequenceForRow(i).getName().contains(pattern) ) {
    					//System.out.println("Selecting row " + i + " with value : " + sgPanel.getTable().getValueAt(i, 0).toString());
    					contentPane.selectRow(i);
    					sum++;
    				}
    			}
    			
    		}
    		if (rowMatchingType.equalsIgnoreCase(ROW_SELECTION_MATCHES)) {
    			for(int i=0; i<contentPane.getRowCount(); i++) {	
    				if ( contentPane.getSequenceForRow(i).getName().matches(pattern) ) {
    					//System.out.println("Selecting row " + i + " with value : " + sgPanel.getTable().getValueAt(i, 0).toString());
    					contentPane.selectRow(i);
    					sum++;
    				}
    			}    			
    		}
    		if (rowMatchingType.equalsIgnoreCase(ROW_SELECTION_NOT_CONTAINS)) {
    			for(int i=0; i<contentPane.getRowCount(); i++) {	
    				if (! contentPane.getSequenceForRow(i).getName().contains(pattern) ) {
    					contentPane.selectRow(i);
    					sum++;
    				}
    			}    			
    		}
    	}
		
    	SunFishFrame.getSunFishFrame().setInfoLabelText("Selected " + sum + " sequences");
	}
	
	protected void popupMenuZeroColumnActionPerformed(ActionEvent evt) {
		int col = contentPane.getColumnForPoint(previousPopupPosition);
		if (col>0)
			contentPane.setZeroColumn(col);
		contentPane.repaint();
	}

	protected void addSelectionActionPerformed(ActionEvent evt) {
		addSelectionFrame.setVisible(true);
	}
	
	protected void showRowSelectionFrame(ActionEvent evt) {
		rowSelectionFrame.setVisible(true);
	}
	
    /**
     * Sets all columns to be associated with the default partition,
     */
	protected void clearPartitions() {
		if (currentSG!=null) {
			currentSG.clearPartitions();
		}
		contentPane.repaint();
	}
	
	/**
     * Create a new partition from the current selection.
     */
	public void newPartitionFromSelection() {
		if (getNumSelectedColumns()==0)
			return;
		
		int[] cols = contentPane.getSelectedColumns();
		currentSG.createPartition(cols);
		contentPane.flashSequences();
		SunFishFrame.getSunFishFrame().setInfoLabelText("New partition from " + cols.length + " columns");
		repaint();
	}
	
	/**
	 * Select all sequences whose names appear in the list of names. This is 
	 * used by HaplotypeNetwork analyzer to select members of a particular haplotype
	 * @param names
	 */
	public void selectSequencesByName(List<String> names) {
		contentPane.clearSelection();
		int sum = 0;
		for(int i=0; i<currentSG.size(); i++) {
			if (names.contains(currentSG.get(i).getName())) {
				contentPane.selectRow(i);
				sum++;
			}
		}
		SunFishFrame.getSunFishFrame().setInfoLabelText("Selected " + sum + " sequences");
		contentPane.repaint();
	}

	protected void selectColumnsActionPerformed(ActionEvent evt) {
		columnSelectionFrame.setVisible(true);
	}


	private void popupMenuDuplicateActionPerformed(java.awt.event.ActionEvent evt ) {
		if (currentSG != null) {
			sunfishParent.displayData( new DisplayData(getSourceFile(), currentSG.clone()), filename + "[copy]" );
		}
	}

	private void popupMenuCloseActionPerformed(java.awt.event.ActionEvent evt ) {
		sunfishParent.closeSelectedDisplay();
	}

	private void popupMenuConsensusActionPerformed(java.awt.event.ActionEvent evt ) {
		System.out.println("Popup Consensus selected");
	}

	protected void exportSelectionActionPerformed(java.awt.event.ActionEvent evt ) {
		if (contentPane.getNumSelectedColumns()==0 && contentPane.getNumSelectedRows()==0) {
			return;
		}
		else {
			if (contentPane.getNumSelectedRows()>0) {
				if (contentPane.getNumSelectedRows()==1) {
					int[] rows = contentPane.getSelectedRows();
					Sequence seq = currentSG.get(rows[0]);
					sunfishParent.displayData( new DisplayData(null, seq), seq.getName());
				}
				else {

					sunfishParent.displayData( new DisplayData(null, currentSG.getRows(contentPane.getSelectedRows())), "Selected Sequences");
				}
			}
			else {
				sunfishParent.displayData( new DisplayData(null, currentSG.getCols(contentPane.getSelectedColumns())), "Selected Columns");    				
			}
		}
	}

	/**
	 * Removes currently selected rows / columns and posts a new undo action 
	 */
	void removeSelection() {
		if (contentPane.getNumSelectedColumns()==0 && contentPane.getNumSelectedRows()==0) {
			return;
		}
		else {
			String message = "";
			if (contentPane.getNumSelectedRows()>0 ) {
				SequenceGroup sgBefore = currentSG.clone();

				currentSG.removeRows(contentPane.getSelectedRows());
				SGUndoableAction removeAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Remove rows");
				setHasUnsavedChanges(true);
				undoManager.postNewAction(removeAction);
				message = "Removed " + contentPane.getNumSelectedRows() + " sequences";
			}
			else {
				SequenceGroup sgBefore = currentSG.clone();

				int[] cols = contentPane.getSelectedColumns();
	    			
	    			currentSG.removeCols(cols);
	    			
	    			SGUndoableAction removeAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Remove columns");
	    			setHasUnsavedChanges(true);
	    			undoManager.postNewAction(removeAction);
	    			message = "Removed " + cols.length + " columns";
	    			
			}

			contentPane.setSequenceGroup(currentSG);
			contentPane.clearSelection();
			SunFishFrame.getSunFishFrame().setInfoLabelText(message);
			contentPane.drawAllImages();
			contentPane.repaint();
		}    	
	}

	@Override
	protected boolean update(SunFishFrame parent, DisplayData data) {
		super.removeProgressPanel();
		currentSG = (SequenceGroup)data.getData(0);
		contentPane.setSequenceGroup(currentSG);
		contentPane.setToNaturalSize();
		scrollPane.getVerticalScrollBar().setUnitIncrement(contentPane.getRowHeight());
		scrollPane.getHorizontalScrollBar().setUnitIncrement(Math.max(5, contentPane.getColumnWidth()));

        JPanel corner1 = new JPanel();
        corner1.setBackground(Color.white);
        scrollPane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, corner1);
        JPanel corner2 = new JPanel();
        corner2.setBackground(Color.white);
        scrollPane.setCorner(JScrollPane.LOWER_LEFT_CORNER, corner2);
        JPanel corner3 = new JPanel();
        corner3.setBackground(Color.white);
        scrollPane.setCorner(JScrollPane.UPPER_LEFT_CORNER, corner3);
		
		try {
			//Find the right index for gc-at row painter
			int index = 0;
			for(int i=0; i<rowPainters.size(); i++) {
				if (rowPainters.get(i).equals(GC_AT_RowPainter.getIdentifier())) {
					index = i;
					break;
				}
			}
			changeRowPainter(index);
		} catch (Exception ex) {
			ErrorWindow.showErrorWindow(ex);
		}
        zoomValueChanged();
        
		repaint();
		
		if (parent.getAnalysisPane().getCurrentAnalyzer()==null) {
			displaySummary();
		}
		return true;
	}

	
	public void construct() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		setBackground(Color.white);
		contentPane = new SGContentPanel(this);
		scrollPane = new JScrollPane(contentPane);
		scrollPane.setBorder(null);
		scrollPane.setViewportBorder(null);

		topPanel = new JPanel();
		topPanel.setLayout(new BorderLayout());
		topPanel.setMinimumSize(new Dimension(10, 22));
		topPanel.setPreferredSize(new Dimension(500, 22));
		topPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
		topPanel.setBackground(Color.WHITE);
	
		this.add(topPanel);
		
		JPanel fillerPanel = new JPanel();
		fillerPanel.setMinimumSize(new Dimension(10, 25));
		fillerPanel.setPreferredSize(new Dimension(10, 25));
		fillerPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 25));
		fillerPanel.setBackground(Color.white);
		this.add(fillerPanel);

		this.add(scrollPane);
		
		previousPopupPosition = new Point(0, 0);
		
		contentPane.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent me) {
				requestFocusInWindow();		
			}
			
			public void mouseClicked(MouseEvent me) {
				requestFocusInWindow();		
			}
		});

		
    	rowSelectionFrame = new RowSelectionFrame(this);
    	columnSelectionFrame = new ColumnSelectionFrame(this);
    	addSelectionFrame = new AddSelectionFrame(sunfishParent.getDisplayPane(), this);
    	
		Font defaultFont = new Font("Sans", Font.PLAIN, 11);
		
		dropPane = new GlassDropPane(sunfishParent);
    	dropPane.setBackground(Color.white);
    	dropPane.setAlignmentX(Component.LEFT_ALIGNMENT);
    	dropPane.setAlignmentY(Component.TOP_ALIGNMENT);
		
		
		zoomSlider = new JSlider();
		zoomSlider.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				zoomValueChanged();
			}
		});
		zoomSlider.setPreferredSize(new Dimension(100, 20));
		zoomSlider.setToolTipText("Change column width");
		zoomSlider.setFont(new Font("Sans", Font.PLAIN, 0));
		zoomSlider.setPaintLabels(false);
		zoomSlider.setPaintTicks(false);
		
		topPanel.add(zoomSlider, BorderLayout.EAST);
		
    	optionsPane = new GlassPaneThing(dropPane, upperPanel);
		optionsPane.setVisible(false);
		
    	appearancePane = new GlassPaneThing(dropPane, upperPanel);
		appearancePane.setVisible(false);
		
		analysisPane = new GlassPaneThing(dropPane, upperPanel);
		analysisPane.setVisible(false);
		
		selectionPane = new GlassPaneThing(dropPane, upperPanel);
		selectionPane.setVisible(false);
		
		editingPane = new GlassPaneThing(dropPane, upperPanel);
		editingPane.setVisible(false);

        
        JButton saveButton = new CFButton("Save" , getIcon("save_22x22.png"));
		saveButton.setToolTipText("Save");
		saveButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	try {
					save();
				} catch (SaveCancelledException e) {
					//User cancelled the save, do nothing. 
				}
            }
        });
		optionsPane.addComponent(saveButton);
		
        reattachButton = new JButton("Reattach");
		reattachButton.setToolTipText("Move to main window");
		reattachButton.setEnabled(false);
		reattachButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                reattachButtonPressed(evt);
            }
        });
		optionsPane.addComponent(reattachButton);
		
//		JButton test = new JButton("Resize header");
//		test.addActionListener(new ActionListener() {
//			public void actionPerformed(ActionEvent arg0) {
//				
//				sgPanel.getRowHeaderView().setMaximumSize(new Dimension(50, Integer.MAX_VALUE));
//				sgPanel.getRowHeaderView().setMinimumSize(new Dimension(50, 1));
//				sgPanel.revalidate();
//				sgPanel.repaint();
//			}
//		});
//		optionsPane.addComponent(test);
		
        JButton summaryButton = new CFButton("Summary", getIcon("summary_22x22.png"));
        summaryButton.setFont(defaultFont);
		summaryButton.setToolTipText("Summary");
		summaryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displaySummary();
            }
        });
		analysisPane.addComponent(summaryButton);
		
		
        JButton chartButton = new CFButton("Line charts", getIcon("lineCharts.png"));
        chartButton.setFont(defaultFont);
		chartButton.setToolTipText("Show Line Charts");
		chartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displayWindowChart();
            }
        });
		analysisPane.addComponent(chartButton);
        
		JButton barChartButton = new CFButton("Spectrum", getIcon("barchart.png"));
		barChartButton.setToolTipText("Show allele frequency distribution");
		barChartButton.setFont(defaultFont);
		barChartButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displayAlleleFreqChart();
            }
        });
		analysisPane.addComponent(barChartButton);
		
		JButton pairwiseDifsButton = new CFButton("Pairwise difs.", getIcon("pairDifChart.png"));
		pairwiseDifsButton.setToolTipText("Show the distirbution of pairwise differences");
		pairwiseDifsButton.setFont(defaultFont);
		pairwiseDifsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displayPairwiseDifsChart();
            }
        });
		analysisPane.addComponent(pairwiseDifsButton);
		
		JButton networkButton = new CFButton("Network", getIcon("networkIcon.png"));
		networkButton.setToolTipText("Show haplotype network");
		networkButton.setFont(defaultFont);
		networkButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	showHaplotypeNetwork();
            }
        });
		analysisPane.addComponent(networkButton);
		
		
		usageLogoButton = new CFButton("Usage", getIcon("usageIcon.png"));
		usageLogoButton.setToolTipText("Show base pair usage diagram");
		usageLogoButton.setFont(defaultFont);
		usageLogoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displayUsageLogo();
            }
        });
		analysisPane.addComponent(usageLogoButton);
		
		
		JButton dotPlotButton = new CFButton("Dot plot", getIcon("dotPlotIcon.png"));
		dotPlotButton.setToolTipText("Show a dot plot");
		dotPlotButton.setFont(defaultFont);
		dotPlotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	displayDotPlot();
            }
        });
		analysisPane.addComponent(dotPlotButton);

		
		translateBox = new JComboBox();
		translateBox.setBackground(Color.WHITE);
		translateBox.setFont(new Font("Sans", Font.PLAIN, 11));
		FormatBoxMenuItem trItem1 = new FormatBoxMenuItem("Nucleotides");
		FormatBoxMenuItem trItem2 = new FormatBoxMenuItem("Amino acids");
		translateBox.addItem(trItem1);
		translateBox.addItem(trItem2);
		translateBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				switchTranslation();
			}
		});
		appearancePane.addComponent(translateBox);
		
		
		formatBox = new JComboBox();
		formatBox.setFont(new Font("Sans", Font.PLAIN, 11));
		
		//Construction of actual items in the list happens in update(..) since we need to pass 
		//references to the currentSG to each row painter, which we don't have until update(..) is called
		formatBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeRowPainter(formatBox.getSelectedIndex());
            }
        });
		
        //Construction of the row painters for both the nucleotide format box and
        //aa format box happens here, since we need references to the currentSG to create them
        constructRowPainters();
        int GCIndex = 0;
		for(String id : rowPainters) {
			FormatBoxMenuItem formatItem = new FormatBoxMenuItem(id);
			formatBox.addItem(formatItem);
			if (formatItem.getText().equals(GC_AT_RowPainter.getIdentifier())) {
				GCIndex = formatBox.getItemCount();
			}
		}
        
		formatBox.setSelectedIndex(GCIndex-1);
		//TODO fix rowpainter selection stuff
		
		colorLabel = new JLabel("Colors:");
		colorLabel.setFont(defaultFont);
		appearancePane.addComponent(colorLabel);
		appearancePane.addComponent(formatBox);

		frameLabel = new JLabel("Frame");
		frameLabel.setFont(defaultFont);
		aaFormatBox = new JComboBox();
		aaFormatBox.setFont(new Font("Sans", Font.PLAIN, 11));
		for(String id : aaRowPainters) {
			FormatBoxMenuItem formatItem = new FormatBoxMenuItem(id);
			aaFormatBox.addItem(formatItem);
		}
		aaFormatBox.setSelectedIndex(0);
		aaFormatBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeRowPainter(aaFormatBox.getSelectedIndex());
            }
        });
		
		letterBox = new JComboBox();
		letterBox.setBackground(Color.WHITE);
		letterBox.setFont(new Font("Sans", Font.PLAIN, 11));
		FormatBoxMenuItem letterItem1 = new FormatBoxMenuItem("All");
		FormatBoxMenuItem letterItem2 = new FormatBoxMenuItem("None");
		FormatBoxMenuItem letterItem3 = new FormatBoxMenuItem("Difs.");

		letterBox.addItem(letterItem1);
		letterBox.addItem(letterItem2);
		letterBox.addItem(letterItem3);
		letterBox.setSelectedIndex(0);
		letterBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                letterBoxActionPerformed(evt);
            }
        });
		letterLabel = new JLabel("Symbols:");
		letterLabel.setFont(defaultFont);
		
		JLabel frameLabel = new JLabel("Frame:");
		frameLabel.setFont(defaultFont);
		
		frameBox = new JComboBox();
		FormatBoxMenuItem frame0 = new FormatBoxMenuItem("+0");
		FormatBoxMenuItem frame1 = new FormatBoxMenuItem("+1");
		FormatBoxMenuItem frame2 = new FormatBoxMenuItem("+2");
		FormatBoxMenuItem framem0 = new FormatBoxMenuItem("-0");
		FormatBoxMenuItem framem1 = new FormatBoxMenuItem("-1");
		FormatBoxMenuItem framem2 = new FormatBoxMenuItem("-2");
		frameBox.addItem(frame0);
		frameBox.addItem(frame1);
		frameBox.addItem(frame2);
		frameBox.addItem(framem0);
		frameBox.addItem(framem1);
		frameBox.addItem(framem2);
		frameBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				changeFrame();
			}
		});
		
		appearancePane.addComponent(letterLabel);
		appearancePane.addComponent(letterBox);
		

		selectRowsButton = new CFButton("Select rows", getIcon("selectRowsDialog.png"));
		selectRowsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showRowSelectionFrame(evt);
            }
        });
		selectionPane.addComponent(selectRowsButton);
		
		selectColumnsButton = new CFButton("Select Columns", getIcon("selectColumnsDialog.png"));
		selectColumnsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                selectColumnsActionPerformed(evt);
            }
        });
		selectionPane.addComponent(selectColumnsButton);
		
//		addSelectionToButton = new CFButton("Add selection..", getIcon("addSelection.png"));
//		addSelectionToButton.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                addSelectionActionPerformed(evt);
//            }
//        });
//		selectionPane.addComponent(addSelectionToButton);
		
		newPartitionButton = new CFButton("New partition", getIcon("addPartition.png"));
		newPartitionButton.addActionListener(new ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				newPartitionFromSelection();
            }
		});
		selectionPane.addComponent(newPartitionButton);
		
		JButton managePartitionsButton = new CFButton("Clear partitions", getIcon("clearPartitions.png"));
		managePartitionsButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				clearPartitions();	
			}
		});
		selectionPane.addComponent(managePartitionsButton);
	
		
		JButton removeGapsButton = new CFButton("Remove gaps", getIcon("removeGaps.png"));
		removeGapsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	removeGapsActionPerformed(evt);
            }
        });
		editingPane.addComponent(removeGapsButton);
		
		JButton removeUnknownsButton = new CFButton("Remove ?s", getIcon("removeGaps.png"));
		removeUnknownsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	removeUnknownsActionPerformed(evt);
            }
        });
		editingPane.addComponent(removeUnknownsButton);
		
		
		removeSelectionButton = new CFButton("Remove selection", getIcon("removeSelection.png"));
		removeSelectionButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	removeSelection();
            }
        });
		editingPane.addComponent(removeSelectionButton);
		
		exportSelection = new CFButton("Export selection", getIcon("exportSelection.png"));
		exportSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	exportSelectionActionPerformed(evt);
            }
        });
		editingPane.addComponent(exportSelection);
		
		CFButton maskColumnsButton = new CFButton("Mask",  getIcon("maskCols.png"));
		maskColumnsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	maskColumns(evt);
            }
        });
		editingPane.addComponent(maskColumnsButton);
		
			
		
        
        
        dropPane.addPanel("Options", optionsPane);
        dropPane.addPanel("Appearance", appearancePane);
        dropPane.addPanel("Analysis", analysisPane);
        dropPane.addPanel("Selection", selectionPane);
        dropPane.addPanel("Editing", editingPane);
        
        topPanel.add(dropPane, BorderLayout.WEST);

        
      /// Popup Menu ///

		popup = new JPopupMenu();
		popup.setBorder(BorderFactory.createLineBorder(Color.DARK_GRAY) );
		if (!SunFishFrame.getSunFishFrame().onAMac())
			popup.setBackground(new Color(100,100,100) );
		
		JMenuItem popupItemCopy = new JMenuItem("Copy");
		popupItemCopy.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                copy();
            }
        });
		popup.add(popupItemCopy);
		
		JMenuItem popupItemCut = new JMenuItem("Cut");
		popupItemCut.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cut();
            }
        });
		popup.add(popupItemCut);
		
		JMenuItem popupItemPaste = new JMenuItem("Paste");
		popupItemPaste.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                paste();
            }
        });
		popup.add(popupItemPaste);
		
		popup.add(GTKFixSeparator.makeSeparator());
		
		JMenuItem popupItemClose = new JMenuItem("Close");
		popupItemClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuCloseActionPerformed(evt);
            }
        });
		popup.add(popupItemClose);
		

		JMenuItem popupItemDuplicate = new JMenuItem("Duplicate");
		popupItemDuplicate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuDuplicateActionPerformed(evt);
            }
        });
		popup.add(popupItemDuplicate);

		JMenuItem popupItemAddToAnalyzer = new JMenuItem("Add to analyzer");
		popupItemAddToAnalyzer.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
            	popupItemAddToAnalyzer(evt);
            }
        });
		popup.add(popupItemAddToAnalyzer);
		
		JMenuItem popupItemConsensus = new JMenuItem("Display Consensus");
		popupItemConsensus.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuConsensusActionPerformed(evt);
            }
        });
		popup.add(popupItemConsensus);
		
		
		popupItemZeroColumn = new JMenuItem("Set as Column 0");
		popupItemZeroColumn.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                popupMenuZeroColumnActionPerformed(evt);
            }
        });
		popup.add(popupItemZeroColumn);
		
		
		popupItemDisplaySelection = new JMenuItem("Display Selection");
		popupItemDisplaySelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exportSelectionActionPerformed(evt);
            }
        });
		popup.add(popupItemDisplaySelection);
		popupItemDisplaySelection.setEnabled(false);
		
		popupItemRemoveSelection = new JMenuItem("Remove Selection");
		popupItemRemoveSelection.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeSelection();
            }
        });
		popup.add(popupItemRemoveSelection);
		popupItemRemoveSelection.setEnabled(false);
		
		PopupListener popupListener = new PopupListener(); 
		contentPane.addMouseListener(popupListener);
	}



	/**
	 * Attempt to add this sequenceGroup to the current analyzer through use of the 
	 * analyzer.addObjectData(sg) method.  This won't work if there's no current
	 * analyzer or if it doesn't accept SequenceGroups in the method. 
	 * @param evt
	 */
	protected void popupItemAddToAnalyzer(ActionEvent evt) {
		Analyzable analyzer = SunFishFrame.getSunFishFrame().getCurrentAnalyzer();
		if (analyzer != null) {
			analyzer.addObjectData(currentSG);
		}
	}

	/**
	 * Called when the 'frame box' has changed, which changes the translation frame
	 */
	protected void changeFrame() {
		if (frameBox.getSelectedItem().toString().equals("+0")) {
			contentPane.setRowPainterTranslationFrame(0, false);
		}
		if (frameBox.getSelectedItem().toString().equals("+1")) {
			contentPane.setRowPainterTranslationFrame(1, false);
		}
		if (frameBox.getSelectedItem().toString().equals("+2")) {
			contentPane.setRowPainterTranslationFrame(2, false);
		}
		if (frameBox.getSelectedItem().toString().equals("-0")) {
			contentPane.setRowPainterTranslationFrame(0, true);
		}
		if (frameBox.getSelectedItem().toString().equals("-1")) {
			contentPane.setRowPainterTranslationFrame(1, true);
		}
		if (frameBox.getSelectedItem().toString().equals("-2")) {
			contentPane.setRowPainterTranslationFrame(2, true);
		}
		
	}


	/**
	 * Called when the user switches between nucleotide vs. amino acid mode
	 */
	public void switchTranslation() {
		//System.out.println("Switiching translation prev mode: " + prevTranslationMode + " translation box index: " + translateBox.getSelectedIndex());
		//Only call if the mode has actually changed, this prevents multiple calls with the same mode which creates a bug
		if (prevTranslationMode != translateBox.getSelectedIndex()) {
			if (translateBox.getSelectedIndex()==0) {
				appearancePane.removeComponent(frameLabel);
				appearancePane.removeComponent(frameBox);
				appearancePane.removeComponent(colorLabel);
				appearancePane.removeComponent(aaFormatBox);
				appearancePane.addComponent(colorLabel);
				appearancePane.addComponent(formatBox);
				appearancePane.addComponent(letterLabel);
				appearancePane.addComponent(letterBox);
				changeRowPainter(formatBox.getSelectedIndex());
			}
			else {
				appearancePane.removeComponent(colorLabel);
				appearancePane.removeComponent(letterLabel);
				appearancePane.removeComponent(formatBox);
				appearancePane.removeComponent(letterBox);

				appearancePane.addComponent(frameLabel);
				appearancePane.addComponent(frameBox);
				appearancePane.addComponent(colorLabel);
				appearancePane.addComponent(aaFormatBox);
				changeRowPainter(aaFormatBox.getSelectedIndex());
			}
		}
		prevTranslationMode = translateBox.getSelectedIndex(); 
	}


	/**
	 * Called when the frame containing this display changes, here we need to notify the guiWidgets.glassDropPane
	 * to use the new frame
	 */
	public void setFrame(JFrame newFrame) {
		this.myFrame = newFrame;
		dropPane.changeParent(myFrame);
		if (myFrame==sunfishParent) {
			reattachButton.setEnabled(false);
		}
		else {
			reattachButton.setEnabled(true);
		}
	}
	
	
	/**
	 * Creates a new TransferableSeqeucenes object with the sequences constructed from the current
	 * selection state of the current sg, and returns it. If no sequences are selected an empty
	 * (but not null) SG is returned  
	 */
	public Transferable copyData() {
		SequenceGroup sel = contentPane.getSelectionAsSG();
		if (sel.size() > 0) {
			if (contentPane.selectionMode==Selection.ROWS) {
				SunFishFrame.getSunFishFrame().setInfoLabelText("Copied " + sel.size() + " sequences");
			}
			if (contentPane.selectionMode==Selection.COLUMNS) {
				SunFishFrame.getSunFishFrame().setInfoLabelText("Copied " + sel.getMaxSeqLength() + " columns");
			}
		}
		contentPane.flashSequences();
		return new TransferableSequences(sel);
	}
	
	/**
	 * Called when the user chooses cut from the edit menu and this Display is the current focusOwner
	 * (as defined in TransferActionListener). Displays which support data cutting should return the cut
	 * data here. 
	 * @return The data cut from this display
	 */
	public Transferable cutData() {
		SequenceGroup sel = contentPane.getSelectionAsSG();
		removeSelection();
		if (sel.size() > 0) {
			if (contentPane.selectionMode==Selection.ROWS) {
				SunFishFrame.getSunFishFrame().setInfoLabelText("Cut " + sel.size() + " sequences");
			}
			if (contentPane.selectionMode==Selection.COLUMNS) {
				SunFishFrame.getSunFishFrame().setInfoLabelText("Cut " + sel.getMaxSeqLength() + " columns");
			}
		}
		return new TransferableSequences(sel);
	}
	
	/**
	 * Called when the user pastes data into this display.
	 * @param data
	 * @throws IOException 
	 * @throws UnsupportedFlavorException 
	 */
	public void pasteData(Transferable data) {
		//try to get a sequence group
		try {
			Object obj = data.getTransferData(new DataFlavor(SequenceGroup.class, "SequenceGroup"));
			SequenceGroup newSeqs = (SequenceGroup)obj;
			
			Object[] options = {"New Sequences", "New Columns", "Cancel"};
			int n = JOptionPane.showOptionDialog(SunFishFrame.getSunFishFrame(),
					"Paste sequence data as new sequences or columns?",
					"Paste sequences",
					JOptionPane.YES_NO_CANCEL_OPTION,
					JOptionPane.QUESTION_MESSAGE,
					null,
					options,
					options[2]);
			if (n==0) {
				SequenceGroup clonedSG = new SequenceGroup();
				for(int i=0; i<newSeqs.size(); i++) {
					Sequence seq = newSeqs.get(i).clone();
					clonedSG.add(seq);
				}
				addSequences(clonedSG); //This method handles undo, etc. 
			}
			if (n==1) {
				PasteColumnsFrame pasteFrame = new PasteColumnsFrame(this, newSeqs);
			}
			
		} catch (UnsupportedFlavorException e) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Caught unsupported flavor exception while attepmting to paste sequence group data into " + this);
		} catch (IOException e) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Caught IO (?) exception while attepmting to paste sequence group data into " + this);
		}
		catch (ClassCastException ccex) {
			SunFishFrame.getSunFishFrame().getLogger().warning("Caught class cast (?) exception while attepmting to paste sequence group data into " + this);
		}
		
	}
	
	/**
	 * Insert the given sequences as new columns in the current sg. 
	 * @param newSeqs The sequences to be inserted
	 * @param pos The position at which at insert the sequences (0 indicates the start)
	 * @param findMatches If sequences should be added to those with the same name in the current sg
	 * @param ignoreUnknowns If sequences without a name match are ignored. Otherwise they will be added as new sequences
	 */
	public void insertSGAsColumns(SequenceGroup newSeqs, int pos, boolean findMatches, boolean ignoreUnknowns) {
		SequenceGroup sgBefore = currentSG.clone();
		for(int i=0; i<newSeqs.size(); i++) {
			Sequence receiver = null;
		
			if (findMatches) {
				receiver = currentSG.getSequenceForName(newSeqs.get(i).getName());
				if (receiver == null && (!ignoreUnknowns))  {
					receiver = new StringSequence("", newSeqs.get(i).getName());
					currentSG.add(receiver);
				}
			}
			else {
				if (i<currentSG.size())
					receiver = currentSG.get(i);
				else {
					receiver = new StringSequence("", newSeqs.get(i).getName());
					currentSG.add(receiver);
				}
			}
			
			if (receiver != null)
				receiver.insert(newSeqs.get(i).toString(), pos, true);	
		}
		
		contentPane.setSequenceGroup(currentSG);
		setHasUnsavedChanges(true);
		SGUndoableAction insertAction = new SGUndoableAction(this, sgBefore, hasUnsavedChanges(), "Insert columns");
		undoManager.postNewAction(insertAction);
	}
	
	/**
	 * Change the row painter to that given by the currently selected index in the formatBox. The meaning
	 * of the argument is slightly context-dependent. If we're in "nucleotide mode", which means that the
	 * nucleotide item is selected in the translate combo box, then the argument is assumed to be the index
	 * of the row painter in the rowPainters list. Otherwise, it is assumed that we're in Amino Acid mode,
	 * and the argument refers to the index of the aaRowPainter to be chosen. 
	 * 
	 */
	protected void changeRowPainter(int index) {
		
		if (currentSG == null) //Prevents errors when attempting to instantiate rowPainters with a null sequence group
			return;
		
		if (translateBox.getSelectedIndex()==0) { //We're in nucleotide mode

			AbstractRowPainter.Instantiator maker = rowPainterMap.get(rowPainters.get(index));
			System.out.println("In nucleotide mode, changing row painter class to index " + index + " which has id: " + rowPainters.get(index) );
			AbstractRowPainter painter = null;
			
			painter = maker.getNewRowPainter(currentSG);
			
			if (painter instanceof ORFRowPainter) {
				addORFAnnotations();
			}
			contentPane.setRowPainter(painter);
		}
		else { //We're in AA viewing mode
			AbstractRowPainter.Instantiator maker = rowPainterMap.get(aaRowPainters.get(index));
			System.out.println("In AA mode, changing row painter class to index " + index + " which has id: " + rowPainters.get(index)  );

			AbstractRowPainter painter = null;
			painter = maker.getNewRowPainter(currentSG);
			
			painter.setTranslate(true, GeneticCodeFactory.createGeneticCode(GeneticCodes.Universal), 0, false); //Last two values are ignored && set by changeFrame
			contentPane.setRowPainter(painter);
			changeFrame(); //Must come after we set the new row painter, also sets rev. comp.
		}
	}


	private class FormatBoxMenuItem extends JLabel {

		public FormatBoxMenuItem(String text) {
			super(text);
			setBackground(new Color(253, 253, 253));
		}

		public String toString() {
			return getText();
		}
	}
	
	private class PopupListener extends MouseAdapter {
	    public void mousePressed(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    public void mouseReleased(MouseEvent e) {
	        maybeShowPopup(e);
	    }

	    private void maybeShowPopup(MouseEvent e) {
	        if (e.isPopupTrigger()) {
	        	if (contentPane.getNumSelectedColumns()==0 && contentPane.getNumSelectedRows()==0) {
	        		popupItemDisplaySelection.setEnabled(false);
	        		popupItemRemoveSelection.setEnabled(false);
	        	}
	        	else {
	        		popupItemDisplaySelection.setEnabled(true);
	        		popupItemRemoveSelection.setEnabled(true);
	        		if (contentPane.getNumSelectedColumns()>0) {
	        			popupItemDisplaySelection.setText("Display Selected Columns");
	        			popupItemRemoveSelection.setText("Remove Selected Columns");
	        		}
	        		else {
	        			popupItemDisplaySelection.setText("Display Selected Sequences");
	        			popupItemRemoveSelection.setText("Remove Selected Sequences");
	        		}
	        	}
	            popup.show(e.getComponent(),
	                       e.getX(), e.getY());
	            previousPopupPosition.x = e.getX();
	            previousPopupPosition.y = e.getY();
	        }
	    }
	}
	
	
	class FinalState extends UndoableAction {

		SequenceGroup finalSG;
		boolean hasUnsavedChanges;
		int formatBoxIndex;
		
		public FinalState(UndoableActionSource source, SequenceGroup sg, boolean hasUnsavedChanges, int formatIndex) {
			super(source);
			finalSG = sg;
			this.hasUnsavedChanges = hasUnsavedChanges;
			formatBoxIndex = formatIndex;
		}

		@Override
		/**
		 * Ignored, we only use this for 'final state' stuff
		 */
		public String getDescription() {
			return "Final state";
		}
		
	}
	
	class AppeareanceAction extends UndoableAction {

		int prevSelIndex;
		
		public AppeareanceAction(UndoableActionSource source, int prevSelectedIndex) {
			super(source);
			prevSelIndex = prevSelectedIndex;
		}

		@Override
		public String getDescription() {
			return "Appearence mode";
		}
		
	}
	
//	class RemoveColumnsAction extends UndoableAction {
//		
//		SequenceGroup sgBeforeRemoval;
//
//		public RemoveColumnsAction(UndoableActionSource source, SequenceGroup sgBefore) {
//			super(source);
//			sgBeforeRemoval = sgBefore;
//		}
//
//		@Override
//		public String getDescription() {
//			return "Remove columns";
//		}	
//	}
//	
//	class InsertColumnsAction extends UndoableAction {
//		
//		SequenceGroup sgBeforeRemoval;
//
//		public InsertColumnsAction(UndoableActionSource source, SequenceGroup sgBefore) {
//			super(source);
//			sgBeforeRemoval = sgBefore;
//		}
//
//		@Override
//		public String getDescription() {
//			return "Insert columns";
//		}	
//	}
//	
//	class InsertRowsAction extends UndoableAction {
//		
//		SequenceGroup sgBeforeRemoval;
//
//		public InsertRowsAction(UndoableActionSource source, SequenceGroup sgBefore) {
//			super(source);
//			sgBeforeRemoval = sgBefore;
//		}
//
//		@Override
//		public String getDescription() {
//			return "Add sequences";
//		}	
//	}
//	
//	class RemoveGapsAction extends UndoableAction {
//		
//		SequenceGroup sgBeforeRemoval;
//
//		public RemoveGapsAction(UndoableActionSource source, SequenceGroup sgBefore) {
//			super(source);
//			sgBeforeRemoval = sgBefore;
//		}
//		
//		@Override
//		public String getDescription() {
//			return "Remove gaps";
//		}
//	}
//	
//	class RemoveRowsAction extends UndoableAction {
//		
//		SequenceGroup sgBeforeRemoval;
//
//		public RemoveRowsAction(UndoableActionSource source, SequenceGroup sgBefore) {
//			super(source);
//			sgBeforeRemoval = sgBefore;
//		}
//
//		@Override
//		public String getDescription() {
//			return "Remove rows";
//		}	
//	}
	
	private JPopupMenu popup;
	private JMenuItem popupItemDisplaySelection;
	private JMenuItem popupItemRemoveSelection;
	private JMenuItem popupItemZeroColumn;	
	Point previousPopupPosition;
	
	
	JSlider zoomSlider;
	JLabel letterLabel;
	JLabel colorLabel;
	JLabel frameLabel;
	private JComboBox frameBox;
	private JComboBox translateBox;
	private JComboBox aaFormatBox;
	private JComboBox formatBox;
	private JComboBox letterBox;
	ColumnSelectionFrame columnSelectionFrame;
	plugins.SGPlugin.display.RowSelectionFrame rowSelectionFrame;
	plugins.SGPlugin.display.AddSelectionFrame addSelectionFrame;
	JButton newPartitionButton;
	JButton networkButton;
	JButton pairwiseDifsButton;
	JButton removeSelectionButton;
	JButton selectRowsButton;
	JButton selectColumnsButton;
	JButton exportSelection;
	JButton addSelectionToButton;
	JButton reattachButton;
	JButton usageLogoButton;
	JPanel upperPanel;
	JPanel fillerPanel;
	GlassDropPane dropPane;
	GlassPaneThing optionsPane;
	GlassPaneThing appearancePane;
	GlassPaneThing analysisPane;
	GlassPaneThing selectionPane;
	GlassPaneThing editingPane;

	    	
    	public static final String ROW_SELECTION_NAME = "name";
    	public static final String ROW_SELECTION_ATTRIBUTE = "attribute";
    	public static final String ROW_SELECTION_CONTAINS = "containing";
    	public static final String ROW_SELECTION_MATCHES = "matching";
    	public static final String ROW_SELECTION_NOT_CONTAINS = "not containing";


}
