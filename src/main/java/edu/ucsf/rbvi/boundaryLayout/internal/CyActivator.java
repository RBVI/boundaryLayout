package edu.ucsf.rbvi.boundaryLayout.internal;

import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.INSERT_SEPARATOR_BEFORE;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.TITLE;

import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.work.undo.UndoSupport;
import org.osgi.framework.BundleContext;

import edu.ucsf.rbvi.boundaryLayout.internal.layouts.ForceDirectedLayout;

public class CyActivator extends AbstractCyActivator {
	
	public CyActivator() {
		super();
	}

	public void start(BundleContext bc) {
		// See if we have a graphics console or not
		boolean haveGUI = true;
		CySwingApplication cyApplication = getService(bc, CySwingApplication.class);
		CyApplicationManager cyApplicationManager = getService(bc, CyApplicationManager.class);
		CyServiceRegistrar registrar = getService(bc, CyServiceRegistrar.class);

		if (cyApplication == null) {
			haveGUI = false;
		}

		/* Layouts */
		UndoSupport undoSupport = getService(bc, UndoSupport.class);
		// TODO: pass
		CyLayoutAlgorithm forceDirectedLayoutAlgorithm = new ForceDirectedLayout(registrar, undoSupport);
		Properties forceDirectedLayoutAlgorithmProps = new Properties();
		forceDirectedLayoutAlgorithmProps.setProperty("preferredTaskManager","menu");
    forceDirectedLayoutAlgorithmProps.setProperty(TITLE,forceDirectedLayoutAlgorithmProps.toString());
    forceDirectedLayoutAlgorithmProps.setProperty(MENU_GRAVITY,"20.1");
    registerService(bc, forceDirectedLayoutAlgorithm, CyLayoutAlgorithm.class, forceDirectedLayoutAlgorithmProps);
	}
}
