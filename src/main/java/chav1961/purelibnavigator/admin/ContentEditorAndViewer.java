package chav1961.purelibnavigator.admin;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.FlavorListener;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.net.URI;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JToolBar;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.basic.interfaces.LoggerFacade;
import chav1961.purelib.basic.interfaces.LoggerFacade.Severity;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.i18n.interfaces.Localizer.LocaleChangeListener;
import chav1961.purelib.json.JsonNode;
import chav1961.purelib.model.interfaces.ContentMetadataInterface;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;
import chav1961.purelib.streams.char2char.CreoleWriter.CreoleLexema;
import chav1961.purelib.ui.swing.SwingUtils;
import chav1961.purelib.ui.swing.interfaces.OnAction;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent;
import chav1961.purelib.ui.swing.useful.JBackgroundComponent.FillMode;
import chav1961.purelib.ui.swing.useful.JCreoleEditor;

class ContentEditorAndViewer extends JPanel implements LocaleChangeListener {
	private static final long 		serialVersionUID = 1L;
	private static final String		MENU_COPY_LINKS = "ContentEditorAndViewer.menu.copylinks";
	private static final String		MENU_INSERT_LINKS = "ContentEditorAndViewer.menu.insertlinks";
	private static final String		MENU_TOTAL_TREE = "ContentEditorAndViewer.menu.totaltree";
	private static final String		LABEL_NOT_SELECTED = "ContentEditorAndViewer.label.notselected";

	public static enum EditorContentType {
		CREOLE("creole"),
		IMAGE("image"),
		COMMON("common");
		
		private final String	cardName;
		
		private EditorContentType(final String cardName) {
			this.cardName = cardName;
		}
		
		public String getCardName() {
			return cardName;
		}
	}
	
	@FunctionalInterface
	static interface EditorContentSaveCallback {
		void save(EditorContentType contentType, Object content) throws IOException;
	}
	
	private final Localizer					localizer;
	private final LoggerFacade				logger;
	private final ContentMetadataInterface	mdi;
	private final EditorContentSaveCallback	callback;
	private final CardLayout				layout = new CardLayout();
	private final JToolBar					creoleToolBar;
	private final JCreoleEditor				editor = new JCreoleEditor();
	private final JToolBar					imageToolBar;
	private final JBackgroundComponent		image;
	private final JLabel					notSelectedLabel = new JLabel("", JLabel.CENTER);
	
	private EditorContentType						contentType = EditorContentType.CREOLE;
	private JsonNode						navigator = null, parent =  null, current = null;
	private boolean							editorContentChanged = true;
	private boolean							imageContentChanged = true;
	
	public ContentEditorAndViewer(final Localizer localizer, final LoggerFacade logger, final ContentMetadataInterface mdi, final EditorContentSaveCallback callback) throws NullPointerException, LocalizationException {
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

			commonPanel.add(notSelectedLabel, BorderLayout.CENTER);
			
			add(creolePanel, EditorContentType.CREOLE.getCardName());
			add(imagePanel, EditorContentType.IMAGE.getCardName());
			add(commonPanel, EditorContentType.COMMON.getCardName());
			setContenTypeInternal(EditorContentType.COMMON);
			
			editor.getDocument().addDocumentListener(new DocumentListener() {
				@Override public void removeUpdate(DocumentEvent e) {setEditorContentChanged(true);}
				@Override public void insertUpdate(DocumentEvent e) {setEditorContentChanged(true);}
				@Override public void changedUpdate(DocumentEvent e) {setEditorContentChanged(true);}
			});
			editor.addMouseListener(new MouseListener() {
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseExited(MouseEvent e) {}
				@Override public void mouseEntered(MouseEvent e) {}
				
				@Override
				public void mouseClicked(final MouseEvent e) {
					if (e.getButton() == MouseEvent.BUTTON3) {
						try {
							final JPopupMenu	popup = new JPopupMenu();
							final JMenu			copyLinks = new JMenu(localizer.getValue(MENU_COPY_LINKS));
							final JMenu			insertLinks = new JMenu(localizer.getValue(MENU_INSERT_LINKS));
							final JMenu			insertTotal = new JMenu(localizer.getValue(MENU_TOTAL_TREE));
	
							if (!AdminUtils.buildInternalLinksMenu(copyLinks, editor.getText(), current.getChild(AdminUtils.F_ID).getStringValue()+"#"))  {
								copyLinks.setEnabled(false);
							}
							else {
								SwingUtils.assignActionListeners(copyLinks, (ev)->{
									Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(ev.getActionCommand()), null);
								});
							}
							copyLinks.setIcon(new ImageIcon(this.getClass().getResource("/images/locallink.png")));
							popup.add(copyLinks);
	
							if (AdminUtils.buildTreeLinksMenu(insertTotal, navigator, (t)->true)) {
								insertTotal.setIcon(new ImageIcon(this.getClass().getResource("/images/open.png")));
								insertLinks.add(insertTotal);
								insertLinks.addSeparator();
							}
							if (AdminUtils.buildInternalLinksMenu(insertLinks, editor.getText(),"#")) {
								insertLinks.addSeparator();
							}
							AdminUtils.buildSiblingLinksMenu(insertLinks, parent, (t)->true);
							SwingUtils.assignActionListeners(insertLinks, (ev)->{
								try {
									editor.getDocument().insertString(editor.getCaretPosition(), ev.getActionCommand(), editor.getCharacterStyles(CreoleLexema.LinkRef));
								} catch (BadLocationException exc) {
									exc.printStackTrace();
								}
							});
							insertLinks.setIcon(new ImageIcon(this.getClass().getResource("/images/externallink.png")));
							popup.add(insertLinks);
							
							popup.show(editor, e.getX(), e.getY());
						} catch (LocalizationException exc) {
						}
					}
				}
			});
			Toolkit.getDefaultToolkit().getSystemClipboard().addFlavorListener((e)->{
				final Clipboard	cl = (Clipboard)e.getSource();
				final ContentNodeMetadata	imageMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":action:/imagePaste"))[0];
				
				SwingUtils.findComponentByName(imageToolBar, imageMeta.getName()).setEnabled(cl.isDataFlavorAvailable(DataFlavor.imageFlavor));
			});			
			setEditorContentChanged(false);
			setImageContentChanged(false);
			fillLocalizedStrings();
		}
	}

	@Override
	public void localeChanged(Locale oldLocale, Locale newLocale) throws LocalizationException {
		fillLocalizedStrings();
	}
	
	public EditorContentType getContentType() {
		return contentType;
	}
	
	public void setContentType(final EditorContentType contentType, final JsonNode navigator, final JsonNode parent, final JsonNode current) {
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
			switch (contentType) {
				case COMMON	:
					break;
				case CREOLE	:
					setEditorContentChanged(false);
					break;
				case IMAGE	:
					setImageContentChanged(false);
					break;
				default :
					throw new UnsupportedOperationException("Editor content type ["+contentType+"] is not supported yet");
			}
		}
	}

	public boolean creoleContentWasChanged() {
		return editorContentChanged;
	}

	public boolean imageContentWasChanged() {
		return editorContentChanged;
	}

	public JCreoleEditor getCreoleEditor() {
		return editor;
	}
	
	public JBackgroundComponent getImageContainer() {
		return image;
	}
	
	private void setContenTypeInternal(final EditorContentType contentType) {
		this.contentType = contentType;
		layout.show(this, contentType.getCardName());
	}

	@OnAction("action:/creoleClear")
	private void clearCreoleContent() {
		editor.setText("");
	}

	@OnAction("action:/creoleSave")
	void saveCreoleContent() {
		try{callback.save(EditorContentType.CREOLE, editor.getText());
			 setEditorContentChanged(false);
		} catch (IOException e) {
			logger.message(Severity.error, e.getLocalizedMessage(), e);
		}
	}
	
	private void setEditorContentChanged(final boolean newState) {
		if (editorContentChanged != newState) {
			final ContentNodeMetadata	creoleMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":action:/creoleSave"))[0];
			
			SwingUtils.findComponentByName(creoleToolBar, creoleMeta.getName()).setEnabled(newState);
			editorContentChanged = newState;
		}
	}

	private void setImageContentChanged(final boolean newState) {
		if (imageContentChanged != newState) {
			final ContentNodeMetadata	imageMeta = mdi.byApplicationPath(URI.create(ContentMetadataInterface.APPLICATION_SCHEME+":action:/imageSave"))[0];
			
			SwingUtils.findComponentByName(imageToolBar, imageMeta.getName()).setEnabled(newState);
			imageContentChanged = newState;
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

	@OnAction("action:/imageCopy")
	private void copyImage() {
	}
	
	@OnAction("action:/imagePaste")
	private void pasteImage() {
		try{final Image	image = (Image)Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.imageFlavor);
			
			getImageContainer().setBackgroundImage(image);
			setImageContentChanged(true);
		} catch (HeadlessException | UnsupportedFlavorException | IOException e) {
			logger.message(Severity.error, e.getLocalizedMessage());
		}
	}

	@OnAction("action:/imageSave")
	void saveImage() {
		try{callback.save(EditorContentType.IMAGE, image.getBackgroundImage());
			setImageContentChanged(false);
		} catch (IOException e) {
			logger.message(Severity.error, e.getLocalizedMessage(), e);
		}
	}
	
	private void fillLocalizedStrings() throws LocalizationException {
		notSelectedLabel.setText(localizer.getValue(LABEL_NOT_SELECTED));
	}
}
