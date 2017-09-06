package edu.ucsf.rbvi.boundaryLayout.internal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;
import javax.swing.JToolBar;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.application.CyApplicationManager;

import edu.ucsf.rbvi.boundaryLayout.internal.CyActivator;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwrite;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent, ActionListener {
	private static final long serialVersionUID = 1L;
	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private TaskManager taskManager;
	private ImageIcon templateIcon;
	private JPanel templatePanel;
	private Map<String, JButton> templatesMap;
	private JPanel buttonTasksPanel;
	private JScrollPane scrollPane;
	private List<JButton> buttonTasks;
	private Map<String, Object> tasksMap;
	
	public TemplateThumbnailPanel() {
		this(null, null, null);
	}

	@SuppressWarnings("rawtypes")
	public TemplateThumbnailPanel(CyServiceRegistrar registrar, 
			TemplateManager manager, Map<String, Object> tasks) {
		super();
		this.registrar = registrar;
		this.manager = manager;
		this.tasksMap = tasks;
		this.cyApplicationManager = registrar.getService(CyApplicationManager.class);
		this.taskManager = (TaskManager) registrar.getService(TaskManager.class);
		this.templatesMap = new HashMap<>();
		
		// Consider adding a button bar to import/export/add templates

		// This will contain all of our template buttons
		initButtonTasks(); //gets a list of buttons for each task
		templatePanel = new JPanel();

		scrollPane = new JScrollPane(templatePanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.SOUTH);
		this.add(buttonTasksPanel, BorderLayout.NORTH);

		// Draw our panel
		updatePanel();
	}

	public void updatePanel() {
		templatePanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatePanel(template);
		scrollPane.setViewportView(templatePanel);
	}
	
	private void addToTemplatePanel(String template) {
		Image thumbnail = manager.getThumbnail(template);
		JButton templateButton = new JButton(template, new ImageIcon(thumbnail));
		templateButton.setActionCommand(template);
		templateButton.addActionListener(this);
		templatePanel.add(Box.createRigidArea(new Dimension(0,1)));
		templatePanel.add(templateButton);
		templatesMap.put(template, templateButton);
	}
	
	private void removeFromTemplatePanel(String template) {
		templatePanel.remove(templatesMap.get(template));
		templatesMap.remove(template);
	}
	
	public void updateTemplateButtonsAdd() {
		for(String template : manager.getTemplateNames()) {
			if(!templatesMap.containsKey(template)) {
				addToTemplatePanel(template);
			}
		}
	}
	
	public void updateTemplateButtonsRemove() {
		Map<String, List<String>> templatesInManager = manager.getTemplateMap();
		for(String template : templatesMap.keySet()) {
			if(!templatesInManager.containsKey(template)) {
				removeFromTemplatePanel(template);
			}
		}
	}
	
	public void updateTemplateButtonsOverwrite(String templateName) {
		removeFromTemplatePanel(templateName);
		addToTemplatePanel(templateName);
	}
	
	private void initButtonTasks() {
		buttonTasks = new ArrayList<>();
		for(String taskName : tasksMap.keySet()) {
			JButton taskButton = new JButton(taskName);
			taskButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent buttonPressed) {
					try {
						processTask(buttonPressed);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			buttonTasks.add(taskButton);
		}
		createToolBar();
	}
	
	private void createToolBar() {
		buttonTasksPanel = new JPanel();
		JToolBar toolBar = new JToolBar("Tools"); 
		toolBar.setBackground(Color.GRAY);
		for(JButton taskButton : buttonTasks) {
			taskButton.setBackground(Color.WHITE);
			toolBar.add(taskButton);
		}
		buttonTasksPanel.add(toolBar);
	}

	protected void processTask(ActionEvent buttonPressed) throws Exception {
		String taskName = buttonPressed.getActionCommand();
		Object taskObject = tasksMap.get(taskName);
		TaskIterator taskIterator = null;
		if(taskObject instanceof TaskFactory) {
			TaskFactory task = (TaskFactory) taskObject;
			taskIterator = task.createTaskIterator();
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			taskIterator = netViewTask.createTaskIterator(registrar.getService(
					CyApplicationManager.class).getCurrentNetworkView());
		}
		taskManager.execute(taskIterator);
		if(taskName.equals(CyActivator.SAVE_TEMPLATE) || 
				taskName.equals(CyActivator.IMPORT_TEMPLATE))
			updateTemplateButtonsAdd();
		else if(taskName.equals(CyActivator.DELETE_TEMPLATE)) 
			updateTemplateButtonsRemove();
		else if(taskName.equals(CyActivator.OVERWRITE_TEMPLATE)) {
			TemplateOverwriteTask overwriteTask = (TemplateOverwriteTask) taskIterator.next();
			updateTemplateButtonsOverwrite(overwriteTask.templateNames.getSelectedValue());
		}
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Boundaries";
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String template = e.getActionCommand();
		// Get the current network
		CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
		// Add the template
		manager.useTemplate(template, view);
	}
}
