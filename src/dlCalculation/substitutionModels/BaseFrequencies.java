package dlCalculation.substitutionModels;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cern.jet.random.Gamma;

import logging.StringUtils;
import math.RandomSource;
import modifier.DirichletModifier;
import modifier.IllegalModificationException;
import modifier.ModificationImpossibleException;
import modifier.Modifier;

import parameter.AbstractParameter;
import parameter.InvalidParameterValueException;
import sequence.DNAUtils;

/**
 * A class to encapsulate the stationary frequencies for the evolutionary model
 * These typically get modified by a Dirichlet modifier. 
 * @author brendan
 *
 */
public class BaseFrequencies extends AbstractParameter<double[]> {
	
	static final int T = DNAUtils.T;
	static final int C = DNAUtils.C;
	static final int A = DNAUtils.A;
	static final int G = DNAUtils.G;
	
	//String[] logKeys = {"a.freq", "c.freq", "t.freq", "g.freq"};
	String[] logKey = new String[]{"a.freq\tc.freq\tg.freq\tt.freq"};
			
	public BaseFrequencies(Map<String, String> attrs, Modifier<?> mod) {
		this(attrs);
		addModifier(mod);
	}
	
	public BaseFrequencies(Map<String, String> attrs) {
		super(attrs);
		String statStr = attrs.get(TN93Matrix.XML_STATIONARIES);
		String[] stats = statStr.split(" ");
		if (stats.length != 4) {
			throw new IllegalArgumentException("Could not parse stationaries from argument list, got : " + statStr);
		}
	
		currentValue = new double[4];
		try {
			currentValue[A] = Double.parseDouble(stats[A]);
			currentValue[C] = Double.parseDouble(stats[C]);
			currentValue[T] = Double.parseDouble(stats[T]);
			currentValue[G] = Double.parseDouble(stats[G]);
		}
		catch (NumberFormatException nfe) {
			throw new IllegalArgumentException("Could not parse a number from the stationaries list : " + statStr);
		}	
		
		activeValue = currentValue;
	}
	
	
	public BaseFrequencies(double[] stationaries) {
		super(new HashMap<String, String>());
		currentValue = new double[4];
		System.arraycopy(stationaries, 0, currentValue, 0, 4);
		activeValue = currentValue;
	}
	
	
	public void addModifier(Modifier mod) {
		//TODO Make sure this modifier is OK for this parameter
		mod.setParameter(this);
		modifiers.add(mod);
		modFrequenciesKnown = false;
	}
	
	
	/**
	 * Returns the active stationary list
	 * @return
	 */
	public double[] getStationaries() {
		return getValue();
	}


	@Override
	public String getName() {
		return "base.frequencies";
	}

	public int getKeyCount() {
		return 1;
	}
	
	/**
	 * Returns a list of keys associated with the loggable items of this param. 
	 * Default implementation is a single item whose key is 'getName()'
	 */
	public String[] getLogKeys() {
		return logKey;
	}
	
	/**
	 * Return the current value of the log item associated with the given key.
	 * Default implementation is to always just return currentValue
	 */
	public Object getLogItem(String key) {
		double[] freqs = getStationaries();
		return "" + freqs[A] + "\t" + freqs[C] + "\t" + freqs[G] + "\t" + freqs[T];
	}
	
	public String getLogString() {
		double[] freqs = getStationaries();
		return "" + freqs[A] + "\t" + freqs[C] + "\t" + freqs[G] + "\t" + freqs[T];		
	}
	

	
//	public static void main(String[] args) {
//		RandomSource.initialize();
//		double[] stats = new double[]{0.25, 0.25, 0.25, 0.25};
//
//		BaseFrequencies freqs = new BaseFrequencies(stats);
//		DirichletModifier mod = new DirichletModifier();
//		freqs.addModifier(mod);
//		
//		try {
//			BufferedWriter writer = new BufferedWriter(new FileWriter("freqData.log"));
//			writer.write("A \t C \t T \t G \n");
//			for(int i=0; i<100000; i++) {
//				try {
//					mod.modify();
//				} catch (InvalidParameterValueException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (IllegalModificationException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				} catch (ModificationImpossibleException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//				freqs.acceptValue();
//				stats = freqs.getValue();
//				if (i%50==0) {
//					System.out.println(stats[0] + "\t" + stats[1] + "\t" + stats[2] + "\t" + stats[3]);
//					writer.write(stats[0] + "\t" + stats[1] + "\t" + stats[2] + "\t" + stats[3] + "\n" );
//				}
//			}
//			writer.close();
//
//		} catch (IOException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}
}
