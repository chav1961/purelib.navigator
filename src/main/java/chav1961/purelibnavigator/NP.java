package chav1961.purelibnavigator;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.PreparationException;

public class NP extends JComponent {
	private static final long 			serialVersionUID = 1L;
	private static final String			TEX_NAME = "", ORD1_NAME = "", ORD2_NAME = "";
	private static final String			NOTE_REPO_NAME = "";
	private static final Image			TEX, ORD_1, ORD_2;
	private static final int			MAX_NOTES = 10;
	private static final NoteRepository	REPO;
	
	static {
		try{TEX = ImageIO.read(NP.class.getResource(TEX_NAME));
			ORD_1 = ImageIO.read(NP.class.getResource(ORD1_NAME));
			ORD_2 = ImageIO.read(NP.class.getResource(ORD2_NAME));
			REPO = new NoteRepository(NP.class.getResourceAsStream(NOTE_REPO_NAME));
		} catch (IOException e) {
			throw new PreparationException("Image ["+TEX_NAME+"] can't be loaded");
		} 
	}

	private final List<Note>	notes = new ArrayList<>();
	private final Font			captionFont = new Font("Courier",Font.PLAIN,1);
	private final Font			bodyFont = new Font("Courier",Font.PLAIN,1);
	private final Font			headerFont = new Font("Courier",Font.PLAIN,1);
	
	public NP() {
		
	}
	
	public void clearNotifications() {
		notes.clear();
	}

	public void notify(final String caption, final String body) {
		notes.add(new Note(caption, body));
	}
	
	public void appendScratchNotifications() {
		while (notes.size() < MAX_NOTES) {
			final int	random = (int) (REPO.getSize()*Math.random());
			
			notes.add(REPO.getNote(random));
		}
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();

		pickCoordinates(g2d);
		paintBackground(g2d);
		paintTopBar(g2d);
		paintColumn(g2d, notes);
		g2d.setTransform(oldAt);
	}

	private void pickCoordinates(final Graphics2D g2d) {
		// TODO Auto-generated method stub
		
	}

	private void paintBackground(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform();
		
		g2d.drawImage(TEX, at, null);
	}

	private void paintTopBar(final Graphics2D g2d) {
		final AffineTransform	oldAt = new AffineTransform();
		final AffineTransform	at = new AffineTransform();
		final AffineTransform	atText = new AffineTransform(oldAt);
		final Font				oldFont = g2d.getFont();
		
		g2d.drawImage(ORD_1, at, null);
		g2d.drawImage(ORD_2, at, null);
		g2d.setTransform(atText);
		g2d.setFont(headerFont);
		g2d.drawString(REPO.getCaption(), MAX_NOTES, MAX_NOTES);
		g2d.setFont(oldFont);
	}

	private void paintColumn(final Graphics2D g2d, final Iterable<Note> columns) {
		final Font				oldFont = g2d.getFont();
		
		for (Note item : columns) {
			final String	caption = item.getCaption();
			final String	body = insertNL(item.getBody(),g2d);
			
			g2d.setFont(captionFont);
			g2d.drawString(caption, MAX_NOTES, MAX_NOTES);
			g2d.setFont(bodyFont);
			g2d.drawString(body, MAX_NOTES, MAX_NOTES);
		}
		g2d.setFont(oldFont);
	} 
	
	private String insertNL(final String body, final Graphics2D g2d) {
		// TODO Auto-generated method stub
		
		return body;
	}

	private static class Note {
		private final String	caption;
		private final String 	body;
		
		public Note(final String caption, final String body) {
			this.caption = caption;
			this.body = body;
		}

		public String getCaption() {
			return caption;
		}

		public String getBody() {
			return body;
		}

		@Override
		public String toString() {
			return "Note [caption=" + caption + ", body=" + body + "]";
		}
	}
	
	private static class NoteRepository {
		private final List<Note>	notes = new ArrayList<>();
		
		public NoteRepository(final InputStream repoContent) {
			// TODO Auto-generated constructor stub
		}

		public int getSize() {
			return notes.size();
		}

		public Note getNote(final int noteIndex) {
			return notes.get(noteIndex);
		}

		public String getCaption() {
			return null;
		}
		
		@Override
		public String toString() {
			return "NoteRepository [notes=" + notes + "]";
		}
	}
}
