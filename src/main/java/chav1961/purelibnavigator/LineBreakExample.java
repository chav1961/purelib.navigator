package chav1961.purelibnavigator;

import java.awt.*;
import java.text.*;
import javax.swing.*;
import java.awt.event.*;
import java.awt.font.*;
import java.util.Hashtable;

public class LineBreakExample extends JPanel {
  private LineBreakMeasurer lineBreakMeasurer;
 private int start, end;
  private static Hashtable hash = new Hashtable();
  
  private static AttributedString attributedString = new AttributedString(
       "Java is an Object Oriented Programming Language which has "
      + " an extensive class library available in the core language packages. Java "
      + "was designed with networking in mind and comes with many classes "
	  +" to develop sophisticated Internet communications. ",
      hash);
  public LineBreakExample() {
    AttributedCharacterIterator attributedCharacterIterator = attributedString.getIterator();
    start = attributedCharacterIterator.getBeginIndex();
    end = attributedCharacterIterator.getEndIndex();
    lineBreakMeasurer = new LineBreakMeasurer(attributedCharacterIterator,
     new FontRenderContext(null, false, false));
  }
  public void paintComponent(Graphics g) {
	Graphics2D graphics2D = (Graphics2D) g;
    Dimension size = getSize();
    float width = (float) size.width;
    float height = (float) size.height;
    float width2 = width/2;
	float  X, Y = 0, Xstart = 0;
    lineBreakMeasurer.setPosition(start);
    while (lineBreakMeasurer.getPosition() < end) {
        while (lineBreakMeasurer.getPosition() < end && Y < height) {
		    TextLayout textLayout = lineBreakMeasurer.nextLayout(width2);
		    Y += textLayout.getAscent();
		    X = Xstart;
		    textLayout.draw(graphics2D, X, Y);
			Y += textLayout.getDescent() + textLayout.getLeading();
        }
        Y = 0;
        Xstart += width2;
    }
  }
  public static void main(String[] args) {
    JFrame frame = new JFrame("Show Line Break");
    LineBreakExample controller = new LineBreakExample();
    frame.getContentPane().add(controller,"Center");
    frame.setSize(new Dimension(200, 200));
    frame.show();
  }
}