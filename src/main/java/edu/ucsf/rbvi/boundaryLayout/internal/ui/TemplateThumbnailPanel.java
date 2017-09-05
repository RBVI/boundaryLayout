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

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.TaskFactory;
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
	private JPanel buttonPanel;
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
		//initButtonTasks(); //gets a list of buttons for each task
		buttonPanel = new JPanel();

		scrollPane = new JScrollPane(buttonPanel);
		scrollPane.setLayout(new ScrollPaneLayout());
		this.add(scrollPane);

		// Draw our panel
		updatePanel();
	}

	public void updatePanel() {
		buttonPanel.removeAll();
		for (String template : manager.getTemplateNames()) {
			Image thumbnail = manager.getThumbnail(template);
			JButton templateButton = new JButton(template, new ImageIcon(thumbnail));
			templateButton.setActionCommand(template);
			templateButton.addActionListener(this);
			buttonPanel.add(Box.createRigidArea(new Dimension(0,5)));
			buttonPanel.add(templateButton);
		}
		scrollPane.setViewportView(buttonPanel);
	}
	
	public void initButtonTasks() {
		for(String taskName : tasksMap.keySet()) {
			JButton taskButton = new JButton(taskName);
			taskButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					processTask(e);
				}
			});
			buttonTasks.add(taskButton);
		}
	}

	protected void processTask(ActionEvent e) {
		String taskName = e.getActionCommand();
		Object taskObject = tasksMap.get(taskName);
		if(taskObject instanceof TaskFactory) {
			TaskFactory task = (TaskFactory) taskObject;
			task.createTaskIterator();
		}
		else if(taskObject instanceof NetworkViewTaskFactory) {
			NetworkViewTaskFactory netViewTask = (NetworkViewTaskFactory) taskObject;
			netViewTask.createTaskIterator(registrar.getService(
					CyApplicationManager.class).getCurrentNetworkView());
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
