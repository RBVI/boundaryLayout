package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateUseTask extends AbstractTask {	
	private TemplateManager templateManager;
	private CyNetworkView networkView;
	
	@Tunable(description = "Choose the template text file to import")
	public ListSingleSelection<String> templateNames = null;

	public TemplateUseTask(CyNetworkView networkView, 
			TemplateManager templateManager) {
		super();
		this.networkView = networkView;
		this.templateManager = templateManager;
		templateNames = new ListSingleSelection<>(
				templateManager.getTemplateNames());
	}

	@Override
	public void run(TaskMonitor taskMonitor) {	
		templateManager.useTemplate(
				templateNames.getSelectedValue(), networkView);
	}
}