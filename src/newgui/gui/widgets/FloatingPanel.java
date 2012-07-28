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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.RadialGradientPaint;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;

/**
 * A panel with a shadow below and right that appears to "float" above the
 * background
 * @author brendan
 *
 */
public class FloatingPanel extends JPanel {
	
	private int shadowXDepth = 4;
	private int shadowYDepth = 4;
	private JPanel mainPanel;
	private JPanel rightPanel;
	private JPanel bottomPanel;
	
	private Color darkShadowColor = new Color(0.4f, 0.4f, 0.4f, 0.6f);
	private Color lightShadowColor = new Color(0.7f, 0.7f, 0.7f, 0.2f);
	private Color transparentColor = new Color(0.5f, 0.5f, 0.5f, 0.0f);
	private GradientPaint gradientX = null;
	private GradientPaint gradientY = null;
	
	
	public FloatingPanel() {
		super.setLayout(new BorderLayout());
		mainPanel = new JPanel();
		super.add(mainPanel, BorderLayout.CENTER);
		
		rightPanel = new JPanel();
		rightPanel.setLayout(new BoxLayout(rightPanel, BoxLayout.Y_AXIS));
		rightPanel.add(Box.createRigidArea(new Dimension(shadowXDepth+2, shadowXDepth+2)));
		rightPanel.setOpaque(false);
		rightPanel.setBackground(transparentColor);
		super.add(rightPanel, BorderLayout.EAST);
		
		bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
		bottomPanel.add(Box.createRigidArea(new Dimension(shadowYDepth+2, shadowYDepth+1)));
		bottomPanel.setOpaque(false);
		bottomPanel.setBackground(transparentColor);
		super.add(bottomPanel, BorderLayout.SOUTH);
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
	
	public void add(Component comp, Object constraints) {
		mainPanel.add(comp, constraints);
	}
	
	public JPanel getMainPanel() {
		return mainPanel;
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);

		//g2d.setColor(darkShadowColor);
		//g2d.drawLine(3, getHeight()-shadowYDepth-1, getWidth()-7, getHeight()-shadowYDepth-1);
	
		gradientX = new GradientPaint(new Point2D.Double(getWidth()-shadowXDepth-4, 5), darkShadowColor, new Point2D.Double(getWidth()-1, 5), lightShadowColor);
		g2d.setPaint(gradientX);
		g2d.fillRoundRect(getWidth()-shadowXDepth-2, 4, shadowXDepth+2, getHeight()-8, 2, 4);
		
		gradientY = new GradientPaint(new Point2D.Double(5, getHeight()-shadowYDepth-2), darkShadowColor, new Point2D.Double(5, getHeight()), lightShadowColor);
		g2d.setPaint(gradientY);
		g2d.fillRoundRect(3, getHeight()-shadowYDepth-1, getWidth()-9, shadowYDepth, 4, 2);
		
		RadialGradientPaint corner = new RadialGradientPaint(new Point2D.Double(getWidth()-shadowXDepth-2, getHeight()-shadowYDepth-3), 6f, new float[]{0.25f, 1.0f}, new Color[]{darkShadowColor, lightShadowColor});
		g2d.setPaint(corner);
		g2d.fillRoundRect(getWidth()-shadowXDepth-2, getHeight()-shadowYDepth, shadowXDepth+1, shadowYDepth+1, 1, 1);
		super.paintComponent(g);
	}
}
