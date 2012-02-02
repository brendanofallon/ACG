package newgui.gui.widgets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;

/**
 * A fairly thin wrapper for a JPanel with a gridlayout. Components must be added via addComponent
 * and removed via removeComponent
 * @author brendano
 *
 */
public class MultiFigurePanel extends JPanel {

	private List<JComponent> compList =new ArrayList<JComponent>();
	
	private int columns = 2; //Flexible number of columns
	
	public MultiFigurePanel() {
		this.setOpaque(false);
	}

	/**
	 * Set number of rows that are used to display components. 0 means flexible number,
	 * but both rows and columns cannot be zero
	 * @param cols
	 */
//	public void setRows(int rows) {
//		if (this.rows != rows) {
//			this.rows = rows;
//			relayout();
//		}
//	}
	
	/**
	 * Set number of columns that are used to display components. 0 means flexible number,
	 * but both rows and columns cannot be zero
	 * @param cols
	 */
	public void setColumns(int cols) {
		if (this.columns != cols) {
			this.columns = cols;
			relayout();
		}
	}
	
	/**
	 * Add a new component to this panel
	 * @param comp
	 */
	public void addComponent(JComponent comp) {
		compList.add(comp);
		relayout();
	}
	
	public void removeComponent(JComponent comp) {
		compList.remove(comp);
		relayout();
	}
	
	/**
	 * Redo the layout for all components. This should be called after every add and remove 
	 * of a component
	 */
	public void relayout() {
		this.removeAll();
		
		if (compList.size()==1) {
			this.setLayout(new BorderLayout());
			this.add(compList.get(0), BorderLayout.CENTER);
		}
		else {
			this.setLayout(new GridLayout(0, columns));
			//this.setLayout(new FlowLayout(FlowLayout.CENTER));
			Integer height = this.getHeight();
			Integer width = this.getWidth();

			int totRows = Math.max(1, compList.size() / columns);

			//Dimension componentSize = new Dimension(width / Math.min(compList.size(), columns), height / totRows-100);

			//System.out.println("Preferred size = " + componentSize.getWidth() + ", " + componentSize.getHeight());

			int count = 0;
			for(JComponent comp : compList) {
				//comp.setPreferredSize(componentSize);
				if (count > 0 && count % columns == 0)
					this.add(comp);
				else
					this.add(comp);
				this.add(comp);

				count++;
			}
		}
		revalidate();
		repaint();
	}
	
	
//	public Component add(Component comp) {
//		addComponent( (JComponent)comp);
//		return comp;
//	}
//	
//	public Component add(Component comp, int index) {
//		throw new IllegalArgumentException("Must use addComponent for MultiFigurePanels");
//	}
//	
//	public void add(Component comp, Object obj) {
//		throw new IllegalArgumentException("Must use addComponent for MultiFigurePanels");
//	}
}
