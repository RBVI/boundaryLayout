package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateExport extends AbstractTaskFactory {
	private TemplateManager templateManager;
	
	public TemplateExport(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new TemplateExportTask(templateManager));
	}
}