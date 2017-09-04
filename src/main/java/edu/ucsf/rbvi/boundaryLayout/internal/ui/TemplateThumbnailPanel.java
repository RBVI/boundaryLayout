package edu.ucsf.rbvi.boundaryLayout.internal.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Image;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JWindow;
import javax.swing.ScrollPaneLayout;
import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.service.util.CyServiceRegistrar;

import edu.ucsf.rbvi.boundaryLayout.internal.model.TemplateManager;

public class TemplateThumbnailPanel extends JPanel implements CytoPanelComponent, ActionListener {
	private static final long serialVersionUID = 1L;
	private CyServiceRegistrar registrar;
	private TemplateManager manager;
	private ImageIcon templateIcon;
	private JScrollPane scrollPane;
	
	public TemplateThumbnailPanel() {
		this(null, null);
	}

	public TemplateThumbnailPanel(CyServiceRegistrar registrar, TemplateManager manager) {
		super();
		this.registrar = registrar;
		this.manager = manager;

		// Consider adding a button bar to import/export/add templates

		// This will contain all of our template buttons
		scrollPane = new JScrollPane();
		scrollPane.setLayout(new ScrollPaneLayout());
		this.add(scrollPane);

		// Draw our panel
		updatePanel();
	}

	public void updatePanel() {
		for (String template : manager.getTemplateNames()) {
			Image thumbnail = manager.getThumbnail(template);
			JButton templateButton = new JButton(template, new ImageIcon(thumbnail));
			templateButton.setActionCommand(template);
			templateButton.addActionListener(this);
			scrollPane.add(Box.createRigidArea(new Dimension(0,5)));
			scrollPane.add(templateButton);
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
		return "Boundary Layout Templates";
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String template = e.getActionCommand();
		// Replace (add) template
	}
}