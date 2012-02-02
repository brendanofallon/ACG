package tools;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public class StringUtilities {

	static NumberFormat formatter = new DecimalFormat("0.0###");
	
	public static String format(double val) {
		double absVal = Math.abs(val);
		
		if (absVal>100) {
			formatter.setMaximumFractionDigits(1);
		}
		else {
			if (absVal>1)
				formatter.setMaximumFractionDigits(2);
			else {
				double log = Math.log10(absVal);
				int dig = -1*(int)Math.round(log)+2; 
				formatter.setMaximumFractionDigits(dig);
			}
		}
		
		return formatter.format(val);
	}
	
	/**
	 * Format the given value to a human-readable string. If the absolute value
	 * of the value is less than one, it is given maximum 'digits' number of digits
	 * after the decimal point. If the value is greater than one but less than 100, it 
	 * is given 2 such digits, and other wise one. 
	 * 
	 * @param val
	 * @param digits
	 * @return
	 */
	public static String format(double val, int digits) {
		double absVal = Math.abs(val);

		if (absVal>100) {
			formatter.setMaximumFractionDigits(1);
		}
		else {
			if (absVal>1)
				formatter.setMaximumFractionDigits(2);
			else {
				double log = Math.log10(absVal);
				int dig = -1*(int)Math.round(log)+digits; 
				formatter.setMaximumFractionDigits(dig);
			}
		}
		
		return formatter.format(val);
	}
	
}
