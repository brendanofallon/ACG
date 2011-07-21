package gui;

import component.LikelihoodComponent;

import parameter.AbstractParameter;

public class StatusFigure {

	private AbstractParameter<?> param;
	
	public StatusFigure(AbstractParameter<?> param, String[] logKeys) {
		// ???
	}
	
	public StatusFigure(AbstractParameter<?> param) {
		this.param = param;
		Object t = param.getValue();
		if (t instanceof Double) {
			//awesome
		}
		else {
			if (t instanceof double[]) {
				//also fine, but we'll draw multiple lines
			}
			else {
				if (t instanceof Integer) {
					//OK
					//We could theoretically do an array of integers?
				}
				else {
					throw new IllegalArgumentException("Can't create a Figure for parameter with type " + t.getClass());
				}
			}
		}
	}
	
	public StatusFigure(LikelihoodComponent comp) {
		//ok, we're fine here
	}
}
