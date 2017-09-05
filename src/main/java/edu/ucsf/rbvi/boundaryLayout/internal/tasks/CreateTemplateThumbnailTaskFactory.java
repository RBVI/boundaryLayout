package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskIterator;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CreateTemplateThumbnailTaskFactory extends AbstractTaskFactory {
	public CytoPanelComponent thumbnail;
	private CytoPanel thumbnailPanel;
	
	public CreateTemplateThumbnailTaskFactory(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
		thumbnailPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);
		thumbnail = new TemplateThumbnailPanel(registrar, manager);
		registrar.registerService(thumbnail, CytoPanelComponent.class, new Properties());
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new CreateTemplateThumbnailTask(thumbnailPanel));
	}
}