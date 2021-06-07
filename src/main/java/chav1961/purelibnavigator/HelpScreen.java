package chav1961.purelibnavigator;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.Position;
import javax.swing.text.AbstractDocument.BranchElement;

import chav1961.purelib.basic.PureLibSettings;
import chav1961.purelib.basic.URIUtils;
import chav1961.purelib.basic.Utils;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.FieldFormat;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.ui.swing.useful.JCreoleHelpWindow;

public class HelpScreen extends JPanel {
	private static final long 			serialVersionUID = 4057265063657628793L;
	
	private final Localizer				localizer;
	private final ContentNodeMetadata	meta;
	private final JLabel				caption1 = new JLabel("", JLabel.CENTER);
	private final JLabel				caption2 = new JLabel("", JLabel.CENTER);
	private final JLabel				image = new JLabel();
	private final JCreoleHelpWindow		content;

	public HelpScreen(final Localizer localizer, final ContentNodeMetadata meta, final URI javaId) throws LocalizationException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (javaId == null) {
			throw new NullPointerException("Java id can't be null");
		}
		else if (meta.getHelpId() == null) {
			throw new IllegalArgumentException("Metadata must contain help Id");
		}
		else if (meta.getIcon() == null) {
			throw new IllegalArgumentException("Metadata must contain icon attribute");
		}
		else {
			final JPanel	topPanel = new JPanel(); 
			final JPanel	topRightPanel = new JPanel();
			
			this.localizer = localizer;
			this.meta = meta;
			this.content = new JCreoleHelpWindow(localizer, meta.getHelpId()); 
			
			setLayout(new BorderLayout(5,5));
			topPanel.setLayout(new BorderLayout(5,5));
			topRightPanel.setLayout(new GridLayout(2,1));
			topRightPanel.add(caption1);
			topRightPanel.add(caption2);
			topPanel.add(image, BorderLayout.WEST);
			topPanel.add(topRightPanel, BorderLayout.CENTER);
			add(topPanel, BorderLayout.NORTH);
			
			final JScrollPane	pane = new JScrollPane(content);
			add(pane, BorderLayout.CENTER);
			
			try{image.setIcon(new ImageIcon(meta.getIcon().toURL()));
			} catch (MalformedURLException exc) {
				throw new LocalizationException(exc.getLocalizedMessage(),exc);
			}
			
			fillLocalizedStrings();
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		caption1.setText(localizer.getValue(meta.getLabelId()));
		
		if (meta.getTooltipId() != null) {
			caption2.setText(localizer.getValue(meta.getTooltipId()));
		}
	}

	public static void main(String[] args) throws LocalizationException, IOException {
		final ContentNodeMetadata	meta = new ContentNodeMetadata() {
										@Override public Iterator<ContentNodeMetadata> iterator() {return null;}
										@Override public String getName() {return null;}
										@Override public boolean mounted() {return false;}
										@Override public Class<?> getType() {return String.class;}
										@Override public String getLabelId() {return "testSet1";}
										@Override public String getTooltipId() {return "testSet2";}
										@Override public String getHelpId() {return "testSet4";}
										@Override public FieldFormat getFormatAssociated() {return null;}
										@Override public URI getApplicationPath() {return URI.create("app:http://ya.ru");}
										@Override public URI getUIPath() {return null;}
										@Override public URI getRelativeUIPath() {return null;}
										@Override public URI getLocalizerAssociated() {return null;}
										@Override public URI getIcon() {
											try{return this.getClass().getResource("Java.png").toURI();
											} catch (URISyntaxException e) {
												return null;
											}
										}
										@Override public ContentNodeMetadata getParent() {return null;}
										@Override public int getChildrenCount() {return 0;}
										@Override public ContentNodeMetadata getChild(int index) {return null;}
										@Override public ContentMetadataInterface getOwner() {return null;}
									}; 
		final HelpScreen		hs = new HelpScreen(PureLibSettings.PURELIB_LOCALIZER, meta, URI.create("test"));
		
		hs.setMinimumSize(new Dimension(200,200));
		hs.setPreferredSize(new Dimension(200,200));
		JOptionPane.showMessageDialog(null, hs);
		
		final JPanel	panel = new JPanel();
		panel.add(new JGotoURIButton(PureLibSettings.PURELIB_LOCALIZER,meta));
		
		JOptionPane.showMessageDialog(null, panel);
	}

}
