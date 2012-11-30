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


package newgui.gui.widgets;

import java.awt.Component;

import javax.swing.JComponent;

/**
 * Anything that can apply a style (background color, opacity, default font, etc.) to a JComponent
 * @author brendano
 *
 */
public interface Style {

	/**
	 * Perform some action on the component
	 * @param comp
	 */
	public void apply(JComponent comp);
	
}
