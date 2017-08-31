package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.List;

import org.cytoscape.model.CyColumn;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListMultipleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

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
		CyColumn templateColumn = networkView.getModel().
				getDefaultNetworkTable().getColumn(TemplateManager.NETWORK_TEMPLATES);
		List<String> templatesUsedNames = templateColumn.getValues(String.class);
		templateNames = new ListMultipleSelection<>(templatesUsedNames);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		templateManager.networkRemoveTemplates(networkView, 
				templateNames.getSelectedValues());
	}
}