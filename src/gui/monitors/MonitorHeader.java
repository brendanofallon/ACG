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


package gui.monitors;

import gui.ACGFrame;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import newgui.gui.ViewerWindow;
import newgui.gui.widgets.BorderlessButton;

public class MonitorHeader extends JPanel {

	protected JLabel text;
	protected JLabel label;
	protected Font font = ViewerWindow.sansFont.deriveFont(11f); //new Font("Sans", Font.PLAIN, 11);
	
	
	Color[] gradient = new Color[25];
	Color bottomLight = new Color(0.98f, 0.98f, 0.98f, 0.7f);
	Color bottomDark = new Color(0.7f, 0.7f, 0.7f, 0.4f);
	
	ImageIcon saveIcon = ACGFrame.getIcon("icons/saveIcon3.png");
	
	public MonitorHeader() {
		this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		this.setMinimumSize(new Dimension(10, 24));
		this.add(Box.createRigidArea(new Dimension(10, 20)));
		setOpaque(false);
		
		text = new JLabel("");
		label = new JLabel("");
		text.setFont(font);
		label.setFont(font);
		
		add(label);
		add(text);
		
		add(Box.createHorizontalGlue());
		
		BorderlessButton saveButton = new BorderlessButton(saveIcon);
		add(saveButton);
		
		float fadeStart = 0.7f;
		float fadeEnd = 1.0f;
		for(int i=0; i<gradient.length; i++) {
			float c = fadeStart + (fadeEnd-fadeStart)*(1.0f-(float)i/(float)(gradient.length-1));
			gradient[i] = new Color(c, c, c, 0.6f);
		}
	}
	
	public void setText(String text) {
		this.text.setText( text );
		repaint();
	}
	
	public void setLabel(String label) {
		this.label.setText(label);
		repaint();
	}
	
	
	
	public void setFont(Font font) {
		this.font = font;
	}
	
	public void setFontSize(float size) {
		font = font.deriveFont(size);
		repaint();
	}
	
	public void paintComponent(Graphics g) {
		Graphics2D g2d = (Graphics2D)g;
		g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
		
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, getWidth(), getHeight());
		
		
		for(int i=Math.max(1, getHeight()-gradient.length); i<getHeight(); i++) {
			g2d.setColor(gradient[i]);
			g2d.drawLine(0, i, getWidth(), i);
		}

		super.paintComponent(g);
		
	}
	
}
