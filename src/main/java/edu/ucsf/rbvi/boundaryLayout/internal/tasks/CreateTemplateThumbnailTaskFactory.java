package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

public class CreateTemplateThumbnailTaskFactory extends AbstractNetworkViewTaskFactory {
	private CyServiceRegistrar registrar;
	
	public CreateTemplateThumbnailTaskFactory(CyServiceRegistrar registrar) {
		super();
		this.registrar = registrar;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new CreateTemplateThumbnailTask(
				registrar, networkView));
	}
}