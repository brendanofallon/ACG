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
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import arg.ARG;

import mcmc.MCMC;
import parameter.AbstractParameter;
import parameter.CompoundParameter;
import parameter.DoubleParameter;

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
		List<String> mcLabels = file.getMCMCLabels();
		MCMC chain;
		try {
			chain = (MCMC)file.getObjectForLabel(mcLabels.get(0));
			int freq = chain.getUserRunLength() / 5000;
			if (freq < 100)
				freq = 100;
				
			MainOutputFrame outputPane = new MainOutputFrame(chain, freq, 2, 2);

			for(PlottableInfo plottable : selectedPlottables) {
				Object obj = file.getObjectForLabel(plottable.label);
				if (obj instanceof AbstractParameter<?>) {
					outputPane.addChart( (AbstractParameter<?>)obj, plottable.key);
				}
				if (obj instanceof LikelihoodComponent) {
					outputPane.addChart( (LikelihoodComponent)obj);
				}
			}

			acgParent.initializeProgressBar(chain);
			acgParent.replaceCenterPanel(outputPane);

			ExecutingChain runner = file.runMCMC();
			acgParent.setRunner(runner);
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
