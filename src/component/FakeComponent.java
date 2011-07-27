package component;

import java.util.Map;

import parameter.Parameter;

public class FakeComponent extends LikelihoodComponent {

	public FakeComponent(Map<String, String> attrs) {
		super(attrs);
		// TODO Auto-generated constructor stub
	}


	@Override
	public Double computeProposedLikelihood() {
		return 0.5;
	}

	
	public String getLogHeader() {
		return "Const likelihood";
	}
}
