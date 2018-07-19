package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import java.util.List;

import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.annotations.Annotation;
import org.cytoscape.view.presentation.annotations.AnnotationManager;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

/**
 * Overwrites a template with new annotation information in the templates view 
 */
public class TemplateOverwriteTask extends AbstractTask implements ObservableTask {
	public static final int CANCEL_STATE = -1;
	public static final int OVERWRITE_STATE = 0;
	public static final int SAVE_NEW_STATE = 1;
	private final CyServiceRegistrar registrar;
	private final CyNetworkView networkView;
	private TemplateManager templateManager;
	private String templateOverwriteName;
	private int stateOfButton = CANCEL_STATE;
	
	@Tunable (description = ("Click OK to add as new template. Check box to overwrite current template."))
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
		if(overwrite) {
			List<Annotation> annotations = registrar.getService(AnnotationManager.class).getAnnotations(networkView);
			templateManager.overwriteTemplate(templateOverwriteName, annotations);
			stateOfButton = OVERWRITE_STATE;
		} else {
			stateOfButton = SAVE_NEW_STATE;
			TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, templateManager);
			insertTasksAfterCurrentTask(saveTask);
		}
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		return null;
	}
	
	public int getButtonState() {
		return stateOfButton;
	}
}