package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.net.URI;

import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import chav1961.purelib.basic.exceptions.FlowException;
import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.ui.interfaces.ActionFormManager;
import chav1961.purelib.ui.interfaces.RefreshMode;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent.FillMode;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

class ContentEditorAndViewer extends JPanel {
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
	private JsonNode						navigator = null, parent =  null, current = null;
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
				@Override public void removeUpdate(DocumentEvent e) {setContentChanged(true);}
				@Override public void insertUpdate(DocumentEvent e) {setContentChanged(true);}
				@Override public void changedUpdate(DocumentEvent e) {setContentChanged(true);}
			});
			editor.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						final JPopupMenu	popup = new JPopupMenu();
						final JMenu			copyLinks = new JMenu("Copy links");
						final JMenu			insertLinks = new JMenu("Insert links");
						final JMenu			insertTotal = new JMenu("Whole navigator tree");
						
						if (!AdminUtils.buildInternalLinksMenu(copyLinks, editor.getText()))  {
							copyLinks.setEnabled(false);
						}
						else {
							SwingUtils.assignActionListeners(copyLinks, (ev)->{
								System.err.println("Name="+ev.getActionCommand());
							});
						}
						popup.add(copyLinks);

						if (AdminUtils.buildTreeLinksMenu(insertTotal, navigator, (t)->true)) {
							insertLinks.add(insertTotal);
							insertLinks.addSeparator();
						}
						if (AdminUtils.buildInternalLinksMenu(insertLinks, editor.getText())) {
							insertLinks.addSeparator();
						}
						AdminUtils.buildSiblingLinksMenu(insertLinks, parent, (t)->true);
						SwingUtils.assignActionListeners(insertLinks, (ev)->{
							try {
								editor.getDocument().insertString(editor.getCaretPosition(), ev.getActionCommand(), editor.getCharacterStyles(CreoleLexema.LinkRef));
							} catch (BadLocationException e1) {
								// TODO Auto-generated catch block
								e1.printStackTrace();
							}
							System.err.println("Link="+ev.getActionCommand());
						});
						popup.add(insertLinks);
						
						popup.show(editor, e.getX(), e.getY());
					}
				}
			});
			setContentChanged(false);
		}
	}
	
	public ContentType getContentType() {
		return contentType;
	}
	
	public void setContentType(final ContentType contentType, final JsonNode navigator, final JsonNode parent, final JsonNode current) {
		if (contentType == null) {
			throw new NullPointerException("Content type can't be null"); 
		}
		else if (navigator == null) {
			throw new NullPointerException("Navigator node can't be null"); 
		}
		else if (parent == null) {
			throw new NullPointerException("Parent node can't be null"); 
		}
		else if (current == null) {
			throw new NullPointerException("Current node can't be null"); 
		}
		else {
			setContenTypeInternal(contentType);
			this.navigator = navigator; 
			this.parent = parent; 
			this.current = current; 
			setContentChanged(false);
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
			setContentChanged(false);
		} catch (IOException e) {
			logger.message(Severity.error, e.getLocalizedMessage(), e);
		}
	}
	
	private void setContentChanged(final boolean newState) {
		if (editorContentChanged != newState) {
			final ContentNodeMetadata	meta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":action:/creoleSave"))[0];
			
			SwingUtils.findComponentByName(creoleToolBar, meta.getName()).setEnabled(newState);
			editorContentChanged = newState;
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
