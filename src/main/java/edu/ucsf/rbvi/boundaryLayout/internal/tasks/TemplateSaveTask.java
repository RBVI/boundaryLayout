package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateSaveTask extends AbstractTask {
	private final CyServiceRegistrar registrar;
	private final CyNetworkView networkView;
	private TemplateManager templateManager;
	
	@Tunable(description = "Name of new template: ")
	public String templateName = "";
	
	public TemplateSaveTask(CyServiceRegistrar registrar, 
			CyNetworkView networkView, TemplateManager templateManager) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		this.templateManager = templateManager;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		List<Annotation> annotations = registrar.getService(
				AnnotationManager.class).getAnnotations(networkView);
		templateManager.addTemplate(templateName, annotations);
	}
}