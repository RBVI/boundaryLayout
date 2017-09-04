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

import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CreateTemplateThumbnailTask extends AbstractTask {
	private CyNetworkView networkView;
	public CytoPanelComponent thumbnail;
	private final CyServiceRegistrar registrar;

	public CreateTemplateThumbnailTask(CyServiceRegistrar registrar, CyNetworkView networkView) {
		super();
		this.networkView = networkView;
		this.registrar = registrar;
	}

	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		CySwingApplication swingApplication = registrar.getService(CySwingApplication.class);
		CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);
		thumbnail = new TemplateThumbnailPanel(registrar, networkView);
		registrar.registerService(thumbnail, CytoPanelComponent.class, new Properties());
		if (cytoPanel.getState() == CytoPanelState.HIDE)
			cytoPanel.setState(CytoPanelState.DOCK);
	}
}