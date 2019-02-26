package chav1961.purelib.navigator.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.util.Locale;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.navigator.LocalizationKeys;

public class LuceneNavigator extends JPanel implements LocaleChangeListener {
	private static final long serialVersionUID = 6225927119842941130L;

	private final Localizer		localizer;
	private final JTextField	query;
	private final JLabel		enterSearch = new JLabel();
	private final JEditorPane	content = new JEditorPane();
	
	public LuceneNavigator(final Localizer localizer) throws LocalizationException {
		super(new BorderLayout(5,5));
		
		this.localizer = localizer;
		this.query = new JTextField();
		query.setColumns(50);
		query.setFocusable(true);
		
		final JPanel		searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		final JScrollPane	scroll = new JScrollPane(content); 
		
		searchPanel.add(enterSearch);
		searchPanel.add(query);
		searchPanel.setBorder(new LineBorder(Color.BLACK));
		add(searchPanel,BorderLayout.NORTH);

		content.setEditable(false);
		content.setFocusable(true);
		
		add(scroll,BorderLayout.CENTER);
		fillLocalizedString(localizer.currentLocale().getLocale());
	}

	public void prepare(final Dimension area) {
		query.requestFocusInWindow();
	}

	
	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedString(newLocale);
	}

	private void fillLocalizedString(final Locale newLocale) throws LocalizationException {
		enterSearch.setText(localizer.getValue(LocalizationKeys.LABEL_LUCENENAVIGATOR_ENTER_SEARCH));
		query.setToolTipText(localizer.getValue(LocalizationKeys.LABEL_LUCENENAVIGATOR_ENTER_SEARCH));
	}
}
