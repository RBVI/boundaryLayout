package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

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
			System.getProperty("user.home") + File.separator + 
			"\\CytoscapeConfiguration\\boundaryLayoutTemplates.json";
	private TemplateManager templateManager;
	
	public TemplateListener(TemplateManager templateManager) {
		this.templateManager = templateManager;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(CyShutdownEvent shutdownEvent) {	
		try {
			File templateFile = new File(boundaryLayoutTemplatesPath);
			System.out.println(templateFile.getAbsolutePath());
			if(templateFile.exists())
				templateFile.delete();
			templateFile.createNewFile();
			FileWriter templateWriter = new FileWriter(templateFile);
			JSONArray templatesInformation = new JSONArray();
			Map<String, List<String>> templateMap = 
					templateManager.getTemplateMap();
			for(String templateName : templateMap.keySet()) {
				JSONObject templateObject = new JSONObject();
				JSONArray annotationsArray = new JSONArray();
				for(String annotationInformation : templateMap.get(templateName))
					annotationsArray.add(annotationInformation);
				templateObject.put("name",templateName);
				templateObject.put("thumbnail", null);
				templateObject.put("annotations", annotationsArray);
				templatesInformation.add(templateObject);
			}
			templateWriter.write(templatesInformation.toJSONString());
			templateWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void handleEvent(CyStartEvent startEvent) {		
	}
}