package edu.ucsf.rbvi.boundaryLayout.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.io.File;
import java.util.Properties;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.io.BasicCyFileFilter;
import org.cytoscape.io.DataCategory;
import org.cytoscape.io.read.InputStreamTaskFactory;
import org.cytoscape.io.util.StreamUtil;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.boundaryLayout.internal.layouts.ForceDirectedLayout;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLoad;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;

public class CyActivator extends AbstractCyActivator {

	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// See if we have a graphics console or not
		boolean haveGUI = true;
		final StreamUtil streamUtil = getService(bc, StreamUtil.class);
		final CySwingApplication cyApplication = getService(bc, CySwingApplication.class);
		final CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);
				
		if (cyApplication == null)
			haveGUI = false;
		
		/* Layouts */
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		CyLayoutAlgorithm forceDirectedLayoutAlgorithm = new ForceDirectedLayout(registrar, undoSupport);
		Properties forceDirectedLayoutAlgorithmProps = new Properties();
		forceDirectedLayoutAlgorithmProps.setProperty("preferredTaskManager", "menu");
		forceDirectedLayoutAlgorithmProps.setProperty(TITLE, forceDirectedLayoutAlgorithmProps.toString());
		forceDirectedLayoutAlgorithmProps.setProperty(MENU_GRAVITY, "20.1");
		registerService(bc, forceDirectedLayoutAlgorithm, CyLayoutAlgorithm.class, forceDirectedLayoutAlgorithmProps);
		
		NetworkViewTaskFactory templateSaveFactory = new TemplateSave(registrar);
		Properties templateSaveProps = new Properties();
		templateSaveProps.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateSaveProps.setProperty(TITLE, "Save Template");
		templateSaveProps.setProperty(IN_MENU_BAR, "true");
		templateSaveProps.setProperty(MENU_GRAVITY, "1");
		registerService(bc, templateSaveFactory, NetworkViewTaskFactory.class, templateSaveProps);
		
		TaskFactory templateLoadFactory = new TemplateLoad(registrar);
		Properties templateLoadProps = new Properties();
		templateSaveProps.setProperty(PREFERRED_MENU, "Apps.Boundary Constraint App");
		templateSaveProps.setProperty(TITLE, "Load Template");
		templateSaveProps.setProperty(IN_MENU_BAR, "true");
		templateSaveProps.setProperty(MENU_GRAVITY, "2");
		registerService(bc, templateLoadFactory, TaskFactory.class, templateLoadProps);
	}
}