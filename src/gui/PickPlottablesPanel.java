package gui;

import gui.document.ACGDocument;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
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

import arg.ARG;

import logging.StateLogger;
import mcmc.MCMC;
import mcmc.mc3.MC3;
import parameter.AbstractParameter;
import parameter.CompoundParameter;
import parameter.DoubleParameter;
import sequence.Alignment;

import component.LikelihoodComponent;

public class PickPlottablesPanel extends JPanel {

	ACGDocument file;
	ACGFrame acgParent;
	
	//Holds list of things we'll display in the next panel
	List<PlottableInfo> selectedPlottables = new ArrayList<PlottableInfo>();
	
	public PickPlottablesPanel(ACGFrame acgParent, ACGDocument file) {
		this.acgParent = acgParent;
		this.file = file;
		initComponents();
		setBackground(ACGFrame.backgroundColor);
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(new JLabel("Pick items to plot from list below"));
		topPanel.add(Box.createHorizontalGlue());
		topPanel.setBackground(ACGFrame.backgroundColor);
		
		this.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.X_AXIS));
		centerPanel.setBackground(ACGFrame.backgroundColor);
		List<String> paramLabels = file.getParameterLabels();
		List<String> likeLabels = file.getLikelihoodLabels();
		
		//All the things we could potentially plot
		List<PlottableInfo> plottables = new ArrayList<PlottableInfo>();
		for(String pLabel : paramLabels) {
			List<PlottableInfo> items = findPlottableInfo(pLabel);
			plottables.addAll(items);
		}
		
		for(String lLabel : likeLabels) {
			List<PlottableInfo> items = findPlottableInfo(lLabel);
			plottables.addAll(items);
		}
		
		//Add mc.speed plottable info to list as a special item
		PlottableInfo speedInfo = new PlottableInfo();
		speedInfo.label = "mc.speed";
		speedInfo.descriptor = "Speed of chain (states / sec)";
		plottables.add(speedInfo);
		
		PlottableInfo[] plotArr = new PlottableInfo[plottables.size()];
		for(int i=0; i<plottables.size(); i++) {
			plotArr[i] = plottables.get(i);
		}
		
		plottableList = new JList(plotArr);
		plottableList.setCellRenderer(new PlottableItemRenderer());
		plottableList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
					flipSelection(me);	
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(plottableList);
		centerPanel.add(Box.createGlue());
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		centerPanel.add(Box.createGlue());
		this.add(centerPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		JButton nextButton = new JButton("Start chain");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startChain();
			}
		});
		bottomPanel.add(nextButton);
		bottomPanel.setBackground(ACGFrame.backgroundColor);
		this.add(bottomPanel, BorderLayout.SOUTH);
	}

	protected void flipSelection(MouseEvent me) {
		Point p = me.getPoint();
		int index = plottableList.locationToIndex(p);
		PlottableInfo info = (PlottableInfo)plottableList.getModel().getElementAt(index);
		info.selected = !info.selected;
		
		if (info.selected) {
			if (! selectedPlottables.contains(info)) 
				selectedPlottables.add(info);
		}
		else {
			selectedPlottables.remove(info);
		}
		plottableList.repaint();
	}

	private List<PlottableInfo> findPlottableInfo(String label) {
		List<PlottableInfo> list = new ArrayList<PlottableInfo>();
		try {
			Object obj = file.getObjectForLabel(label);
			if (obj instanceof LikelihoodComponent) {
				PlottableInfo info = new PlottableInfo();
				info.label = label;
				info.descriptor = "Likelihood";
				list.add(info);
			}
			
			if (obj instanceof AbstractParameter<?>) {
				if (obj instanceof CompoundParameter) {
					return list;
				}
				
				AbstractParameter<?> par = (AbstractParameter<?>)obj;
				
				String[] logKeys = par.getLogKeys();
				for(int i=0; i<logKeys.length; i++) {
					PlottableInfo info = new PlottableInfo();
					info.label = label;
					info.key = logKeys[i];
					info.descriptor = logKeys[i];
					list.add(info);
				}

			}
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return list;
	}
	


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
			if (freq < 100)
				freq = 100;	

			List<String> alignmentLabels = file.getLabelForClass(Alignment.class);
			for(String alnLabel : alignmentLabels) {
				Object obj = file.getObjectForLabel(alnLabel);
				if (obj instanceof Alignment) {
					Alignment aln = (Alignment)obj;
					if (aln.hasGapOrUnknown()) {
						JOptionPane.showMessageDialog(this, "The alignment with label " + alnLabel + " has gaps (-) or unknown (N, ?). Please remove these columns before running the file.");
						return;
					}
				}
			}
			
			
			MainOutputFrame outputPane = new MainOutputFrame(chain, freq);

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
					outputPane.addChart(new SpeedMonitor());
				}
				else {
					Object obj = file.getObjectForLabel(plottable.label);
					if (obj instanceof AbstractParameter<?>) {
						outputPane.addChart( (AbstractParameter<?>)obj, plottable.key);
					}
					if (obj instanceof LikelihoodComponent) {
						outputPane.addChart( (LikelihoodComponent)obj);
					}
				}
			}

			acgParent.initializeProgressBar(chain, runMax);
			acgParent.replaceCenterPanel(outputPane);

			ExecutingChain runner = file.runMCMC();
			acgParent.setRunner(runner);
		} catch (InstantiationException e) {
			ErrorWindow.showErrorWindow(e);
		} catch (IllegalAccessException e) {
			ErrorWindow.showErrorWindow(e);
		}
		catch (RuntimeException rex) {
			ErrorWindow.showErrorWindow(rex);
		}
	}
	
	class PlottableInfo {
		String label; //Label of the object
		String key;	  //LogKey of item to log
		boolean selected = false; //Whether or not item is selected
		String descriptor;
	}
	
	
	private JList plottableList;
}
