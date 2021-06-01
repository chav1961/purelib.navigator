package chav1961.purelibnavigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;

import chav1961.purelib.basic.exceptions.PreparationException;

public class NP extends JComponent {
	private static final long 			serialVersionUID = 1L;
	private static final String			TEX_NAME = "", ORD1_NAME = "", ORD2_NAME = "", H_BAR_NAME = "";
	private static final String			NOTE_REPO_NAME = "";
	private static final Image			TEX, ORD_1, ORD_2, H_BAR;
	private static final int			MAX_NOTES = 10;
	private static final NoteRepository	REPO;
	
	private static final float			SCREEN_WIDTH = 100;
	private static final int			SCREEN_COL = 3;
	private static final float[][]		MESH_LIST = {{0f,0f,0f,0f}};
	private static final Color 			MESH_COLOR = Color.BLACK;
	private static final float			MESH_WIDTH = 0.5f;
	private static final Stroke			MESH_STROKE = new BasicStroke(MESH_WIDTH);
	private static final GeneralPath	MESH_PATH = new GeneralPath();
	
	
	static {
		try{TEX = ImageIO.read(NP.class.getResource(TEX_NAME));
			ORD_1 = ImageIO.read(NP.class.getResource(ORD1_NAME));
			ORD_2 = ImageIO.read(NP.class.getResource(ORD2_NAME));
			H_BAR = ImageIO.read(NP.class.getResource(H_BAR_NAME));
			
			try(final InputStream	is = NP.class.getResourceAsStream(NOTE_REPO_NAME)) {
				REPO = new NoteRepository(is);
			}
			
			for (float[] item : MESH_LIST) {
				MESH_PATH.moveTo(item[0], item[1]);
				MESH_PATH.lineTo(item[2], item[3]);
			}
		} catch (IOException e) {
			throw new PreparationException("Image ["+TEX_NAME+"] can't be loaded");
		} 
	}

	public enum Quarter {
		Q1, Q2, Q3, Q4
	}
	
	public enum NoteType {
		All
	}
	
	private final List<Note>	notes = new ArrayList<>();
	private final Font			headerFont = new Font("Courier",Font.PLAIN,1);
	private int					year = 0;
	private Quarter				quarter = Quarter.Q1;
	private Notes				toDraw = null;
	
	public NP() {
		
	}
	
	public int currentYear() {
		return year;
	}
	
	public Quarter currentQuarter() {
		return quarter;
	}
	
	public void startNotifications(final int year, final Quarter quarter) {
		if (year < REPO.getMinYear() || year > REPO.getMaxYear()) {
			throw new IllegalArgumentException("Year ["+year+"] must be in range "+REPO.getMinYear()+".."+REPO.getMaxYear()); 
		}
		else if (quarter == null) {
			throw new NullPointerException("Quarter can't be null"); 
		}
		else {
			notes.clear();
			this.year = year;
			this.quarter = quarter;
		}
	}

	public void notify(final String caption, final String body) {
		if (caption == null || caption.isEmpty()) {
			throw new IllegalArgumentException("Note caption can't be null or empty"); 
		}
		else if (body == null || body.isEmpty()) {
			throw new IllegalArgumentException("Note body can't be null or empty"); 
		}
		else {
			notify(new Note(caption, body));
		}
	}

	public void notify(final Note note) {
		if (note == null) {
			throw new NullPointerException("Note can't be null"); 
		}
		else {
			notes.add(note);
		}
	}
	
	public void appendScratchNotifications() {
		while (notes.size() < MAX_NOTES) {
			final int	index = (int) (REPO.getSize(currentYear(), currentQuarter()) * Math.random()); 
			
			notes.add(REPO.getNote(currentYear(), currentQuarter(), index));
		}
	}

	public void complete() {
		toDraw = new Notes(notes);
		repaint();
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();

		pickCoordinates(g2d);
		paintBackground(g2d);
		paintTopBar(g2d);
		if (toDraw != null) {
			paintMesh(g2d);
			paintColumn(g2d, toDraw);
		}
		g2d.setTransform(oldAt);
	}

	private void pickCoordinates(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform();
		final float				kx = 1.0f * getWidth() / SCREEN_WIDTH;
		final float				ky = 1.0f * getHeight() / getWidth();
		
		at.scale(kx, kx*ky);
		g2d.setTransform(at);
	}

	private void paintBackground(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform();
		
		g2d.drawImage(TEX, at, null);
	}

	private void paintMesh(final Graphics2D g2d) {
		final Color		oldColor = g2d.getColor();
		final Stroke	oldStroke = g2d.getStroke();
		
		g2d.setColor(MESH_COLOR);
		g2d.setStroke(MESH_STROKE);
		g2d.draw(MESH_PATH);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
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

	private void paintColumn(final Graphics2D g2d, final AttributedCharacterIterator columns) {
	    final float		width = SCREEN_WIDTH;
	    final float 	height = width * getHeight() / getWidth();
	    final float 	colWidth = width/SCREEN_COL;
	    final LineBreakMeasurer	lbm = new LineBreakMeasurer(columns, g2d.getFontRenderContext());
	    final int 		start = columns.getBeginIndex();
	    final int 		end = columns.getEndIndex();
		
	    float  			Y = 0, Xstart = 0;
		
	    lbm.setPosition(start);
	    while (lbm.getPosition() < end) {
	        while (lbm.getPosition() < end && Y < height) {
			    final TextLayout 	textLayout = lbm.nextLayout(colWidth);
			    
			    Y += textLayout.getAscent();
			    textLayout.draw(g2d, Xstart, Y);
				Y += textLayout.getDescent() + textLayout.getLeading();
	        }
	        Xstart += colWidth;
	        Y = 0;
	    }
	} 
	
	private static class Note {
		private final String	caption;
		private final String 	body;
		
		public Note(final String caption, final String body) {
			this.caption = caption.endsWith("\n") ? caption : caption+'\n';
			this.body = body.endsWith("\n") ? body : body+'\n';
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
		
		public static Note getTypedNote(final NoteType type, final Object... parameters) {
			if (type == null) {
				throw new NullPointerException("Note type can't be null");
			}
			else {
				switch (type) {
					case All:
						break;
					default:
						throw new UnsupportedOperationException("Note type ["+type+"] is not supported yet");
				}
				return null;
			}
		}
	}

	private static class Notes implements AttributedCharacterIterator {
		private static final Attribute[]					CAPTION_KEYS = {}; 
		private static final Map.Entry<Attribute, Object>[]	CAPTION_ATTR = null; 
		private static final Attribute[]					BODY_KEYS = {}; 
		private static final Map.Entry<Attribute, Object>[]	BODY_ATTR = null;
		private static final Set<Attribute>					ALL_ATTR; 
		
		static {
			final Set<Attribute>	temp = new HashSet<>();
			
			temp.addAll(Arrays.asList(CAPTION_KEYS));
			temp.addAll(Arrays.asList(BODY_KEYS));
			ALL_ATTR = Collections.unmodifiableSet(temp);
		}
		
		private final char[]				content;
		private final Map<Attribute, Object>[]	attributes;
		private final int[][]				attrRanges;
		private int							currentPos = 0;
		
		public Notes(final List<Note> notes) {
			if (notes == null || notes.isEmpty()) {
				throw new IllegalArgumentException("Noted list can't be null or empty"); 
			}
			else {
				final StringBuilder		sb = new StringBuilder();
				final List<Map.Entry<Attribute, Object>[]>	attrs = new ArrayList<>();
				final List<int[]>		ranges = new ArrayList<>();
				
				for (Note item : notes) {
					attrs.add(CAPTION_ATTR);
					ranges.add(new int[] {sb.length(),sb.length() + item.getCaption().length()});
					sb.append(item.getCaption());
					attrs.add(BODY_ATTR);
					ranges.add(new int[] {sb.length(),sb.length() + item.getBody().length()});
					sb.append(item.getBody());
				}
				this.attributes = attrs.toArray(new Map[attrs.size()]);
				this.attrRanges = ranges.toArray(new int[ranges.size()][]);
				this.content = new char[sb.length()];
				sb.getChars(0, this.content.length, this.content, 0);
			}
		}
		
		@Override
		public Object clone() {
			try{return super.clone();
			} catch (CloneNotSupportedException e) {
				return this;
			}			
		}
		
		@Override
		public char first() {
			setIndex(getBeginIndex());
			
			if (inside()) {
				return current();
			}
			else {
				return DONE;
			}
		}

		@Override
		public char last() {
			setIndex(getEndIndex());
			
			if (inside()) {
				return current();
			}
			else {
				return DONE;
			}
		}

		@Override
		public char current() {
			if (inside()) {
				return content[getIndex()];
			}
			else {
				return DONE;
			}
		}

		@Override
		public char next() {
			if (getIndex() >= getEndIndex()) {
				return DONE;
			}
			else {
				setIndex(getIndex()+1);
				return current();
			}
		}

		@Override
		public char previous() {
			if (getIndex() <= getBeginIndex()) {
				return DONE;
			}
			else {
				setIndex(getIndex()-1);
				return current();
			}
		}

		@Override
		public char setIndex(final int position) {
			currentPos = position;
			return current();
		}

		@Override
		public int getBeginIndex() {
			return 0;
		}

		@Override
		public int getEndIndex() {
			return content.length-1;
		}

		@Override
		public int getIndex() {
			return currentPos;
		}

		@Override
		public int getRunStart() {
			return attrRanges[getRange(current())][0];
		}

		@Override
		public int getRunStart(final Attribute attribute) {
			for (int index = getRange(current()); index >= 0; index--) {
				if (!attributes[index].containsKey(attribute)) {
					return index + 1;
				}
			}
			return attrRanges[0][0];
		}

		@Override
		public int getRunStart(final Set<? extends Attribute> attr) {
			for (int index = getRange(current()); index >= 0; index--) {
				if (!attributes[index].keySet().contains(attr)) {
					return attrRanges[index + 1][0];
				}
			}
			return attrRanges[0][0];
		}

		@Override
		public int getRunLimit() {
			return attrRanges[getRange(current())][1];
		}

		@Override
		public int getRunLimit(final Attribute attribute) {
			for (int index = getRange(current()); index < attributes.length; index++) {
				if (!attributes[index].containsKey(attribute)) {
					return attrRanges[index - 1][1];
				}
			}
			return attrRanges[attributes.length-1][1];
		}

		@Override
		public int getRunLimit(final Set<? extends Attribute> attr) {
			for (int index = getRange(current()); index < attributes.length; index++) {
				if (!attributes[index].keySet().contains(attr)) {
					return attrRanges[index - 1][1];
				}
			}
			return attrRanges[attributes.length-1][1];
		}

		@Override
		public Map<Attribute, Object> getAttributes() {
			final int	current = current();
			
			for (int index = 0; index < attrRanges.length; index++) {
				if (current >= attrRanges[index][0] && current <= attrRanges[index][1]) {
					return attributes[index];
				}
			}
			return null;
		}

		@Override
		public Object getAttribute(final Attribute attribute) {
			return attributes[getRange(current())].get(attribute);
		}

		@Override
		public Set<Attribute> getAllAttributeKeys() {
			return ALL_ATTR;
		}
		
		private boolean inside() {
			return getIndex() >= getBeginIndex() && getIndex() < getEndIndex();
		}
		
		private int getRange(final int index) {
			for (int r = 0, maxR = attrRanges.length; r < maxR; r++) {
				if (index >= attrRanges[r][0] && index <= attrRanges[r][1]) {
					return r;
				}
			}
			return 0;
		}
	}
	
	private static class NoteRepository {
		private final NoteRecord[]	notes;
		
		public NoteRepository(final InputStream repoContent) {
			// TODO Auto-generated constructor stub
			this.notes = null;
		}

		public int getSize(final int year, final Quarter quarter) {
			for (NoteRecord item : notes) {
				if (item.year == year && item.quarter == quarter) {
					return item.notes.length;
				}
			}
			return 0;
		}
		
		public Note getNote(final int year, final Quarter quarter, final int noteIndex) {
			for (NoteRecord item : notes) {
				if (item.year == year && item.quarter == quarter) {
					return item.notes[noteIndex];
				}
			}
			return null;
		}

		public String getCaption() {
			return null;
		}

		public int getMinYear() {
			return 0;
		}

		public int getMaxYear() {
			return 0;
		}
		
		@Override
		public String toString() {
			return "NoteRepository [notes=" + Arrays.toString(notes) + "]";
		}
		
		private static class NoteRecord {
			final int		year;
			final Quarter	quarter;
			final Note[]	notes;
			
			private NoteRecord(final int year, final Quarter quarter, final Note[] notes) {
				this.year = year;
				this.quarter = quarter;
				this.notes = notes;
			}

			@Override
			public String toString() {
				return "NoteRecord [year=" + year + ", quarter=" + quarter + ", notes=" + Arrays.toString(notes) + "]";
			}
		}
	}
}
