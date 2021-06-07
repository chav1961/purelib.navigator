package chav1961.purelibnavigator;

import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.net.URI;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JOptionPane;

import chav1961.purelib.basic.exceptions.LocalizationException;
import chav1961.purelib.i18n.interfaces.Localizer;
import chav1961.purelib.model.interfaces.ContentMetadataInterface.ContentNodeMetadata;

public class JGotoURIButton extends JButton {
	private static final long 	serialVersionUID = 1L;

	private final Localizer				localizer;
	private final ContentNodeMetadata	meta;
	
	public JGotoURIButton(final Localizer localizer, final ContentNodeMetadata meta) throws IOException {
		if (localizer == null) {
			throw new NullPointerException("Localizer can't be null");
		}
		else if (meta == null) {
			throw new NullPointerException("Metadata can't be null");
		}
		else if (meta.getApplicationPath() == null) {
			throw new IllegalArgumentException("Metadata must contain application URI");
		}
		else if (meta.getIcon() == null) {
			throw new IllegalArgumentException("Metadata must contain icon URI");
		}
		else {
			final Icon		icon = new ImageIcon(meta.getIcon().toURL());
			final URI		gotoReference = meta.getApplicationPath(); 
			final Dimension	size = new Dimension(icon.getIconWidth()+2,icon.getIconHeight()+2);
			
			this.localizer = localizer;
			this.meta = meta;
			
			setIcon(icon);
			setMinimumSize(size);
			setPreferredSize(size);
			setMaximumSize(size);
			addActionListener((e)->{
				if (Desktop.isDesktopSupported()) {
					try{Desktop.getDesktop().browse(URI.create(gotoReference.getSchemeSpecificPart()));
					} catch (IOException exc) {
						JOptionPane.showMessageDialog(null, "Desktop error: "+exc.getLocalizedMessage());
					}
				}
				else {
					JOptionPane.showMessageDialog(null, "Desktop is not supported");
				}				
			});
		}
	}
	
	@Override
	public String getToolTipText() {
		if (meta.getTooltipId() != null) {
			try{return localizer.getValue(meta.getTooltipId());
			} catch (LocalizationException | IllegalArgumentException e) {
			}
		}
		return super.getToolTipText();
	}
}
