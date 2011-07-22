package gui;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import mcmc.MCMC;
import parameter.AbstractParameter;
import parameter.DoubleParameter;

import component.LikelihoodComponent;

public class PickPlottablesPanel extends JPanel {

	RunnableInputFile file;
	ACGFrame acgParent;
	
	//Holds list of things we'll display in the next panel
	List<PlottableInfo> selectedPlottables = new ArrayList<PlottableInfo>();
	
	public PickPlottablesPanel(ACGFrame acgParent, RunnableInputFile file) {
		this.acgParent = acgParent;
		this.file = file;
		initComponents();
	}

	private void initComponents() {
		setLayout(new BorderLayout());
		
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(new JLabel("Pick items to plot from list below"));
		topPanel.add(Box.createHorizontalGlue());
		this.add(topPanel, BorderLayout.NORTH);
		
		JPanel centerPanel = new JPanel();
		centerPanel.setLayout(new BorderLayout());
		
		List<String> paramLabels = file.getParameterLabels();
		List<String> likeLabels = file.getLikelihoodLabels();
		
		//All the things we could potentially plot
		List<PlottableInfo> plottables = new ArrayList<PlottableInfo>();
		for(String pLabel : paramLabels) {
			PlottableInfo info = findPlottableInfo(pLabel);
			if (info != null)
				plottables.add(info);
		}
		
		for(String lLabel : likeLabels) {
			PlottableInfo info = findPlottableInfo(lLabel);
			if (info != null)
				plottables.add(info);
		}
		
		PlottableInfo[] plotArr = new PlottableInfo[plottables.size()];
		for(int i=0; i<plottables.size(); i++) {
			plotArr[i] = plottables.get(i);
		}
		
		//It's not really just the labels we want to plot... each 
		//thing needs it's own checkbox and descriptor as well as label
		plottableList = new JList(plotArr);
		plottableList.setCellRenderer(new PlottableItemRenderer());
		plottableList.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent me) {
					flipSelection(me);	
			}
		});
		
		JScrollPane scrollPane = new JScrollPane(plottableList);
		centerPanel.add(scrollPane, BorderLayout.CENTER);
		this.add(centerPanel, BorderLayout.CENTER);
		
		JPanel bottomPanel = new JPanel();
		JButton nextButton = new JButton("Start chain");
		nextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				startChain();
			}
		});
		bottomPanel.add(nextButton);
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

	private PlottableInfo findPlottableInfo(String label) {
		try {
			Object obj = file.getObjectForLabel(label);
			if (obj instanceof LikelihoodComponent) {
				PlottableInfo info = new PlottableInfo();
				info.label = label;
				info.descriptor = "Likelihood";
				return info;
			}
			
			if (obj instanceof AbstractParameter<?>) {
				AbstractParameter<?> par = (AbstractParameter<?>)obj;
				PlottableInfo info = new PlottableInfo();
				info.label = label;
				Object t = par.getValue();
				if (t instanceof Double)
					info.descriptor = "Single-value parameter";
				if (t instanceof double[]) {
					int l = ((double[])t).length;
					info.descriptor = "" + l + "-value parameter";
				}
				if (t instanceof Double[]){
					int l = ((Double[])t).length;
					info.descriptor = "" + l + "-value parameter";
				}
				if (t instanceof Integer) { 
					info.descriptor = "Single-value parameter";
				}
				
				if (info.descriptor == null)
					return null;
				else 
					return info;
			}
			else {
				return null;
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Returns true if the object with the given label is an AbstractParameter
	 * whose value is of type Double, double[], or Integer
	 * @param pLabel
	 * @return
	 */
	private boolean isPlottable(String pLabel) {
		try {
			Object obj = file.getObjectForLabel(pLabel);
			if (obj instanceof AbstractParameter<?>) {
				AbstractParameter<?> par = (AbstractParameter<?>)obj;
				Object t = par.getValue();
				if (t instanceof Double)
					return true;
				if (t instanceof double[])
					return true;
				if (t instanceof Double[])
					return true;
				if (t instanceof Integer) 
					return true;
				
				return false;
			}
			else {
				return false;
			}
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return false;
	}

	protected void startChain() {
		List<String> mcLabels = file.getMCMCLabels();
		MCMC chain;
		try {
			chain = (MCMC)file.getObjectForLabel(mcLabels.get(0));
			MainOutputFrame outputPane = new MainOutputFrame(chain, 5000, 2, 2);

			for(PlottableInfo plottable : selectedPlottables) {
				Object obj = file.getObjectForLabel(plottable.label);
				if (obj instanceof AbstractParameter<?>) {

					System.out.println("Adding parameter " + obj);
					outputPane.addChart( (AbstractParameter<?>)obj);
				}
				if (obj instanceof LikelihoodComponent) {
					System.out.println("Adding likelihood " + obj);
					outputPane.addChart( (LikelihoodComponent)obj);
				}
			}

			acgParent.initializeProgressBar(chain);
			

			acgParent.replaceCenterPanel(outputPane);

			file.runMCMC();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	class PlottableInfo {
		String label;
		boolean selected = false;
		String descriptor;
	}
	
	
	private JList plottableList;
}
