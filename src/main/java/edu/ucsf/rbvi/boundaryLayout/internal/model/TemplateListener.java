package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.FileWriter;
import java.io.IOException;

import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.CyStartEvent;
import org.cytoscape.application.events.CyStartListener;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TemplateListener implements CyStartListener, CyShutdownListener {
	public static final String boundaryLayoutTemplatesPath =
			"/Users/<username>/CytoscapeConfiguration/boundaryLayoutTemplates.JSON";
	
	@Override
	public void handleEvent(CyShutdownEvent shutdownEvent) {	
		try {
			FileWriter templateWriter = new FileWriter(boundaryLayoutTemplatesPath);
			templateWriter.write("yay");
			templateWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleEvent(CyStartEvent startEvent) {		
	}
}