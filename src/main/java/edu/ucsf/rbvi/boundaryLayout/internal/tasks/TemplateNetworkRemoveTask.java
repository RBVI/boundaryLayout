package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

/*
 * Remove specific template from the templates view
 */
public class TemplateNetworkRemoveTask extends AbstractTask {
	private final CyNetworkView networkView;
	private TemplateManager templateManager;

	@Tunable(description = "Choose templates to remove from view: ")
	public ListMultipleSelection<String> templateNames = null;

	public TemplateNetworkRemoveTask(CyNetworkView networkView, 
			TemplateManager templateManager) {
		super();
		this.networkView = networkView;
		this.templateManager = templateManager;
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		CyRow networkRow = networkTable.getRow(networkView.getSUID());
		List<String> activeTemplates = (List<String>) networkRow.getRaw(TemplateManager.NETWORK_TEMPLATES);
		if(activeTemplates != null)
			templateNames = new ListMultipleSelection<>(activeTemplates);
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(templateNames != null)
			templateManager.networkRemoveTemplates(networkView, templateNames.getSelectedValues());
	}
}