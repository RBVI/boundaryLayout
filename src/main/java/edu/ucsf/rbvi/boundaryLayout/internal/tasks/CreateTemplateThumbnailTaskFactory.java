package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.Map;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CreateTemplateThumbnailTaskFactory extends AbstractTaskFactory {
	public CytoPanelComponent thumbnailPanel;
	private CyServiceRegistrar registrar;
	
	public CreateTemplateThumbnailTaskFactory(CyServiceRegistrar registrar, 
			TemplateManager manager, Map<String, Object> tasks) {
		super();
		this.registrar = registrar;
		thumbnailPanel = new TemplateThumbnailPanel(registrar, manager, tasks);
		thumbnailPanel.getComponent().setVisible(false);
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateTemplateThumbnailTask(
				registrar, thumbnailPanel));
	}
}