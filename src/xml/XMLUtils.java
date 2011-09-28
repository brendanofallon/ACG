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


package xml;

import java.util.Map;

/**
 * Some oft-used static methods for parsing stuff from text / xml
 * @author brendan
 *
 */
public class XMLUtils {
	

	/**
	 * Attempt to find a Double in the map associated with the given key. 
	 * @param key
	 * @param attrs
	 * @return A new Double parsed from the value associated with the key provided
	 * @throws AttributeNotFoundException If key if not found in map 
	 */
	public static Double getDoubleOrFail(String key, Map<String, String> attrs) throws AttributeNotFoundException {
		String value = attrs.get(key);
		if (value == null) {
			throw new AttributeNotFoundException("Could not find required attribute : " + key);
		}
		
		try {
			Double dVal = Double.parseDouble(value);
			return dVal;
		}
		catch (NumberFormatException nfe) {
			throw new UnparseableValueException("Could not parse value from required attribute: " + key + " (got " + value + ")");
		}
		
	}
	
	/**
	 * Attempt to find a Double in the map associated with the given key. 
	 * @param key
	 * @param attrs
	 * @return A new Double parsed from the value associated with the key provided
	 * @throws AttributeNotFoundException If key if not found in map 
	 */
	public static Integer getIntegerOrFail(String key, Map<String, String> attrs) throws AttributeNotFoundException {
		String value = attrs.get(key);
		if (value == null) {
			throw new AttributeNotFoundException("Could not find required attribute : " + key);
		}
		
		try {
			Integer val = Integer.parseInt(value);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new UnparseableValueException("Could not parse integer from required attribute: " + key + " (got " + value + ")");
		}
		
	}

	
	/**
	 * Attempt to find a Integer in the map associated with the given key. 
	 * @param key
	 * @param attrs
	 * @return A new Integer parsed from the value associated with the key provided
	 * @throws AttributeNotFoundException If key if not found in map 
	 */
	public static Integer getOptionalInteger(String key, Map<String, String> attrs) throws AttributeNotFoundException {
		String value = attrs.get(key);
		if (value == null) {
			return null;
		}
		
		try {
			Integer val = Integer.parseInt(value);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new UnparseableValueException("Could not parse integer from optional attribute: " + key + " (got " + value + ")");
		}
		
	}
	
	
	/**
	 * Attempt to find a boolean (string) in the map associated with the given key. 
	 * @param key
	 * @param attrs
	 * @return A new Boolean parsed from the value associated with the key provided
	 * @throws AttributeNotFoundException If key if not found in map 
	 */
	public static Boolean getOptionalBoolean(String key, Map<String, String> attrs) throws AttributeNotFoundException {
		String value = attrs.get(key);
		if (value == null) {
			return null;
		}
		
		try {
			Boolean val = Boolean.parseBoolean(value);
			return val;
		}
		catch (NumberFormatException nfe) {
			throw new UnparseableValueException("Could not parse boolean from optional attribute: " + key + " (got " + value + ")");
		}
		
	}
	
	/**
	 * Attempt to find and parse a double value associated with the key in the map provided. This will not throw a runtime exception
	 * if the value is not found (but an exception IS thrown if something is provided but a double value cannot
	 * be parsed from it)
	 * @param key
	 * @param attrs
	 * @return
	 */
	public static Double getOptionalDouble(String key, Map<String, String> attrs) {
		String value = attrs.get(key);
		if (value == null) {
			return null;
		}
		
		try {
			Double dVal = Double.parseDouble(value);
			return dVal;
		}
		catch (NumberFormatException nfe) {
			throw new UnparseableValueException("Could not parse value from required attribute: " + key + " (got " + value + ")");
		}
		
	}
	
	/**
	 * Return the (String) value associated with the given key or null if there is no entry for the key
	 * @param key
	 * @param attrs
	 * @return
	 */
	public static String getOptionalString(String key,
			Map<String, String> attrs) {
		return attrs.get(key);
	}
	
	/**
	 * Return a the string value associated with the given key, or throw a runtime exception if there
	 * is no such value
	 * @param key
	 * @param attrs
	 * @return
	 * @throws AttributeNotFoundException
	 */
	public static String getStringOrFail(String key, Map<String, String> attrs) throws AttributeNotFoundException {
		String str = attrs.get(key);
		if (str == null)
			throw new AttributeNotFoundException("Could not find required attribute : " + key);
		else 
			return str;
	}
	
	/**
	 * Thrown when a non-optional attribute has not been found
	 * @author brendan
	 *
	 */
	static class AttributeNotFoundException extends RuntimeException {
	
		public AttributeNotFoundException(String message) {
			super(message);
		}
	}
	
	static class UnparseableValueException extends RuntimeException {
		
		public UnparseableValueException(String message) {
			super(message);
		}
	}


	
}
