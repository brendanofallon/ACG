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

import java.util.Map;

import modifier.ModificationImpossibleException;

import parameter.AbstractParameter;
import parameter.DoubleParameter;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;


import sequence.DNAUtils;

public class TN93Matrix<Void> extends MutationModel<Void> {

	public static final String XML_STATIONARIES = "stationaries";
	public static final String XML_KAPPA = "kappa"; //The transition to transversion ratio, Ts/Tv, as defined by Felsenstein 2004
	public static final String XML_RHO = "rho"; // The rate of R-transitions to Y-transitions, alphaR = rho * alphaY
		
	private double aR = 1; //alphaR, rate of transitions among purines
	private double aY = 1; //alphaY, rate of transitions among pyrimidines
	private double b = 1; //beta, rate of transversions, usually scaled so total rate is 1.0
	
	private double piR; 
	private double piY;
	
	static final int T = DNAUtils.T;
	static final int C = DNAUtils.C;
	static final int A = DNAUtils.A;
	static final int G = DNAUtils.G;
	

	private DoubleParameter kParam;
	private DoubleParameter rParam;
	
	
	//This gets set to true when some base frequencies or other things have
	//changed and we must recalculate some values before we can compute a matrix
	protected boolean recalcIntermediates = true;
	
	public TN93Matrix(Map<String, String> attrs, BaseFrequencies stationaries) {
		super(4, stationaries); //Dummy args that get overwritten immediately
		
		double[] freqs = stationaries.getStationaries();
		double sum = freqs[A] + freqs[C] + freqs[T] + freqs[G];
		
		addParameter(stationaries);
		
		//parse stationaries...
		String statStr = attrs.get(XML_STATIONARIES);
		if (sum <= 0 && statStr == null) {
			throw new IllegalArgumentException("No initial base frequencies have been set");
		}

		if (statStr != null) {
			String[] stats = statStr.split(" ");
			if (stats.length != 4) {
				throw new IllegalArgumentException("Could not parse stationaries from argument list, got : " + statStr);
			}

			double[] userStats = new double[4];
			try {
				userStats[A] = Double.parseDouble(stats[A]);
				userStats[C] = Double.parseDouble(stats[C]);
				userStats[T] = Double.parseDouble(stats[T]);
				userStats[G] = Double.parseDouble(stats[G]);

				stationaries.proposeValue(userStats);
				stationaries.acceptValue();
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse a number from the stationaries list : " + statStr);
			} catch (ModificationImpossibleException e) {
				throw new IllegalArgumentException("Invalid initial value for stationaries.");
			}
		}
		
		
		double kappa = 2.0;
		String kappaStr = attrs.get(XML_KAPPA);
		if (kappaStr != null) {
			try {
				kappa = Double.parseDouble(kappaStr);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse a number for kappa attribute : " + kappaStr);
			}
		}
		kParam = new DoubleParameter(kappa, "tn93.kappa", "TN93 param k", 0, 1000);
		try {
			kParam.proposeValue(kappa);
		} catch (ModificationImpossibleException e1) {
			throw new IllegalArgumentException("Invalid initial value for kappa : " + kappa);
		}
		kParam.acceptValue();
		
		
		//Right now we think its OK if the user doesn't specify an initial value for the
		//badly-named rho parameter for this mutation model. The default is one. 
		double rho = 1.0;
		String rhoStr = attrs.get(XML_RHO);
		if (rhoStr != null) {
			try {
				rho = Double.parseDouble(rhoStr);
			}
			catch (NumberFormatException nfe) {
				throw new IllegalArgumentException("Could not parse a number for alphaY attribute : " + rhoStr);
			}
		}
		
		rParam = new DoubleParameter(rho, "tn93.rho", "TN93 param r", 0, 1000);
		try {
			rParam.proposeValue(rho);
		} catch (ModificationImpossibleException e) {
			System.err.println("Invalid initial value for rho : " + rho);
		}
		rParam.acceptValue();
		
		//setKappaRho(kappa, rho);
		//System.out.println("Creating new TN93 matrix with stationaries A: " + stationaries[A] + " G: " + stationaries[G] + " C: " + stationaries[C] + " T:" + stationaries[T] + "\t kappa : " + kappa + " rho: " + rho);
	}
	
	public TN93Matrix(Map<String, String> attrs, BaseFrequencies stats, DoubleParameter kappaParam, DoubleParameter rhoParam) {
		super(4, stats);
		this.kParam = kappaParam;
		this.rParam = rhoParam;
		kappaParam.acceptValue();
		rhoParam.acceptValue();
		addParameter(stats);
		addParameter(kParam);
		addParameter(rParam);
	}
	
	public TN93Matrix(BaseFrequencies stats, DoubleParameter kappaParam, DoubleParameter rhoParam) {
		super(4, stats);
		this.kParam = kappaParam;
		this.rParam = rhoParam;
		addParameter(stats);
		addParameter(kParam);
		addParameter(rParam);
	}
	
	public TN93Matrix(BaseFrequencies stats, double kappa, double rho) {
		super(4, stats);
		kParam = new DoubleParameter(kappa, "tn93.kappa", "TN93 param k", 0, 1000);
		try {
			kParam.proposeValue(kappa);
		} catch (ModificationImpossibleException e1) {
			System.err.println("Invalid initial value for kappa : " + kappa);
		}
		kParam.acceptValue();
		
		rParam = new DoubleParameter(rho, "tn93.rho", "TN93 param r", 0, 1000);
		try {
			rParam.proposeValue(rho);
		} catch (ModificationImpossibleException e) {
			System.err.println("Invalid initial value for rho : " + rho);
		}
		rParam.acceptValue();
		stats.addListener(this);
	}
		
	/**
	 * Return the stationary frequencies associated with this mutational model
	 */
	public double[] getStationaries() {
		return stationaries.getStationaries();
	}
	
	
	private void recalculateIntermediates() {
		double[] pi = stationaries.getStationaries();
		double kappa = kParam.getValue();
		double rho = rParam.getValue();
		
		piY = pi[C] + pi[T];
		piR = pi[A] + pi[G];
		this.b = 1.0/(2.0*piR*piY*(1+kappa));
		
		this.aY = (piR*piY*kappa - pi[A]*pi[G] - pi[C]*pi[T]) / (2.0*(1.0+kappa)*(piY*pi[A]*pi[G]*rho + piR*pi[C]*pi[T]));
		this.aR = rho*aY;

		if (this.aY < 0) {
			System.out.println("Aaarg, still bad parameter combos...");
			throw new IllegalArgumentException("Bad parameter combo in TN93");
		}
		recalcIntermediates = false;
	}
	
	private void checkParamValidity() throws BadParameterComboException {
		double[] pi = stationaries.getValue();
		if ((pi[A]+pi[G])*(pi[T]+pi[C])*kParam.getValue() - (pi[A]*pi[G]) - (pi[C]*pi[T]) < 0) {
			throw new BadParameterComboException("Combination of stationaries and kappa is invalid");
		}
	}
	
	/**
<<<<<<< TREE
	 * A non-optimized but known to be working version. Or is it?
=======
	 * A non-optimized but probably working version
>>>>>>> MERGE-SOURCE
	 * @throws ModificationImpossibleException 
	 */
	public void setBranchLength(double t)  {
		if (recalcIntermediates) {
			recalculateIntermediates();
		}
		
		t /= b; //Rescale branch length by b
		
		double eb = Math.exp(-t);		
		double[] pi = stationaries.getStationaries();
		//TODO tons of potential optimizations here...
		
		//matrix[x][y] describes the probability of going from state x to y
		//matrix[i][j] = Pr{j | i}, going from i to j

		matrix[A][A] =  Math.exp(-(aR+b)*t) + eb * (1.0-Math.exp(-aR*t)) * pi[A] / piR + (1.0-eb)*pi[A];
		matrix[A][C] =    																 (1.0-eb)*pi[C];
		matrix[A][G] =    					  eb * (1.0-Math.exp(-aR*t)) * pi[G] / piR + (1.0-eb)*pi[G];
		matrix[A][T] =       															 (1.0-eb)*pi[T];
		
		matrix[T][T] = Math.exp(-(aY+b)*t) + eb * (1.0-Math.exp(-aY*t)) * pi[T] / piY + (1.0-eb)*pi[T];
		matrix[T][C] = 						 eb * (1.0-Math.exp(-aY*t)) * pi[C] / piY + (1.0-eb)*pi[C];
		matrix[T][A] = 																	(1.0-eb)*pi[A];
		matrix[T][G] =  																(1.0-eb)*pi[G];
		
		matrix[C][T] = 						 eb * (1.0-Math.exp(-aY*t)) * pi[T] / piY + (1.0-eb)*pi[T];
		matrix[C][C] = Math.exp(-(aY+b)*t) + eb * (1.0-Math.exp(-aY*t)) * pi[C] / piY + (1.0-eb)*pi[C];
		matrix[C][A] = 																	(1.0-eb)*pi[A];
		matrix[C][G] = 																	(1.0-eb)*pi[G];
		
		matrix[G][T] = 																	(1.0-eb)*pi[T];
		matrix[G][C] =                        											(1.0-eb)*pi[C];
		matrix[G][A] =       				 eb * (1.0-Math.exp(-aR*t)) * pi[A] / piR + (1.0-eb)*pi[A];
		matrix[G][G] = Math.exp(-(aR+b)*t) + eb * (1.0-Math.exp(-aR*t)) * pi[G] / piR + (1.0-eb)*pi[G];
	}
	
	@Override
	public void setMatrix(double t, double[][] tmatrix) {
		if (recalcIntermediates) {
			recalculateIntermediates();
		}
		
		// t /= b; //Rescale branch length by b
		
		double eb = Math.exp(-b*t);
		
//		double er0 = Math.exp(-(aR)*t);
//		double er1 = eb * (1.0- Math.exp(-aR*t));
//		double er2 = (1.0- eb);
//
//		double ey0 = Math.exp(-(aY+b)*t);
//		double ey1 = eb * (1.0- Math.exp(-aY*t));
//		double ey2 = er2;
		
		double[] pi = stationaries.getStationaries();
		//TODO tons of potential optimizations here...
		
		//matrix[x][y] describes the probability of going from state x to y
		//matrix[i][j] = Pr{j | i}, going from i to j

		tmatrix[A][A] =  Math.exp(-(aR+b)*t) + eb * (1.0-Math.exp(-aR*t)) * pi[A] / piR + (1.0-eb)*pi[A];
		tmatrix[A][C] =    																 (1.0-eb)*pi[C];
		tmatrix[A][G] =    					  eb * (1.0-Math.exp(-aR*t)) * pi[G] / piR + (1.0-eb)*pi[G];
		tmatrix[A][T] =       															 (1.0-eb)*pi[T];
		
		tmatrix[T][T] = Math.exp(-(aY+b)*t) + eb * (1.0-Math.exp(-aY*t)) * pi[T] / piY + (1.0-eb)*pi[T];
		tmatrix[T][C] = 						 eb * (1.0-Math.exp(-aY*t)) * pi[C] / piY + (1.0-eb)*pi[C];
		tmatrix[T][A] = 																(1.0-eb)*pi[A];
		tmatrix[T][G] =  																(1.0-eb)*pi[G];
		
		tmatrix[C][T] = 						 eb * (1.0-Math.exp(-aY*t)) * pi[T] / piY + (1.0-eb)*pi[T];
		tmatrix[C][C] = Math.exp(-(aY+b)*t) + eb * (1.0-Math.exp(-aY*t)) * pi[C] / piY + (1.0-eb)*pi[C];
		tmatrix[C][A] = 																	(1.0-eb)*pi[A];
		tmatrix[C][G] = 																	(1.0-eb)*pi[G];
		
		tmatrix[G][T] = 																	(1.0-eb)*pi[T];
		tmatrix[G][C] =                        											(1.0-eb)*pi[C];
		tmatrix[G][A] =       				 eb * (1.0-Math.exp(-aR*t)) * pi[A] / piR + (1.0-eb)*pi[A];
		tmatrix[G][G] = Math.exp(-(aR+b)*t) + eb * (1.0-Math.exp(-aR*t)) * pi[G] / piR + (1.0-eb)*pi[G];
	}
	
	
//	public void setBranchLength(double t) {
//		double eb = Math.exp(-b*t);
//		
//		double expAR = Math.exp(-aR*t);
//		double expAY = Math.exp(-aY*t);
//		
//		double er0 = expAR*eb;
//		double er1 = eb * (1.0- expAR);
//		double er2 = (1.0- eb);
//
//		double ey0 = expAY*eb;
//		double ey1 = eb * (1.0- expAY);
//		double ey2 = er2;
//
//		double exPiT = ey2*pi[T];
//		double exPiC = ey2*pi[C];
//		double exPiG = ey2*pi[G];
//		double exPiA = ey2*pi[A];
//		
//		double eyPiTOverPiY = ey1 * pi[T] / piY;
//		double eyPiCOverPiY = ey1 * pi[C] / piY;
//		double eyPiAOverPiR = er1 * pi[A] / piR;
//		double eyPiGOverPiR = er1 * pi[G] / piR;
//		
//		//matrix[x][y] describes the probability of going from state x to y
//		//matrix[i][j] = Pr{j | i}, going from i to j
//		
//		matrix[T][T] = ey0 + eyPiTOverPiY + exPiT;
//		matrix[T][C] = 		 eyPiCOverPiY + exPiC;
//		matrix[T][A] = 						exPiA;
//		matrix[T][G] =  					exPiG;
//		
//		matrix[C][T] = 		 eyPiTOverPiY + exPiT;
//		matrix[C][C] = ey0 + eyPiCOverPiY + exPiC;
//		matrix[C][A] = 						exPiA;
//		matrix[C][G] = 						exPiG;
//
//		matrix[A][T] =                       exPiT;  //Purine to pyrimidine
//		matrix[A][C] =                       exPiC;
//		matrix[A][A] =  er0 + eyPiAOverPiR + exPiA;
//		matrix[A][G] =        eyPiGOverPiR + exPiG;
//		
//		matrix[G][T] = 						exPiT;
//		matrix[G][C] =                      exPiC;
//		matrix[G][A] =       eyPiAOverPiR + exPiA;
//		matrix[G][G] = er0 + eyPiGOverPiR + exPiG;
//	}

	@Override
	public void setMatrix(double t, float[][] matrix) {
		throw new IllegalArgumentException("Single precision not implemeted for TN93 model yet");
	}


	@Override
	public String getName() {
		return "TN93 mutation model";
	}
	
	public void parameterChanged(Parameter<?> source) throws ModificationImpossibleException {	
		if (source.isProposed()) {
			try {
				checkParamValidity();
				newValuesCount++;
				fireParameterChange();
			} catch (BadParameterComboException ex) {
				((AbstractParameter<?>)source).revertSilently();
				//System.out.println("Skipping bad parameter combo...");
				throw new ModificationImpossibleException("Bad parameter combo from TN93");
			} //Alert DL of change
		}
		
		recalcIntermediates = true;
	}



	@Override
	public void acceptValue() {
		recalcIntermediates = true;
	}
	
	@Override
	public void revertValue() {
		recalcIntermediates = true;
	}

	@Override
	protected void proposeNewValue(Parameter<?> source) {
		
	}

	





}
