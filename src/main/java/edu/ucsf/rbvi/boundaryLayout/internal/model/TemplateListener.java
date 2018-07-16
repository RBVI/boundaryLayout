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
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.service.util.CyServiceRegistrar;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class TemplateListener implements CyShutdownListener {
	public static String boundaryLayoutTemplatesPath;
	private TemplateManager templateManager;
	private File templateFile;
	private static final String NAME = "name";
	private static final String THUMBNAIL = "thumbnail";
	private static final String ANNOTATIONS = "annotations";
	String init = "{edgeThickness=1.0, canvas=background, fillOpacity=14.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=df5f67a0-1bfd-493d-8f64-f814d37ef5be, fillColor=-52429, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=Lysosome, x=2498.7094363723795, width=95.45130508157301, y=1314.176601020426, z=0, height=86.49958877167413}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=50.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=a7deefb7-4ca0-4fd7-91a7-61405d8f59c7, fillColor=-3355393, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=nucleus, x=3930.49153219153, width=211.5359999275279, y=1121.8476627760624, z=5, height=154.9257269564735}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=16.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=a855ef21-3d7a-49d3-9980-bdfd4e5db64d, fillColor=-6750004, shapeType=ROUNDEDRECTANGLE, edgeColor=-16777216, edgeOpacity=100.0, name=golgi, x=1948.4349741732283, width=160.0220366804211, y=571.5732005769113, z=7, height=71.13626185659939}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=26.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=2ad8c8fa-0b64-4ec4-847f-f4266f870834, fillColor=-13159, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=mitochondrion, x=2600.216376001349, width=190.18918233873916, y=-112.26302429193676, z=3, height=106.25416327947752}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=11.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=7beac7b9-ee57-49c1-814f-b6983a37a5c6, fillColor=-16711732, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=Peroxisome, x=3887.751768137227, width=86.66518144074628, y=-176.37267037339126, z=6, height=82.37482592387764}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=12.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=fd270129-eb5e-4d21-8fd6-1a347ae3adb0, fillColor=-39271, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=Endosome, x=4587.615404526438, width=93.05993443187481, y=160.20297155424487, z=2, height=86.07875928185177}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=100.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=f3843916-d405-4f92-9b4d-febd5f36e22b, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=plasma membrane, x=1382.1331004537135, width=861.7350341305499, y=-598.4278404096334, z=4, height=620.0288660207617}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=27.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=36987975-1a1b-4662-957c-a548453ac1c4, fillColor=-3342388, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=endoplasmic reticulum, x=3658.0255363453484, width=315.1638062338759, y=934.8611950384868, z=8, height=223.84511770331312}\r\n" + 
			"{edgeThickness=0.0, canvas=foreground, fillOpacity=100.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=a0cfeb88-34c5-4881-859c-593c961efcc8, shapeType=RECTANGLE, edgeColor=-16777216, edgeOpacity=100.0, name=ShapeAnnotation_111, x=3.775709702441549, width=18.71793270111084, y=-5.413614156179258, z=0, height=18.71793270111084}\r\n" + 
			"{edgeThickness=1.0, canvas=background, fillOpacity=13.0, zoom=0.18717932064003912, type=org.cytoscape.view.presentation.annotations.ShapeAnnotation, uuid=7d0da099-4d26-4963-a806-1761b5cc91e2, fillColor=-3342337, shapeType=ELLIPSE, edgeColor=-16777216, edgeOpacity=100.0, name=cytoplasm, x=1713.3662718745618, width=746.4805199888715, y=-379.3865496313305, z=1, height=532.5637831821884}\r";
	private CyServiceRegistrar registrar;

	/*
	 * Constructs a Template Listener as well as loads the templates from the json file
	 */
	public TemplateListener(TemplateManager templateManager, CyServiceRegistrar registrar) {
		this.templateManager = templateManager;
		this.registrar = registrar;
		CyApplicationConfiguration appConfig = registrar.getService(CyApplicationConfiguration.class);
		File configurationDirectory = appConfig.getConfigurationDirectoryLocation();
		boundaryLayoutTemplatesPath = configurationDirectory.getAbsolutePath() + File.separator + "boundaryLayoutTemplates.json";
		templateFile = new File(boundaryLayoutTemplatesPath);

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
				templateReader.close();
			} catch (Exception e) {}
		}
	}

	/*
	 * Saves the current templates to the json file where template information is being
	 * stored. Each template has a list of annotation information and everything 
	 * is stored in order.
	 * 
	 * @param shutdownEvent occurs when the user is about to shut down the session
	 */
	@Override
	public void handleEvent(CyShutdownEvent shutdownEvent) {	
		try {
			if(templateFile.exists())
				templateFile.delete();
			templateFile.createNewFile();
			FileWriter templateWriter = new FileWriter(templateFile);
			JSONArray templatesInformation = new JSONArray();
			Map<String, List<String>> templateMap = templateManager.getTemplateMap();
			for(String templateName : templateMap.keySet()) {
				JSONObject templateObject = new JSONObject();
				JSONArray annotationsArray = new JSONArray();
				for(String annotationInformation : templateMap.get(templateName))
					annotationsArray.add(annotationInformation);
				templateObject.put("name", templateName);
				templateObject.put("thumbnail", templateManager.getEncodedThumbnail(templateName));
				templateObject.put("annotations", annotationsArray);
				templatesInformation.add(templateObject);
			}
			printJSON(templateWriter, templatesInformation);
			templateWriter.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/* Private method
	 * Prints the information of the template into the given file
	 */
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

	/* Private method
	 * Adds a template to the template manager
	 */
	private void addTemplate(JSONObject template) {
		String templateName = (String) template.get(NAME);
		String thumbnail = (String) template.get(THUMBNAIL);
		JSONArray annotations = (JSONArray) template.get(ANNOTATIONS);
		List<String> annotationList = new ArrayList<>();
		for (Object ann : annotations) 
			annotationList.add((String) ann);
		templateManager.addTemplateStrings(templateName, annotationList);
		templateManager.addThumbnail(templateName, thumbnail);
	}
}
