package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent.FillMode;
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
	
	@FunctionalInterface
	static interface CreoleContentSaveCallback {
		void save(String content) throws IOException;
	}
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentMetadataInterface	mdi;
	private final CreoleContentSaveCallback	callback;
	private final CardLayout				layout = new CardLayout();
	private final JToolBar					creoleToolBar;
	private final JCreoleEditor				editor = new JCreoleEditor();
	private final JToolBar					imageToolBar;
	private final JBackgroundComponent		image;
	private final JLabel					common = new JLabel("<not selected>", JLabel.CENTER);
	
	private ContentType						contentType = ContentType.CREOLE;
	private boolean							editorContentChanged = false;
	
	public ContentEditorAndViewer(final Localizer localizer, final LoggerFacade logger, final ContentMetadataInterface mdi, final CreoleContentSaveCallback callback) throws NullPointerException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (logger == null) {
			throw new NullPointerException("Logger can't be null");
		}
		else if (mdi == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (callback == null) {
			throw new NullPointerException("Savre content callback can't be null");
		}
		else {
			this.localizer = localizer;
			this.logger = logger;
			this.mdi = mdi;
			this.callback = callback;
			this.image = new JBackgroundComponent(localizer);
			setLayout(layout);
			
			final JPanel	creolePanel = new JPanel(new BorderLayout());
			this.creoleToolBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.creoleEditorToolbar")), JToolBar.class);
			
			creoleToolBar.setFloatable(false);
			creolePanel.add(creoleToolBar, BorderLayout.NORTH);
			creolePanel.add(new JScrollPane(editor), BorderLayout.CENTER);
			SwingUtils.assignActionListeners(creoleToolBar, this);
			
			final JPanel	imagePanel = new JPanel(new BorderLayout());
			this.imageToolBar = SwingUtils.toJComponent(mdi.byUIPath(URI.create("ui:/model/navigation.top.imageViewToolbar")), JToolBar.class);
			
			imageToolBar.setFloatable(false);
			imagePanel.add(imageToolBar, BorderLayout.NORTH);
			imagePanel.add(image, BorderLayout.CENTER);
			SwingUtils.assignActionListeners(imageToolBar, this);
			
			final JPanel	commonPanel = new JPanel(new BorderLayout());

			commonPanel.add(common, BorderLayout.CENTER);
			
			add(creolePanel, ContentType.CREOLE.getCardName());
			add(imagePanel, ContentType.IMAGE.getCardName());
			add(commonPanel, ContentType.COMMON.getCardName());
			setContenTypeInternal(ContentType.COMMON);
			
			editor.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void removeUpdate(DocumentEvent e) {editorContentChanged = true;}
				@Override public void insertUpdate(DocumentEvent e) {editorContentChanged = true;}
				@Override public void changedUpdate(DocumentEvent e) {editorContentChanged = true;}
			});
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

	public boolean creoleContentWasChanged() {
		return editorContentChanged;
	}
	
	public JBackgroundComponent getImageContainer() {
		return image;
	}
	
	private void setContenTypeInternal(final ContentType contentType) {
		this.contentType = contentType;
		layout.show(this, contentType.getCardName());
	}

	@OnAction("action:/creoleClear")
	private void clearCreoleContent() {
		editor.setText("");
	}

	@OnAction("action:/creoleSave")
	void saveCreoleContent() {
		try{callback.save(editor.getText());
			editorContentChanged = false;
		} catch (IOException e) {
			logger.message(Severity.error, e.getLocalizedMessage(), e);
		}
	}
	
	@OnAction("action:/imageFill")
	private void fillImage() {
		image.setFillMode(FillMode.FILL);
	}

	@OnAction("action:/imageOriginal")
	private void originalImage() {
		image.setFillMode(FillMode.ORIGINAL);
	}
}
