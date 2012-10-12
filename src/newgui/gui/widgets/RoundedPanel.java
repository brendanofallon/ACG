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

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.Point;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A panel with a rounded border a gradient background
 * @author brendan
 *
 */
public class RoundedPanel extends JPanel {

	private Color borderColor = Color.gray;
	private BasicStroke borderStroke = new BasicStroke(1.2f);
	private Color gradientBottom = new Color(0.85f, 0.85f, 0.85f);
	private Color gradientTop = new Color(0.95f, 0.95f, 0.95f);
	private GradientPaint gradient = null;
	private JPanel mainPanel = null;
	
	public RoundedPanel() {
		setLayout(new BorderLayout());
		setBorder(BorderFactory.createEmptyBorder(10, 8, 8, 8));
		setAlignmentY(CENTER_ALIGNMENT);
		setAlignmentX(CENTER_ALIGNMENT);
		mainPanel = new JPanel();
		mainPanel.setLayout(new FlowLayout(FlowLayout.LEFT)); // new BoxLayout(mainPanel, BoxLayout.X_AXIS));
		mainPanel.setOpaque(false);
		mainPanel.setBackground(new Color(1.0f, 1.0f, 1.0f, 0f));
		mainPanel.setPreferredSize(new Dimension(500, 50));
		super.add(mainPanel, BorderLayout.CENTER);
	}
	
	public void setLayout(LayoutManager layout) {
		//setLayout is called in JPanel constructor, which happens before we call any code here
		//so if mainPanel has not been initialized we're probably calling from JPanel.init,
		//in which case we should be using the JPanel version of setLayout()
		if (mainPanel == null)
			super.setLayout(layout);
		else
			mainPanel.setLayout(layout);
	}
	
	public Component add(Component comp) {
		return mainPanel.add(comp);
	}
	
	public Component add(Component comp, int i) {
		return mainPanel.add(comp, i);
	}
	
	public void add(Component comp, String str) {
		mainPanel.add(comp, str);
	}
	
	public void add(Component comp, Object constraints) {
		mainPanel.add(comp, constraints);
	}
	
	public void remove(Component comp) {
		mainPanel.remove(comp);
		mainPanel.revalidate();
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		super.paintComponent(g);
		
		if (gradient == null)
			gradient = new GradientPaint(new Point(10, 2), gradientTop, new Point(10, getHeight()-2), gradientBottom);
			
		g2d.setPaint(gradient);
		g2d.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		
		g2d.setColor(borderColor);
		g2d.setStroke(borderStroke);
		g2d.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
		
	}
}
