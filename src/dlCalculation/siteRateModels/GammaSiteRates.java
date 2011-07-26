package dlCalculation.siteRateModels;

import java.util.Map;

import cern.jet.random.AbstractContinousDistribution;
import cern.jet.random.Gamma;
import math.RandomSource;
import modifier.ModificationImpossibleException;
import parameter.DoubleParameter;
import parameter.InvalidParameterValueException;
import parameter.Parameter;
import parameter.ParameterListener;
import xml.XMLUtils;

/**
 * A discretized gamma-site rate variation model
 * 
 * @author brendan
 *
 */
public class GammaSiteRates extends AbstractSiteRateModel {

	DoubleParameter alphaParam;
	
	private final double[] equalProbs;
	final int categories;

	
	public GammaSiteRates(int categories, double alpha) {
		super(categories);
		this.categories = categories;
		
		equalProbs = new double[categories];
		for(int i=0; i<categories; i++)
			equalProbs[i] = 1.0/(double)categories;
		
		//If no parameter is supplied, one pops into existence here
		alphaParam = new DoubleParameter(alpha, "siteRates.alpha", "alpha", 0, 1000);
		try {
			proposeRates();
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial value for alpha: " + alphaParam.getValue());
		} //Set rates
		catch (InvalidParameterValueException e) {
			throw new IllegalArgumentException("Invalid initial value for alpha: " + alphaParam.getValue());
		}
	}
	
	public GammaSiteRates(Map<String, String> attrs, DoubleParameter alphaParam) {
		this( XMLUtils.getIntegerOrFail("categories", attrs), alphaParam);
	}
	
	public GammaSiteRates(int categories, DoubleParameter alphaParam) {
		super(categories);

		this.categories = categories;
		equalProbs = new double[categories];
		alphaParam.setLowerBound(0.01);
		alphaParam.setUpperBound(50.0);
		for(int i=0; i<categories; i++)
			equalProbs[i] = 1.0/(double)categories;
		
		this.alphaParam = alphaParam;
		try {
			proposeRates();
			alphaParam.acceptValue();
		} catch (ModificationImpossibleException e) {
			throw new IllegalArgumentException("Invalid initial value for alpha: " + alphaParam.getValue());
		} //Set rates 
		catch (InvalidParameterValueException e) {
			throw new IllegalArgumentException("Invalid initial value for alpha: " + alphaParam.getValue());
		}
		addParameter(alphaParam);
	}

	@Override
	public String getName() {
		return "Gamma-distributed site rates";
	}

	/**
	 * When alpha changes we need to recompute the rates. But first check to make sure it's alpha and that a 
	 * new state has been proposed for alpha, if not ignore this
	 */
	protected void proposeNewValue(Parameter<?> source) {
		if (source == alphaParam && source.isProposed()) {
			try {
				proposeRates();
			} catch (ModificationImpossibleException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidParameterValueException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * called when a new value has been proposed for alpha,
	 * @throws ModificationImpossibleException 
	 * @throws InvalidParameterValueException 
	 */
	private void proposeRates() throws ModificationImpossibleException, InvalidParameterValueException {
		if (alphaParam.getValue() > alphaParam.getUpperBound()) {
			throw new InvalidParameterValueException(alphaParam, "Alpha value too low for accurate calculation of gamma-site-rates (" + alphaParam.getValue() + ")");
		}
		//System.out.println("Proposing alpha of : " + alphaParam.getValue());
		SiteRates proposal = new SiteRates();
		proposal.probabilities = equalProbs;
		proposal.rates = new double[categories];
		
		double prob = 1.0/(double)categories;
		
		double totalMean = 0;
		double rateSum = 0;
		System.out.println("Proposing new rates, alpha: " + alphaParam.getValue() );
		if (categories > 1) {
			Gamma cDist = new Gamma(1.0/alphaParam.getValue(), 1.0/alphaParam.getValue(), null); //Not a bug, both parameters really are alpha to get the right cdf
			Gamma gamDist = new Gamma(1.0/alphaParam.getValue(), alphaParam.getValue(), null);

			double prevCutoff = 0;
			for(int i=0; i<categories-1; i++) {
				double cutoff;
				cutoff = findCutoff(cDist, prob, 1e-6); //Finds edge of bin 

				double rate = calcRate(gamDist, prevCutoff, cutoff); //Finds rate for this bin
				proposal.rates[i] = rate;
				rateSum += rate;
				totalMean += rate * 1.0/(double)categories;

				System.out.println("Category: " + i + "\t rate: " + rate);
				prevCutoff = cutoff;
				prob += 1.0/(double)categories;
			}
		}
		
		//The rate for the last bin is hard to compute, since where is the upper boundary (it's infinite)? 
		//Instead, we assume we've done the math right for the other categories, and thus we know 
		//what the rate should be for the last bin (since all rates average to 1.0)
		//This is robust but in a bad way, since if we don't calculate the rates correctly for the
		//other categories we'll get everything wrong and not know about it... but at least the mean will always be 1.0
		proposal.rates[categories-1] = categories - rateSum;  
		totalMean += proposal.rates[categories-1] / (double)categories;
		
		System.out.println("Category: " + (categories-1) + "\t rate: " + proposal.rates[categories-1]);
		System.out.println("Overall average rate: "+ totalMean);
		
		if (totalMean < 0.99 || totalMean > 1.01) {
			throw new IllegalStateException("There was an error calculating the rate categories for the gamma distributed rates model, alpha: " + alphaParam.getValue() + " mean of rates: " + totalMean);
		}
		
//		for(int i=0; i<categories; i++) {
//			proposal.rates[i] = 1.0;
//		}
		proposeValue(proposal);
	}
	
	/**
	 * Computes the approximate expectation of the gamma function for the given intervql
	 * @param gamDist
	 * @param lower
	 * @param upper
	 * @return
	 */
	public static double calcRate(Gamma gamDist, double lower, double upper) {
		final int bins = 1000;
		final double binWidth = (upper-lower)/(double)bins;
		
		double x = lower;
		double y = gamDist.pdf(x);
		double area = 0;
		double ex = 0; 
		for(int i=0; i<bins+1; i++) {
			double x1 = x+binWidth;
			double y1 = gamDist.pdf(x1);
			
			double m = (y1-y)/(x1-x); //Find slope and intercept of line that goes through (x, y) and (x1, x1)
			double b = y - m*x;
			
			area += m*0.5*(x1*x1-x*x) + b*(x1-x);
			ex += m*0.333*(x1*x1*x1 - x*x*x) + b*0.5*(x1*x1 - x*x);
			
			y = y1;
			x = x1;
		}
		
		//System.out.println("upper: " + upper + " area: " + area);
		return ex / area;
	}


	/**
	 * Finds the x-value which lies above x% of the mass of the given distribution, to tolerance given by tol
	 * I guess you could call this a badly implemented inverse-cdf function. 
	 * @param d
	 * @param tol
	 * @return
	 */
	public static double findCutoff(Gamma cdfFunction, double x, double tol) {		
		if (x>=1)
			return 100.0;
		
		double lower = 0;
		double upper = 100;
		

		while(cdfFunction.cdf(upper)<x) {
			upper *= 2;
			System.out.println("Expanding upper bound of search to : " + upper);
		}
		double mid = (upper+lower)/2.0;	
		
		int count = 0;
		double cdf = cdfFunction.cdf(mid);		
		
		//Use a bisection search to find the point
		while(Math.abs(cdf-x) > tol) {
			mid = (upper+lower)/2.0;	
			cdf = cdfFunction.cdf(mid);
			if (cdf > x)
				upper = mid;
			else
				lower = mid;
			//System.out.println(lower + ".." + upper + " : " + cdf);
			
			count++;
		}
		
		//System.out.println("Iterations : " + count);
		return mid;
	}


	public static void emit(Gamma d, double alpha, double beta) {
		Gamma cD = new Gamma(alpha, alpha, null);
		for(double x=0.1; x<5; x+=0.1) {
			System.out.println(x + "\t" + d.pdf(x) + "\t" + cD.cdf(x));
		}
	}
	
	
	public static void main(String[] args) {
		RandomSource.initialize();
		double alpha = 0.01;
		Gamma d = new Gamma(alpha, 1.0/alpha,  null);
		Gamma cD = new Gamma(alpha, alpha, null);
		
		DoubleParameter alphaParam = new DoubleParameter(alpha, "alpha", "alpha", 0, 100);
		GammaSiteRates gRates = new GammaSiteRates(4, alphaParam);
		
		
		for(double x=0.1; x<10; x+=0.1) {
			try {
				alphaParam.proposeValue(x);
				alphaParam.acceptValue();
				gRates.acceptValue();
			} catch (ModificationImpossibleException e) {
				e.printStackTrace();
			}
			
		}
		
//		//double r = calcRate(d, 1.0, 1.25);
//		//System.out.println("Rate : " + r);
//		
//		//double c = findCutoff(cD, 0.1, 1e-6);
////		
////		System.out.println("Found cutoff:" + c);
////		System.out.println("CDF at cutoff:" + cD.cdf(c));
	}

	public double getAlpha() {
		return alphaParam.getValue();
	}

}
