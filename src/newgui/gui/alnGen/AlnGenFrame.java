package newgui.gui.alnGen;

import gui.ErrorWindow;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import newgui.gui.filepanel.InputFilesManager;

import sequence.Alignment;
import sequence.BasicSequenceAlignment;
import sequence.Sequence;
import tools.alnGen.AlignmentGenerator;
import tools.alnGen.ProtoSequence;
import tools.alnGen.SampleReader;
import tools.alnGen.VCFReader;

/**
 * Main frame for alignment creation from Reference + VCF
 * @author brendano
 *
 */
public class AlnGenFrame extends JFrame {

	List<SampleReader> sampleReaders = new ArrayList<SampleReader>();
	
	public AlnGenFrame() {
		super("Alignment Creation");
		initComponents();
		setLocationRelativeTo(null);
		pack();
	}
	
	
	
	private void initComponents() {
		this.setPreferredSize(new Dimension(500, 500));
		this.getRootPane().setLayout(new BorderLayout());
		
		JPanel topInfoPanel = new JPanel();
		topInfoPanel.setLayout(new BorderLayout());
		topInfoPanel.setBorder(BorderFactory.createEmptyBorder(8, 10, 4, 10));
		topInfoPanel.setPreferredSize(new Dimension(500, 60));
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
		referenceFileField.setPreferredSize(new Dimension(100, 32));
		referenceFileField.setMaximumSize(new Dimension(100, 32));
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
		regionPanel.add(new JLabel("Contig:"));
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
		
		
		
		//Contains vcf & sample selection panels on left and produced sequences on right 
		JPanel lowerPanel = new JPanel();
		mainPanel.add(lowerPanel, BorderLayout.CENTER);
		lowerPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
		
		JPanel lowerLeftPanel = new JPanel();
		lowerLeftPanel.setLayout(new BoxLayout(lowerLeftPanel, BoxLayout.Y_AXIS));
		lowerPanel.add(lowerLeftPanel);
		
		JPanel lowerRightPanel = new JPanel();
		lowerRightPanel.setLayout(new BoxLayout(lowerRightPanel, BoxLayout.Y_AXIS));
		lowerPanel.add(lowerRightPanel);
		
		JPanel pickVCFPanel = new JPanel();
		pickVCFPanel.setLayout(new BoxLayout(pickVCFPanel, BoxLayout.X_AXIS));
		pickVCFPanel.add(new JLabel("VCF file:"));
		vcfFileField = new JTextField("choose");
		vcfFileField.setPreferredSize(new Dimension(120, 32));
		vcfFileField.setMaximumSize(new Dimension(120, 32));
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
		lowerLeftPanel.add(pickVCFPanel);
		
		vcfSampleList = new JList();
		JScrollPane sampleScrollPane = new JScrollPane(vcfSampleList);
		vcfSampleList.setPreferredSize(new Dimension(100, 200));
		sampleScrollPane.setPreferredSize(new Dimension(100, 200));
		lowerLeftPanel.add(sampleScrollPane);
		
		final JButton addSampleButton = new JButton("Add sample");
		addSampleButton.setToolTipText("Add the sample to new alignment");
		addSampleButton.setEnabled(false);
		addSampleButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				addSelectedSamples();
			}
		});
		vcfSampleList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				int count = vcfSampleList.getSelectedValues().length;
				if (count == 0)
					addSampleButton.setEnabled(false);
				else
					addSampleButton.setEnabled(true);
			}
		});
		lowerLeftPanel.add(addSampleButton);
		
		
		DefaultListModel defaultModel = new DefaultListModel();
		addedSamplesList = new JList(defaultModel);
		JScrollPane addedSamplesSP = new JScrollPane(addedSamplesList);
		lowerRightPanel.add(addedSamplesSP);
		addedSamplesSP.setPreferredSize(new Dimension(100, 200));
		JButton clearSamplesButton = new JButton("Clear");
		clearSamplesButton.setToolTipText("Remove added samples");
		clearSamplesButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				clearAddedSamples();
			}
		});
	
		
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				cancel();
			}
		});
		bottomPanel.add(Box.createHorizontalGlue());
		bottomPanel.add(cancelButton);
		JButton buildButton = new JButton("Build Alignment");
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
	 * Use the current reference, contig, positions, and samples to create an actual alignment. The user
	 * is prompted to save the new alignment on successful creation
	 */
	protected void buildAlignment() {
		 //Do some error checking! we need a valid
		
		for(SampleReader reader : sampleReaders) {
			alnGen.addSampleReader(reader);
		}
		
		String contig = contigField.getText();
		Integer startPos = Integer.parseInt(startPosField.getText());
		Integer endPos = Integer.parseInt(endPosField.getText());
		try {
			List<ProtoSequence> protoSeqs = alnGen.getAlignment(contig, startPos, endPos);
			BasicSequenceAlignment aln = new BasicSequenceAlignment();
			for(ProtoSequence pSeq : protoSeqs) {
				aln.addSequence( pSeq.toSimpleSequence() );
			}
			
			

			InputFilesManager.getManager().saveAlignment(aln, "new_alignment");
			
		} catch (IOException e) {
			e.printStackTrace();
			ErrorWindow.showErrorWindow(e, "Could not create alignment");
		}
		
		
	}


	/**
	 * Close this JFrame
	 */
	protected void cancel() {
		this.setVisible(false);
		this.dispose();
	}



	protected void clearAddedSamples() {
		sampleReaders.clear();
	}



	protected void addSelectedSamples() {
		Object[] samples = vcfSampleList.getSelectedValues();
		for(int i=0; i<samples.length; i++) {
			String sampleName = samples[i].toString();
			SampleReader reader = vcfReader.getReaderForSample(sampleName, 0);
			sampleReaders.add(reader);
			((DefaultListModel)addedSamplesList.getModel()).addElement(sampleName);
			System.out.println("Adding reader for sample " + sampleName);
		}
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
		}
	}


	/**
	 * Remove current contents of samples list and fill it with samples read from 
	 * the currently selected vcf file
	 */
	private void updateSampleListFromVCF() {
		if (vcfReader == null)
			throw new IllegalStateException("VCF Reader not initialized");
		
		DefaultListModel model = new DefaultListModel();
		for(String name : vcfReader.getSampleNames()) {
			model.addElement(name);
		}
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
			alnGen = new AlignmentGenerator(referenceFile);
		}
	}


	JTextField contigField;
	JTextField startPosField;
	JTextField endPosField;
	
	private VCFReader vcfReader = null; //Reads sample names and creates SampleReaders from a vcf file
	private AlignmentGenerator alnGen = null; //Used to actually build the alignments, initialized when a reference is picked
	private JList addedSamplesList;
	private JList vcfSampleList;
	private File referenceFile = null;
	private JFileChooser fileChooser = new JFileChooser();
	private JTextField referenceFileField;
	private JTextField vcfFileField;
	private File vcfFile = null;
	
	private final String infoText = "Create a new alignment by selecting a reference file and samples from one or more vcf files.";
}
