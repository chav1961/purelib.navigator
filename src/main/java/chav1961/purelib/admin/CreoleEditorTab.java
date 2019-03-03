package chav1961.purelib.admin;

import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.fsys.FileSystemFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;
import chav1961.purelib.ui.swing.useful.JFileContentManipulator;
import chav1961.purelib.ui.swing.useful.JStateString;

public class CreoleEditorTab implements LocaleChangeListener {
	final JCreoleEditor				editor = new JCreoleEditor();
	final JFileContentManipulator	manipulator;
	
	private final Localizer			localizer; 
	private final JStateString		state;
	private final DocumentListener	listener = new DocumentListener() {
										@Override public void removeUpdate(DocumentEvent e) {manipulator.setModificationFlag();}
										@Override public void insertUpdate(DocumentEvent e) {manipulator.setModificationFlag();}				
										@Override public void changedUpdate(DocumentEvent e) {manipulator.setModificationFlag();}
									};

	public CreoleEditorTab(final Localizer localizer, final JStateString state) throws IOException {
		this.localizer = localizer;
		this.state = state;
		this.manipulator = new JFileContentManipulator(FileSystemFactory.createFileSystem(URI.create("fsys:file:./")),localizer,editor);
		turnOnDocumentListener();
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		// TODO Auto-generated method stub
		
	}
	
	void turnOffDocumentListener() {
		editor.getDocument().removeDocumentListener(listener);
	}

	void turnOnDocumentListener() {
		editor.getDocument().addDocumentListener(listener);
	}
}
