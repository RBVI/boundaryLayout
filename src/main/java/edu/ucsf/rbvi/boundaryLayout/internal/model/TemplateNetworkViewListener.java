package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;

/**
 * Handles newly added or destroyed network views as well as newly loaded sessions. This class adjusts the template manager 
 * according to the manipulation of network views
 */
public class TemplateNetworkViewListener implements NetworkViewAddedListener, NetworkViewDestroyedListener, SessionLoadedListener{
	private CyServiceRegistrar registrar;
	private TemplateManager manager;

	/**
	 * Construct a network view listener which listens to the user manipulation of network
	 * views such as adding or destroying or loading sessions
	 * @param registrar provides services such as various managers
	 * @param manager is the template manager which must be adjusted when the events are fired
	 */
	public TemplateNetworkViewListener(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		this.registrar = registrar;
		this.manager = manager;
	}

	/**
	 * Handles an added network view event
	 */
	@Override
	public void handleEvent(NetworkViewAddedEvent viewAdded) {
		CyNetworkView addedView = viewAdded.getNetworkView();
		AnnotationManager annotationManager = registrar.getService(AnnotationManager.class);
		List<Annotation> annotations = annotationManager.getAnnotations(addedView);
		List<String> uuids = new ArrayList<>();
		if(annotations != null && !annotations.isEmpty())
			for(Annotation annotation : annotations) 
				uuids.add(annotation.getUUID().toString());
		manager.handleAddedNetworkView(addedView, uuids);
	}

	/**
	 * Handles a destroyed network view event
	 */
	@Override
	public void handleEvent(NetworkViewDestroyedEvent viewDestroyed) {
		Set<CyNetworkView> views = registrar.getService(CyNetworkViewManager.class).getNetworkViewSet();
		manager.handleDeletedNetworkView(views);
	}

	/**
	 * Handles a session loaded event 
	 */
	@Override
	public void handleEvent(SessionLoadedEvent loadedEvent) {
		Set<CyNetworkView> networkViews = loadedEvent.getLoadedSession().getNetworkViews();
		AnnotationManager annotationManager = registrar.getService(AnnotationManager.class);

		for(CyNetworkView newView : networkViews) {
			List<Annotation> annotations = annotationManager.getAnnotations(newView);
			List<String> uuids = new ArrayList<>();
			if(!annotations.isEmpty())
				for(Annotation annotation : annotations) 
					uuids.add(annotation.getUUID().toString());
			manager.handleAddedNetworkView(newView, uuids);
		}
	}
}