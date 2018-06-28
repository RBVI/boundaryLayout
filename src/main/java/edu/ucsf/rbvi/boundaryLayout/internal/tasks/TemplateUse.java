package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateUse extends AbstractNetworkViewTaskFactory {
	private final TemplateManager templateManager;
	
	public TemplateUse(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView netView) {
		return new TaskIterator(new TemplateUseTask(netView, templateManager));
	}
}