package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

/**
 * Overwrite the template with the same name as the import task
 */
public class TemplateImportOverwriteTask extends AbstractTask {
	private TemplateManager templateManager;
	private TemplateImportTask importTask;

	@Tunable (description = "There already exists a template with this name. "
			+ "Are you sure you would like to overwrite this template: ")
	public boolean overwrite = false;

	public TemplateImportOverwriteTask(TemplateManager templateManager, TemplateImportTask importTask) {
		super();
		this.templateManager = templateManager;
		this.importTask = importTask;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		if(overwrite)
			templateManager.importTemplate(importTask.templateName, importTask.templateFile);
	}
}
