package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskMonitor;


public class CreateTemplateThumbnailTask extends AbstractTask {
	public CytoPanelComponent thumbnailPanel;
	private CyServiceRegistrar registrar;
	private boolean showComponent;
	private Properties panelProperties;
	private TaskFactory panelFactory;

	public CreateTemplateThumbnailTask(CyServiceRegistrar registrar, TaskFactory panelFactory, 
			CytoPanelComponent thumbnailPanel, Properties panelProperties, boolean showComponent) {
		super();
		this.registrar = registrar;
		this.thumbnailPanel = thumbnailPanel;
		this.showComponent = showComponent;
		this.panelProperties = panelProperties;
		this.panelFactory = panelFactory;
	}

	@Override
	public void run(TaskMonitor taskMonitor) {
		if(!showComponent) {
			panelProperties.setProperty(TITLE, "Show Templates in Control Panel");
			registrar.unregisterService(thumbnailPanel, CytoPanelComponent.class);
		}
		else {
			panelProperties.setProperty(TITLE, "Hide Templates in Control Panel");
			registrar.registerService(thumbnailPanel, CytoPanelComponent.class, new Properties());
		}
		registrar.registerService(panelFactory, TaskFactory.class, panelProperties);
	}
}