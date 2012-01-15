package newgui.gui.display;


import gui.widgets.BorderlessButton;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import sequence.Alignment;

import newgui.alignment.AlignmentSummary;
import newgui.alignment.AlignmentView;
import newgui.gui.widgets.TextButton;
import newgui.gui.widgets.VerticalTextButtons;

/**
 * Displays an alignment and some basic info. Just for testing purposes.
 * @author brendan
 *
 */
public class FirstDisplay extends Display {

	private AlignmentView alignmentView;
	private JPanel alnContainer;
	private JPanel bottomHalf;

	public FirstDisplay(String title, Alignment aln) {
		setTitle(title);
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		
		//aln.setReference(aln.getConsensus());
		AlignmentSummary alnSummary = new AlignmentSummary(title, aln);

		alnContainer = new JPanel();
		alnContainer.setBorder(BorderFactory.createEmptyBorder(4, 10, 4, 10));
		alnContainer.setBackground(Display.defaultDisplayBackground);
		alnContainer.setLayout(new GridLayout(2, 0));
		alnContainer.setPreferredSize(new Dimension(500, 200));
		alnContainer.setMaximumSize(new Dimension(10000, 300));
		
		this.add(alnContainer);
		alnContainer.add(alnSummary, BorderLayout.CENTER);
		
		bottomHalf = new JPanel();
		bottomHalf.setBackground(Display.defaultDisplayBackground);
		
		bottomHalf.setMinimumSize(new Dimension(300, 200));
		bottomHalf.setPreferredSize(new Dimension(500, 300));
		
		bottomHalf.setLayout(new BoxLayout(bottomHalf, BoxLayout.X_AXIS));
		bottomHalf.setBorder(BorderFactory.createEmptyBorder(6, 20, 0, 0));
		VerticalTextButtons buttonGroup = new VerticalTextButtons();
		buttonGroup.setPadding(10);
		TextButton addAlnButton = new TextButton("Add alignment");
		buttonGroup.addTextButton(addAlnButton);
		TextButton confButton = new TextButton("Configure alignment");
		buttonGroup.addTextButton(confButton);
		TextButton startButton = new TextButton("Start analysis");
		startButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				showConfigAnalysisWindow();
			}
		});
		buttonGroup.addTextButton(startButton);
		bottomHalf.add(buttonGroup);
		
		this.add(new JSeparator(JSeparator.HORIZONTAL));
		this.add(bottomHalf);
		this.add(Box.createVerticalGlue());
	}

	protected void showConfigAnalysisWindow() {
		
	}
	
}
