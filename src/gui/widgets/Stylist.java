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


package gui.widgets;

import java.awt.Component;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;

/**
 * A Stylist applies a number of Styles to a Component. For instance, if you're going to make a bunch 
 * of Components with the same background, font, etc., just add those as Styles to this and then
 * call applyStyles(Component comp)
 * @author brendano
 *
 */
public class Stylist {

	List<Style> styles;
	
	public void addStyle(Style s) {
		if (styles == null)
			styles = new ArrayList<Style>();
		styles.add(s);
	}

	
	/**
	 * Apply all styles in the list to the given component
	 * @param comp
	 * @return
	 */
	public JComponent applyStyle(JComponent comp) {
		if (styles == null)
			return comp;
		for(Style style : styles) {
			style.apply(comp);
		}
		
		return comp;
	}
}
