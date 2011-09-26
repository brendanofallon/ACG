package gui.inputPanels.loggerConfigs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import gui.ErrorWindow;
import gui.inputPanels.Configurator.InputConfigException;

public class ConsensusTreeView extends AbstractLoggerView {

	JTextField siteField;
	ConsensusTreeModel bpModel;
	
	public ConsensusTreeView() {
		this( new ConsensusTreeModel() );
		filenameField.setText("ConsensusTree.tre");
	}
	
	public ConsensusTreeView(final ConsensusTreeModel model) {
		super(model);
		this.bpModel = model;
		siteField = new JTextField("Enter site");
		siteField.setMinimumSize(new Dimension(60, 10));
		siteField.setMaximumSize(new Dimension(60, 1000));
		siteField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					updateSiteField();
				} catch (InputConfigException e) {
					
				}
			}
		});
		add(new JLabel("Site :"));
		add(siteField);
	}
	
	
	protected void updateSiteField() throws InputConfigException {
		try {
			Integer site = Integer.parseInt(siteField.getText());
			bpModel.setSite(site);
		}
		catch (NumberFormatException nfe) {
			throw new InputConfigException("Please enter an integer for the site at which to build the consensus tree");
		}
	}

	@Override
	public String getName() {
		return "Consensus site tree";
	}

	@Override
	public String getDescription() {
		return "Consensus of trees at single site";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		updateSiteField();
	}

}
