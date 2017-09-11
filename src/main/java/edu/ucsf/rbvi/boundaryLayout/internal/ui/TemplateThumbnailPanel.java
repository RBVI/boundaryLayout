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
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskManager;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
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
	public static final String ADD_TEMPLATE = "add";
	public static final String IMPORT_TEMPLATE = "import";
	public static final String EXPORT_TEMPLATE = "export";
	public static final String DELETE_TEMPLATE = "Remove Template";	
	private final List<String> toolList;
	private static final long serialVersionUID = 1L;

	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private TaskManager taskManager;
	private ImageIcon templateIcon;
	private JPanel templatePanel;
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
		this.templatePanel = new JPanel();
		this.templatesBox = Box.createVerticalBox();
		this.currentTemplateName = null;
		this.toolList = new ArrayList<>();
		toolList.add(IMPORT_TEMPLATE);
		toolList.add(EXPORT_TEMPLATE);
		toolList.add(ADD_TEMPLATE);

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
		if(currentTemplateName.equals(oldName))
			currentTemplateName = newName;
		if(templatesMap.containsKey(newName) && !templatesMap.containsKey(oldName))
			return true;
		return false;
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

		if(toolList != null && !toolList.isEmpty()) 
			for(String toolName : toolList) 
				initNewButton(buttonPanel, toolName);

		this.add(lowerPanel, BorderLayout.SOUTH);
		lowerPanel.repaint();
	}

	private void initNewButton(JPanel buttonPanel, String buttonName) {
		if(getClass().getResource("/icons/" + buttonName + ".png") != null) {
			ImageIcon newIcon = new ImageIcon(getClass().getResource("/icons/" + buttonName + ".png"));
			newIcon.setImage(newIcon.getImage().getScaledInstance(30, 30, Image.SCALE_SMOOTH));
			JButton newToolButton = new JButton(newIcon);
			newToolButton.setActionCommand(buttonName);
			newToolButton.addActionListener(new TemplateButtonListener());
			buttonPanel.add(newToolButton);
		}
	}

	private void addToTemplatesBox(String templateName) {
		Image thumbnail = manager.getThumbnail(templateName);
		JPanel templatePanel = new JPanel(new BorderLayout());
		JButton templateButton = new JButton(new ImageIcon(thumbnail));
		templateButton.setActionCommand(templateName);
		templateButton.addActionListener(new TemplateSelectedListener());
		addPopupMenuItem(templatePanel, templateButton);
		templatesBox.add(templatePanel);
		templatesMap.put(templateName, templatePanel);
	}

	private void addPopupMenuItem(JPanel templatePanel, JButton templateButton) {
		JLabel templateNameLabel = new JLabel(templateButton.getActionCommand());
		JPopupMenu templatePopup = new JPopupMenu();
		JMenuItem removeTemplate = new JMenuItem("Remove Template");
		removeTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(removeTemplate);
		JMenuItem renameTemplate = new JMenuItem("Rename Template");
		renameTemplate.addActionListener(new TemplateMenuItemListener(templateNameLabel));
		templatePopup.add(renameTemplate);
		templatePanel.addMouseListener(new TemplatePopupListener(templatePopup));
		templateButton.addMouseListener(new TemplatePopupListener(templatePopup));
		templatePanel.add(templateNameLabel, BorderLayout.SOUTH);
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
		if(templatesMap.keySet().size() > manager.getTemplateMap().size()) {
			System.out.println("remove a template!");
			updateTemplateButtonsRemove();
		}
		else if(templatesMap.keySet().size() < manager.getTemplateMap().size()) {
			System.out.println("add a template!");
			updateTemplateButtonsAdd();
		}
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
		updateTemplatesPanel();
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
				popupMenu.show(TemplateThumbnailPanel.this, e.getX(), e.getY());
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