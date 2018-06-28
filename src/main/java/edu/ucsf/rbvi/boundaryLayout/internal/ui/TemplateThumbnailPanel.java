package edu.ucsf.rbvi.boundaryLayout.internal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.FlowLayout;
import java.awt.Image;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Scanner;

import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.CyShutdownEvent;
import org.cytoscape.application.events.CyShutdownListener;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;

import edu.ucsf.rbvi.boundaryLayout.internal.model.CurrentNetworkViewTemplateListener;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDeleteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLabelChangeTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSaveShutdownTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSaveTask;

/*
 * This class controls the UI of the Boundaries tab
 */
public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent, TaskObserver {
	public static final String USE_TEMPLATE = "apply";
	public static final String REMOVE_TEMPLATE_FROM_VIEW = "remove";
	public static final String ADD_TEMPLATE = "add to your list of templates";
	public static final String IMPORT_TEMPLATE = "import a template";
	public static final String EXPORT_TEMPLATE = "export a template";
	public static final String DELETE_TEMPLATE = "Remove Template";	
	public static final String RENAME_TEMPLATE = "Rename Template";	
	private final Map<String, String> toolMap;
	private final List<String> buttonList;
	private static final long serialVersionUID = 1L;

	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private TaskManager taskManager;
	private JPanel templatesPanel;
	private Box templatesBox;
	private Map<String, JPanel> templatesMap;
	private Map<String, JButton> thumbnailsMap;
	private JScrollPane scrollPane;
	private Map<String, Object> tasksMap;
	public String currentTemplateName;

	public TemplateThumbnailPanel() {
		this(null, null, null);
	}

	public TemplateThumbnailPanel(CyServiceRegistrar registrar, TemplateManager manager, Map<String, Object> tasks) {
		super();
		this.registrar = registrar;
		this.manager = manager;
		this.tasksMap = tasks;
		this.cyApplicationManager = registrar.getService(CyApplicationManager.class);
		this.taskManager = (TaskManager) registrar.getService(TaskManager.class);
		this.templatesMap = new HashMap<>();
		this.thumbnailsMap = new HashMap<>();
		this.templatesPanel = new JPanel();
		this.templatesBox = Box.createVerticalBox();
		this.currentTemplateName = null;
		this.buttonList = new ArrayList<>();
		this.toolMap = new HashMap<>();
		buttonList.add(IMPORT_TEMPLATE);
		buttonList.add(ADD_TEMPLATE);
		buttonList.add(EXPORT_TEMPLATE);
		toolMap.put(IMPORT_TEMPLATE, IconManager.ICON_DOWNLOAD);
		toolMap.put(EXPORT_TEMPLATE, IconManager.ICON_UPLOAD);
		toolMap.put(ADD_TEMPLATE, IconManager.ICON_PLUS);
		
		CurrentNetworkViewTemplateListener networkViewTemplateListener = new CurrentNetworkViewTemplateListener(this);
		registrar.registerService(networkViewTemplateListener, SetCurrentNetworkViewListener.class, new Properties());

		this.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(templatesPanel);
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

	/*
	 * Renames a template to a new name
	 */
	public boolean renameTemplate(String oldName, String newName) {
		if(!templatesMap.containsKey(oldName) || !thumbnailsMap.containsKey(oldName))
			return false;
		thumbnailsMap.get(oldName).setActionCommand(newName);;
		templatesMap.put(newName, templatesMap.get(oldName));
		templatesMap.remove(oldName);
		if(currentTemplateName == null || currentTemplateName.equals(oldName))
			currentTemplateName = newName;
		if(templatesMap.containsKey(newName) && !templatesMap.containsKey(oldName))
			return true;
		return false;
	}

	/*
	 * Updates the contents of this panel
	 */
	public void updatePanel() {
		templatesPanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatesBox(template);
		templatesPanel.add(templatesBox);
		scrollPane.setViewportView(templatesPanel);
		initLowerPanel();
	}

	/*
	 * Initialize the lower panel of the Boundaries tab, holding the tools available to the user
	 */
	private void initLowerPanel() {
		JPanel lowerPanel = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		lowerPanel.add(buttonPanel, BorderLayout.CENTER);

		if(toolMap != null && !toolMap.isEmpty() && buttonList != null && !buttonList.isEmpty()) 
			for(String toolName : buttonList) 
				initNewButton(buttonPanel, toolName);

		this.add(lowerPanel, BorderLayout.SOUTH);
		lowerPanel.repaint();
	}

	/* Private method
	 * Initializes a new button in the button panel, holding the template
	 */
	private void initNewButton(JPanel buttonPanel, String buttonName) {
		JButton newToolButton = new JButton(toolMap.get(buttonName)); 
		newToolButton.setFont(registrar.getService(IconManager.class).getIconFont(14.0f));
		newToolButton.setActionCommand(buttonName);
		newToolButton.setToolTipText(buttonName);
		newToolButton.addActionListener(new TemplateButtonListener());
		buttonPanel.add(newToolButton);
	}

	/* Private method
	 * Adds a template to the Boundaries tab
	 */
	private void addToTemplatesBox(String templateName) {
		if(templateName != null) {
			Image thumbnail = manager.getThumbnail(templateName);
			JPanel templatePanel = new JPanel(new BorderLayout());
			JButton templateButton = new JButton(new ImageIcon(thumbnail));
			templateButton.setActionCommand(templateName);
			templateButton.addActionListener(new TemplateSelectedListener());
			addPopupMenu(templatePanel, templateButton);
			templatesBox.add(templatePanel);
			templatesMap.put(templateName, templatePanel);
			thumbnailsMap.put(templateName, templateButton);
		}
	}

	/*
	 * Replaces the thumbnail of a given template in the Boundaries tab
	 */
	public void replaceThumbnailTemplate(String templateName) {
		if(templateName == null || !thumbnailsMap.containsKey(templateName) || !templatesMap.containsKey(templateName)) 
			return;
		thumbnailsMap.get(templateName).setIcon(new ImageIcon(manager.getNewThumbnail(templateName)));
		thumbnailsMap.get(templateName).repaint();
		templatesMap.get(templateName).repaint();
	}

	/* Private method
	 * Adds a popup menu to the panel
	 */
	private void addPopupMenu(JPanel templatePanel, JButton templateButton) {
		JPanel labelPanel = new JPanel(new BorderLayout());
		JLabel templateNameLabel = new JLabel(templateButton.getActionCommand());
		templateNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		labelPanel.add(templateNameLabel, BorderLayout.CENTER);
		JPopupMenu templatePopup = new JPopupMenu();
		JMenuItem removeTemplate = new JMenuItem(DELETE_TEMPLATE);
		removeTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(removeTemplate);
		JMenuItem renameTemplate = new JMenuItem(RENAME_TEMPLATE);
		renameTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(renameTemplate);
		templatePanel.addMouseListener(new TemplatePopupListener(templatePopup));
		templateButton.addMouseListener(new TemplatePopupListener(templatePopup));
		templatePanel.add(labelPanel, BorderLayout.SOUTH);
		templatePanel.add(templateButton, BorderLayout.CENTER);
	}

	/* Private method
	 * Remove the given list of templates from the list of templates and user side
	 */
	private void removeFromTemplatesBox(List<String> removeTemplates) {
		if(!removeTemplates.isEmpty())
			for(String template : removeTemplates) {
				templatesBox.remove(templatesMap.get(template));
				templatesMap.remove(template);
			}
	}

	/*
	 * if a template is added, update the panel
	 */
	public void updateTemplateButtonsAdd() {
		for(String template : manager.getTemplateNames()) 
			if(!templatesMap.containsKey(template)) 
				addToTemplatesBox(template);
	}

	/*
	 * if a template is removed, update the panel
	 */
	public void updateTemplateButtonsRemove() {
		Map<String, List<String>> templatesInManager = manager.getTemplateMap();
		List<String> templatesToRemove = new ArrayList<>();
		for(String template : templatesMap.keySet()) 
			if(!templatesInManager.containsKey(template)) 
				templatesToRemove.add(template);
		removeFromTemplatesBox(templatesToRemove);
		setCurrentTemplateName(null);
	}
	
	/*
	 * updates the templates panel if needed
	 */
	public void updateTemplatesPanel() {
		if(templatesMap.size() > manager.getTemplateMap().size())
			updateTemplateButtonsRemove();
		else if(templatesMap.size() < manager.getTemplateMap().size())
			updateTemplateButtonsAdd();
		templatesBox.revalidate();
		this.revalidate();
	}

	/*
	 * Executes the given task, whether its a save or overwrite or applying the template
	 */
	private boolean executeTask(Object taskObject) {
		TaskIterator taskIterator = null;
		if(taskObject instanceof TaskFactory) {
			TaskFactory task = (TaskFactory) taskObject;
			taskIterator = task.createTaskIterator();
			taskManager.execute(taskIterator, this);
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			CyNetworkView networkView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			if(netViewTask != null && netViewTask instanceof TemplateSave) {
				TemplateOverwriteTask overwriteTask = null;
				if(currentTemplateName != null) {
					overwriteTask = new TemplateOverwriteTask(registrar, networkView, manager, currentTemplateName);
					taskManager.execute(new TaskIterator(overwriteTask), this);
				} else {
					TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, manager);
					taskManager.execute(new TaskIterator(saveTask), this);
				}
			}
		}
		if(taskObject != null) 
			return true;
		return false;
	}

	/*
	 * After the task is handled, updates the panel
	 */
	@Override
	public void allFinished(FinishStatus status) {
		updateTemplatesPanel();
		repaintPanel();
	}

	/*
	 * Handles saves and overwrites to templates
	 * 
	 * @param task is the task being performed in response to the user
	 */
	@Override
	public void taskFinished(ObservableTask task) {
		if (task instanceof TemplateOverwriteTask) {
			TemplateOverwriteTask overwriteTask = (TemplateOverwriteTask) task;
			if(overwriteTask.getButtonState() == TemplateOverwriteTask.OVERWRITE_STATE) 
				replaceThumbnailTemplate(currentTemplateName);
		} else if (task instanceof TemplateSaveTask) {
			TemplateSaveTask saveTask = (TemplateSaveTask) task;
			setCurrentTemplateName(saveTask.templateName);
		}
	}

	/*
	 * Button, Mouse, and Action Listeners for Swing features
	 */
	private class TemplateButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent buttonPressed){
			String taskName = buttonPressed.getActionCommand();	
			Object taskObject = tasksMap.get(taskName);

			executeTask(taskObject);//first handle underlying code
		}
	}

	/*
	 * Listener for when the user presses on a template
	 */
	private class TemplateSelectedListener implements ActionListener {
		public void actionPerformed(ActionEvent templateChosen) {
			CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
			if (view != null) {
				manager.clearNetworkofTemplates(view);
				boolean isTemplateApplied = manager.useTemplate(templateChosen.getActionCommand(), view);
				if(isTemplateApplied) 
					setCurrentTemplateName(templateChosen.getActionCommand());
			}
		}
	}

	/*
	 * Listener for mouse clicks on the Boundaries tab
	 */
	private class TemplatePopupListener implements MouseListener {
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

		@Override
		public void mouseEntered(MouseEvent e) {}

		@Override
		public void mouseExited(MouseEvent e) {}

		private void checkPopup(MouseEvent e) {
			if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) 
				popupMenu.show((Component) e.getSource(), e.getX(), e.getY());
		}
	}

	/*
	 * Listener for deleting a template and changing the label name
	 */
	private class TemplateMenuItemListener implements ActionListener {
		private JLabel templateLabel;

		public TemplateMenuItemListener(JLabel templateLabel) {
			super();
			this.templateLabel = templateLabel;
		}

		public void actionPerformed(ActionEvent menuItemEvent) {            
			TaskIterator iterator = null;
			if(menuItemEvent.getActionCommand().equals(DELETE_TEMPLATE)) {
				iterator = new TaskIterator(new TemplateDeleteTask(manager, TemplateThumbnailPanel.this, templateLabel.getText()));
			} else {
				iterator = new TaskIterator(new TemplateLabelChangeTask(manager, TemplateThumbnailPanel.this, templateLabel));
			}
			if(iterator != null)
				taskManager.execute(iterator);
			updateTemplatesPanel();
		}    
	}   
}