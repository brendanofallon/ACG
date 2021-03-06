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


package parameter;

/**
 * Exceptions of this type are thrown when an invalid value is supplied as the argument to setValue to a parameter
 * @author brendan
 *
 */
public class InvalidParameterValueException extends Exception {

	Parameter source;
	String message;
	
	public InvalidParameterValueException(Parameter source, String message) {
		super(message);
		this.source = source;
		this.message = message;
	}
	
	public Parameter getSource() {
		return source;
	}
	
}
