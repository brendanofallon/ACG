package newgui.gui.modelViews.loggerViews;

import gui.loggerConfigs.PopSizeLoggerModel;
import gui.loggerConfigs.RootHeightModel;

import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import newgui.gui.modelElements.Configurator.InputConfigException;

public class PopSizeView extends DefaultLoggerView {

	JSpinner binsSpinner;
	PopSizeLoggerModel popSizeModel;
	
	public PopSizeView() {
		this(new PopSizeLoggerModel());
	}
	
	public PopSizeView(final PopSizeLoggerModel model) {
		super(model);
		this.popSizeModel = model;
		SpinnerNumberModel binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		binsSpinner = new JSpinner(binsModel);
		binsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBins( (Integer)binsSpinner.getValue());
			}
		});
		centerPanel.add(new JLabel("Bins:"));
		centerPanel.add(binsSpinner);
		updateView();
	}

	public Dimension getPreferredDimensionsLarge() {
		return new Dimension(400, 180);
	}
	
	@Override
	public String getName() {
		return "Pop. Size";
	}

	@Override
	public String getDescription() {
		return "Population size over time";
	}

	@Override
	protected void updateModelFromView() throws InputConfigException {
		popSizeModel.setBins( (Integer)binsSpinner.getValue() );
	}
	
	/**
	 * Updates widgets with info from model
	 */
	public void updateView() {
		loggerLabelField.setText( model.getModelLabel() );
		loggerLabelField.repaint();
		burninSpinner.setValue( model.getBurnin() );
		burninSpinner.repaint();
		freqSpinner.setValue( model.getLogFrequency() );
		binsSpinner.setValue( popSizeModel.getBins());
		revalidate();
		repaint();
	}


}
