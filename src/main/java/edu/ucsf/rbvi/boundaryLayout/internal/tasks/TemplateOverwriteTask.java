package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.util.ListSingleSelection;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateOverwriteTask extends AbstractTask {
	private final CyServiceRegistrar registrar;
	private final CyNetworkView networkView;
	private TemplateManager templateManager;
	
	@Tunable(description = "Select template to overwrite: ")
	public ListSingleSelection<String> templateNames = null;
	
	public TemplateOverwriteTask(CyServiceRegistrar registrar, 
			CyNetworkView networkView, TemplateManager templateManager) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		this.templateManager = templateManager;
		templateNames = new ListSingleSelection<>(
				templateManager.getTemplateNames());
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		List<Annotation> annotations = registrar.getService(
				AnnotationManager.class).getAnnotations(networkView);
		templateManager.overwriteTemplate(templateNames.getSelectedValue(), 
				annotations);
	}
}