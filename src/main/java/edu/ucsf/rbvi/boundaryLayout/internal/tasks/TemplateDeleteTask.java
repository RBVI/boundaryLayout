package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateDeleteTask extends AbstractTask {
	private TemplateManager templateManager;
	
	@Tunable (description= "Choose template to delete: ")
	public ListSingleSelection<String> templateNames = null;
	
	public TemplateDeleteTask(TemplateManager templateManager) {
		this.templateManager = templateManager;
		templateNames = new ListSingleSelection<>(
				templateManager.getTemplateNames());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {		
		templateManager.deleteTemplate(templateNames.getSelectedValue());
	}
}
