package laf;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JComponent;
import javax.swing.plaf.ComponentUI;
import javax.swing.plaf.basic.BasicLabelUI;

public class MyLabelUI extends BasicLabelUI {
	private static MyLabelUI instance = new MyLabelUI();

	public static ComponentUI createUI ( JComponent c ) {
		return instance;
	}	

	public void installUI( JComponent c ) {
		super.installUI (c);
	}

	public void paint ( Graphics g, JComponent c ) {
		Graphics2D g2d = ( Graphics2D ) g;
		
		super.paint(g, c);
		g2d.setPaint(Color.GRAY);
		g2d.drawRoundRect ( 0, 0, c.getWidth () - 1, c.getHeight () - 1, 6, 6 );
	}
}
