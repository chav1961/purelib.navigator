package chav1961.purelib.navigator;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.w3c.dom.Document;

public class SearchScreen extends JPanel {
	private static final long serialVersionUID = -8655690774982200581L;

	private final JTree		leftNavigator = new JTree();
	private final JTextArea	rightContent = new JTextArea();
	private final Document	modelContent;
	
	public SearchScreen() {
		final JSplitPane	jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		
		this.modelContent = null;
		this.setLayout(new BorderLayout());
		jsp.setLeftComponent(new JScrollPane(leftNavigator));
		jsp.setRightComponent(rightContent);
		add(jsp,BorderLayout.CENTER);
		jsp.setDividerLocation(400);
	}
}