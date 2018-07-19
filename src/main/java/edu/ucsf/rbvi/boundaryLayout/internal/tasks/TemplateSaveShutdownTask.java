package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

/**
 * Prompts to save the template when the session is about to be closed
 */
public class TemplateSaveShutdownTask extends AbstractTask {
	@Tunable(description = "Would you like to save this template before closing? ")
	public boolean saveTemplate = false;
	
	private CyServiceRegistrar registrar; 
	private CyNetworkView networkView;
	private TemplateManager manager;
	private TemplateThumbnailPanel templatePanel;
	private TaskManager taskManager;
	
	public TemplateSaveShutdownTask(CyServiceRegistrar registrar, CyNetworkView networkView, 
			TemplateManager manager, TemplateThumbnailPanel templatePanel) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		this.manager = manager;
		this.templatePanel = templatePanel;
		this.taskManager = (TaskManager) registrar.getService(TaskManager.class);
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {
		if(saveTemplate) {
			TemplateOverwriteTask overwriteTask = null;
			if(manager.getCurrentActiveTemplate() != null) {
				overwriteTask = new TemplateOverwriteTask(registrar, networkView, manager, manager.getCurrentActiveTemplate());
				taskManager.execute(new TaskIterator(overwriteTask));
				if(overwriteTask.overwrite) 
					templatePanel.replaceThumbnailTemplate(manager.getCurrentActiveTemplate());
			}
			if(overwriteTask == null || !overwriteTask.overwrite) {
				TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, manager);
				taskManager.execute(new TaskIterator(saveTask));
			}
		}
	}
}