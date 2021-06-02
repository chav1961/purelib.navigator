package chav1961.purelibnavigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.im.InputMethodHighlight;
import java.io.IOException;
import java.io.InputStream;
import java.text.AttributedCharacterIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;
import javax.swing.text.StyleConstants;
import javax.swing.text.AttributeSet.ParagraphAttribute;

import chav1961.purelib.basic.exceptions.PreparationException;

public class NP extends JComponent {
	private static final long 			serialVersionUID = 1L;
	private static final String			TEX_NAME = "background.png", ORD1_NAME = "", ORD2_NAME = "", H_BAR_NAME = "";
	private static final String			NOTE_REPO_NAME = "";
	private static final Image			TEX, ORD_1, ORD_2, H_BAR;
	private static final int			MAX_NOTES = 10;
	private static final NoteRepository	REPO;
	
	private static final float			PAGE_WIDTH = 100;
	private static final float			PAGE_LEFT_LINE = 3;
	private static final float			PAGE_RIGHT_LINE = 97;
	private static final float			PAGE_TOP_LINE = 12;
	private static final float			PAGE_UPPER_LINE = 13;
	private static final float			PAGE_LOWER_LINE = 96;
	private static final float			PAGE_LEFT_X_GAP = 1;
	private static final float			PAGE_RIGHT_X_GAP = 1;
	private static final float			PAGE_TOP_Y_GAP = 1;
	private static final float			PAGE_BOTTOM_Y_GAP = 3;
	private static final float			PAGE_INNER_X_GAP = 1;
	private static final float			PAGE_INNER_Y_GAP = 1;
	private static final int			PAGE_COLUMNS = 3;
	private static final Color 			PAGE_COLOR = new Color(132,78,24);
	private static final float			MESH_WIDTH = 0.25f;
	private static final Stroke			MESH_STROKE = new BasicStroke(MESH_WIDTH);
	private static final Font			CAPTION_FONT = new Font("Times New Roman",Font.BOLD,6); 
	private static final Font			SEASON_FONT = new Font("Times New Roman",Font.PLAIN,2); 
	
	
	static {
		try{TEX = ImageIO.read(NP.class.getResource(TEX_NAME));
			ORD_1 = ImageIO.read(NP.class.getResource(ORD1_NAME));
			ORD_2 = ImageIO.read(NP.class.getResource(ORD2_NAME));
			H_BAR = ImageIO.read(NP.class.getResource(H_BAR_NAME));
			
			try(final InputStream	is = NP.class.getResourceAsStream(NOTE_REPO_NAME)) {
				REPO = new NoteRepository(is);
			}
			
		} catch (IOException e) {
			throw new PreparationException("Image ["+TEX_NAME+"] can't be loaded");
		} 
	}

	public enum Quarter {
		WINTER("Çèìà"), 
		SPRING("Âåñíà"), 
		SUMMER("Ëåòî"), 
		AUTUMN("Îñåíü");
		
		private final String	seasonName;
		
		private Quarter(final String seasonName) {
			this.seasonName = seasonName;
		}
		
		public String getSeasonName() {
			return seasonName;
		}
	}
	
	public enum NoteType {
		All
	}
	
	private final List<Note>	notes = new ArrayList<>();
	private final List<AttributedCharacterIterator>	toDraw = new ArrayList<>();
	private int					year = 1800;
	private Quarter				quarter = Quarter.SPRING;
	
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
		for (Note note : notes) {
			toDraw.add(new NoteItem(note.getCaption(),true));
			toDraw.add(new NoteItem(note.getBody(),false));
		}
		repaint();
	}
	
	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D		g2d = (Graphics2D)g;
		final AffineTransform	oldAt = g2d.getTransform();
		final AffineTransform	newAt = pickCoordinates(g2d);

		g2d.setTransform(newAt);
		paintBackground(g2d);
		paintMeshAndCaption(g2d);
		
		if (toDraw != null) {
			paintColumn(g2d, toDraw);
		}
		g2d.setTransform(oldAt);
	}

	private float getYSize() {
		return 1.0f * getHeight() / getWidth();
	}
	
	private AffineTransform pickCoordinates(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform(g2d.getTransform());
		final float				kx = 1.0f * getWidth() / PAGE_WIDTH;
		final float				ky = getYSize();

		at.scale(kx, kx);
		return at;
	}

	private void paintBackground(final Graphics2D g2d) {
		final AffineTransform	at = new AffineTransform();
		final int				w = TEX.getWidth(null), h = TEX.getHeight(null);
		final float				kx = PAGE_WIDTH / w, ky = PAGE_WIDTH * getYSize() / h;
		
		at.scale(kx, ky);
		g2d.drawImage(TEX, at, null);
	}

	private void paintMeshAndCaption(final Graphics2D g2d) {
		final Color			oldColor = g2d.getColor();
		final Stroke		oldStroke = g2d.getStroke();
		final String		caption = REPO.getCaption();
		final String		season = currentQuarter().getSeasonName()+" "+currentYear()+" ã.";
		final GeneralPath	path = new GeneralPath();
		final TextLayout	captionLayout = new TextLayout(caption, CAPTION_FONT, g2d.getFontRenderContext());
		final TextLayout	seasonLayout = new TextLayout(season, SEASON_FONT, g2d.getFontRenderContext());
		final float			columnWidth = PAGE_WIDTH / PAGE_COLUMNS;
		
		path.moveTo(PAGE_LEFT_LINE, PAGE_TOP_LINE);		
		path.lineTo(PAGE_RIGHT_LINE, PAGE_TOP_LINE);
		for (int index = 1; index < PAGE_COLUMNS; index++) {
			path.moveTo(index * columnWidth, PAGE_UPPER_LINE);
			path.lineTo(index * columnWidth, PAGE_LOWER_LINE * getYSize());
		}
		
		g2d.setColor(PAGE_COLOR);
		g2d.setStroke(MESH_STROKE);
		g2d.draw(path);
		captionLayout.draw(g2d, 8, 10);
		seasonLayout.draw(g2d, 87, 11.5f);
		g2d.setStroke(oldStroke);
		g2d.setColor(oldColor);
	}
	
	private void paintColumn(final Graphics2D g2d, final Iterable<AttributedCharacterIterator> iterableText) {
	    final float		width = PAGE_WIDTH;
	    final float 	height = width * getHeight() / getWidth();
	    final float 	colWidth = width/PAGE_COLUMNS;

	    float	yLine = PAGE_UPPER_LINE + PAGE_TOP_Y_GAP, xStart = PAGE_LEFT_LINE + PAGE_LEFT_X_GAP;
	    int		colNo = 0;

all:	for (AttributedCharacterIterator columns : iterableText) {
		    final LineBreakMeasurer	lbm = new LineBreakMeasurer(columns, g2d.getFontRenderContext());
		    final int 	start = columns.getBeginIndex();
		    final int 	end = columns.getEndIndex();
		    final float	currentWidth = (colNo+1)*colWidth - xStart - (colNo == PAGE_COLUMNS - 1 ? PAGE_RIGHT_X_GAP : PAGE_INNER_X_GAP); 
		    
		    lbm.setPosition(start);
		    while (lbm.getPosition() < end) {
		        while (lbm.getPosition() < end && yLine < height - PAGE_BOTTOM_Y_GAP) {
				    final TextLayout 	textLayout = lbm.nextLayout(currentWidth);
				    
				    yLine += textLayout.getAscent();
				    textLayout.draw(g2d, xStart, yLine);
					yLine += textLayout.getDescent() + textLayout.getLeading();
		        }
		        if (yLine >= height - PAGE_BOTTOM_Y_GAP) {
			        if (++colNo >= PAGE_COLUMNS) {
			        	break all;
			        }
			        else {
				        xStart = colNo * colWidth + PAGE_INNER_X_GAP;
				        yLine = PAGE_UPPER_LINE + PAGE_TOP_Y_GAP;
			        }
		        }
		        else {
		        	yLine += PAGE_INNER_Y_GAP;
		        }
		    }
	    }
	} 

	public static void main(final String[] args) {
		final NP	np = new NP();
		
		np.setPreferredSize(new Dimension(1024,768));
		np.setBorder(new LineBorder(Color.RED));
		np.startNotifications(1800, Quarter.AUTUMN);
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.notify("Caption1","Body1 dklfjklsdjfkl  sdfjkljsdfkjsdfkl   sdfkljsdfj   sdfklsdklfjksjdfkjsdfkl  sdflsdkljfsdjfkljsdfklj");
		np.notify("Caption2","Body2");
		np.complete();
		
		JOptionPane.showMessageDialog(null, np);
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

	private static class NoteItem implements AttributedCharacterIterator {
		private static final Attribute[]					CAPTION_KEYS = {TextAttribute.SIZE, 
																			TextAttribute.FAMILY, 
																			TextAttribute.WEIGHT,
																			TextAttribute.FOREGROUND}; 
		private static final Map<Attribute, Object>			CAPTION_ATTR = new HashMap<>(); 
		private static final Attribute[]					BODY_KEYS = {TextAttribute.SIZE, 
																			TextAttribute.FAMILY,
																			TextAttribute.WEIGHT,
																			TextAttribute.FOREGROUND};
		private static final Map<Attribute, Object>			BODY_ATTR = new HashMap<>();
		private static final Set<Attribute>					ALL_ATTR; 
		
		static {
			final Set<Attribute>	temp = new HashSet<>();
			
			temp.addAll(Arrays.asList(CAPTION_KEYS));
			temp.addAll(Arrays.asList(BODY_KEYS));
			ALL_ATTR = Collections.unmodifiableSet(temp);

			CAPTION_ATTR.put(TextAttribute.SIZE, 2f);
			CAPTION_ATTR.put(TextAttribute.FAMILY, "Times New Roman");
			CAPTION_ATTR.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_ULTRABOLD);
			CAPTION_ATTR.put(TextAttribute.FOREGROUND, PAGE_COLOR);

			BODY_ATTR.put(TextAttribute.SIZE, 1.6f);
			BODY_ATTR.put(TextAttribute.FAMILY, "Times New Roman");
			BODY_ATTR.put(TextAttribute.WEIGHT, TextAttribute.WEIGHT_SEMIBOLD);
			BODY_ATTR.put(TextAttribute.FOREGROUND, PAGE_COLOR);
		}
		
		private final char[]				content;
		private final Map<Attribute, Object>[]	attributes;
		private final int[][]				attrRanges;
		private int							currentPos = 0;
		
		public NoteItem(final String note, final boolean isCaption) {
			if (note == null) {
				throw new IllegalArgumentException("Noted list can't be null or empty"); 
			}
			else {
				final List<Map<Attribute, Object>>	attrs = new ArrayList<>();
				final List<int[]>		ranges = new ArrayList<>();
				
				if (isCaption) {
					attrs.add(CAPTION_ATTR);
					ranges.add(new int[] {0,note.length() - 1});
				}
				else {
					attrs.add(BODY_ATTR);
					ranges.add(new int[] {0,note.length() - 1});
				}
				this.attributes = attrs.toArray(new Map[attrs.size()]);
				this.attrRanges = ranges.toArray(new int[ranges.size()][]);
				this.content = note.toCharArray();
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
			return attrRanges[getRange(getIndex())][0];
		}

		@Override
		public int getRunStart(final Attribute attribute) {
			for (int index = getRange(getIndex()); index >= 0; index--) {
				if (!attributes[index].containsKey(attribute)) {
					return index + 1;
				}
			}
			return attrRanges[0][0];
		}

		@Override
		public int getRunStart(final Set<? extends Attribute> attr) {
			for (int index = getRange(getIndex()); index >= 0; index--) {
				if (!attributes[index].keySet().contains(attr)) {
					return attrRanges[index + 1][0];
				}
			}
			return attrRanges[0][0];
		}

		@Override
		public int getRunLimit() {
			return attrRanges[getRange(getIndex())][1]+1;
		}

		@Override
		public int getRunLimit(final Attribute attribute) {
			for (int index = getRange(getIndex()); index < attributes.length; index++) {
				if (!attributes[index].containsKey(attribute)) {
					return attrRanges[index - 1][1]+1;
				}
			}
			return attrRanges[attributes.length-1][1];
		}

		@Override
		public int getRunLimit(final Set<? extends Attribute> attr) {
			for (int index = getRange(getIndex()); index < attributes.length; index++) {
				if (!attributes[index].keySet().contains(attr)) {
					return attrRanges[index - 1][1];
				}
			}
			return attrRanges[attributes.length-1][1];
		}

		@Override
		public Map<Attribute, Object> getAttributes() {
			final int	current = getIndex();
			
			for (int index = 0; index < attrRanges.length; index++) {
				if (current >= attrRanges[index][0] && current <= attrRanges[index][1]) {
					return attributes[index];
				}
			}
			return null;
		}

		@Override
		public Object getAttribute(final Attribute attribute) {
			return attributes[getRange(getIndex())].get(attribute);
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
			return "ÈÌÏÅÐÑÊÀß ÏÐÀÂÄÀ";
		}

		public int getMinYear() {
			return 1800;
		}

		public int getMaxYear() {
			return 1917;
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
