package edu.ucsf.rbvi.boundaryLayout.internal.model;

import java.io.BufferedReader;
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
	public static String boundaryLayoutTemplatesPath;
	private TemplateManager templateManager;
	private CyServiceRegistrar registrar;
	private File templateFile;
	private static final String NAME = "name";
	private static final String THUMBNAIL = "thumbnail";
	private static final String ANNOTATIONS = "annotations";

	public TemplateListener(TemplateManager templateManager, CyServiceRegistrar registrar) {
		this.templateManager = templateManager;
		this.registrar = registrar;
		CyApplicationConfiguration appConfig = registrar.getService(CyApplicationConfiguration.class);
		File configurationDirectory = appConfig.getConfigurationDirectoryLocation();
		boundaryLayoutTemplatesPath = configurationDirectory.getAbsolutePath() + 
				File.separator + "boundaryLayoutTemplates.json";
		templateFile = new File(boundaryLayoutTemplatesPath);
		System.out.print("template listener constructor");

		//load our templates
		if(templateFile.exists()) {
			try {
				BufferedReader templateReader = new BufferedReader(new FileReader(templateFile));
				String template;
				while((template = templateReader.readLine()) != null)  {
					Object jsonTemplateObj = new JSONParser().parse(template);
					if(jsonTemplateObj instanceof JSONObject) 
						addTemplate((JSONObject) jsonTemplateObj);
					else if(jsonTemplateObj instanceof JSONArray) 
						throw new RuntimeException("Not supposed to be an array!");
				}
			} catch (Exception e) {}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(CyShutdownEvent shutdownEvent) {	
		try {
			if(templateFile.exists())
				templateFile.delete();
			templateFile.createNewFile();
			FileWriter templateWriter = new FileWriter(templateFile);
			JSONArray templatesInformation = new JSONArray();
			Map<String, List<String>> templateMap = 
					templateManager.getTemplateMap();
			for(String templateName : templateMap.keySet()) {
				System.out.println(templateName);
				JSONObject templateObject = new JSONObject();
				JSONArray annotationsArray = new JSONArray();
				for(String annotationInformation : templateMap.get(templateName))
					annotationsArray.add(annotationInformation);
				templateObject.put("name", templateName);
				templateObject.put("thumbnail", null);
				templateObject.put("annotations", annotationsArray);
				templatesInformation.add(templateObject);
			}
			printJSON(templateWriter, templatesInformation);
			System.out.println(templatesInformation.toJSONString());
			templateWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void printJSON(FileWriter templateWriter, JSONArray templatesInformation) throws IOException {
		int newLineCounter = 0;
		for(Object templateObject : templatesInformation) {
			if(newLineCounter++ != 0)
				templateWriter.write("\n");
			JSONObject template = (JSONObject) templateObject;
			String templateOutput = template.toJSONString();
			templateWriter.write(templateOutput);
		}
	}

	private void addTemplate(JSONObject template) {
		String templateName = (String) template.get(NAME);
		String thumbnail = (String) template.get(THUMBNAIL);
		JSONArray annotations = (JSONArray) template.get(ANNOTATIONS);
		List<String> annotationList = new ArrayList<>();
		for (Object ann : annotations) {
			annotationList.add((String) ann);
		}
		templateManager.addTemplateStrings(templateName, annotationList);
	}
}