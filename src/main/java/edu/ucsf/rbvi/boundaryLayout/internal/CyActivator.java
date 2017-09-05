package edu.ucsf.rbvi.boundaryLayout.internal;

import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.CreateTemplateThumbnailTaskFactory;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDelete;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateExport;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateImport;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateNetworkRemove;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwrite;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;

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
		Properties templateImportProperties = new Properties();
		templateImportProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateImportProperties.setProperty(TITLE, "Import Template");
		templateImportProperties.setProperty(IN_MENU_BAR, "true");
		templateImportProperties.setProperty(MENU_GRAVITY, "1.0");
		registerService(bc, templateImportFactory, TaskFactory.class, templateImportProperties);
		taskFactories.put(templateImportProperties.getProperty(TITLE), templateImportFactory);
		
		TaskFactory templateExportFactory = new TemplateExport(templateManager);
		Properties templateExportProperties = new Properties();
		templateExportProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateExportProperties.setProperty(TITLE, "Export Template");
		templateExportProperties.setProperty(IN_MENU_BAR, "true");
		templateExportProperties.setProperty(MENU_GRAVITY, "1.1");
		registerService(bc, templateExportFactory, TaskFactory.class, templateExportProperties);
		taskFactories.put(templateExportProperties.getProperty(TITLE), templateExportFactory);
		
		NetworkViewTaskFactory templateSaveFactory = new TemplateSave(registrar, templateManager);
		Properties templateSaveProperties = new Properties();
		templateSaveProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateSaveProperties.setProperty(TITLE, "Save Annotations Template");
		templateSaveProperties.setProperty(IN_MENU_BAR, "true");
		templateSaveProperties.setProperty(MENU_GRAVITY, "0.9");
		registerService(bc, templateSaveFactory, NetworkViewTaskFactory.class, templateSaveProperties);
		taskFactories.put(templateSaveProperties.getProperty(TITLE), templateSaveFactory);	
		
		NetworkViewTaskFactory templateOverwriteFactory = new TemplateOverwrite(registrar, templateManager);
		Properties templateOverwriteProperties = new Properties();
		templateOverwriteProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateOverwriteProperties.setProperty(TITLE, "Overwrite Template");
		templateOverwriteProperties.setProperty(IN_MENU_BAR, "true");
		templateOverwriteProperties.setProperty(MENU_GRAVITY, "0.95");
		registerService(bc, templateOverwriteFactory, NetworkViewTaskFactory.class, templateOverwriteProperties);
		taskFactories.put(templateOverwriteProperties.getProperty(TITLE), templateOverwriteFactory);
		
		NetworkViewTaskFactory templateUseFactory = new TemplateUse(templateManager);
		Properties templateUseProperties = new Properties();
		templateUseProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateUseProperties.setProperty(TITLE, "Use Template");
		templateUseProperties.setProperty(IN_MENU_BAR, "true");
		templateUseProperties.setProperty(MENU_GRAVITY, "2.0");
		registerService(bc, templateUseFactory, NetworkViewTaskFactory.class, templateUseProperties);
		taskFactories.put(templateUseProperties.getProperty(TITLE), templateUseFactory);
		
		TaskFactory templateDeleteFactory = new TemplateDelete(templateManager);
		Properties templateDeleteProperties = new Properties();
		templateDeleteProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateDeleteProperties.setProperty(TITLE, "Delete Template");
		templateDeleteProperties.setProperty(IN_MENU_BAR, "true");
		templateDeleteProperties.setProperty(MENU_GRAVITY, "3.0");
		registerService(bc, templateDeleteFactory, TaskFactory.class, templateDeleteProperties);
		taskFactories.put(templateDeleteProperties.getProperty(TITLE), templateDeleteFactory);
		
		NetworkViewTaskFactory templateNetworkRemoveFactory = new TemplateNetworkRemove(templateManager);
		Properties templateNetworkRemoveProperties = new Properties();
		templateNetworkRemoveProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateNetworkRemoveProperties.setProperty(TITLE, "Remove Template from View");
		templateNetworkRemoveProperties.setProperty(IN_MENU_BAR, "true");
		templateNetworkRemoveProperties.setProperty(MENU_GRAVITY, "2.9");
		registerService(bc, templateNetworkRemoveFactory, NetworkViewTaskFactory.class, templateNetworkRemoveProperties);
		taskFactories.put(templateNetworkRemoveProperties.getProperty(TITLE), templateNetworkRemoveFactory);
		
		TaskFactory templateThumbnailFactory = new CreateTemplateThumbnailTaskFactory(registrar, templateManager, taskFactories); 
		Properties templateThumbnailProperties = new Properties();
		templateThumbnailProperties.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateThumbnailProperties.setProperty(TITLE, "Show Templates in Control Panel");
		templateThumbnailProperties.setProperty(IN_MENU_BAR, "true");
		templateThumbnailProperties.setProperty(MENU_GRAVITY, "10.0");
		registerService(bc, templateThumbnailFactory, TaskFactory.class, templateThumbnailProperties);
		
		CyLayoutAlgorithm forceDirectedLayoutAlgorithm = new ForceDirectedLayout(registrar, undoSupport);
		Properties forceDirectedLayoutAlgorithmProperties = new Properties();
		forceDirectedLayoutAlgorithmProperties.setProperty("preferredTaskManager", "menu");
		forceDirectedLayoutAlgorithmProperties.setProperty(TITLE, forceDirectedLayoutAlgorithmProperties.toString());
		forceDirectedLayoutAlgorithmProperties.setProperty(MENU_GRAVITY, "20.1");
		registerService(bc, forceDirectedLayoutAlgorithm, CyLayoutAlgorithm.class, forceDirectedLayoutAlgorithmProperties);
	}
}
