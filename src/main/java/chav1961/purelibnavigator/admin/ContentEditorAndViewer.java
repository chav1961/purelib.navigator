package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.net.URI;

import javax.swing.JPanel;
import javax.swing.JToolBar;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

public class ContentEditorAndViewer extends JPanel {
	private static final long serialVersionUID = 1L;

	public static enum ContentType {
		CREOLE("creole"),
		IMAGE("image"),
		COMMON("common");
		
		private final String	cardName;
		
		private ContentType(final String cardName) {
			this.cardName = cardName;
		}
		
		public String getCardName() {
			return cardName;
		}
	}
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentMetadataInterface	mdi;
	private final CardLayout				layout = new CardLayout();
	private final JCreoleEditor				editor = new JCreoleEditor();
	private final JBackgroundComponent		image;
	
	private ContentType	contentType = ContentType.CREOLE;
	
	public ContentEditorAndViewer(final Localizer localizer, final LoggerFacade logger, final ContentMetadataInterface mdi) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (mdi == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.mdi = mdi;
			this.image = new JBackgroundComponent(localizer);
			setLayout(layout);
			
			final JPanel	creolePanel = new JPanel(new BorderLayout());
			final JToolBar	creoleToolBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("")), JToolBar.class);
			
			creolePanel.add(creoleToolBar, BorderLayout.NORTH);
			creolePanel.add(editor, BorderLayout.NORTH);
			SwingUtils.assignActionListeners(creoleToolBar, this);
			
			final JPanel	imagePanel = new JPanel(new BorderLayout());
			final JToolBar	imageToolBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("")), JToolBar.class);
			
			imagePanel.add(imageToolBar, BorderLayout.NORTH);
			imagePanel.add(image, BorderLayout.NORTH);
			SwingUtils.assignActionListeners(imageToolBar, this);
			
			final JPanel	commonPanel = new JPanel(new BorderLayout());
			final JToolBar	commonToolBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("")), JToolBar.class);

			commonPanel.add(commonToolBar, BorderLayout.NORTH);
			commonPanel.add(editor, BorderLayout.NORTH);
			SwingUtils.assignActionListeners(commonToolBar, this);
			
			add(creolePanel, ContentType.CREOLE.getCardName());
			add(imagePanel, ContentType.IMAGE.getCardName());
			add(commonPanel, ContentType.COMMON.getCardName());
			setContenTypeInternal(ContentType.COMMON);
		}
	}
	
	public ContentType getContentType() {
		return contentType;
	}
	
	public void setContentType(final ContentType contentType) {
		if (contentType == null) {
			throw new NullPointerException("Content type can't be null"); 
		}
		else {
			setContenTypeInternal(contentType);
		}
	}

	public JCreoleEditor getCreoleEditor() {
		return editor;
	}

	public JBackgroundComponent getImageContainer() {
		return image;
	}
	
	private void setContenTypeInternal(final ContentType contentType) {
		this.contentType = contentType;
		layout.show(this, contentType.getCardName());
	}
	
	@OnAction("")
	private void onExit() {
		
	}
}
