package edu.ucsf.rbvi.boundaryLayout.internal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneConstants;
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
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;

import edu.ucsf.rbvi.boundaryLayout.internal.CyActivator;
import edu.ucsf.rbvi.boundaryLayout.internal.model.CurrentNetworkViewTemplateListener;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDelete;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLabelChangeTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwrite;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSaveTask;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent {
	public static final String USE_TEMPLATE = "Apply";
	public static final String REMOVE_TEMPLATE_FROM_VIEW = "Remove Template from View";
	public static final String ADD_TEMPLATE = "Add";
	public static final String IMPORT_TEMPLATE = "Import";
	public static final String EXPORT_TEMPLATE = "Export";
	public static final String DELETE_TEMPLATE = "Delete";	
	private static final long serialVersionUID = 1L;

	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private TaskManager taskManager;
	private ImageIcon templateIcon;
	private JPanel templatePanel;
	private Box templatesBox;
	private Map<String, JButton> templatesMap;
	private JScrollPane scrollPane;
	private Map<String, Object> tasksMap;
	private String currentTemplateName;

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
		this.templatePanel = new JPanel();
		this.templatesBox = Box.createVerticalBox();
		this.currentTemplateName = null;
		CurrentNetworkViewTemplateListener networkViewTemplateListener = 
				new CurrentNetworkViewTemplateListener(this);
		registrar.registerService(networkViewTemplateListener, SetCurrentNetworkViewListener.class, new Properties());

		this.setLayout(new BorderLayout());

		scrollPane = new JScrollPane(templatePanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.add(scrollPane, BorderLayout.CENTER);

		updatePanel();
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

	public void repaintPanel() {
		this.repaint();
	}

	public void setCurrentTemplateName(String templateName) {
		this.currentTemplateName = templateName;
	}
	
	public void updatePanel() {
		templatePanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatesBox(template);
		templatePanel.add(templatesBox);
		scrollPane.setViewportView(templatePanel);
		initLowerPanel();
	}

	private void initLowerPanel() {
		JPanel lowerPanel = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		lowerPanel.add(buttonPanel, BorderLayout.CENTER);

		JButton importButton = new JButton(IMPORT_TEMPLATE, new ImageIcon());//ADD IMPORT ICON
		JButton exportButton = new JButton(EXPORT_TEMPLATE, new ImageIcon());//ADD EXPORT ICON
		JButton addButton = new JButton(ADD_TEMPLATE, new ImageIcon());//ADD "ADD" ICON
		importButton.setActionCommand(IMPORT_TEMPLATE);
		exportButton.setActionCommand(EXPORT_TEMPLATE);
		addButton.setActionCommand(ADD_TEMPLATE);
		importButton.addActionListener(new TemplateButtonListener());
		exportButton.addActionListener(new TemplateButtonListener());
		addButton.addActionListener(new TemplateButtonListener());	
		
		buttonPanel.add(importButton);
		buttonPanel.add(exportButton);
		buttonPanel.add(addButton);
		this.add(lowerPanel, BorderLayout.SOUTH);
		lowerPanel.repaint();
	}

	private void addToTemplatesBox(String templateName) {
		Image thumbnail = manager.getThumbnail(templateName);
		JButton templateButton = new JButton(new ImageIcon(thumbnail));
		templateButton.setActionCommand(templateName);
		templateButton.addActionListener(new TemplateSelectedListener(templateName));
		templatesBox.add(templateButton);
		templatesMap.put(templateName, templateButton);
	}

	private void removeFromTemplatesBox(String template) {
		templatesBox.remove(templatesMap.get(template));
		templatesMap.remove(template);
	}

	public void updateTemplateButtonsAdd() {
		for(String template : manager.getTemplateNames()) {
			if(!templatesMap.containsKey(template)) {
				addToTemplatesBox(template);
			}
		}
	}

	public void updateTemplateButtonsRemove() {
		Map<String, List<String>> templatesInManager = manager.getTemplateMap();
		for(String template : templatesMap.keySet()) {
			if(!templatesInManager.containsKey(template)) {
				removeFromTemplatesBox(template);
			}
		}
	}

	public void updateTemplateButtonsOverwrite(String templateName) {
		removeFromTemplatesBox(templateName);
		addToTemplatesBox(templateName);
	}

	/*Button, Mouse, and Action Listeners for Swing features*/
	private class TemplateButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent buttonPressed){
			String taskName = buttonPressed.getActionCommand();	
			Object taskObject = tasksMap.get(taskName);

			executeTask(taskObject);//first handle underlying code

			if(taskName.equals(ADD_TEMPLATE) || //then worry about UI
					taskName.equals(IMPORT_TEMPLATE))
				updateTemplateButtonsAdd();
			repaintPanel();
		}
	}

	private boolean executeTask(Object taskObject) {
		TaskIterator taskIterator = null;
		if(taskObject instanceof TaskFactory) {
			TaskFactory task = (TaskFactory) taskObject;
			taskIterator = task.createTaskIterator();
			taskManager.execute(taskIterator);
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			CyNetworkView networkView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			if(netViewTask != null && netViewTask instanceof TemplateSave && currentTemplateName != null) {
				TemplateOverwriteTask overwriteTask = new TemplateOverwriteTask(registrar, networkView, 
						manager, currentTemplateName);
				taskManager.execute(new TaskIterator(overwriteTask));
				System.out.println(overwriteTask.overwrite);
				if(!overwriteTask.overwrite) {
					TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, manager);
					taskManager.execute(new TaskIterator(saveTask));
					setCurrentTemplateName(saveTask.templateName);
				}
			}
			else {
				taskIterator = netViewTask.createTaskIterator(networkView);
				taskManager.execute(taskIterator);
			}
		}
		if(taskObject != null) 
			return true;
		return false;
	}

	private class TemplateSelectedListener implements ActionListener {
		private String templateName;

		public TemplateSelectedListener(String templateName) {
			super();
			this.templateName = templateName;
		}

		public void actionPerformed(ActionEvent templateChosen) {
			CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
			manager.clearNetworkofTemplates(view);
			boolean isTemplateApplied = manager.useTemplate(templateName, view);
			if(isTemplateApplied) 
				setCurrentTemplateName(templateName);
		}
	}

	private class TemplatePopupListener extends MouseAdapter {
		private JPopupMenu popupMenu;

		public TemplatePopupListener(JPopupMenu popupMenu) {
			super();
			this.popupMenu = popupMenu;
		}

		@Override
		public void mousePressed(MouseEvent e) {
			checkPopup(e);
		}

		@Override
		public void mouseClicked(MouseEvent e) {
			checkPopup(e);
		}
		
		@Override
		public void mouseReleased(MouseEvent e) {
			checkPopup(e);
		}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger()) {
				popupMenu.show(TemplateThumbnailPanel.this, e.getX(), e.getY());
			}
		}
	}

	private class TemplateMenuItemListener implements ActionListener {
		private JLabel templateLabel;
		
		public TemplateMenuItemListener(JLabel templateLabel) {
			super();
			this.templateLabel = templateLabel;
		}

		public void actionPerformed(ActionEvent menuItemEvent) {            
			String taskName = menuItemEvent.getActionCommand();
			Object taskObject = tasksMap.get(taskName);
			boolean taskExecuted = executeTask(taskObject);
			if(!taskExecuted)
				taskManager.execute(new TaskIterator(new 
						TemplateLabelChangeTask(templateLabel)));
		}    
	}   
}