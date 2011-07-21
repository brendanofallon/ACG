package logging;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringUtils {

	static NumberFormat formatter = new DecimalFormat("0.0###");
	
	public static String format(double val) {
		double absVal = Math.abs(val);
		
		if (absVal>1)
			formatter.setMaximumFractionDigits(1);
		else {
			double log = Math.log10(absVal);
			int dig = -1*(int)Math.round(log)+1; 
			formatter.setMaximumFractionDigits(dig);
		}
		
		
		return formatter.format(val);
	}
	
	public static String format(double val, int digits) {
		double absVal = Math.abs(val);
		
		if (absVal>1)
			formatter.setMaximumFractionDigits(digits);
		else {
			double log = Math.log10(absVal);
			int dig = -1*(int)Math.round(log)+digits; 
			formatter.setMaximumFractionDigits(dig);
		}
		
		
		return formatter.format(val);
	}
	
}
