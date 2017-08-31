package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.File;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateImportTask extends AbstractTask {
	private TemplateManager templateManager;

	@Tunable (description = "Name of template: ")
	public String templateName = "";
	
	@Tunable (description = "Location of file to import: ")
	public File templateFile = null;

	public TemplateImportTask(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		templateManager.importTemplate(
				templateName, templateFile);
	}
}