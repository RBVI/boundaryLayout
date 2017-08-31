package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateNetworkRemove extends AbstractNetworkViewTaskFactory {
	private TemplateManager templateManager;
	
	public TemplateNetworkRemove(TemplateManager templateManager) {
		super();
		this.templateManager = templateManager;
	}
	
	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return new TaskIterator(new TemplateNetworkRemoveTask( 
				networkView, templateManager));
	}
}