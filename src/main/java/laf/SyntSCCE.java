package laf;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.synth.ColorType;
import javax.swing.plaf.synth.Region;
import javax.swing.plaf.synth.SynthConstants;
import javax.swing.plaf.synth.SynthContext;
import javax.swing.plaf.synth.SynthLookAndFeel;
import javax.swing.plaf.synth.SynthPainter;
import javax.swing.plaf.synth.SynthStyle;
import javax.swing.plaf.synth.SynthStyleFactory;

public class SyntSCCE {
        public static void main(String[] args) throws Exception
        {
                final SynthLookAndFeel laf = new SynthLookAndFeel();
                UIManager.setLookAndFeel(laf);
                SynthLookAndFeel.setStyleFactory(new StyleFactory());

                SwingUtilities.invokeLater(new Runnable()
                {
                        @Override
                        public void run()
                        {
                                final JFrame f = new JFrame();
                                {
                                        f.add(new JButton("Works properly"));
                                        f.setUndecorated(true);
                                        f.setBackground(new Color(0, true));
                                        f.setSize(300, 300);
                                        f.setLocation(0, 0);
                                        f.setVisible(true);
                                }
                                {
                                        final JDialog d = new JDialog(f);
                                        final JButton btn = new JButton("WTF?");
                                        // uncomment and notice that this has no effect
                                        // btn.setContentAreaFilled(false);
                                        d.add(btn);
                                        d.setUndecorated(true);
                                        d.setBackground(new Color(0, true));
                                        d.setSize(300, 300);
                                        d.setLocation(320, 0);
                                        d.setVisible(true);
                                }
                        }
                });
        }

        static class StyleFactory extends SynthStyleFactory
        {
                private final SynthStyle style = new Style();

                @Override
                public SynthStyle getStyle(JComponent c, Region id)
                {
                        return style;
                }
        }

        static class Style extends SynthStyle
        {
                private final SynthPainter painter = new Painter();

                @Override
                protected Color getColorForState(SynthContext context, ColorType type)
                {
                        if (context.getRegion() == Region.BUTTON && type == ColorType.FOREGROUND)
                                return Color.GREEN;

                        return null;
                }

                @Override
                protected Font getFontForState(SynthContext context)
                {
                        return Font.decode("Monospaced-BOLD-30");
                }

                @Override
                public SynthPainter getPainter(SynthContext context)
                {
                        return painter;
                }

                @Override
                public boolean isOpaque(SynthContext context)
                {
                        return false;
                }
        }

        static class Painter extends SynthPainter
        {
                @Override
                public void paintPanelBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
                        final Graphics g2 = g.create();
                        try
                        {
                                g2.setColor(new Color(255, 255, 255, 128));

                                g2.fillRect(x, y, w, h);
                        }
                        finally
                        {
                                g2.dispose();
                        }
                }

                @Override
                public void paintButtonBackground(SynthContext context, Graphics g, int x, int y, int w, int h) {
                        final Graphics g2 = g.create();
                        try
                        {
                                if ((context.getComponentState() & SynthConstants.MOUSE_OVER) == SynthConstants.MOUSE_OVER)
                                        g2.setColor(new Color(255, 0, 0, 255));
                                else
                                        g2.setColor(new Color(0xAA, 0xAA, 0xAA, 255));
                                g2.fillRoundRect(x, y, w, h, w / 2, h / 2);
                        }
                        finally
                        {
                                g2.dispose();
                        }
                }
        }
}