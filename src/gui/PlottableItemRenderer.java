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

import gui.PickMonitorsPanel.PlottableInfo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeCellRenderer;

public class PlottableItemRenderer extends JPanel implements TreeCellRenderer {

	Color stripeColor = new Color(220, 230, 240);
	JCheckBox checkBox;
	JLabel label;
	JLabel prop;
	
	//For non-selectable items (ones that aren't leaves) we return this component instead
	JPanel labelPanel;
	JLabel mainLabel;
	
	public PlottableItemRenderer() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		checkBox = new JCheckBox();
		label = new JLabel("  ");
		prop = new JLabel("  ");
		this.add(checkBox);
		this.add(Box.createHorizontalStrut(15));
		this.add(label);
		this.add(Box.createHorizontalStrut(30));
		this.add(prop);
		setPreferredSize(new Dimension(250, 30));
		setMaximumSize(new Dimension(2000, 30));
		
		labelPanel = new JPanel();
		labelPanel.setLayout(new BorderLayout());
		mainLabel = new JLabel();
		mainLabel.setFont( new Font("Sans", Font.BOLD, 12));
		labelPanel.setMinimumSize(new Dimension(50, 55));
		labelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
		//labelPanel.setPreferredSize(new Dimension(50, 30));
		labelPanel.add(mainLabel, BorderLayout.CENTER);
		labelPanel.setBackground(Color.white);
		mainLabel.setBackground(Color.white);
	}

	@Override
	public Component getTreeCellRendererComponent(JTree tree, Object item,
			boolean selected, boolean expanded, boolean leaf, int row, boolean focus) {
	
		DefaultMutableTreeNode node = (DefaultMutableTreeNode)item;
		Object userObject = node.getUserObject();
		PlottableInfo info = null;
		if (userObject instanceof gui.PickMonitorsPanel.PlottableInfo) {
			info = (PlottableInfo)userObject;
		}
		
		if (info != null) {
			checkBox.setSelected(info.selected);
			label.setText(info.descriptor);
		}
		else {
			label.setText(item.toString());
		}
		if (row%2==0)
			this.setBackground(stripeColor);
		else
			this.setBackground(Color.white);
		
		if (leaf)
			return this;
		else {
			mainLabel.setText(item.toString());
			return labelPanel;
		}
	}

}
