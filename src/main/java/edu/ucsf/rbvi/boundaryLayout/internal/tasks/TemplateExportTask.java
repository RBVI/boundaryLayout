package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.File;
import java.nio.file.Path;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateExportTask extends AbstractTask {
	private TemplateManager templateManager;

	@Tunable (description = "Choose template to export: ")
	public ListSingleSelection<String> templateNames = null;
	
	@Tunable (description = "Location to export file: ")
	public File exportTemplateFile = null;

	public TemplateExportTask(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
		templateNames = new ListSingleSelection<>(
				templateManager.getTemplateNames());
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {	
		templateManager.exportTemplate(
				templateNames.getSelectedValue(), exportTemplateFile.getAbsolutePath());
	}
}