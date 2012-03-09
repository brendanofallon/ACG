package newgui.gui.alignmentViewer;

import element.sequence.*;
import undoRedo.UndoableAction;
import undoRedo.UndoableActionSource;

/**
 * A base class to describe various types of actions that may be undone / redone by an SGDisplay
 * @author brendan
 *
 */
public class SGUndoableAction extends UndoableAction {

	SequenceGroup sgBeforeRemoval;
	boolean hasUnsavedChanges;
	String description;

	public SGUndoableAction(UndoableActionSource source, SequenceGroup sgBefore, boolean hasUnsavedChanges, String description) {
		super(source);
		sgBeforeRemoval = sgBefore;
		this.hasUnsavedChanges = hasUnsavedChanges;
		this.description = description;
	}
	
	@Override
	public String getDescription() {
		return description;
	}
}
