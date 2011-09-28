/********************************************************************
*
* 	Copyright 2011 Brendan O'Fallon
*
*   Licensed under the Apache License, Version 2.0 (the "License");
*   you may not use this file except in compliance with the License.
*   You may obtain a copy of the License at
*
*       http://www.apache.org/licenses/LICENSE-2.0
*
*   Unless required by applicable law or agreed to in writing, software
*   distributed under the License is distributed on an "AS IS" BASIS,
*   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
*   See the License for the specific language governing permissions and
*   limitations under the License.
*
***********************************************************************/


package dlCalculation.substitutionModels;

import java.util.HashMap;
import java.util.Map;


import parameter.AbstractParameter;
import parameter.DoubleParameter;
import parameter.Parameter;
import modifier.ModificationImpossibleException;


public class F84Matrix extends TN93Matrix<Void> {
	
	//Some intermediates
	double alpha;
	double beta;
	
	private double piR; 
	private double piY;
	
	double piTOverPiY;
	double piCOverPiY;
	double piAOverPiR;
	double piGOverPiR;
	
	DoubleParameter kappaParam;
	
	public F84Matrix(Map<String, String> attrs) {
		super(attrs, new BaseFrequencies(new double[]{0, 0, 0, 0})); //super-constructor gets stationaries from attrs 

		//If the user has not specified a kappa parameter we make one, but dont set any modifiers
		//and just use it to hold the value
		this.kappaParam = new DoubleParameter(1.0, "f84.kappa", "Ts/Tv ratio", 0, 1000);
		
		
		String kapStr = attrs.get(XML_KAPPA);
		if (kapStr != null) {
			double k = Double.parseDouble(kapStr);
			try {
				kappaParam.proposeValue(k);
			} catch (ModificationImpossibleException e) {
				throw new IllegalArgumentException("Invalid initial value for kappa : " + k);
			}
			kappaParam.acceptValue();
		}
		else {
			throw new IllegalArgumentException("You must supply either an kappa parameter or specify kappa=X as an attribute");
		}
		
	}
	
	public F84Matrix(BaseFrequencies stationaries, DoubleParameter kappa) {
		super(new HashMap<String, String>(), stationaries);
		this.kappaParam = kappa;

		addParameter(kappa);
		addParameter(stationaries);
		
		try {
			checkParamValidity();
		} catch (BadParameterComboException e) {
			throw new IllegalArgumentException("Invalid initial values for stationaries and kappa in F84 model");
		}
	}
	
	/**
	 * Constructs a new F84 model. This constructor is the one called when we instantiate directly from XML, but
	 * since all tree nodes have their own transition matrix, objects instantiated from this constructor are never
	 * used directly. Instead, we call .getNew() from this object, which in turn uses a different constructor
	 * to create F84 objects that are then given to the tree nodes. 
	 * @param attrs
	 * @param kappaParam
	 */
	public F84Matrix(Map<String, String> attrs, DoubleParameter kappaParam) {
		super(attrs, new BaseFrequencies(new double[]{0.25, 0.25, 0.25, 0.25})); //super-constructor gets stationaries from attrs 
		
		this.kappaParam = kappaParam;
		addParameter(kappaParam);
		kappaParam.acceptValue();
		try {
			checkParamValidity();
		} catch (BadParameterComboException e) {
			throw new IllegalArgumentException("Invalid initial values for stationaries and kappa in F84 model");
		}
	}
	
	public F84Matrix(Map<String, String> attrs, BaseFrequencies stationaries, DoubleParameter kappaParam) {
		super(attrs, stationaries); //super-constructor gets stationaries from attrs 
		
		this.kappaParam = kappaParam;
		addParameter(kappaParam);
		kappaParam.acceptValue();
		try {
			checkParamValidity();
		} catch (BadParameterComboException e) {
			throw new IllegalArgumentException("Invalid initial values for stationaries and kappa in F84 model");
		}
	}
	
	/**
	 * XML-unfriendly constructor
	 */
	public F84Matrix(double[] stationaries, double kappa) {
		super(new HashMap<String, String>(), new BaseFrequencies(stationaries)); //dummy values that will get over written in the next several lines
		
		this.kappaParam = new DoubleParameter(1.0, "f84.kappa", "Ts/Tv ratio", 0, 1000);
		kappaParam.addListener(this);
		try {
			kappaParam.proposeValue(kappa);
			checkParamValidity();
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial value for kappa : " + kappa);
		}
		
		kappaParam.acceptValue();
	}
	
	protected void checkParamValidity() throws BadParameterComboException {
		if (kappaParam != null && stationaries.getValue() != null) { //This may get called during construction before kappa is initialized
			double[] pi = stationaries.getValue();
			if ((pi[A]+pi[G])*(pi[T]+pi[C])*kappaParam.getValue() - (pi[A]*pi[G]) - (pi[C]*pi[T]) < 0) {
				throw new BadParameterComboException("Combination of stationaries and kappa is invalid");
			}
		}
	}
	
	private void recalculateIntermediates() {
		double[] pi = stationaries.getStationaries();
		
		double k = kappaParam.getValue();
		
		piR = pi[A] + pi[G];
		piY = pi[C] + pi[T];
		piTOverPiY = pi[T] / piY;
		piCOverPiY = pi[C] / piY;
		piAOverPiR = pi[A] / piR;
		piGOverPiR = pi[G] / piR;		
		
		
		beta = 1.0 / (2.0*piR*piY*(1.0+k)); //This must be OK? since all 'invariant' calculations appear fine
		alpha = ( piR*piY*k - pi[A]*pi[G] - pi[C]*pi[T] ) / ( 2.0*(1.0+k)*(piY*pi[A]*pi[G] + piR*pi[C]*pi[T]) );
		
		if (alpha < 0) {
			throw new IllegalArgumentException("Invalid combination of stationaries and kappa, in F84 mutation model");
		}
		//System.out.println("Recalculating intermediates with kappa = " + kappaParam.getValue());
		recalcIntermediates = false;
	}
	
	public void setBranchLength(double t) {
		if (recalcIntermediates) {
			recalculateIntermediates();
		}
		
		double[] pi = stationaries.getStationaries();
		
		double eb = Math.exp(-beta*t);		
		double expA = Math.exp(-alpha*t);
		
		double er0 = expA*eb;
		double er1 = eb * (1.0- expA); //This is bigger for smaller t
		double er2 = 1.0- eb;

		double exPiT = er2*pi[T];
		double exPiC = er2*pi[C];
		double exPiG = er2*pi[G];
		double exPiA = er2*pi[A];
		
		double eyPiTOverPiY = er1 * piTOverPiY;
		double eyPiCOverPiY = er1 * piCOverPiY;
		double eyPiAOverPiR = er1 * piAOverPiR;
		double eyPiGOverPiR = er1 * piGOverPiR;
		
		//matrix[x][y] describes the probability of going from state x to y
		//matrix[i][j] = Pr{j | i}, going from i to j
		//in forwards or backwards time?
		
		matrix[T][T] = er0 + eyPiTOverPiY + exPiT;
		matrix[T][C] = 		 eyPiCOverPiY + exPiC;
		matrix[T][A] = 						exPiA;
		matrix[T][G] =  					exPiG;
		
		matrix[C][T] = 		 eyPiTOverPiY + exPiT;
		matrix[C][C] = er0 + eyPiCOverPiY + exPiC;
		matrix[C][A] = 						exPiA;
		matrix[C][G] = 						exPiG;

		matrix[A][T] =                       exPiT;  //Purine to pyrimidine
		matrix[A][C] =                       exPiC;
		matrix[A][A] =  er0 + eyPiAOverPiR + exPiA;
		matrix[A][G] =        eyPiGOverPiR + exPiG;
		
		matrix[G][T] = 						exPiT;
		matrix[G][C] =                      exPiC;
		matrix[G][A] =       eyPiAOverPiR + exPiA;
		matrix[G][G] = er0 + eyPiGOverPiR + exPiG;
		
	}
	
	public void setMatrix(double t, double[][] tMatrix) {
		if (recalcIntermediates) {
			recalculateIntermediates();
		}
		
		double[] pi = stationaries.getStationaries();
		
		double eb = Math.exp(-beta*t);		
		double expA = Math.exp(-alpha*t);
		
		double er0 = expA*eb;
		double er1 = eb * (1.0- expA); 
		double er2 = 1.0- eb;

		double exPiT = er2*pi[T];
		double exPiC = er2*pi[C];
		double exPiG = er2*pi[G];
		double exPiA = er2*pi[A];
		
		double eyPiTOverPiY = er1 * piTOverPiY;
		double eyPiCOverPiY = er1 * piCOverPiY;
		double eyPiAOverPiR = er1 * piAOverPiR;
		double eyPiGOverPiR = er1 * piGOverPiR;
		
		//matrix[x][y] describes the probability of going from state x to y
		//matrix[i][j] = Pr{j | i}, going from i to j
		//in forwards or backwards time?
		
		tMatrix[T][T] = er0 + eyPiTOverPiY + exPiT;
		tMatrix[T][C] = 		 eyPiCOverPiY + exPiC;
		tMatrix[T][A] = 						exPiA;
		tMatrix[T][G] =  					exPiG;
		
		tMatrix[C][T] = 	  eyPiTOverPiY + exPiT;
		tMatrix[C][C] = er0 + eyPiCOverPiY + exPiC;
		tMatrix[C][A] = 					 exPiA;
		tMatrix[C][G] = 					 exPiG;

		tMatrix[A][T] =                       exPiT;  //Purine to pyrimidine
		tMatrix[A][C] =                       exPiC;
		tMatrix[A][A] =  er0 + eyPiAOverPiR + exPiA;
		tMatrix[A][G] =        eyPiGOverPiR + exPiG;
		
		tMatrix[G][T] = 					 exPiT;
		tMatrix[G][C] =                      exPiC;
		tMatrix[G][A] =       eyPiAOverPiR + exPiA;
		tMatrix[G][G] = er0 + eyPiGOverPiR + exPiG;
		
		
	}

	
	/**
	 * Returns a new F84 matrix with the current stationaries and parameter
	 */
//	public MutationModel getNew() {
//		return new F84Matrix(stationaries, kappaParam);
//	}


	public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException {
		if (source.isProposed()) {
			try {
				checkParamValidity();
				fireParameterChange(); //Tell DL calc to recalc everything
			}
			catch (BadParameterComboException ex) {
				((AbstractParameter<?>)source).revertSilently(); //Don't fire an event
				//System.out.println("Param misfire... throwing exception");
				throw new ModificationImpossibleException(ex.getMessage());
			}
		}
		

		recalcIntermediates = true;
	}

	
	public double getKappa() {
		return kappaParam.getValue();
	}
	
	public String toString() {
		double[] stats = stationaries.getStationaries();
		return "F84 Matrix kappa= " + kappaParam.getValue() + " A=" + stats[A] + " C=" + stats[C] + " G=" + stats[G] + " T=" + stats[T] + "\n"; 
	}
	
	
	@Override
	public String getName() {
		return "F84 substitution model";
	}


}
