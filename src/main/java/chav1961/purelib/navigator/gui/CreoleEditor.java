package chav1961.purelib.navigator.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;

import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;

public class CreoleEditor extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = -7646387095653590787L;
	
	private final JEditorPane	left = new JEditorPane();
	private final JScrollPane	leftScroll = new JScrollPane(left);	
	private final JTextArea		right = new JTextArea();
	private final JScrollPane	rightScroll = new JScrollPane(right);	
	private final JSplitPane	pane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,leftScroll,rightScroll);
	
	public CreoleEditor(final Localizer licalizer) {
		setLayout(new BorderLayout(5,5));
		
		left.setEditable(false);
		left.setFocusable(false);
		right.setEditable(true);
		right.setFocusable(true);
		
		add(pane,BorderLayout.CENTER);
	}
	
	public void prepare(final Dimension area) {
		pane.setDividerLocation(area.height/2);
		right.requestFocusInWindow();
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
}
