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
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableHandler;
import org.cytoscape.work.TunableHandlerFactory;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.application.CyApplicationManager;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent, ActionListener {
	private static final long serialVersionUID = 1L;
	private CyServiceRegistrar registrar;
	private CyApplicationManager cyApplicationManager;
	private TemplateManager manager;
	private ImageIcon templateIcon;
	private JPanel templatePanel;
	private JPanel buttonTasksPanel;
	private JScrollPane scrollPane;
	private List<JButton> buttonTasks;
	private Map<String, Object> tasksMap;
	
	public TemplateThumbnailPanel() {
		this(null, null, null);
	}

	public TemplateThumbnailPanel(CyServiceRegistrar registrar, 
			TemplateManager manager, Map<String, Object> tasks) {
		super();
		this.registrar = registrar;
		this.manager = manager;
		this.tasksMap = tasks;
		this.cyApplicationManager = registrar.getService(CyApplicationManager.class);

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
		for (String template : manager.getTemplateNames()) {
			Image thumbnail = manager.getThumbnail(template);
			JButton templateButton = new JButton(template, new ImageIcon(thumbnail));
			templateButton.setActionCommand(template);
			templateButton.addActionListener(this);
			templatePanel.add(Box.createRigidArea(new Dimension(0,1)));
			templatePanel.add(templateButton);
		}
		scrollPane.setViewportView(templatePanel);
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
		System.out.println(taskName);
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
		TunableHandlerFactory handler = registrar.getService(TunableHandlerFactory.class);
		
		if(taskIterator != null)
			while(taskIterator.hasNext()) 
				taskIterator.next().run(null);
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
