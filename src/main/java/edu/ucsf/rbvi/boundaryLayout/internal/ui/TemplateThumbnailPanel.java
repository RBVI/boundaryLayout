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

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDeleteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLabelChangeTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSaveTask;

/**
 * This class controls the UI of the Boundaries tab. This corresponds to Template Mode 
 * functionality provided by Boundary Layout
 */
public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent, TaskObserver {
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
	private TemplateManager manager;
	private CyApplicationManager cyApplicationManager;
	private TaskManager taskManager;
	private JPanel templatesPanel;
	private Box templatesBox;
	private Map<String, JPanel> templatesMap;
	private Map<String, JButton> thumbnailsMap;
	private JScrollPane scrollPane;
	private Map<String, Object> tasksMap;

	/**
	 * Construct a TemplateThumbnailPanel, which is a functionality provided by Boundary Layout in the context of a
	 * Template mode. This mode allows the user to save, delete, import, export, apply, and manage a collection of templates, 
	 * where a template is simply a collection of boundaries. This component of cytoscape is under the westward tab menu, labeled
	 * Boundaries. By making templates a convenient resource for the user, Boundary Layout becomes more readily usable by the user.
	 * @param registrar provides many of the services used by this component, including managers and factories
	 * @param manager is the template manager, which this works synonymously with to manage templates
	 * @param tasks is a mapping of the features provided by this panel
	 */
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
		this.buttonList = new ArrayList<>();
		this.toolMap = new HashMap<>();
		buttonList.add(IMPORT_TEMPLATE);
		buttonList.add(ADD_TEMPLATE);
		buttonList.add(EXPORT_TEMPLATE);
		toolMap.put(IMPORT_TEMPLATE, IconManager.ICON_DOWNLOAD);
		toolMap.put(EXPORT_TEMPLATE, IconManager.ICON_UPLOAD);
		toolMap.put(ADD_TEMPLATE, IconManager.ICON_PLUS);
		
		this.setLayout(new BorderLayout());
		scrollPane = new JScrollPane(templatesPanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.add(scrollPane, BorderLayout.CENTER);
		updatePanel();
	}

	/**
	 * Get this component for cytoscape to handle adding this Boundaries tab
	 * @return this, representing the Tempate Mode's Boundaries tab
	 */
	@Override
	public Component getComponent() {
		return this;
	}

	/**
	 * Get the panel in which the Boundaries tab will be displayed under. This can
	 * be toggled on and off in the App menu
	 * @return WEST, where the tab will appear in cytoscape
	 */
	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	/**
	 * Get the icon of this Template Mode tab
	 * @return null since there is no icon associated with this tab
	 */
	@Override
	public Icon getIcon() {
		return null;
	}

	/**
	 * Get the title of this Template Mode tab: "Boundaries"
	 * @return the title of this cytoscape tab
	 */
	@Override
	public String getTitle() {
		return "Boundaries";
	}

	/** Private method
	 * repaint this panel
	 */
	private void repaintPanel() {
		this.repaint();
	}

	/** 
	 * Renames a template to a new name
	 * @param oldName is the current name of the template defined by the user
	 * @param newName is the new name of the template which the user has renamed
	 * @return true if the template has been renamed
	 */
	public boolean renameTemplate(String oldName, String newName) {
		if(!templatesMap.containsKey(oldName) || !thumbnailsMap.containsKey(oldName))
			return false;
		thumbnailsMap.get(oldName).setActionCommand(newName);
		thumbnailsMap.put(newName, thumbnailsMap.get(oldName));
		thumbnailsMap.remove(oldName);
		templatesMap.put(newName, templatesMap.get(oldName));
		templatesMap.remove(oldName);
		if(templatesMap.containsKey(newName) && !templatesMap.containsKey(oldName))
			return true;
		return false;
	}

	/** Private method
	 * Updates the contents of this panel
	 */
	private void updatePanel() {
		templatesPanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatesBox(template);
		templatesPanel.add(templatesBox);
		scrollPane.setViewportView(templatesPanel);
		initLowerPanel();
	}

	/** Private method
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

	/** Private method
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

	/** Private method
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

	/**
	 * Given a template, this method updates the thumbnail of that template in the Boundaries tab. 
	 * This occurs in the case of a template overwrite.
	 * @param templateName is the name of the specified template defined by the user
	 */
	public void replaceThumbnailTemplate(String templateName) {
		if(templateName == null || !thumbnailsMap.containsKey(templateName) || !templatesMap.containsKey(templateName)) 
			return;
		ImageIcon icon = (ImageIcon) thumbnailsMap.get(templateName).getIcon();
		icon.setImage(manager.getThumbnail(templateName));
		thumbnailsMap.get(templateName).repaint();
		templatesMap.get(templateName).repaint();
	}

	/** Private method
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

	/** Private method
	 * Remove the given list of templates from the list of templates and user side
	 */
	private void removeFromTemplatesBox(List<String> removeTemplates) {
		if(!removeTemplates.isEmpty())
			for(String template : removeTemplates) {
				templatesBox.remove(templatesMap.get(template));
				templatesMap.remove(template);
			}
	}

	/** Private method
	 * if a template is added, update the panel
	 */
	private void updateTemplateButtonsAdd() {
		for(String template : manager.getTemplateNames()) 
			if(!templatesMap.containsKey(template)) 
				addToTemplatesBox(template);
	}

	/** Private method
	 * if a template is removed, update the panel
	 */
	private void updateTemplateButtonsRemove() {
		Map<String, List<String>> templatesInManager = manager.getTemplateMap();
		List<String> templatesToRemove = new ArrayList<>();
		for(String template : templatesMap.keySet()) 
			if(!templatesInManager.containsKey(template)) 
				templatesToRemove.add(template);
		removeFromTemplatesBox(templatesToRemove);
	}
	
	/**
	 * This method updates the templates panel if needed. If a template has been added, we add and 
	 * update that template in the list of templates visible in the Boundaries tab. If a template has
	 * been destroyed, we remove that template from the Boundaries. Otherwise, if a template is currently 
	 * active we update the thumbnail of that template (in the case of an overwrite).
	 */
	public void updateTemplatesPanel() {
		if(templatesMap.size() > manager.getTemplateMap().size())
			updateTemplateButtonsRemove();
		else if(templatesMap.size() < manager.getTemplateMap().size())
			updateTemplateButtonsAdd();
		else if(manager.getCurrentActiveTemplate() != null)
			replaceThumbnailTemplate(manager.getCurrentActiveTemplate());
		templatesBox.revalidate();
		this.revalidate();
	}
	
	/**
	 * Executes the given task, whether its a save template, overwrite current template, or applying the template
	 * to the current network view. 
	 * @param taskObject is the generalzied task that is being run
	 * @return true if the task was executed properly
	 */
	private boolean executeTask(Object taskObject) {
		TaskIterator taskIterator = null;
		if(taskObject instanceof TaskFactory) {
			TaskFactory task = (TaskFactory) taskObject;
			taskIterator = task.createTaskIterator();
			taskManager.execute(taskIterator, this);
		} else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			CyNetworkView networkView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			if(netViewTask != null && netViewTask instanceof TemplateSave) {
				TemplateOverwriteTask overwriteTask = null;
				if(manager.getCurrentActiveTemplate() != null) {
					overwriteTask = new TemplateOverwriteTask(registrar, networkView, manager, manager.getCurrentActiveTemplate());
					taskManager.execute(new TaskIterator(overwriteTask), this);
				} else {
					TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, manager);
					taskManager.execute(new TaskIterator(saveTask), this);
				}
			}
		} else return false;
		return true;
	}

	/**
	 * After the task is handled, updates the panel
	 * @param status is the status of the task
	 */
	@Override
	public void allFinished(FinishStatus status) {
		updateTemplatesPanel();
		repaintPanel();
	}

	/**
	 * Handles saves and overwrites to templates. This method updates the Boundaries tab
	 * when the user has overwritten a template
	 * @param task is the task being performed in response to the user
	 */
	@Override
	public void taskFinished(ObservableTask task) {
		if (task instanceof TemplateOverwriteTask) {
			TemplateOverwriteTask overwriteTask = (TemplateOverwriteTask) task;
			if(overwriteTask.getButtonState() == TemplateOverwriteTask.OVERWRITE_STATE) 
				replaceThumbnailTemplate(manager.getCurrentActiveTemplate());
		} 
	}

	/**
	 * Button, Mouse, and Action Listeners for Swing features. This includes the three buttons
	 * on the bottom of the Boundaries tab: import a template from a file, add a template or 
	 * overwrite the current template, and export a template to a file
	 */
	private class TemplateButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent buttonPressed){
			String taskName = buttonPressed.getActionCommand();	
			Object taskObject = tasksMap.get(taskName);

			executeTask(taskObject);
		}
	}

	/**
	 * Listener for when the user presses on a template. This should remove any
	 * template which is currently in the network view in which the user is looking at
	 * and apply the template in which they chose
	 */
	private class TemplateSelectedListener implements ActionListener {
		public void actionPerformed(ActionEvent templateChosen) {
			CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
			if (view != null) {
				manager.removeTemplate(view);
				manager.useTemplate(templateChosen.getActionCommand(), view);
			}
		}
	}

	/**
	 * Listener for mouse clicks on the Boundaries tab. When this fires, a popupmenu
	 * should appear to the user. This menu has the choice of renaming a certain template 
	 * or deleting the template from their list of templates
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

	/**
	 * Listener for deleting a template and renaming the template 
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
				taskManager.execute(iterator);
				updateTemplatesPanel();
			} else {
				iterator = new TaskIterator(new TemplateLabelChangeTask(manager, TemplateThumbnailPanel.this, templateLabel));
				taskManager.execute(iterator);
			}
		}    
	}   
}