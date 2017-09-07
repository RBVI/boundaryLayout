package edu.ucsf.rbvi.boundaryLayout.internal.tasks;

import javax.swing.JLabel;

import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;

public class TemplateLabelChangeTask extends AbstractTask {
	private JLabel templateLabel;
	
	@Tunable (description = "New name of template:")
	private String newTemplateName = null;
	
	public TemplateLabelChangeTask(JLabel templateLabel) {
		this.templateLabel = templateLabel;
	}
	
	@Override
	public void run(TaskMonitor taskMonitor) throws Exception {
		templateLabel.setText(newTemplateName);
		templateLabel.update(templateLabel.getGraphics());
	}
}