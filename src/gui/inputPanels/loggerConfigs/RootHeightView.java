package gui.inputPanels.loggerConfigs;


import javax.swing.JLabel;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class RootHeightView extends AbstractLoggerView {

	JSpinner binsSpinner;
	RootHeightModel rootHeightModel;
	
	public RootHeightView() {
		this(new RootHeightModel());
	}
	
	public RootHeightView(final RootHeightModel model) {
		super(model);
		this.rootHeightModel = model;
		SpinnerNumberModel binsModel = new SpinnerNumberModel(500, 1, 50000, 10);
		binsSpinner = new JSpinner(binsModel);
		binsSpinner.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				model.setBins( (Integer)binsSpinner.getValue());
			}
		});
		add(new JLabel("Bins:"));
		add(binsSpinner);
	}
	
	@Override
	public String getName() {
		return "Marginal TMRCA";
	}

	@Override
	public String getDescription() {
		return "Root height along length of sequence";
	}
	
}
