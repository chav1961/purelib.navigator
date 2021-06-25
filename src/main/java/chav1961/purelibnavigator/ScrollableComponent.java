package chav1961.purelibnavigator;

import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;

public class ScrollableComponent extends JComponent {
	private static final long 	serialVersionUID = 1L;
	private static final float	WHEEL_STEP = 0.1f;
	
	private enum WhatFocused {
		MAIN, MINI
	}

	private final JComponent	mini = new ScrollableMini();
	private final float			logicalWidth, logicalHeight, minimumSize;
	private boolean				firstResize = false;
	private float				currentSize = 1;
	private WhatFocused			focus = null;
	private boolean				processDrag = false;
	private JViewport			view;
	private Point				dragPoint;
	
	public ScrollableComponent(final float logicalWidth, final float logicalHeight, final float minimumSize) {
		if (logicalWidth <= 0) {
			throw new IllegalArgumentException("Logical width ["+logicalWidth+"] must be positive"); 
		}
		else if (logicalHeight <= 0) {
			throw new IllegalArgumentException("Logical height ["+logicalHeight+"] must be positive"); 
		}
		else if (minimumSize <= 0 || minimumSize > 1) {
			throw new IllegalArgumentException("Minimum size ["+minimumSize+"] must be positive and less or equals than 1"); 
		}
		else {
			this.logicalWidth = logicalWidth;
			this.logicalHeight = logicalHeight;
			this.minimumSize = minimumSize;
			
			addMouseListener(new MouseListener() {
				@Override public void mouseExited(MouseEvent e) {focus = null;}
				@Override public void mouseEntered(MouseEvent e) {focus = WhatFocused.MAIN;}
				@Override public void mouseReleased(MouseEvent e) {endDrag(e);}
				@Override public void mousePressed(MouseEvent e) {startDrag(e);}
				@Override public void mouseClicked(MouseEvent e) {}
			});
			addMouseMotionListener(new MouseMotionListener() {
				@Override public void mouseMoved(MouseEvent e) {}
				@Override public void mouseDragged(MouseEvent e) {drag(e);}
			});
			addMouseWheelListener(new MouseWheelListener() {
				@Override
				public void mouseWheelMoved(MouseWheelEvent e) {
					resizing(e);
				}
			});
			this.mini.addMouseListener(new MouseListener() {
				@Override public void mouseExited(MouseEvent e) {focus = null;}
				@Override public void mouseEntered(MouseEvent e) {focus = WhatFocused.MINI;}
				@Override public void mouseReleased(MouseEvent e) {}
				@Override public void mousePressed(MouseEvent e) {}
				@Override public void mouseClicked(MouseEvent e) {miniClicked(e);}
			});
		}
	}

	public JComponent getMini() {
		return mini;
	}

	protected void customizedPaint(final JComponent component, final Graphics2D g2d, final Rectangle visibleRect, final boolean isMini) {
		final Rectangle	bound = component.getBounds();
		System.err.println("Paint: "+bound);
		final Paint		p = new GradientPaint(new Point(bound.getLocation()), Color.GREEN, new Point(bound.getSize().width, bound.getSize().height), Color.BLUE);

		g2d.setPaint(p);
		g2d.fillRect(bound.x,bound.y,bound.width,bound.height);
		g2d.setColor(Color.RED);
		g2d.drawLine(bound.x, bound.y, bound.x + bound.width, bound.y + bound.height);
		g2d.drawLine(bound.x + bound.width, bound.y, bound.x, bound.y + bound.height);
	}

	@Override
	protected void paintComponent(Graphics g) {
		view = findViewport();
		
		customizedPaint(this, (Graphics2D)g, view != null ? view.getVisibleRect() : null, false);
	}
	
	private void prepareShow() {
		if ((view = findViewport()) != null) {
			final Rectangle	viewRect = view.getVisibleRect();
			final float		viewRatio = 1.0f * viewRect.height / viewRect.width;
			final float		wantedRatio = logicalHeight / logicalWidth;
			final Rectangle	wantedRect;
			
			if (wantedRatio < viewRatio) {
				wantedRect = new Rectangle(0, 0, (int)(viewRatio * viewRect.width / wantedRatio), viewRect.height);
			}
			else {
				wantedRect = new Rectangle(0, 0, viewRect.width, (int)(wantedRatio * viewRect.height / viewRatio));
			}
			final Dimension	newSize = new Dimension(wantedRect.width, wantedRect.height); 
			
			setPreferredSize(newSize);
			revalidate();
			repaint();
		}
	}
	
	private void startDrag(final MouseEvent e) {
		if (view != null) {
			processDrag = true;
			dragPoint = e.getPoint();
		}
	}

	private void drag(final MouseEvent e) {
		if (processDrag) {
			final Point	newPoint = e.getPoint();
			final int 	deltaX = newPoint.x - dragPoint.x, deltaY = newPoint.y - dragPoint.y;
			final Point	location = view.getViewPosition();
			
			location.translate(deltaX, deltaY);
			view.setViewPosition(location);

			dragPoint = newPoint;
		}
	}

	private void endDrag(final MouseEvent e) {
		processDrag = false;
	}
	
	private void resizing(final MouseWheelEvent e) {
		if (view != null) {
			final Point		windowAnchor = e.getPoint();
			final Point		viewAnchor = SwingUtilities.convertPoint(this, e.getPoint(), view);
			final Rectangle	viewRect = view.getVisibleRect();
			final Rectangle	ownRect = getBounds();
			final Rectangle	visibleRect = SwingUtilities.convertRectangle(view, viewRect, this);
			final float		xRatio = 1.0f * e.getPoint().x / ownRect.width, yRatio = 1.0f * e.getPoint().y / ownRect.height;   
			final float		xViewRatio = 1.0f * viewAnchor.x / viewRect.width, yViewRatio = 1.0f * viewAnchor.y / viewRect.height;
			
			System.err.println("W ratio="+xRatio+"/"+yRatio+", V ratio="+xViewRatio+"/"+yViewRatio);
			System.err.println("Window anchor="+windowAnchor);
			
			int				rotation = e.getWheelRotation();
			float			oldSize = currentSize;
			
			if (rotation > 0) {
				for (int index = rotation; index > 0; index--) {
					currentSize = Math.max(currentSize - WHEEL_STEP, minimumSize);
				}
			}
			else {
				for (int index = -rotation; index > 0; index--) {
					currentSize = Math.min(currentSize + WHEEL_STEP, 1);
				}
			}
			final Dimension	newSize = new Dimension((int)(1.0f * viewRect.width / currentSize), (int)(1.0f * viewRect.height / currentSize)); 
			
			setSize(newSize);
			setPreferredSize(newSize);
			System.err.println("New windowAnchor="+windowAnchor);
			windowAnchor.x = (int)(1.0f * windowAnchor.x * (1 - oldSize / currentSize));  
			windowAnchor.y = (int)(1.0f * windowAnchor.y * (1 - oldSize / currentSize));
			System.err.println("New windowAnchor="+windowAnchor);
			
			final Point		newViewAnchor = SwingUtilities.convertPoint(this, windowAnchor, view);
			
			viewRect.x = newViewAnchor.x - viewAnchor.x; 
			viewRect.y = newViewAnchor.y - viewAnchor.y; 
			
			scrollRectToVisible(viewRect);
			System.err.println("SCroll: "+viewRect+" windowAnchor="+windowAnchor+", size="+newSize+", anch="+newViewAnchor);
			revalidate();
//			
//			final Rectangle	viewRect = view.getVisibleRect();
//			final Point		viewPoint = view.getViewPosition();
//			final float		viewRatio = 1.0f * viewRect.height / viewRect.width;
//			final float		wantedRatio = logicalHeight / logicalWidth;
//			final Rectangle	wantedRect;
//			
//			System.err.println("Size: "+getSize()+", point="+e.getPoint()+", view="+view.getVisibleRect()+", point="+viewPoint);
//			
//			int				rotation = e.getWheelRotation();
//			float			oldSize = currentSize;
//			
//			if (rotation > 0) {
//				for (int index = rotation; index > 0; index--) {
//					currentSize = Math.max(currentSize - WHEEL_STEP, minimumSize);
//				}
//			}
//			else {
//				for (int index = -rotation; index > 0; index--) {
//					currentSize = Math.min(currentSize + WHEEL_STEP, 1);
//				}
//			}
//
//			if (wantedRatio < viewRatio) {
//				wantedRect = new Rectangle(0, 0, (int)(viewRatio * viewRect.width / wantedRatio), viewRect.height);
//			}
//			else {
//				wantedRect = new Rectangle(0, 0, viewRect.width, (int)(wantedRatio * viewRect.height / viewRatio));
//			}
//			wantedRect.setSize((int)(1.0f * wantedRect.width / currentSize), (int)(1.0f * wantedRect.height / currentSize));
//			
//			final Dimension	dim = new Dimension(wantedRect.width, wantedRect.height); 
//			
//			setPreferredSize(dim);
//			viewPoint.x += (int) (viewRect.width * oldSize / ( 2 * currentSize) );
//			viewPoint.y -= (int) (viewRect.height * oldSize / ( 2 * currentSize) );
//			
//			System.err.println("new size="+dim+", new point = "+viewPoint);
//			scrollRectToVisible(wantedRect);
//			
//			revalidate();
		}
	}

	private void miniClicked(final MouseEvent e) {
		// TODO Auto-generated method stub
		if (view != null) {
		}
		else {
			
		}
	}
	
	private JViewport findViewport() {
		Container	result = getParent();
		
		while (result != null) {
			if (result instanceof JViewport) {
				return (JViewport)result;
			}
			else {
				result = result.getParent();
			}
		}
		return null;
	}
	
	private static class ScrollableMini extends JComponent {
		private static final long serialVersionUID = 1L;
		
	}
	
	public static void main(final String[] args) {
		final ScrollableComponent	comp = new ScrollableComponent(1000f, 1000f, 0.1f);
		final JScrollPane			pane = new JScrollPane(comp, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		
		pane.setPreferredSize(new Dimension(250, 200));
		
		JOptionPane.showMessageDialog(null, pane);
	}
}
