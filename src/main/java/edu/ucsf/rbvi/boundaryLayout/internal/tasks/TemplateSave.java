package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateSave extends AbstractNetworkViewTaskFactory {
	private TemplateManager templateManager;
	private final CyServiceRegistrar registrar;
	
	public TemplateSave(CyServiceRegistrar registrar, TemplateManager templateManager) {
		super();
		this.registrar = registrar;
		this.templateManager = templateManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new TemplateSaveTask(
				registrar, networkView, templateManager));
	}
}