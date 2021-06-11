package chav1961.purelibnavigator;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.image.BufferedImage;
import java.util.Random;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class RandomMeshTest {
	public enum LocatorType {
		LOW(4), HIGH(8);
		
		private final int	initialSize;
		
		LocatorType(final int initialSize) {
			this.initialSize = initialSize;
		}
		
		public int getInitialSize() {
			return initialSize;
		}
	}
	
	public enum Locator {
		LOW1(LocatorType.LOW, '0', Color.RED),
		LOW2(LocatorType.LOW, '1', Color.PINK),
		LOW3(LocatorType.LOW, '2', Color.ORANGE),
		LOW4(LocatorType.LOW, '3', Color.YELLOW),
		LOW5(LocatorType.LOW, '4', Color.GREEN),
		LOW6(LocatorType.LOW, '5', Color.MAGENTA),
		LOW7(LocatorType.LOW, '6', Color.CYAN),
		LOW8(LocatorType.LOW, '7', Color.BLUE),
		HIGH1(LocatorType.HIGH, 'A', Color.RED),
		HIGH2(LocatorType.HIGH, 'B', Color.PINK),
		HIGH3(LocatorType.HIGH, 'C', Color.ORANGE),
		HIGH4(LocatorType.HIGH, 'D', Color.YELLOW),
		HIGH5(LocatorType.HIGH, 'E', Color.GREEN),
		HIGH6(LocatorType.HIGH, 'F', Color.MAGENTA),
		HIGH7(LocatorType.HIGH, 'G', Color.CYAN),
		HIGH8(LocatorType.HIGH, 'H', Color.BLUE);
		
		private final LocatorType	type;
		private final char			symbol;
		private final Color			color;
		
		Locator(final LocatorType type, final char symbol, final Color color) {
			this.type = type;
			this.symbol = symbol;
			this.color = color;
		}
		
		public LocatorType getType() {
			return type;
		}
		
		public char getSymbol() {
			return symbol;
		}
		
		public Color getColor() {
			return color;
		}
	}

	private static final GeneralPath	CELL = new GeneralPath();
	private static final double			HALF = 0.5; 
	private static final double			SQRT_3 = Math.sqrt(3); 
	private static final double			SQRT_3_HALF = SQRT_3 * HALF; 
	
	static {
		CELL.moveTo(-SQRT_3_HALF, HALF);
		CELL.lineTo(0, 1);
		CELL.lineTo(SQRT_3_HALF, HALF);
		CELL.lineTo(SQRT_3_HALF, -HALF);
		CELL.lineTo(0, -1);
		CELL.lineTo(-SQRT_3_HALF, -HALF);
		CELL.closePath();
	}
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		final int			width = 24, height = 20, cellSize = 16;
		final long			startRand = 648598753997723648L;//(long) (Long.MAX_VALUE * Math.random());
		final Random		rand = new Random(startRand);
		final Locator[][]	locations = allocateRandom(rand, width, height);
		final Cell[][]		cells = allocateCells(locations);
		final BufferedImage	bi = new BufferedImage((int)(width * cellSize * SQRT_3), (int)(height * cellSize), BufferedImage.TYPE_INT_RGB);
		final Graphics2D	g2d = (Graphics2D)bi.getGraphics();
		
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				final Cell	c = cells[x][y]; 
				
				if (c != null) {
					final AffineTransform	at = new AffineTransform();
					
					at.translate((x  + (y % 2 == 0 ? 0 : HALF)) * cellSize * SQRT_3, y * cellSize);
					at.scale(cellSize * SQRT_3_HALF, cellSize * SQRT_3_HALF);
					g2d.setColor(new Color(c.locator.getColor().getRGB() & (c.locator.getType() == LocatorType.HIGH ? Color.WHITE : Color.GRAY).getRGB()));
					g2d.fill(CELL.createTransformedShape(at));
				}
			}
		}
		final JLabel	label = new JLabel(new ImageIcon(bi));
		
		JOptionPane.showMessageDialog(null, label);
	}

	private static Locator[][] allocateRandom(final Random rand, final int width, final int height) {
		final Locator[][]	locations = new Locator[width][height];
		
		for (Locator item : Locator.values()) {
			if (item.getType() == LocatorType.HIGH) {
				for (;;) {
					int	x = (int) (rand.nextDouble() * width);
					int	y = (int) (rand.nextDouble() * height);
					
					if (locations[x][y] == null) {
						if (fill(rand, locations, x, y, item.getType().getInitialSize(), item, 0)) {
							break;
						}
					}
				}
			}
		}
		
		for (Locator item : Locator.values()) {
			if (item.getType() == LocatorType.LOW) {
				for (;;) {
					int	x = (int) (rand.nextDouble() * width);
					int	y = (int) (rand.nextDouble() * height);
					
					if (locations[x][y] == null) {
						if (fill(rand, locations, x, y, item.getType().getInitialSize(), item, 0)) {
							break;
						}
					}
				}
			}
		}
		
		return locations;
	}
	
	private static final int[][] 	dirs = {{-1,-1}, {0,-1}, {1,-1}, {1,0}, {1,1}, {0,1}, {-1,1}, {-1,0}};
	
	private static boolean fill(final Random rand, final Locator[][] locations, final int x, final int y, final int count, final Locator id, final int lastDir) {
		if (x < 0 || x >= locations.length || y < 0 || y >= locations[x].length || locations[x][y] != null) {
			return false;
		}
		else if (count == 0) {
			return true;
		}
		else {
			final boolean[]	passed = new boolean[8];
			passed[lastDir] = true;
			
			locations[x][y] = id;
			do {
				final int dir = (int) (8 * rand.nextDouble());
				
				if (!passed[dir]) {
					passed[dir] = true;
					if (fill(rand, locations, x+dirs[dir][0], y+dirs[dir][1], count-1, id, dir)) {
						return true;
					}
				}
			} while (!passed[0] || !passed[1] || !passed[2] || !passed[3] || !passed[4] || !passed[5] || !passed[6] || !passed[7]);
			
			locations[x][y] = null;
			return false;
		}
	}
	
	private static Cell[][] allocateCells(final Locator[][] locators) {
		final Cell[][]	cells = new Cell[locators.length][locators[0].length];
		
		for (int x = 0, maxX = cells.length; x < maxX; x++) {
			for (int y = 0, maxY = cells[x].length; y < maxY; y++) {
				if (locators[x][y] != null) {
					cells[x][y] = new Cell(locators[x][y]); 
				}
			}
		}
		return cells;
	}
	
	private static class Cell {
		private final int		cellSize;
		private final Locator	locator;
		
		public Cell() {
			this(7);
		}
		
		public Cell(final int cellSize) {
			this.cellSize = cellSize;
			this.locator = null;
		}

		public Cell(final Locator locator) {
			this(7,locator);
		}
		
		public Cell(final int cellSize,final Locator locator) {
			this.cellSize = cellSize;
			this.locator = locator;
		}
	}
}
