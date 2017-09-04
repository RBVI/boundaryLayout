package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class CreateTemplateThumbnailTaskFactory extends AbstractTaskFactory {
	private CyServiceRegistrar registrar;
	private TemplateManager manager;
	
	public CreateTemplateThumbnailTaskFactory(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		this.registrar = registrar;
		this.manager = manager;
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateTemplateThumbnailTask(registrar, manager));
	}
}
