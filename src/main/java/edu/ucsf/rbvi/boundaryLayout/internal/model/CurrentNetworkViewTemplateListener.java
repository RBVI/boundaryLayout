package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.application.events.SetCurrentNetworkViewEvent;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;

import edu.ucsf.rbvi.boundaryLayout.internal.ui.TemplateThumbnailPanel;

public class CurrentNetworkViewTemplateListener implements SetCurrentNetworkViewListener {
	private TemplateThumbnailPanel thumbnailPanel;
	
	public CurrentNetworkViewTemplateListener(TemplateThumbnailPanel thumbnailPanel) {
		super();
		this.thumbnailPanel = thumbnailPanel;
	}
	
	@Override
	public void handleEvent(SetCurrentNetworkViewEvent newNetworkView) {		
		CyNetworkView networkView = newNetworkView.getNetworkView();
		CyTable networkTable = networkView.getModel().getDefaultNetworkTable();
		if(!TemplateManager.columnAlreadyExists(networkTable, TemplateManager.NETWORK_TEMPLATES))
			networkTable.createListColumn(TemplateManager.NETWORK_TEMPLATES, String.class, false);
		List<String> templatesActive = networkTable.getRow(networkView.getSUID()).
				getList(TemplateManager.NETWORK_TEMPLATES, String.class);
		if(templatesActive == null) 
			templatesActive = new ArrayList<>();
		if(!templatesActive.isEmpty()) 
			thumbnailPanel.setCurrentTemplateName(templatesActive.get(0));
		else
			thumbnailPanel.setCurrentTemplateName(null);
	}
}