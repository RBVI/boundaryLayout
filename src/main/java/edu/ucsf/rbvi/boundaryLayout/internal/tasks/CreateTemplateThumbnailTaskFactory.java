package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CreateTemplateThumbnailTaskFactory extends AbstractTaskFactory {
	public CytoPanelComponent thumbnailPanel;
	private CyServiceRegistrar registrar;
	private boolean showComponent;
	private Properties panelProperties;
	
	public CreateTemplateThumbnailTaskFactory(CyServiceRegistrar registrar, 
			TemplateManager manager, Map<String, Object> tasks) {
		super();
		this.registrar = registrar;
		thumbnailPanel = new TemplateThumbnailPanel(registrar, manager, tasks);
		panelProperties = new Properties();
		panelProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Layout App");
		panelProperties.setProperty(IN_MENU_BAR, "true");
		panelProperties.setProperty(MENU_GRAVITY, "10.0");
		showComponent = true;
		initializePanel();
		registrar.registerService(thumbnailPanel, CytoPanelComponent.class, new Properties());
	}
	
	@Override
	public TaskIterator createTaskIterator() {
		registrar.unregisterService(this, TaskFactory.class);
		showComponent = (showComponent ? false : true);
		CreateTemplateThumbnailTask panelTask = createTask();
		return new TaskIterator(panelTask);
	}
	
	private CreateTemplateThumbnailTask createTask() {
		return new CreateTemplateThumbnailTask(registrar, this, thumbnailPanel, panelProperties, showComponent); 
	}
	
	private void initializePanel() {
		if(!showComponent)
			panelProperties.setProperty(TITLE, "Show Templates in Control Panel");
		else
			panelProperties.setProperty(TITLE, "Hide Templates in Control Panel");
		registrar.registerService(this, TaskFactory.class, panelProperties);
	}
}