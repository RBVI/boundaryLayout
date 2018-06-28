package edu.ucsf.rbvi.boundaryLayout.internal;

import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.boundaryLayout.internal.layouts.ForceDirectedLayout;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateListener;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateUse;
import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.CreateTemplateThumbnailTaskFactory;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDelete;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateExport;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateImport;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateNetworkRemove;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;

/*
 * This class creates the task factories, managers, and listeners used by boundary layout
 */
public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// See if we have a graphics console or not
		boolean haveGUI = true;
		final CySwingApplication cyApplication = getService(bc, CySwingApplication.class);
		final CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
				
		if (cyApplication == null)
			haveGUI = false;
		
		Map<String, Object> taskFactories = new HashMap<>();
		
		TemplateManager templateManager = new TemplateManager(registrar);
		TemplateListener templateListener = new TemplateListener(templateManager, registrar);
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		registerService(bc, templateListener, CyShutdownListener.class, new Properties());
		
		/* Tasks */
		TaskFactory templateImportFactory = new TemplateImport(templateManager);
		taskFactories.put(TemplateThumbnailPanel.IMPORT_TEMPLATE, templateImportFactory);
		
		TaskFactory templateExportFactory = new TemplateExport(templateManager);
		taskFactories.put(TemplateThumbnailPanel.EXPORT_TEMPLATE, templateExportFactory);
		
		NetworkViewTaskFactory templateSaveFactory = new TemplateSave(registrar, templateManager);
		taskFactories.put(TemplateThumbnailPanel.ADD_TEMPLATE, templateSaveFactory);	
		
		NetworkViewTaskFactory templateUseFactory = new TemplateUse(templateManager);
		taskFactories.put(TemplateThumbnailPanel.USE_TEMPLATE, templateUseFactory);
		
		TaskFactory templateDeleteFactory = new TemplateDelete(templateManager);
		taskFactories.put(TemplateThumbnailPanel.DELETE_TEMPLATE, templateDeleteFactory);
		
		NetworkViewTaskFactory templateNetworkRemoveFactory = new TemplateNetworkRemove(templateManager);
		taskFactories.put(TemplateThumbnailPanel.REMOVE_TEMPLATE_FROM_VIEW, templateNetworkRemoveFactory);
		
		TaskFactory templateThumbnailFactory = new CreateTemplateThumbnailTaskFactory(registrar, templateManager, taskFactories); 
		
		CyLayoutAlgorithm forceDirectedLayoutAlgorithm = new ForceDirectedLayout(registrar, undoSupport);
		Properties forceDirectedLayoutAlgorithmProperties = new Properties();
		forceDirectedLayoutAlgorithmProperties.setProperty("preferredTaskManager", "menu");
		forceDirectedLayoutAlgorithmProperties.setProperty(TITLE, forceDirectedLayoutAlgorithmProperties.toString());
		forceDirectedLayoutAlgorithmProperties.setProperty(MENU_GRAVITY, "20.1");
		registerService(bc, forceDirectedLayoutAlgorithm, CyLayoutAlgorithm.class, forceDirectedLayoutAlgorithmProperties);
	}
}
