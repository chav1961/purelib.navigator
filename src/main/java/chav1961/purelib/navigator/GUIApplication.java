package chav1961.purelib.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.MenuBar;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.Reader;
import java.nio.channels.UnsupportedAddressTypeException;
import java.util.Locale;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.border.EtchedBorder;
import javax.swing.border.LineBorder;

import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.navigator.gui.CreoleEditor;
import chav1961.purelib.navigator.gui.LuceneNavigator;
import chav1961.purelib.navigator.utils.LuceneIndexWizard;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.XMLDescribedApplication;
import chav1961.purelib.ui.swing.interfaces.OnAction;

public class GUIApplication extends JFrame implements LocaleChangeListener {
	private static final long 				serialVersionUID = -1408706234867048980L;
	private static final String 			INFO_MESSAGE_TEMPLATE = "<html><body><font color=black>%1$s</font></body></html>"; 
	private static final String 			WARNING_MESSAGE_TEMPLATE = "<html><body><font color=bluek>%1$s</font></body></html>"; 
	private static final String 			ERROR_MESSAGE_TEMPLATE = "<html><body><font color=red>%1$s</font></body></html>"; 
	private static final String 			SEVERE_MESSAGE_TEMPLATE = "<html><body><font color=red><b>%1$s</b></font></body></html>"; 

	private final XMLDescribedApplication	xda;
	private final Localizer					localizer;
	private final JTabbedPane				tab = new JTabbedPane();
	private final JLabel					state = new JLabel("(c) 2018 chav1961");
	private final JMenuBar					bar;
	
	public GUIApplication(final XMLDescribedApplication xda, final Localizer parentLocalizer) throws NullPointerException, IllegalArgumentException, EnvironmentException {
		if (xda == null) {
			throw new NullPointerException("Application descriptor can't be null");
		}
		else if (parentLocalizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.xda = xda;
			this.localizer = xda.getLocalizer();
			
			localizer.setParent(parentLocalizer);
			
			this.bar = xda.getEntity("mainmenu",JMenuBar.class,null); 
			
			SwingUtils.assignActionListeners(bar,this);
			getContentPane().add(bar,BorderLayout.NORTH);
			
			tab.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			getContentPane().add(tab,BorderLayout.CENTER);
			
			final JPanel	statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			statePanel.add(state);
			statePanel.setBorder(new LineBorder(Color.BLACK));
			getContentPane().add(statePanel,BorderLayout.SOUTH);
			
			localizer.addLocaleChangeListener(this);
			
			SwingUtils.assignHelpKey((JPanel)getContentPane(),localizer,LocalizationKeys.HELP_ABOUT_APPLICATION);
			SwingUtils.centerMainWindow(this,0.75f);
			addWindowListener(new WindowListener() {
				@Override public void windowOpened(WindowEvent e) {}
				
				@Override 
				public void windowClosing(WindowEvent e) {
					exitApplication();
				}

				@Override public void windowClosed(WindowEvent e) {}
				@Override public void windowIconified(WindowEvent e) {}
				@Override public void windowDeiconified(WindowEvent e) {}
				@Override public void windowActivated(WindowEvent e) {}
				@Override public void windowDeactivated(WindowEvent e) {}
			});
			
			fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(oldLocale,newLocale);
		SwingUtils.refreshLocale(this,oldLocale, newLocale);
	}

	private void message(final String message) {
		state.setText(message);
	}
	
	@OnAction("exit")
	private void exitApplication () {
		setVisible(false);
		dispose();
	}

		
	@OnAction("luceneNavigator")
	private void luceneNavigator() throws LocalizationException {
		final LuceneNavigator	navigator = new LuceneNavigator(localizer);
		
		tab.addTab("Lucene",navigator);
		navigator.prepare(getContentPane().getSize());
	}

	@OnAction("builtin.languages:en")
	private void selectEN() throws LocalizationException {
		localizer.setCurrentLocale(Locale.forLanguageTag("en"));
	}

	@OnAction("builtin.languages:ru")
	private void selectRU() throws LocalizationException {
		localizer.setCurrentLocale(Locale.forLanguageTag("ru"));
	}

	@OnAction("luceneIndex")
	private void buildLuceneIndex() throws LocalizationException, PreparationException, FlowException, InterruptedException {
		final LuceneIndexWizard	wizard = new LuceneIndexWizard(this, localizer);
		
		final Thread			t = new Thread(new Runnable() {
										@Override
										public void run() {
											try{wizard.animate(wizard);
											} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
												showStateMessage(Severity.error,e.getLocalizedMessage());
											}
										}
									});
		t.setDaemon(true);
		t.start();
	}
	
	
	@OnAction("creoleEditor")
	private void creoleEditor() {
		final CreoleEditor	editor = new CreoleEditor(localizer);
		
		tab.addTab("Creole",editor);
		editor.prepare(getContentPane().getSize());
	}
	
	@OnAction("helpAbout")
	private void showAboutScreen() {
		try{final JEditorPane 	pane = new JEditorPane("text/html",null);
			final Icon			icon = new ImageIcon(this.getClass().getResource("avatar.jpg"));
			
			try(final Reader	rdr = localizer.getContent(LocalizationKeys.HELP_ABOUT_APPLICATION,new MimeType("text","x-wiki.creole"),new MimeType("text","html"))) {
				pane.read(rdr,null);
			}
			pane.setEditable(false);
			pane.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			pane.setPreferredSize(new Dimension(640,480));
			
			JOptionPane.showMessageDialog(this,pane,localizer.getValue(LocalizationKeys.TITLE_HELP_ABOUT_APPLICATION),JOptionPane.PLAIN_MESSAGE,icon);
		} catch (LocalizationException | MimeTypeParseException | IOException e) {
			e.printStackTrace();
		}
	}

	private void fillLocalizedStrings(final Locale oldLocale, final Locale newLocale) throws LocalizationException{
		((LocaleChangeListener)bar).localeChanged(oldLocale, newLocale);
		setTitle(localizer.getValue(LocalizationKeys.TITLE_APPLICATION));
	}
	
	private void showStateMessage(final Severity severity, String message, final Object... parameters) {
		String	text = parameters == null || parameters.length == 0 ? message : String.format(message,parameters); 

		text = text.replace("<","&lt;").replace(">","&gt;").replace("&","&amp;");
		switch (severity) {
			case error	: text = String.format(ERROR_MESSAGE_TEMPLATE,text); break;
			case severe	: text = String.format(SEVERE_MESSAGE_TEMPLATE,text); break;
			case warning: text = String.format(WARNING_MESSAGE_TEMPLATE,text); break;
			case debug : case trace : case info : text = String.format(INFO_MESSAGE_TEMPLATE,text); break;
			default : throw new UnsupportedOperationException("Severity level ["+severity+"] is not supported yet");
		}
		state.setText(text);
	}
}
