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
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateDelete;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateLabelChangeTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwrite;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateOverwriteTask;
import edu.ucsf.rbvi.boundaryLayout.internal.tasks.TemplateSave;

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
		this.currentTemplateName = null;

		initLowerPanel();

		scrollPane = new JScrollPane(templatePanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.setLayout(new BorderLayout());
		this.add(scrollPane, BorderLayout.SOUTH);

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

	private void setCurrentTemplateName(String templateName) {
		this.currentTemplateName = templateName;
	}

	private void initLowerPanel() {
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);

		JButton importButton = new JButton(new ImageIcon());//ADD IMPORT ICON
		JButton exportButton = new JButton(new ImageIcon());//ADD EXPORT ICON
		JButton addButton = new JButton(new ImageIcon());//ADD "ADD" ICON
		importButton.setActionCommand(IMPORT_TEMPLATE);
		exportButton.setActionCommand(EXPORT_TEMPLATE);
		addButton.setActionCommand(ADD_TEMPLATE);
		importButton.addActionListener(new TemplateButtonListener());
		exportButton.addActionListener(new TemplateButtonListener());
		addButton.addActionListener(new TemplateButtonListener());	
	}

	public void updatePanel() {
		templatePanel.removeAll();
		for (String template : manager.getTemplateNames())
			addToTemplatePanel(template);
		scrollPane.setViewportView(templatePanel);
	}

	private void addToTemplatePanel(String templateName) {
		Image thumbnail = manager.getThumbnail(templateName);
		JButton templateButton = new JButton(new ImageIcon(thumbnail));
		templateButton.setActionCommand(templateName);
		templateButton.addActionListener(new TemplateSelectedListener(templateName));
		templatePanel.add(Box.createRigidArea(new Dimension(0,1)));
		templatePanel.add(templateButton);
		templatesMap.put(templateName, templateButton);
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
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			if(netViewTask != null && netViewTask instanceof TemplateSave && currentTemplateName != null)
				netViewTask = new TemplateOverwrite(registrar, manager, currentTemplateName);
			taskIterator = netViewTask.createTaskIterator(registrar.getService(
					CyApplicationManager.class).getCurrentNetworkView());
		}
		if(taskObject != null) {
			taskManager.execute(taskIterator);
			return true;
		}
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
			boolean isTemplateApplied = manager.useTemplate(templateName, view);
			if(isTemplateApplied) setCurrentTemplateName(templateName);
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