package gui.widgets;

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
