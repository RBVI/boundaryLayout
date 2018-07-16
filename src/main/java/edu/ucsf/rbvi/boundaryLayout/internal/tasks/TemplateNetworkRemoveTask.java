package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

/*
 * Remove specific template from the templates view
 */
public class TemplateNetworkRemoveTask extends AbstractTask {
	private final CyNetworkView networkView;
	private TemplateManager templateManager;

	public TemplateNetworkRemoveTask(CyNetworkView networkView, 
			TemplateManager templateManager) {
		super();
		this.networkView = networkView;
		this.templateManager = templateManager;
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		CyRow networkRow = networkTable.getRow(networkView.getSUID());
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		templateManager.removeTemplate(networkView);
	}
}