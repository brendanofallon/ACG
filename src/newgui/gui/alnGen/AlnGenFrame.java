package newgui.gui.alnGen;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import app.ACGProperties;

import newgui.ErrorWindow;
import newgui.datafile.AlignmentFile;
import newgui.gui.ViewerWindow;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.Sequence;
import tools.alnGen.AlignmentGenerator;
import tools.alnGen.ContigNotFoundException;
import tools.alnGen.ProtoSequence;
import tools.alnGen.SampleReader;
import tools.alnGen.VCFReader;

/**
 * Main frame for alignment creation from Reference + VCF
 * @author brendano
 *
 */
public class AlnGenFrame extends JFrame {
	
	public static final String REFERENCE_PROP = "vcf.reference";

	List<SampleReader> sampleReaders = new ArrayList<SampleReader>();
	
	public AlnGenFrame() {
		super("Alignment Creation");
		initComponents();
		setLocationRelativeTo(null);
		pack();
	}
	
	/**
	 * Use the current reference, contig, positions, and samples to create an actual alignment. The user
	 * is prompted to save the new alignment on successful creation
	 */
	protected void buildAlignment() {		
		for(SampleReader reader : sampleReaders) {
			reader.reset();
			alnGen.addSampleReader(reader);
		}
		
		String contig = contigField.getText();
		if (contig == null || contig.trim().length()==0) {
			JOptionPane.showMessageDialog(this, "Please enter a chromosome");
			return;
		}
		contig = contig.replace("chr", "");
		if (startPosField.getText() == null || startPosField.getText().trim().length()==0) {
			JOptionPane.showMessageDialog(this, "Please enter a valid starting position");
			return;
		}
		if (startPosField.getText() == null || startPosField.getText().trim().length()==0) {
			JOptionPane.showMessageDialog(this, "Please enter a valid end position");
			return;
		}
		
		Integer startPos = Integer.parseInt(startPosField.getText());
		Integer endPos = Integer.parseInt(endPosField.getText());
		
		if (endPos <= startPos) {
			JOptionPane.showMessageDialog(this, "Please choose a start position before the end position");
			return;		
		}

		final BuilderWorker builder = new BuilderWorker(contig, startPos, endPos);
		
		Container rootPane = this.getRootPane();
		rootPane.removeAll();
		JPanel newPanel = new JPanel();
		newPanel.setLayout(new BoxLayout(newPanel, BoxLayout.Y_AXIS));
		rootPane.add(newPanel, BorderLayout.CENTER);
		newPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JLabel msgLabel = new JLabel("Creating alignment... this may take a few minutes");
		newPanel.add(Box.createVerticalGlue());
		newPanel.add(msgLabel);
		
		progressBar = new JProgressBar();
		progressBar.setIndeterminate(true);
		newPanel.add(progressBar);
		
				
		newPanel.add(Box.createVerticalGlue());
		
		JButton cancelBuildButton = new JButton("Cancel");
		cancelBuildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				builder.cancel(true);
			}
		});
		newPanel.add(cancelBuildButton);
		
		rootPane.validate();
		rootPane.repaint();
		
		builder.execute();
	}
	
	/**
	 * Convert the list of protosequences to a BasicSequenceAlignment and prompt the user
	 * to save it - this happens when an alignment is done being generated
	 * @param seqs
	 */
	protected void buildAndSaveAlignment(List<ProtoSequence> seqs) {
		progressBar.setIndeterminate(false);
		progressBar.setValue(100);
		BasicSequenceAlignment aln = new BasicSequenceAlignment();
		for(ProtoSequence pSeq : seqs) {
			aln.addSequence( pSeq.toSimpleSequence() );
		}

		AlignmentFile alnFile = new AlignmentFile(aln);
		ViewerWindow.getViewer().getFileManager().showSaveDialog(alnFile, "new alignment");
		this.setVisible(false);
	}

	/**
	 * Close this JFrame
	 */
	protected void cancel() {
		this.setVisible(false);
		this.dispose();
	}


	
	/**
	 * Remove all samples from added samples list
	 */
	protected void clearAddedSamples() {
		sampleReaders.clear();
		DefaultListModel newModel = new DefaultListModel();
		addedSamplesList.setModel(newModel);
		addedSamplesList.repaint();
		addedSamplesHeader.setText("No samples added");
		addedSamplesHeader.revalidate();
		buildButton.setEnabled(false);
	}



	protected void addSelectedSamples(int phase) {
		Object[] samples = vcfSampleList.getSelectedValues();
		for(int i=0; i<samples.length; i++) {
			String sampleName = samples[i].toString();
			SampleReader reader = vcfReader.getReaderForSample(sampleName, phase);
			sampleReaders.add(reader);
			((DefaultListModel)addedSamplesList.getModel()).addElement(sampleName + " (" + phase + ")");
		}
		addedSamplesHeader.setText(sampleReaders.size() + " samples added");
		if (referenceFile != null)
			buildButton.setEnabled(true);
		addedSamplesHeader.revalidate();
	}



	protected void browseForVCFFile() {
		int n = fileChooser.showOpenDialog(this);
		if (n == JFileChooser.APPROVE_OPTION) {
			vcfFile = fileChooser.getSelectedFile();
			vcfFileField.setText(vcfFile.getName());
			try {
				vcfReader = new VCFReader(vcfFile);
				updateSampleListFromVCF();
			} catch (IOException e) {
				e.printStackTrace();
				ErrorWindow.showErrorWindow(e, "Could not open vcf file " + vcfFile.getName());
			}
			catch (Exception e) {
				e.printStackTrace();
				ErrorWindow.showErrorWindow(e, "Error reading vcf file " + vcfFile.getName());
			}
		}
	}


	/**
	 * Remove current contents of samples list and fill it with samples read from 
	 * the currently selected vcf file
	 */
	private void updateSampleListFromVCF() {
		if (vcfReader == null)
			throw new IllegalStateException("VCF Reader not initialized");
		
		if (vcfReader.getSampleNames().size() == 0) {
			JOptionPane.showMessageDialog(this, "No samples found in vcf file");
		}
		
		DefaultListModel model = new DefaultListModel();
		for(String name : vcfReader.getSampleNames()) {
			model.addElement(name);
		}
		sampleListHeader.setText(vcfReader.getSourceFile().getName() + ": " + vcfReader.getSampleCount() + " samples found");
		sampleListHeader.revalidate();
		vcfSampleList.setModel(model);
		vcfSampleList.repaint();
	}



	/**
	 * Open the file chooser and let user pick a reference file. 
	 */
	protected void browseForReferenceFile() {
		int n = fileChooser.showOpenDialog(this);
		if (n == JFileChooser.APPROVE_OPTION) {
			referenceFile = fileChooser.getSelectedFile();
			referenceFileField.setText(referenceFile.getName());
			ACGProperties.addProperty(REFERENCE_PROP, referenceFile.getAbsolutePath());
			alnGen = new AlignmentGenerator(referenceFile);
			if (sampleReaders.size()>0)
				buildButton.setEnabled(true);
		}
	}

	
	private void initComponents() {
		this.setPreferredSize(new Dimension(550, 550));
		this.getRootPane().setLayout(new BorderLayout());
		
		JPanel topInfoPanel = new JPanel();
		topInfoPanel.setLayout(new BorderLayout());
		topInfoPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
		topInfoPanel.setPreferredSize(new Dimension(550, 60));
		JTextArea info = new JTextArea();
		topInfoPanel.setBackground(info.getBackground());
		info.setText(infoText);
		info.setLineWrap(true);
		info.setWrapStyleWord(true);
		info.setEditable(false);
		topInfoPanel.add(info, BorderLayout.CENTER);
		this.getRootPane().add(topInfoPanel, BorderLayout.NORTH);
		
		JPanel mainPanel = new JPanel();
		this.getRootPane().add(mainPanel, BorderLayout.CENTER);
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		mainPanel.setLayout(new BorderLayout());
		
		JPanel refRegionPanel = new JPanel();
		refRegionPanel.setLayout(new BoxLayout(refRegionPanel, BoxLayout.Y_AXIS));
		mainPanel.add(refRegionPanel, BorderLayout.NORTH);
		
		//Panel containing label and selector for reference file
		JPanel refPanel = new JPanel();
		refPanel.setLayout(new BoxLayout(refPanel, BoxLayout.X_AXIS));
		refPanel.setAlignmentX(LEFT_ALIGNMENT);
		refRegionPanel.add(refPanel);
		refPanel.add(new JLabel("Reference file:"));
		referenceFileField = new JTextField("choose file");
		String previousReference = ACGProperties.getProperty(REFERENCE_PROP);
		if (previousReference != null) {
			File testRef = new File(previousReference);
			if (testRef.exists() && testRef.canRead()) {
				referenceFile = testRef;
				referenceFileField.setText(testRef.getName());
			}
		}
		referenceFileField.setPreferredSize(new Dimension(150, 32));
		referenceFileField.setMaximumSize(new Dimension(150, 32));
		refPanel.add(referenceFileField);
		JButton chooseButton = new JButton("Choose");
		chooseButton.setToolTipText("Browse for reference file");
		chooseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForReferenceFile();
			}
		});
		refPanel.add(chooseButton);

		JPanel regionPanel = new JPanel();
		regionPanel.setAlignmentX(LEFT_ALIGNMENT);
		regionPanel.setLayout(new BoxLayout(regionPanel, BoxLayout.X_AXIS));
		regionPanel.add(new JLabel("Chromosome:"));
		contigField = new JTextField("");
		contigField.setPreferredSize(new Dimension(50, 32));
		contigField.setMaximumSize(new Dimension(50, 50));
		regionPanel.add(contigField);
		regionPanel.add(Box.createHorizontalStrut(8));
		regionPanel.add(new JLabel("Start:"));
		startPosField = new JTextField();
		startPosField.setPreferredSize(new Dimension(100, 32));
		startPosField.setMaximumSize(new Dimension(100, 50));
		regionPanel.add(startPosField);
		regionPanel.add(Box.createHorizontalStrut(8));
		regionPanel.add(new JLabel("End:"));
		endPosField = new JTextField();
		endPosField.setPreferredSize(new Dimension(100, 32));
		endPosField.setMaximumSize(new Dimension(100, 50));
		regionPanel.add(endPosField);
		refRegionPanel.add(regionPanel);
		refRegionPanel.add(Box.createVerticalStrut(10));
		refRegionPanel.add(new JSeparator(JSeparator.HORIZONTAL));
		
		//VCF selection panel
		JPanel pickVCFPanel = new JPanel();
		pickVCFPanel.setLayout(new BoxLayout(pickVCFPanel, BoxLayout.X_AXIS));
		pickVCFPanel.setAlignmentX(LEFT_ALIGNMENT);
		pickVCFPanel.add(new JLabel("VCF file:"));
		vcfFileField = new JTextField("choose");
		vcfFileField.setMinimumSize(new Dimension(160, 20));
		vcfFileField.setPreferredSize(new Dimension(160, 32));
		vcfFileField.setMaximumSize(new Dimension(160, 32));
		pickVCFPanel.add(vcfFileField);
		JButton chooseVCFButton = new JButton("Choose");
		chooseVCFButton.setToolTipText("Browse for vcf file");
		chooseVCFButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				browseForVCFFile();
			}
		});
		vcfFileField.add(chooseVCFButton);
		pickVCFPanel.add(vcfFileField);
		pickVCFPanel.add(chooseVCFButton);
		refRegionPanel.add(pickVCFPanel);
		
		
		//Contains sample selection panels on left and produced sequences on right 
		JPanel lowerPanel = new JPanel();
		mainPanel.add(lowerPanel, BorderLayout.CENTER);
		lowerPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		
		JPanel lowerLeftPanel = new JPanel();
		lowerLeftPanel.setLayout(new BoxLayout(lowerLeftPanel, BoxLayout.Y_AXIS));
		lowerLeftPanel.setPreferredSize(new Dimension(250, 300));
		lowerPanel.add(lowerLeftPanel);
		
		JPanel lowerRightPanel = new JPanel();
		lowerRightPanel.setLayout(new BoxLayout(lowerRightPanel, BoxLayout.Y_AXIS));
		lowerRightPanel.setPreferredSize(new Dimension(250, 300));
		lowerPanel.add(lowerRightPanel);
		
		sampleListHeader = new JLabel("No samples loaded");
		lowerLeftPanel.add(sampleListHeader);
		
		vcfSampleList = new JList();
		JScrollPane sampleScrollPane = new JScrollPane(vcfSampleList);
		//sampleScrollPane.setMaximumSize(new Dimension(200, 200));
		lowerLeftPanel.add(sampleScrollPane);
		
		final JButton addSampleP0Button = new JButton("Add samples (phase 0)");
		addSampleP0Button.setToolTipText("Add the phase 0 variants from the selected samples");
		addSampleP0Button.setEnabled(false);
		addSampleP0Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addSelectedSamples(0);
			}
		});
		
		final JButton addSampleP1Button = new JButton("Add samples (phase 1)");
		addSampleP1Button.setToolTipText("Add the phase 0 variants from the selected samples");
		addSampleP1Button.setEnabled(false);
		addSampleP1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addSelectedSamples(1);
			}
		});
		
		vcfSampleList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				int count = vcfSampleList.getSelectedValues().length;
				if (count == 0) {
					addSampleP0Button.setEnabled(false);
					addSampleP1Button.setEnabled(false);
				}
				else {
					addSampleP0Button.setEnabled(true);
					addSampleP1Button.setEnabled(true);
				}
			}
		});
		lowerLeftPanel.add(addSampleP0Button);
		lowerLeftPanel.add(addSampleP1Button);
		
		//Lower right panel: contains a list showing which samples have been added
		
		addedSamplesHeader = new JLabel("No samples added");
		//lowerRightPanel.add(Box.createVerticalStrut(30));
		lowerRightPanel.add(addedSamplesHeader);
		DefaultListModel defaultModel = new DefaultListModel();
		addedSamplesList = new JList(defaultModel);
		JScrollPane addedSamplesSP = new JScrollPane(addedSamplesList);
		lowerRightPanel.add(addedSamplesSP);
		//addedSamplesSP.setPreferredSize(new Dimension(150, 200));
		JButton clearSamplesButton = new JButton("Clear");
		clearSamplesButton.setToolTipText("Remove added samples");
		clearSamplesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearAddedSamples();
			}
		});
	
		
		
		
		//Bottom panel with a cancel and build button
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(cancelButton);
		buildButton = new JButton("Build Alignment");
		buildButton.setEnabled(false);
		buildButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				buildAlignment();
			}
		});
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(buildButton);
		bottomPanel.add(Box.createHorizontalGlue());
		this.getRootPane().add(bottomPanel, BorderLayout.SOUTH);
	}

	
	/**
	 * Tiny class to build alignments in background...
	 * @author brendan
	 *
	 */
	class BuilderWorker extends SwingWorker {
		
		final String contig;
		final int startPos;
		final int endPos;
		
		public BuilderWorker(String contig, int startPos, int endPos) {
			this.contig = contig;
			this.startPos = startPos;
			this.endPos = endPos;
		}
		
		@Override
		protected Object doInBackground() throws Exception {
			setProgress(2);
			List<ProtoSequence> protoSeqs = alnGen.getAlignmentParallel(contig, startPos, endPos);
			setProgress(100);
			buildAndSaveAlignment(protoSeqs);
			return null;
		}
		
	}

	private JProgressBar progressBar;
	private JTextField contigField;
	private JTextField startPosField;
	private JTextField endPosField;
	private JButton cancelButton;
	private JLabel addedSamplesHeader;
	private JLabel sampleListHeader;
	private VCFReader vcfReader = null; //Reads sample names and creates SampleReaders from a vcf file
	private AlignmentGenerator alnGen = null; //Used to actually build the alignments, initialized when a reference is picked
	private JList addedSamplesList;
	private JList vcfSampleList;
	private File referenceFile = null;
	private JFileChooser fileChooser = new JFileChooser();
	private JTextField referenceFileField;
	private JTextField vcfFileField;
	private File vcfFile = null;
	private JButton buildButton;
	
	private final String infoText = "Create a new alignment by selecting a reference file and samples from one or more vcf files.";
}
