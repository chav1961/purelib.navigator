package chav1961.purelib.navigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.datatransfer.MimeTypeParseException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.Reader;
import java.net.URI;
import java.util.Locale;

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

import chav1961.purelib.basic.MimeType;
import chav1961.purelib.basic.exceptions.ContentException;
import chav1961.purelib.basic.exceptions.EnvironmentException;
import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.exceptions.PreparationException;
import chav1961.purelib.basic.exceptions.SyntaxException;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.LocalizerFactory;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.navigator.gui.LuceneNavigator;
import chav1961.purelib.navigator.utils.LuceneIndexWizard;
import chav1961.purelib.ui.swing.SwingModelUtils;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JCloseableTab;
import chav1961.purelib.ui.swing.useful.JStateString;

public class GUIApplication extends JFrame implements LocaleChangeListener {
	private static final long 				serialVersionUID = -1408706234867048980L;
	private static final String				TAB_OVERVIEW = "application.tab.overview";
	private static final String				TAB_OVERVIEW_TOOLTIP = "application.tab.overview.tt";
	private static final String				TAB_SEARCH = "application.tab.search";
	private static final String				TAB_SEARCH_TOOLTIP = "application.tab.search.tt";
	private static final String				TAB_SAMPLE = "application.tab.codeSample";
	private static final String				TAB_SAMPLE_TOOLTIP = "application.tab.codeSample.tt";
	
	private final ContentMetadataInterface	xda;
	private final Localizer					localizer;
	private final JStateString				state;
	private final JMenuBar					bar;
	private final JTabbedPane				tab = new JTabbedPane();
	private final OverviewScreen			overviewScreen = new OverviewScreen();
	private final JCloseableTab				overviewMark;
	private final SearchScreen				searchScreen; 
	private final JCloseableTab				searchMark;
	private final CodeSampleScreen			codeSampleScreen = new CodeSampleScreen(); 
	private final JCloseableTab				sampleMark;
	
	public GUIApplication(final ContentMetadataInterface xda, final Localizer parentLocalizer) throws NullPointerException, IllegalArgumentException, EnvironmentException, IOException, SyntaxException, ContentException {
		if (xda == null) {
			throw new NullPointerException("Application descriptor can't be null");
		}
		else if (parentLocalizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else {
			this.xda = xda;
			this.localizer = LocalizerFactory.getLocalizer(xda.getRoot().getLocalizerAssociated());
			this.state = new JStateString(localizer);
			this.overviewMark = new JCloseableTab(localizer);
			this.searchMark = new JCloseableTab(localizer);
			this.sampleMark = new JCloseableTab(localizer);
			
			localizer.setParent(parentLocalizer);
			localizer.addLocaleChangeListener(this);
			this.searchScreen = new SearchScreen(this.localizer); 
			
			this.bar = SwingModelUtils.toMenuEntity(xda.byUIPath(URI.create("ui:/model/navigation.top.mainmenu")),JMenuBar.class); 
			
			SwingUtils.assignActionListeners(bar,this);
			getContentPane().add(bar,BorderLayout.NORTH);
			
			tab.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
			getContentPane().add(tab,BorderLayout.CENTER);
			
			final JPanel	statePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			
			statePanel.add(state);
			statePanel.setBorder(new LineBorder(Color.BLACK));
			getContentPane().add(statePanel,BorderLayout.SOUTH);
			
//			SwingUtils.assignHelpKey((JPanel)getContentPane(),localizer,LocalizationKeys.HELP_ABOUT_APPLICATION);
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
			
			tab.addTab(TAB_OVERVIEW,overviewScreen);
			overviewMark.associate(tab,overviewScreen);
			tab.setTabComponentAt(0,overviewMark);
			tab.addTab(TAB_SEARCH,searchScreen);
			searchMark.associate(tab,searchScreen);
			tab.setTabComponentAt(1,searchMark);
			tab.addTab(TAB_SAMPLE,codeSampleScreen);
			sampleMark.associate(tab,codeSampleScreen);
			tab.setTabComponentAt(2,sampleMark);
			
			fillLocalizedStrings(localizer.currentLocale().getLocale(),localizer.currentLocale().getLocale());
		}
	}

	@Override
	public void localeChanged(final Locale oldLocale, final Locale newLocale) throws LocalizationException {
		fillLocalizedStrings(oldLocale,newLocale);
		SwingUtils.refreshLocale(this,oldLocale, newLocale);
	}

	@OnAction("action:/exit")
	private void exitApplication () {
		setVisible(false);
		dispose();
	}

		
	@OnAction("action:/luceneNavigator")
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

	@OnAction("action:/luceneIndex")
	private void buildLuceneIndex() throws LocalizationException, PreparationException, FlowException, InterruptedException {
		final LuceneIndexWizard	wizard = new LuceneIndexWizard(this, localizer);
		
		final Thread			t = new Thread(new Runnable() {
										@Override
										public void run() {
											try{wizard.animate(wizard);
											} catch (LocalizationException | PreparationException | FlowException | InterruptedException e) {
												state.message(Severity.error,e.getLocalizedMessage());
											}
										}
									});
		t.setDaemon(true);
		t.start();
	}
	
	
	@OnAction("action:/helpAbout")
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
		overviewMark.setText(localizer.getValue(TAB_OVERVIEW));
		overviewMark.setToolTipText(localizer.getValue(TAB_OVERVIEW_TOOLTIP));
		searchMark.setText(localizer.getValue(TAB_SEARCH));
		searchMark.setToolTipText(localizer.getValue(TAB_SEARCH_TOOLTIP));
		sampleMark.setText(localizer.getValue(TAB_SAMPLE));
		sampleMark.setToolTipText(localizer.getValue(TAB_SAMPLE_TOOLTIP));
	}
}
