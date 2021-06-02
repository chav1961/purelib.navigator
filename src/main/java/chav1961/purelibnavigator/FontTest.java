package chav1961.purelibnavigator;

import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GraphicsEnvironment;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class FontTest {

	public static void main(String[] args) {
		try{final File fontFile = new File("c:/tmp/font1/OglIeUcs8.ttf");
		
            if(fontFile.exists()){
            	final Font	myFont = Font.createFont(Font.TRUETYPE_FONT, fontFile);//.deriveFont(Font.PLAIN, 22f);
            	final JLabel	testLabel = new JLabel("ПРОВЕРКА");

            	GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(myFont);
            	System.err.println("Font: "+myFont.getFontName());
            	final AffineTransform at = new AffineTransform();
            	
            	at.scale(48, 24);
            	testLabel.setFont(myFont.deriveFont(Font.PLAIN, at));
            	JOptionPane.showMessageDialog(null, testLabel);
            }
            else{
                System.err.println("File ["+fontFile+"] is missing");
            }
        } catch (FontFormatException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

	}

}
