package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;


public class CreateTemplateThumbnailTask extends AbstractTask {
	public CytoPanelComponent thumbnailPanel;
	private CyServiceRegistrar registrar;

	public CreateTemplateThumbnailTask(CyServiceRegistrar registrar, 
			CytoPanelComponent thumbnailPanel) {
		super();
		this.registrar = registrar;
		this.thumbnailPanel = thumbnailPanel;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		if(thumbnailPanel.getComponent().isVisible())
			thumbnailPanel.getComponent().setVisible(false);
		else 
			thumbnailPanel.getComponent().setVisible(true);
		if(thumbnailPanel.getComponent().isVisible())
			registrar.registerService(thumbnailPanel, CytoPanelComponent.class, new Properties());
		else
			registrar.unregisterService(thumbnailPanel, CytoPanelComponent.class);
	}
}