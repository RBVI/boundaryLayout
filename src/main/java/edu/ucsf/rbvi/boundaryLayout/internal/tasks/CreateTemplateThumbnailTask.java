package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CreateTemplateThumbnailTask extends AbstractTask {
	public CytoPanelComponent thumbnail;
	private final CyServiceRegistrar registrar;
	private final TemplateManager manager;

	public CreateTemplateThumbnailTask(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		this.manager = manager;
		this.registrar = registrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.WEST);
		thumbnail = new TemplateThumbnailPanel(registrar, manager);
		registrar.registerService(thumbnail, CytoPanelComponent.class, new Properties());
		if (cytoPanel.getState() == CytoPanelState.HIDE)
			cytoPanel.setState(CytoPanelState.DOCK);
	}
}
