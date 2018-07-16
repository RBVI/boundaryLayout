package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

/*
 * Imports a template from a particular file
 */
public class TemplateImportTask extends AbstractTask {
	private TemplateManager templateManager;

	@Tunable (description = "Name of template: ")
	public String templateName = "";
	
	@Tunable (description = "Location of file to import: ", params = "input=true")
	public File templateFile = null;

	public TemplateImportTask(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		if(templateManager.hasTemplate(templateName))
			this.insertTasksAfterCurrentTask(new TemplateImportOverwriteTask(templateManager, this));
		else
			templateManager.importTemplate(templateName, templateFile);
	}
}
