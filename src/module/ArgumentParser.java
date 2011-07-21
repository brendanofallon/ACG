package module;

import java.util.HashMap;
import java.util.Map;

/**
 * A class to handle the parsing of an argument list 
 * @author brendano
 *
 */
public class ArgumentParser {

	private boolean parsed = false;
	
	//Stores arguments that have been supplied
	Map<String, String> suppliedArgs = new HashMap<String, String>();
	
	//Stores potential arguments
	//Map<String, Class> potentialArgs = new HashMap<String, Class>();
	
	public ArgumentParser() {
		//potentialArgs.put("length", Integer.class);
		//potentialArgs.put("frequency", Integer.class);
	}
	
	
	public void parse(String[] args) {
		parsed = true;
		for(int i=0; i<args.length; i++) {
			if (args[i].startsWith("-")) {
				String key = args[i].substring(1);
				if (i+1 < args.length) {
					String valStr = args[i+1];
					suppliedArgs.put(key, valStr);
				}
				
			}
		}
	}
	
	
	public Integer getIntegerOp(String key) {
		String op = suppliedArgs.get(key);
		if (op == null)
			return null;
		try {
			Integer opInt = Integer.parseInt(op);
			return opInt;
		}
		catch (NumberFormatException nfe) {
			System.out.println("Could not parse integer value from argument : " + key);
			System.exit(1);
		}
		
		return null;
	}
	
	public String getStringOp(String key) {
		return suppliedArgs.get(key);
	}
	
	
	public Long getLongOp(String key) {
		String op = suppliedArgs.get(key);
		if (op == null)
			return null;
		try {
			Long opL = Long.parseLong(op);
			return opL;
		}
		catch (NumberFormatException nfe) {
			System.out.println("Could not parse value from argument : " + key);
			System.exit(1);
		}
		
		return null;
	}


	public Double getDoubleOp(String key) {
		String op = suppliedArgs.get(key);
		if (op == null)
			return null;
		try {
			Double opD = Double.parseDouble(op);
			return opD;
		}
		catch (NumberFormatException nfe) {
			System.out.println("Could not parse value from argument : " + key);
			System.exit(1);
		}
		
		return null;
	}
}
