package gui;

import gui.document.ACGDocument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTree;
import javax.swing.SpinnerNumberModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import logging.PropertyLogger;
import logging.StateLogger;
import mcmc.MCMC;
import mcmc.mc3.MC3;

import component.LikelihoodComponent;

import parameter.AbstractParameter;
import parameter.CompoundParameter;
import sequence.Alignment;
import xml.XMLLoader;

/**
 * A different (prettier) panel allowing the user to pick some properties of parameters and likelihoods to monitor. 
 * @author brendano
 *
 */
public class PickMonitorsPanel extends JPanel {

	ACGDocument file;
	ACGFrame acgParent;
	
	//Holds list of things we'll display in the next panel
	List<PlottableInfo> selectedPlottables = new ArrayList<PlottableInfo>();
	
	public PickMonitorsPanel(ACGFrame acgParent, ACGDocument file) {
		this.acgParent = acgParent;
		this.file = file;
		initComponents();
		setBackground(ACGFrame.backgroundColor);
	}
	
	/**
	 * Adds various plottable items from the given parameter to the given tree
	 * @param tree
	 * @param root
	 * @param param
	 */
	private void addPropertiesToTree(JTree tree, DefaultMutableTreeNode root, AbstractParameter<?> param) {
		if (param instanceof CompoundParameter) {
			return;
		}
		
		DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(param.getName());
		
		String[] logKeys = param.getLogKeys();
		for(int i=0; i<logKeys.length; i++) {
			PlottableInfo info = new PlottableInfo();
			info.label = param.getName();
			info.key = logKeys[i];
			info.descriptor = logKeys[i];
			DefaultMutableTreeNode node = new DefaultMutableTreeNode(info.descriptor);
			node.setUserObject(info);
			topNode.add(node);
		}
		root.add(topNode);
	}
	
	private void addPropertiesToTree(JTree tree, DefaultMutableTreeNode root, LikelihoodComponent like) {
		DefaultMutableTreeNode topNode = new DefaultMutableTreeNode(like.getAttribute(XMLLoader.NODE_ID));
		PlottableInfo info = new PlottableInfo();
		info.label = like.getAttribute(XMLLoader.NODE_ID);
		info.descriptor = "Log Likelihood";
		DefaultMutableTreeNode node = new DefaultMutableTreeNode(info.descriptor);
		node.setUserObject(info);
		topNode.add(node);
		root.add(topNode);
	}
	
	private void initComponents() {
		this.setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		JLabel topLabel = new JLabel("Choose properties to monitor from the lists below");
		topPanel.add(topLabel);
		add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		
		DefaultMutableTreeNode paramRoot = new DefaultMutableTreeNode("Root");
		paramTree = new JTree(paramRoot);
		
		List<String> paramLabels = file.getParameterLabels();
		List<String> likeLabels = file.getLikelihoodLabels();
		
		//All the things we could potentially plot
		List<PlottableInfo> plottables = new ArrayList<PlottableInfo>();
		for(String pLabel : paramLabels) {
			Object obj;
			try {
				obj = file.getObjectForLabel(pLabel);
				if (obj instanceof AbstractParameter<?>) {
					addPropertiesToTree(paramTree, paramRoot, (AbstractParameter<?>)obj);
				}
			} catch (InstantiationException e) {
				ErrorWindow.showErrorWindow(e);
			} catch (IllegalAccessException e) {
				ErrorWindow.showErrorWindow(e);
			} catch (InvocationTargetException e) {
				ErrorWindow.showErrorWindow(e);
			}
			
		}
		
		//Add mc.speed plottable info to list as a special item
		PlottableInfo speedInfo = new PlottableInfo();
		speedInfo.label = "mc.speed";
		speedInfo.descriptor = "Speed of chain (states / sec)";
		DefaultMutableTreeNode utilNode = new DefaultMutableTreeNode("Utilities");
		DefaultMutableTreeNode rateNode = new DefaultMutableTreeNode("Chain rate (states / sec)");
		utilNode.add(rateNode);
		rateNode.setUserObject(speedInfo);
		paramRoot.add(utilNode);

		
		
		paramTree.expandRow(0);
		paramTree.setRootVisible(false);
		paramTree.setCellRenderer(new PlottableItemRenderer());
		for(int i=0; i<paramRoot.getChildCount(); i++) {
			TreePath path = new TreePath( ((DefaultMutableTreeNode)paramRoot.getChildAt(i)).getPath() );
			paramTree.expandPath( path );
		}
		paramTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				TreePath path = paramTree.getPathForLocation(me.getX(), me.getY());
				if (path != null) {
					Object obj = path.getLastPathComponent();
					if (obj != null && obj instanceof DefaultMutableTreeNode) {
						toggleSelectedParam( (DefaultMutableTreeNode)obj);
					}
				}
			}
		});
		
		
		//plottables.add(speedInfo);
		
		JPanel paramPanel = new JPanel();
		paramPanel.setLayout(new BorderLayout());
		JScrollPane paramScrollPane = new JScrollPane(paramTree);
		paramScrollPane.setPreferredSize(new Dimension(200, 400));
		JLabel paramTopLabel = new JLabel("<html><b>Parameter properties:</b><html>");
		paramPanel.add(paramTopLabel, BorderLayout.NORTH);
		paramPanel.add(paramScrollPane, BorderLayout.CENTER);
		centerPanel.add(paramPanel);
		
		
		DefaultMutableTreeNode likeRoot = new DefaultMutableTreeNode("Root");
		likeTree = new JTree(likeRoot);
				
		//All the things we could potentially plot
		for(String pLabel : likeLabels) {
			Object obj;
			try {
				obj = file.getObjectForLabel(pLabel);
				if (obj instanceof LikelihoodComponent) {
					addPropertiesToTree(likeTree, likeRoot, (LikelihoodComponent)obj);
				}
			} catch (InstantiationException e) {
				ErrorWindow.showErrorWindow(e);
			} catch (IllegalAccessException e) {
				ErrorWindow.showErrorWindow(e);
			} catch (InvocationTargetException e) {
				ErrorWindow.showErrorWindow(e);
			}	
		}
		
		likeTree.expandRow(0);
		likeTree.setRootVisible(false);
		likeTree.setCellRenderer(new PlottableItemRenderer());
		for(int i=0; i<likeRoot.getChildCount(); i++) {
			TreePath path = new TreePath( ((DefaultMutableTreeNode)likeRoot.getChildAt(i)).getPath() );
			likeTree.expandPath( path );
		}
		likeTree.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
				TreePath path = likeTree.getPathForLocation(me.getX(), me.getY());
				if (path != null) {
					Object obj = path.getLastPathComponent();
					if (obj != null && obj instanceof DefaultMutableTreeNode) {
						toggleSelectedLikelihood( (DefaultMutableTreeNode)obj);
					}
				}
			}
		});
		


		JPanel likesPanel = new JPanel();
		likesPanel.setLayout(new BorderLayout());
		
		JScrollPane likesScrollPane = new JScrollPane(likeTree);
		likesScrollPane.setPreferredSize(new Dimension(200, 400));
		JLabel likeTopLabel = new JLabel("<html><b>Likelihood components:</b><html>");
		likesPanel.add(likeTopLabel, BorderLayout.NORTH);
		likesPanel.add(likesScrollPane, BorderLayout.CENTER);
		centerPanel.add(Box.createHorizontalStrut(4));
		centerPanel.add(likesPanel);
		
		JPanel infoPanel = new JPanel();
		infoPanel.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 2));
		infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
		infoPanel.setAlignmentX(LEFT_ALIGNMENT);
		infoPanel.setPreferredSize(new Dimension(250, 400));
		infoPanel.add(new JLabel("<html><b>Analysis properties :</b></html>"));
		
		int modParamCount = countModifiableParameters();
		int likelihoodCount = countLikelihoods();
		int loggerCount = countLoggers();
		int chainLength = findChainLength();
		
		infoPanel.add(Box.createVerticalStrut(6));
		JLabel totParams = new JLabel("Total parameters: " + countAllParameters());
		infoPanel.add(totParams);
		
		JLabel modParams = new JLabel("Modifiable parameters : " + modParamCount);
		infoPanel.add(Box.createVerticalStrut(2));
		infoPanel.add(modParams);
		
		JLabel totLikes = new JLabel("Total likelihoods : " + likelihoodCount);
		infoPanel.add(Box.createVerticalStrut(2));
		infoPanel.add(totLikes);
		
		JLabel chainLengthLabel = new JLabel("Chain length : " + chainLength);
		infoPanel.add(Box.createVerticalStrut(2));
		infoPanel.add(chainLengthLabel);
		
		infoPanel.add(Box.createVerticalStrut(20));
		JLabel loggerLabel = new JLabel("Total loggers : " + loggerCount);
		infoPanel.add(loggerLabel);
		infoPanel.add(Box.createVerticalStrut(2));
		
		List<String> loggerLabels = (file.getLabelForClass(PropertyLogger.class));
		List<String> stateLoggers = (file.getLabelForClass(StateLogger.class));
		loggerLabels.addAll(stateLoggers);
		JPanel loggerPanel = new JPanel();
		loggerPanel.setLayout(new BoxLayout(loggerPanel, BoxLayout.Y_AXIS));
		loggerPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 0));
		for(String logLabel : loggerLabels) {
			JLabel lab = new JLabel(logLabel);
			loggerPanel.add(lab);
		}
		infoPanel.add(loggerPanel);
		
		infoPanel.add(Box.createVerticalGlue());
		
		JPanel bottomInfoPanel = new JPanel();
		bottomInfoPanel.setLayout(new BoxLayout(bottomInfoPanel, BoxLayout.X_AXIS));
		bottomInfoPanel.setPreferredSize(new Dimension(250, 30));
		bottomInfoPanel.setMaximumSize(new Dimension(1050, 30));
		
		SpinnerNumberModel model = new SpinnerNumberModel((int)(chainLength*0.10), 0, chainLength, 1000);
		burninSpinner = new JSpinner(model);
		bottomInfoPanel.add(new JLabel("Burnin :"));
		bottomInfoPanel.add(burninSpinner);
		burninSpinner.setPreferredSize(new Dimension(100, 30));
		bottomInfoPanel.setAlignmentX(LEFT_ALIGNMENT);
		bottomInfoPanel.add(Box.createGlue());
		JButton startButton = new JButton("Start");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startChain();
			}
		});
		
		bottomInfoPanel.add(startButton);
		infoPanel.add(bottomInfoPanel);
		centerPanel.add(infoPanel);
		this.add(centerPanel, BorderLayout.CENTER);	
	}
	
	/**
	 * Returns a count of all property loggers and state loggers
	 * @return
	 */
	private int countLoggers() {
		List<String> loggerLabels = (file.getLabelForClass(PropertyLogger.class));
		List<String> stateLoggers = (file.getLabelForClass(StateLogger.class));
		return loggerLabels.size() + stateLoggers.size();
		
	}

	private int findChainLength() {
		if (file.hasMC3()) {
			List<String> mc3Labels = (file.getLabelForClass(MC3.class));
			MC3 mc3;
			try {
				mc3 = (MC3)file.getObjectForLabel(mc3Labels.get(0));
				return mc3.getRunLength();
			} catch (Exception e) {
				ErrorWindow.showErrorWindow(e);
			} 
			
		}
		else {
			List<String> mcLabels = file.getMCMCLabels();
			MCMC chain;
			try {
				chain = (MCMC)file.getObjectForLabel(mcLabels.get(0));
				return chain.getUserRunLength();
			} catch (Exception e) {
				ErrorWindow.showErrorWindow(e);
			}
			
		}
		
		return 0;
	}

	/**
	 * Counts the number of likelihood components
	 * @return
	 */
	private int countLikelihoods() {
		return file.getLikelihoodLabels().size();
	}

	/**
	 * Total number of parameters
	 * @return
	 */
	private int countAllParameters() {
		return file.getParameterLabels().size();
	}
	
	/**
	 * Counts the number of parameters which have at least one modifier
	 * @return
	 */
	private int countModifiableParameters() {
		int sum = 0;
		List<String> parLabels = file.getParameterLabels();
		for(String par : parLabels) {
			Object obj;
			try {
				obj = file.getObjectForLabel(par);
				if (obj instanceof AbstractParameter<?>) {
					AbstractParameter<?> param = (AbstractParameter<?>)obj;
					if (param.getModifierCount()>0)
						sum++;
				}
			} catch (Exception e) {
				ErrorWindow.showErrorWindow(e);
			} 
			
		}
		return sum;
	}

	/**
	 * Called when the user clicks on param tree, toggles selected state
	 * @param dNode
	 */
	protected void toggleSelectedParam(DefaultMutableTreeNode dNode) {
		Object obj = dNode.getUserObject();
		if (obj != null && obj instanceof PlottableInfo) {
			PlottableInfo info = (PlottableInfo)obj;
			if (info.selected) {
				selectedPlottables.remove(info);
			}
			else {
				selectedPlottables.add(info);				
			}
			info.selected = !info.selected;
			paramTree.repaint();
		}
	}

	/**
	 * Called when the user clicks on param tree, toggles selected state
	 * @param dNode
	 */
	protected void toggleSelectedLikelihood(DefaultMutableTreeNode dNode) {
		Object obj = dNode.getUserObject();
		if (obj != null && obj instanceof PlottableInfo) {
			PlottableInfo info = (PlottableInfo)obj;
			if (info.selected) {
				selectedPlottables.remove(info);
			}
			else {
				selectedPlottables.add(info);				
			}
			info.selected = !info.selected;
			likeTree.repaint();
		}
	}
	
	
	
	/**
	 * Actually start executing the chain. This initializes the MainOutputPanel with whatever is in the
	 * selectedPlottables list, and calls runMCMC on the 'file' field 
	 */
	protected void startChain() {
		MCMC chain;
		try {
			int freq;
			int runMax;
			
			if (file.hasMC3()) {
				List<String> mc3Labels = (file.getLabelForClass(MC3.class));
				MC3 mc3 = (MC3)file.getObjectForLabel(mc3Labels.get(0));
				chain = mc3.getColdChain();
				runMax = mc3.getRunLength();
				
			}
			else {
				List<String> mcLabels = file.getMCMCLabels();
				chain = (MCMC)file.getObjectForLabel(mcLabels.get(0));
				runMax = chain.getUserRunLength();
			}
		
			freq = runMax / 4000;
			if (freq < 1000)
				freq = 1000;	

			//It's OK to have gaps now!
//			List<String> alignmentLabels = file.getLabelForClass(Alignment.class);
//			for(String alnLabel : alignmentLabels) {
//				Object obj = file.getObjectForLabel(alnLabel);
//				if (obj instanceof Alignment) {
//					Alignment aln = (Alignment)obj;
//					if (aln.hasGapOrUnknown()) {
//						JOptionPane.showMessageDialog(this, "The alignment with label " + alnLabel + " has gaps (-) or unknown (N, ?). Please remove these columns before running the file.");
//						return;
//					}
//				}
//			}
			
			
			int burnin = (Integer)burninSpinner.getValue();
			MainOutputFrame outputPane = new MainOutputFrame(chain, freq, burnin);

			//Attempt to turn off System.out writing for state loggers...
			List<String> stateLoggerLabels = file.getLabelForClass(StateLogger.class);
			for(String loggerLabel : stateLoggerLabels) {
				Object obj = file.getObjectForLabel(loggerLabel);
				if (obj instanceof StateLogger) { //Should be one of these, but you never know
					((StateLogger)obj).removeStream(System.out);
				}
			}
			
			
			for(PlottableInfo plottable : selectedPlottables) {
				if (plottable.label.equals("mc.speed")) {
					outputPane.addMonitor(new SpeedMonitor(burnin));
				}
				else {
					Object obj = file.getObjectForLabel(plottable.label);
					if (obj instanceof AbstractParameter<?>) {
						outputPane.addChart( (AbstractParameter<?>)obj, plottable.key);
					}
					if (obj instanceof LikelihoodComponent) {
						outputPane.addMonitor( (LikelihoodComponent)obj);
					}
				}
			}

			acgParent.initializeProgressBar(chain, runMax);
			acgParent.replaceCenterPanel(outputPane);
			
			int charts = outputPane.getMonitorCount();
			if (charts == 0) {
				acgParent.setPreferredSize(new Dimension(350, 70));
				acgParent.setSize(new Dimension(350, 70));
			}
			else {
				if (charts < 4) {
					acgParent.setPreferredSize(new Dimension(1000, 500));
					acgParent.setSize(new Dimension(1000, 500));
				}
				else {
					acgParent.setPreferredSize(new Dimension(1000, 600));
					acgParent.setSize(new Dimension(1000, 800));
				}
			}
			acgParent.pack();
			acgParent.validate();
			
			
			ExecutingChain runner = file.runMCMC();
			acgParent.setRunner(runner);
		} catch (InstantiationException e) {
			ErrorWindow.showErrorWindow(e);
		} catch (IllegalAccessException e) {
			ErrorWindow.showErrorWindow(e);
		}
		catch (RuntimeException rex) {
			ErrorWindow.showErrorWindow(rex);
		} catch (Exception e) {
			ErrorWindow.showErrorWindow(e);
		}
	}
	
	//Small class to store some info about a particular plottable (monitorable) item
	class PlottableInfo {
		String label; //Label of the object
		String key;	  //LogKey of item to log
		boolean selected = false; //Whether or not item is selected
		String descriptor;
	}
		
	private JSpinner burninSpinner;
	private JTree paramTree;
	private JTree likeTree;
}
