package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationConfiguration;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class TemplateListener implements CyShutdownListener {
	public static final String boundaryLayoutTemplatesPath = 
			System.getProperty("user.home") + File.separator + 
			"\\CytoscapeConfiguration\\boundaryLayoutTemplates.json";
	private TemplateManager templateManager;
	private CyServiceRegistrar registrar;
	private File templateFile;
	
	public TemplateListener(TemplateManager templateManager, CyServiceRegistrar registrar) {
		this.templateManager = templateManager;
		this.registrar = registrar;
		CyApplicationConfiguration appConfig = registrar.getService(CyApplicationConfiguration.class);
		File configurationDirectory = appConfig.getConfigurationDirectoryLocation();
		templateFile = new File(configurationDirectory.getAbsolutePath() + File.separator + "boundaryLayoutTemplates.json");

		// OK, now load our templates
		try {
			Object json = (new JSONParser()).parse(new FileReader(templateFile));
			if (json instanceof JSONObject) {
				// This is actually an error....
				throw new RuntimeException("JSON template file must be an array");
			} else if (json instanceof JSONArray) {
				for (Object tempObj: (JSONArray)json) {
					if (!(tempObj instanceof JSONObject))
						throw new RuntimeException("JSON template not formatted correctly");

					addTemplate((JSONObject) tempObj);

				}
			}
		} catch (Exception e) {}
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(CyShutdownEvent shutdownEvent) {	
		try {
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

	private void addTemplate(JSONObject template) {
		String templateName = (String)template.get("name");
		String thumbnail = (String)template.get("thumbnail");
		JSONArray annotations = (JSONArray)template.get("annotations");
		List<String> annotationList = new ArrayList<>();
		for (Object ann: annotations) {
			annotationList.add((String)ann);
		}
		templateManager.addTemplateStrings(templateName, annotationList);
	}

}
