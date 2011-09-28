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


package gui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.RoundRectangle2D;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

/**
 * A fancily-decorated JPanel where the user can enter / choose an input file
 * @author brendano
 *
 */
public class FancyInputBox extends JPanel {

	static Color bgColor = new Color(253, 253, 253);
	
	static Color gray1 = Color.white;
	static Color gray2 = new Color(250, 250, 250, 100);
	static float topDark = 0.635f;
	static Color dark1 = new Color(topDark, topDark, topDark);
	static Color dark2 = new Color(220, 220, 220, 100);
	static Color shadowColor = new Color(0f, 0f, 0f, 0.1f);
	static Color lineColor = new Color(0.25f, 0.25f, 0.25f, 0.5f);
	static Stroke lineStroke = new BasicStroke(1.5f);
	
	static Stroke shadowStroke = new BasicStroke(1.6f);
	static Stroke normalStroke = new BasicStroke(1.0f);
	
	static final int roundCorner = 50;
	
	public FancyInputBox() {
		setBackground(bgColor);
		setBorder(BorderFactory.createEmptyBorder(5, 3, 0, 0));
	}
	
	public void paintComponent(Graphics g) {
		((Graphics2D)g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
				RenderingHints.VALUE_ANTIALIAS_ON);
		Graphics2D g2d = (Graphics2D)g;
	
		Shape newClip = new RoundRectangle2D.Double(1, 1, getWidth()-2, getHeight(), roundCorner-3, roundCorner-3);
		g2d.setClip(newClip);
		
		g2d.setColor(bgColor);
		((Graphics2D)g).setStroke(normalStroke);
		g.fillRoundRect(1, 1, getWidth()-2, getHeight()-2, roundCorner-3, roundCorner-3);
	
		//A gradient
		int sub = 0;
		float gradMax = Math.min(200, Math.max( getHeight()/2f, 50));
		for(float i=1; i<gradMax; i++) {
			float newVal = topDark + (0.99f-topDark)*(1.0f-(gradMax-i)/gradMax );
			g.setColor( new Color(newVal, newVal, newVal));
			g.drawLine(2+sub, getHeight()-(int)i, getWidth()-2-sub, getHeight()-(int)i);
		}
		
		g2d.setColor(lineColor);
		g2d.setStroke(lineStroke);
		g2d.drawRoundRect(1, 1, getWidth()-3, getHeight()-1, roundCorner-4, roundCorner-4);
		//g.setClip(prevClip);
	}
	
}
