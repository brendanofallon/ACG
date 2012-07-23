package newgui.gui.modelViews;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import gui.modelElements.CoalescentModelElement;
import gui.modelElements.DoubleParamView;
import gui.modelElements.Configurator.InputConfigException;
import gui.modelElements.PopSizeModelElement.PopSizeModel;
import gui.widgets.Style;
import gui.widgets.Stylist;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import org.w3c.dom.Element;

import document.ACGDocument;

/**
 * Represents a CoalescentModelElement graphically. Typically this is included in a DocMemberConfigPanel
 * @author brendano
 *
 */
public class CoalModelView extends JPanel {

	CoalescentModelElement coalModel;
	
	private JComboBox coalModelBox;
	private String[] coalModels = new String[]{"Constant size", "Exponential growth"};
	
	private JComboBox recombModelBox;
	private String[] recombModels = new String[]{"None", "Constant rate"};
	
	List<Element> params = new ArrayList<Element>();
	List<Element> likelihoods = new ArrayList<Element>();;
		
	private JPanel popCenterPanel;
	private JPanel growthRatePanel;
	private DoubleParamView constPopView;
	private DoubleParamView baseSizeView;
	private DoubleParamView growthRateView;
	private DoubleParamView recView;
	
	private JPanel popPanel;
	private JPanel recPanel;
	private JPanel recCenterPanel;
	

	private Stylist stylist = new Stylist();
	
	
	public CoalModelView(CoalescentModelElement coalModel) {
		stylist.addStyle(new Style() {
			public void apply(JComponent comp) {
				comp.setOpaque(false);
				comp.setAlignmentX(Component.LEFT_ALIGNMENT);
				comp.setAlignmentY(Component.TOP_ALIGNMENT);
			}
		});
		
		this.coalModel = coalModel;
		initComponents();
		updateView();
	}
	
	/**
	 * Called when the coalescent model box has a new item selected
	 */
	protected void updateCoalModelBox() {
		if (coalModelBox.getSelectedIndex()==0) {
			coalModel.getPopSizeModel().setModelType( PopSizeModel.Constant );
			popCenterPanel.remove(growthRatePanel);
			popCenterPanel.add(constPopView, BorderLayout.CENTER);
			popCenterPanel.revalidate();
			
		}
		if (coalModelBox.getSelectedIndex()==1) {
			coalModel.getPopSizeModel().setModelType( PopSizeModel.ExpGrowth );
			popCenterPanel.remove(constPopView);
			popCenterPanel.add(growthRatePanel, BorderLayout.CENTER);
			popCenterPanel.revalidate();
		}
		
		popCenterPanel.repaint();
	}

	/**
	 * Obtain the CoalescentModelElement backing this view
	 * @return
	 */
	public CoalescentModelElement getModel() {
		return coalModel;
	}
	
	/**
	 * Replace the model backing this view with the given model. This also re-constructs all UI
	 * components from scratch
	 * @param coalescentModel
	 */
//	public void setCoalModel(CoalescentModelElement coalescentModel) {
//		this.coalModel = coalescentModel;
//		this.removeAll();
//		initComponents();
//		updateView();
//	}
	
	/**
	 * Called when recombination selection has changed
	 */
	protected void updateRecombModelBox() {
		if (recombModelBox.getSelectedIndex()==0) {
			coalModel.setUseRecombination(false);
			recCenterPanel.remove(recView);
			recCenterPanel.revalidate();
			repaint();
		}
		if (recombModelBox.getSelectedIndex()==1) {
			coalModel.setUseRecombination(true);
			recCenterPanel.add( recView, BorderLayout.CENTER);
			recCenterPanel.revalidate();
			repaint();
		}
	}

	/**
	 * Update view components to reflect changes in model
	 */
	public void updateView() {
		if (coalModel.getPopSizeModel().getModelType() == PopSizeModel.Constant)
			coalModelBox.setSelectedIndex(0);
		if (coalModel.getPopSizeModel().getModelType() == PopSizeModel.ExpGrowth)
			coalModelBox.setSelectedIndex(1);
		
		if (coalModel.getUseRecombination())
			recombModelBox.setSelectedIndex(1);
		else {
			recombModelBox.setSelectedIndex(0);
		}
		
		constPopView.updateView();
		baseSizeView.updateView();
		growthRateView.updateView();
		recView.updateView();
		
		repaint();
	}
	
	public void readNodesFromDocument(ACGDocument doc) throws InputConfigException {
		coalModel.readElements(doc);
		updateView();
	}
	
	public void updateModel() {
		//As in SiteModelView, all ui components here update the model as soon as they
		//are changed, so we don't have to explicitly do anything here
	}
	
	/**
	 * Create GUI components
	 */
	private void initComponents() {
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		this.add(Box.createVerticalStrut(30));
		
		stylist.applyStyle(this);
		popPanel = new JPanel();
		JPanel popTop = new JPanel();
		stylist.applyStyle(popTop);
		stylist.applyStyle(popPanel);
		popPanel.setLayout(new BorderLayout());
		popTop.add(new JLabel("Coalescent model:"));
		coalModelBox = new JComboBox(coalModels);
		coalModelBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateCoalModelBox();
			}
		});
		popTop.add(coalModelBox);
		popPanel.add(popTop, BorderLayout.NORTH);
		this.add(popPanel);
		
		popCenterPanel = new JPanel();
		popCenterPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		stylist.applyStyle(popCenterPanel);
		popPanel.add(popCenterPanel, BorderLayout.CENTER);
		constPopView = new DoubleParamView("Population Size", coalModel.getPopSizeModel().getConstSizeModel());
		constPopView.setPreferredSize(new Dimension(300, 36));
		popCenterPanel.add( constPopView);
		
		baseSizeView = new DoubleParamView("Base Size", coalModel.getPopSizeModel().getBaseSizeModel());
		baseSizeView.setPreferredSize(new Dimension(300, 36));
		growthRateView = new DoubleParamView("Growth Rate", coalModel.getPopSizeModel().getGrowthRateModel());
		growthRateView.setPreferredSize(new Dimension(300, 36));
		
		growthRatePanel = new JPanel();
		growthRatePanel.setOpaque(false);
		growthRatePanel.setLayout(new BoxLayout(growthRatePanel, BoxLayout.Y_AXIS));
		growthRatePanel.add(baseSizeView);
		growthRatePanel.add(growthRateView);
		
		
		JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
		this.add(sep);
		
		recPanel = new JPanel();
		stylist.applyStyle(recPanel);
		recPanel.setLayout(new BorderLayout());
		JPanel recTop = new JPanel();
		stylist.applyStyle(recTop);
		
		recCenterPanel = new JPanel();
		stylist.applyStyle(recCenterPanel);
		
		recPanel.add(recTop, BorderLayout.NORTH);
		recView = new DoubleParamView("Recombination rate", coalModel.getRecombModel().getModel());
		stylist.applyStyle(recView);
		recPanel.add(recCenterPanel, BorderLayout.CENTER);
		
		recTop.add(new JLabel("Recombination :"));
		recombModelBox = new JComboBox(recombModels);
		recombModelBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateRecombModelBox();
			}
		});
		

		if (coalModel.getUseRecombination()) {
			recombModelBox.setSelectedIndex(1);
		}
		else {
			recombModelBox.setSelectedIndex(0);
		}
		
		recTop.add(recombModelBox);
		this.add(recPanel);
		this.add(Box.createVerticalGlue());
	}

	
}

