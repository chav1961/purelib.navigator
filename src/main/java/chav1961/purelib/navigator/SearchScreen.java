package chav1961.purelib.navigator;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;

import org.w3c.dom.Document;

import chav1961.purelib.basic.NullLoggerFacade;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.ui.interfacers.FormManager;
import chav1961.purelib.ui.interfacers.RefreshMode;
import chav1961.purelib.ui.swing.AutoBuiltForm;

public class SearchScreen extends JPanel {
	private static final long serialVersionUID = -8655690774982200581L;

	private final JTree				leftNavigator = new JTree();
	private final JTextArea			rightContent = new JTextArea();
	private final Document			modelContent;
	private final NavigationFilter	filter = new NavigationFilter();
	
	public SearchScreen(final Localizer localizer) throws SyntaxException, LocalizationException, ContentException {
		final JSplitPane	jsp = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		final JPanel		leftPanel = new JPanel(new BorderLayout());
		final JPanel		leftTopPanel = new AutoBuiltForm<NavigationFilter>(localizer, filter, new FormManager<Object, NavigationFilter>() {
								@Override
								public RefreshMode onField(NavigationFilter inst, Object id, String fieldName, Object oldValue) throws FlowException, LocalizationException {
									return RefreshMode.DEFAULT;
								}
					
								@Override
								public LoggerFacade getLogger() {
									return new NullLoggerFacade();
								}
							},2);
		
		this.modelContent = null;
		this.setLayout(new BorderLayout());
		leftPanel.add(leftTopPanel,BorderLayout.NORTH);
		leftPanel.add(new JScrollPane(leftNavigator),BorderLayout.CENTER);
		jsp.setLeftComponent(leftPanel);
		jsp.setRightComponent(rightContent);
		add(jsp,BorderLayout.CENTER);
		jsp.setDividerLocation(400);
	}
}