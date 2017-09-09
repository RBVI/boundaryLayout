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

public class TemplateOverwriteTask extends AbstractTask {
	private final CyServiceRegistrar registrar;
	private final CyNetworkView networkView;
	private TemplateManager templateManager;
	private String templateOverwriteName;
	
	@Tunable (description = ("Are you sure you want to overwrite the current template?"))
	public boolean overwrite = false;
	
	public TemplateOverwriteTask(CyServiceRegistrar registrar, CyNetworkView networkView, 
			TemplateManager templateManager, String templateOverwriteName) {
		super();
		this.registrar = registrar;
		this.networkView = networkView;
		this.templateManager = templateManager;
		this.templateOverwriteName = templateOverwriteName;
	}
	
	public void setTemplateOverwrite(String templateName) {
		this.templateOverwriteName = templateName;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {		
		List<Annotation> annotations = registrar.getService(
				AnnotationManager.class).getAnnotations(networkView);
		if(overwrite)
			templateManager.overwriteTemplate(templateOverwriteName, annotations);
	}
}