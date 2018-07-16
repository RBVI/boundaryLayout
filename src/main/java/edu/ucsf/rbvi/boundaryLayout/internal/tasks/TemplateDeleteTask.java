package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

/*
 * Deletes template in the templates view
 */
public class TemplateDeleteTask extends AbstractTask {
	private TemplateManager templateManager;
	private String templateToDelete = null;
	private TemplateThumbnailPanel templatePanel;
	
	@Tunable (description= "Are you sure you want to delete this template?: ")
	public boolean deleteTemplate = true;
	
	public TemplateDeleteTask(TemplateManager templateManager, TemplateThumbnailPanel templatePanel, String templateToDelete) {
		super();
		this.templateManager = templateManager;
		this.templatePanel = templatePanel;
		this.templateToDelete = templateToDelete;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) {	
		if(deleteTemplate && templateToDelete != null) 
			templateManager.deleteTemplate(templateToDelete);
		templatePanel.updateTemplatesPanel();
	}
}
