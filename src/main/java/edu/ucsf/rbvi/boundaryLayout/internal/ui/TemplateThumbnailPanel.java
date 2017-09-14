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
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.util.swing.IconManager;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkViewListener;

import edu.ucsf.rbvi.boundaryLayout.internal.model.CurrentNetworkViewTemplateListener;
import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDeleteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLabelChangeTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSaveTask;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent {
	public static final String USE_TEMPLATE = "apply";
	public static final String REMOVE_TEMPLATE_FROM_VIEW = "remove";
	public static final String ADD_TEMPLATE = "add to your list of templates";
	public static final String IMPORT_TEMPLATE = "import a template";
	public static final String EXPORT_TEMPLATE = "export a template";
	public static final String DELETE_TEMPLATE = "Remove Template";	
	private final Map<String, String> toolMap;
	private static final long serialVersionUID = 1L;

	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private TaskManager taskManager;
	private JPanel templatesPanel;
	private Box templatesBox;
	private Map<String, JPanel> templatesMap;
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
		this.templatesPanel = new JPanel();
		this.templatesBox = Box.createVerticalBox();
		this.currentTemplateName = null;
		this.toolMap = new HashMap<>();
		toolMap.put(IconManager.ICON_DOWNLOAD, IMPORT_TEMPLATE);
		toolMap.put(IconManager.ICON_UPLOAD, EXPORT_TEMPLATE);
		toolMap.put(IconManager.ICON_PLUS, ADD_TEMPLATE);

		CurrentNetworkViewTemplateListener networkViewTemplateListener = 
				new CurrentNetworkViewTemplateListener(this);
		registrar.registerService(networkViewTemplateListener, SetCurrentNetworkViewListener.class, new Properties());

		this.setLayout(new BorderLayout());

		scrollPane = new JScrollPane(templatesPanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.add(scrollPane, BorderLayout.CENTER);

		System.out.println("init!");
		
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

	public boolean renameTemplate(String oldName, String newName) {
		if(!templatesMap.containsKey(oldName))
			return false;
		for(Component component : templatesMap.get(oldName).getComponents()) {
			if(component instanceof JButton) {
				JButton templateButton = (JButton) component;
				templateButton.setActionCommand(newName);
			}
		}
		templatesMap.put(newName, templatesMap.get(oldName));
		templatesMap.remove(oldName);
		if(currentTemplateName == null || currentTemplateName.equals(oldName))
			currentTemplateName = newName;
		if(templatesMap.containsKey(newName) && !templatesMap.containsKey(oldName))
			return true;
		return false;
	}

	public void updatePanel() {
		templatesPanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatesBox(template);
		templatesPanel.add(templatesBox);
		scrollPane.setViewportView(templatesPanel);
		initLowerPanel();
	}

	private void initLowerPanel() {
		JPanel lowerPanel = new JPanel(new FlowLayout());
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
		lowerPanel.add(buttonPanel, BorderLayout.CENTER);

		if(toolMap != null && !toolMap.isEmpty()) 
			for(String toolName : toolMap.keySet()) 
				initNewButton(buttonPanel, toolName);

		this.add(lowerPanel, BorderLayout.SOUTH);
		lowerPanel.repaint();
	}

	private void initNewButton(JPanel buttonPanel, String buttonName) {
		JButton newToolButton = new JButton(buttonName); 
		newToolButton.setFont(registrar.getService(IconManager.class).getIconFont(14.0f));
		newToolButton.setActionCommand(toolMap.get(buttonName));
		newToolButton.setToolTipText(toolMap.get(buttonName));
		newToolButton.addActionListener(new TemplateButtonListener());
		buttonPanel.add(newToolButton);
	}

	private void addToTemplatesBox(String templateName) {
		Image thumbnail = manager.getThumbnail(templateName);
		JPanel templatePanel = new JPanel(new BorderLayout());
		JButton templateButton = new JButton(new ImageIcon(thumbnail));
		templateButton.setActionCommand(templateName);
		templateButton.addActionListener(new TemplateSelectedListener());
		addPopupMenu(templatePanel, templateButton);
		templatesBox.add(templatePanel);
		templatesMap.put(templateName, templatePanel);
	}

	private void addPopupMenu(JPanel templatePanel, JButton templateButton) {
		JPanel labelPanel = new JPanel(new BorderLayout());
		JLabel templateNameLabel = new JLabel(templateButton.getActionCommand());
		templateNameLabel.setHorizontalAlignment(SwingConstants.CENTER);
		labelPanel.add(templateNameLabel, BorderLayout.CENTER);
		JPopupMenu templatePopup = new JPopupMenu();
		JMenuItem removeTemplate = new JMenuItem("Remove Template");
		removeTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(removeTemplate);
		JMenuItem renameTemplate = new JMenuItem("Rename Template");
		renameTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(renameTemplate);
		templatePanel.addMouseListener(new TemplatePopupListener(templatePopup));
		templateButton.addMouseListener(new TemplatePopupListener(templatePopup));
		templatePanel.add(labelPanel, BorderLayout.SOUTH);
		templatePanel.add(templateButton, BorderLayout.CENTER);
	}

	private void removeFromTemplatesBox(List<String> removeTemplates) {
		if(!removeTemplates.isEmpty())
			for(String template : removeTemplates) {
				templatesBox.remove(templatesMap.get(template));
				templatesMap.remove(template);
			}
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
		List<String> templatesToRemove = new ArrayList<>();
		for(String template : templatesMap.keySet()) {
			if(!templatesInManager.containsKey(template)) {
				templatesToRemove.add(template);
			}
		}
		removeFromTemplatesBox(templatesToRemove);
	}

	public void updateTemplatesPanel() {
		System.out.println("update templates panel called!");
		if(templatesMap.keySet().size() > manager.getTemplateMap().size()) {
			System.out.println("remove a template!");
			updateTemplateButtonsRemove();
		}
		else if(templatesMap.keySet().size() < manager.getTemplateMap().size()) {
			System.out.println("add a template!");
			updateTemplateButtonsAdd();
		}
		templatesBox.repaint();
		this.repaint();
	}

	/*Button, Mouse, and Action Listeners for Swing features*/
	private class TemplateButtonListener implements ActionListener {
		public void actionPerformed(ActionEvent buttonPressed){
			String taskName = buttonPressed.getActionCommand();	
			Object taskObject = tasksMap.get(taskName);

			executeTask(taskObject);//first handle underlying code

			updateTemplatesPanel();
			repaintPanel();
		}
	}

	private boolean executeTask(Object taskObject) {
		TaskIterator taskIterator = null;
		if(taskObject instanceof TaskFactory) {
			System.out.println("instance of task factory!");
			TaskFactory task = (TaskFactory) taskObject;
			taskIterator = task.createTaskIterator();
			taskManager.execute(taskIterator);
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			CyNetworkView networkView = registrar.getService(CyApplicationManager.class).getCurrentNetworkView();
			if(netViewTask != null && netViewTask instanceof TemplateSave) {
				TemplateOverwriteTask overwriteTask = null;
				if(currentTemplateName != null) {
					//when overwrite happens and it is true, then change the thumbnail!
					System.out.println("Check overwrite!");
					overwriteTask = new TemplateOverwriteTask(registrar, networkView, 
							manager, currentTemplateName);
					taskManager.execute(new TaskIterator(overwriteTask));
				}
				if(overwriteTask == null || !overwriteTask.overwrite) {
					System.out.println("Check save!");
					TemplateSaveTask saveTask = new TemplateSaveTask(registrar, networkView, manager);
					taskManager.execute(new TaskIterator(saveTask));
					setCurrentTemplateName(saveTask.templateName);
				}
			}
		}
		if(taskObject != null) 
			return true;
		return false;
	}

	private class TemplateSelectedListener implements ActionListener {
		public void actionPerformed(ActionEvent templateChosen) {
			System.out.println(templateChosen.getActionCommand() + " is the template name!");
			CyNetworkView view = cyApplicationManager.getCurrentNetworkView();
			manager.clearNetworkofTemplates(view);
			boolean isTemplateApplied = manager.useTemplate(templateChosen.getActionCommand(), view);
			if(isTemplateApplied) 
				setCurrentTemplateName(templateChosen.getActionCommand());
		}
	}

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

	private class TemplateMenuItemListener implements ActionListener {
		private JLabel templateLabel;

		public TemplateMenuItemListener(JLabel templateLabel) {
			super();
			this.templateLabel = templateLabel;
		}

		public void actionPerformed(ActionEvent menuItemEvent) {            
			System.out.println(menuItemEvent.getActionCommand());
			TaskIterator iterator = null;
			if(menuItemEvent.getActionCommand().equals(DELETE_TEMPLATE))
				iterator = new TaskIterator(new TemplateDeleteTask(manager, 
						TemplateThumbnailPanel.this, templateLabel.getText()));
			else {
				iterator = new TaskIterator(new TemplateLabelChangeTask(manager, 
						TemplateThumbnailPanel.this, templateLabel));
			}
			if(iterator != null)
				taskManager.execute(iterator);
			updateTemplatesPanel();
		}    
	}   
}