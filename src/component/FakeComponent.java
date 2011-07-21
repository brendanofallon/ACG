package component;

import parameter.Parameter;

public class FakeComponent extends LikelihoodComponent {

	@Override
	public Double computeProposedLikelihood() {
		return 0.5;
	}

	
	public String getLogHeader() {
		return "Const likelihood";
	}
}
