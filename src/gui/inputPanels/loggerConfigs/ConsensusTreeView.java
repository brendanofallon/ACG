package gui.inputPanels.loggerConfigs;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public class ConsensusTreeView extends AbstractLoggerView {

	JTextField siteField;
	ConsensusTreeModel bpModel;
	
	public ConsensusTreeView(final ConsensusTreeModel model) {
		super(model);
		JOptionPane.showMessageDialog(this, "You need to refactor things so that multiple of these can be made - right now there's only one that always lives in addLoggerFrame");
		this.bpModel = model;
		siteField = new JTextField("Enter site");
		siteField.setMinimumSize(new Dimension(40, 10));
		siteField.setPreferredSize(new Dimension(40, 32));
		siteField.setMaximumSize(new Dimension(40, 1000));
		siteField.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateSiteField();
			}
		});
		add(new JLabel("Site :"));
		add(siteField);
	}
	
	protected void updateSiteField() {
		try {
			Integer site = Integer.parseInt(siteField.getText());
			bpModel.setSite(site);
		}
		catch (NumberFormatException nfe) {
			JOptionPane.showMessageDialog(this, "Please enter an integer");
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

}
