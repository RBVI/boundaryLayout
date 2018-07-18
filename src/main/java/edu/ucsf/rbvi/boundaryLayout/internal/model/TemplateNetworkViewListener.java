package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

public class TemplateNetworkViewListener implements NetworkViewAddedListener, NetworkViewDestroyedListener {
	private CyServiceRegistrar registrar;
	private TemplateManager manager;
	
	public TemplateNetworkViewListener(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		this.registrar = registrar;
		this.manager = manager;
	}
	
	@Override
	public void handleEvent(NetworkViewAddedEvent addedEvent) {
		System.out.println("Network has been added!");
		CyNetworkView addedView = addedEvent.getNetworkView();
		AnnotationManager annotationManager = registrar.getService(AnnotationManager.class);
		List<Annotation> annotations = annotationManager.getAnnotations(addedView);
		List<String> uuids = new ArrayList<>();
		if(!annotations.isEmpty())
			for(Annotation annotation : annotations) 
				uuids.add(annotation.getUUID().toString());
		manager.handleAddedNetworkView(addedView, uuids);
	}

	@Override
	public void handleEvent(NetworkViewDestroyedEvent destroyedEvent) {
		System.out.println("Network has been destroyed!");
		Set<CyNetworkView> views = registrar.getService(CyNetworkViewManager.class).getNetworkViewSet();
		manager.handleDeletedNetworkView(views);
	}
}
