package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class CreateTemplateThumbnailTask extends AbstractTask {
	public CytoPanel thumbnailPanel;

	public CreateTemplateThumbnailTask(CytoPanel thumbnailPanel) {
		super();
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(thumbnailPanel.getState() == CytoPanelState.HIDE)
			thumbnailPanel.setState(CytoPanelState.DOCK);
		else if(thumbnailPanel.getState() == CytoPanelState.DOCK)
			thumbnailPanel.setState(CytoPanelState.HIDE);
	}
}
