package gui.inputPanels;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.List;

import gui.document.ACGDocument;
import gui.inputPanels.Configurator.InputConfigException;
import gui.inputPanels.DoubleModifierElement.ModType;
import gui.inputPanels.PopSizeModelElement.PopSizeModel;

import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.w3c.dom.Element;

/**
 * Represents a CoalescentModelElement graphically. Typically this is included in a DocMemberConfigPanel
 * @author brendano
 *
 */
public class CoalescentView extends JPanel {

	CoalescentModelElement coalModel;
	
	private JComboBox coalModelBox;
	private String[] coalModels = new String[]{"Constant size", "Exponential growth"};
	
	private JComboBox recombModelBox;
	private String[] recombModels = new String[]{"None", "Constant rate"};
	
	List<Element> params = new ArrayList<Element>();
	List<Element> likelihoods = new ArrayList<Element>();;
		
	private JPanel popPanel;
	private JPanel popCenter;
	private JPanel recPanel;
	private JPanel recCenter;
	
	public CoalescentView() {
		coalModel = new CoalescentModelElement();
		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		setOpaque(false);
		setMaximumSize(new Dimension(1000, 50));
		setPreferredSize(new Dimension(500, 50));
		
		popPanel = new JPanel();
		JPanel popTop = new JPanel();
		popTop.setOpaque(false);
		popPanel.setOpaque(false);
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
		popCenter = new JPanel();
		popCenter.setOpaque(false);
		popPanel.add(popCenter, BorderLayout.CENTER);
		this.add(popPanel);
		
		DoubleParamElement test = new DoubleParamElement();
		test.setLabel("Test!");
		test.setValue(12.4);
		test.setLowerBound(Double.NEGATIVE_INFINITY);
		test.setUpperBound(1238.12);
		test.setModifierType(ModType.Scale);
		
		DoubleParamView constPopView = new DoubleParamView("Population Size", test);
		popPanel.add( constPopView, BorderLayout.CENTER );
		
		recPanel = new JPanel();
		recPanel.setOpaque(false);
		recPanel.setLayout(new BorderLayout());
		JPanel recTop = new JPanel();
		recPanel.add(recTop, BorderLayout.NORTH);
		
		
		recTop.add(new JLabel("Recombination :"));
		recombModelBox = new JComboBox(recombModels);
		recombModelBox.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent e) {
				updateRecombModelBox();
			}
		});
		recombModelBox.setSelectedIndex(1);
		recTop.add(recombModelBox);
		recCenter = new JPanel();
		recPanel.add(recCenter, BorderLayout.CENTER);
		this.add(recPanel);
	}
	
	protected void updateCoalModelBox() {
		if (coalModelBox.getSelectedIndex()==0) {
			coalModel.getPopSizeModel().setModelType( PopSizeModel.Constant );
		}
		if (coalModelBox.getSelectedIndex()==1) {
			coalModel.getPopSizeModel().setModelType( PopSizeModel.ExpGrowth );
		}
	}

	protected void updateRecombModelBox() {
		if (recombModelBox.getSelectedIndex()==0) {
			coalModel.setUseRecombination(false);
		}
		if (recombModelBox.getSelectedIndex()==1) {
			coalModel.setUseRecombination(true);
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
		
		if (coalModel.getRecombModel()==null)
			recombModelBox.setSelectedIndex(0);
		else {
			recombModelBox.setSelectedIndex(1);
		}
		
		repaint();
	}
	
	public void readNodesFromDocument(ACGDocument doc) throws InputConfigException {
		coalModel.readElements(doc);
		updateView();
	}
}
