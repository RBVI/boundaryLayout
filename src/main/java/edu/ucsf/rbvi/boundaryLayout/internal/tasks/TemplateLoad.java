package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.io.InputStream;

import org.cytoscape.io.CyFileFilter;
import org.cytoscape.io.read.AbstractInputStreamTaskFactory;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.work.AbstractTaskFactory;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;

public class TemplateLoad extends AbstractTaskFactory {
	final CyServiceRegistrar registrar;
	
	public TemplateLoad(CyServiceRegistrar registrar) {
		super();
		this.registrar = registrar;
	}

	@Override
	public boolean isReady() {
		return true;
	}

	@Override
	public TaskIterator createTaskIterator() {
		return new TaskIterator(new TemplateLoadTask(registrar));
	}
}