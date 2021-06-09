package chav1961.purelibnavigator;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.PathIterator;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import chav1961.purelib.ui.swing.SwingUtils;

public class ContourTest extends JComponent {
	private static final long 			serialVersionUID = 1L;
	private static final BufferedImage	IMAGE;

	static {
		try{IMAGE = ImageIO.read(ContourTest.class.getResource("pic.png"));
		} catch (IOException e) {
			throw new RuntimeException();
		}
	}
	
	private final GeneralPath	gp;
	
	public ContourTest() {
		gp = SwingUtils.buildContour(IMAGE, Color.WHITE);
		setPreferredSize(new Dimension(IMAGE.getWidth(null), IMAGE.getHeight(null)));
		setBorder(new LineBorder(Color.RED));
	}
	
	public static void main(String[] args) {
		final ContourTest	ct = new ContourTest();
		
		JOptionPane.showMessageDialog(null, ct);
	}

	@Override
	protected void paintComponent(final Graphics g) {
		final Graphics2D	g2d = (Graphics2D)g;
		
		g2d.setStroke(new BasicStroke(3));
		g2d.setColor(Color.BLUE);
		g2d.drawImage(IMAGE, 0, 0, null);
		g2d.draw(gp);
	}
}
