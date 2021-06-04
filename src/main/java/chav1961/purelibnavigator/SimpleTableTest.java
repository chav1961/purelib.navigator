package chav1961.purelibnavigator;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.AbstractCellEditor;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import chav1961.purelibnavigator.ThreeStateSwitchKeeper.SwitchState;

public class SimpleTableTest extends JPanel {
	private static final long serialVersionUID = 1L;

	public SimpleTableTest() {
        super(new GridLayout(1,0));
        
        final JTable table = new JTable(new MyTableModel());
        table.setPreferredScrollableViewportSize(new Dimension(500, 70));
        table.setFillsViewportHeight(true);

        table.setDefaultRenderer(IconKeeper.class, new IconKeeperRenderer());
        table.setDefaultRenderer(ValueKeeper.class, new ValueKeeperRenderer());
        table.setDefaultRenderer(ThreeStateSwitchKeeper.class, new ThreeStateSwitchKeeperRenderer());
     
        table.setDefaultEditor(ThreeStateSwitchKeeper.class, new ThreeStateSwitchEditor());
        table.setDefaultEditor(ValueKeeper.class, new ValueCellEditor());
        
        final TableColumnModel	cm = table.getColumnModel();
        
        final int[]	w = {24, 100, 50, 50, 0};
        
        for (int index = 0; index < cm.getColumnCount(); index++) {
        	prepareCellHeader(cm, index, w[index], Color.GREEN);
        }
        
        table.setShowGrid(false);
        table.setRowHeight(24);
        table.setRowMargin(0);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        
        add(new JScrollPane(table));
	}

	private static void prepareCellHeader(final TableColumnModel model, final int columnIndex, final int width, final Color background) {
        final TableColumn	tc = model.getColumn(columnIndex);
		
        if (width > 0) {
            tc.setPreferredWidth(width);
            tc.setMinWidth(width);
            tc.setMaxWidth(width);
        }
        tc.setHeaderRenderer((table, value, isSelected, hasFocus, row, column) -> {
        		final JLabel	label = new JLabel(value.toString(), JLabel.CENTER);
        		
        		label.setOpaque(true);
        		label.setBackground(background);
				return label;
			}
		);
	}
	
	public static void main(String[] args) {
        final JFrame 			frame = new JFrame("Test");
        final SimpleTableTest	newContentPane = new SimpleTableTest();
        
        newContentPane.setOpaque(true);
        frame.setContentPane(newContentPane);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
        frame.pack();
        frame.setVisible(true);
	}

	class MyTableModel extends DefaultTableModel {
		private static final long 	serialVersionUID = 1L;
		
		private final Record[]	recs = {new Record(new ImageIcon(this.getClass().getResource("icon.png")), 50, 100, SwitchState.LEFT_ON),
										new Record(new ImageIcon(this.getClass().getResource("icon.png")), 40, 150, SwitchState.LEFT_ON)
										};
		
		@Override
		public int getRowCount() {
			if (recs != null) {
				return recs.length;
			}
			else {
				return 0;
			}
		}

		@Override
		public int getColumnCount() {
			return 5;
		}

		@Override
		public String getColumnName(int columnIndex) {
			switch (columnIndex) {
				case 0 	: return "col 1"; 
				case 1 	: return "col 2"; 
				case 2 	: return "col 3"; 
				case 3 	: return "col 4"; 
				case 4 	: return "col 5"; 
				default	: return null;
			}
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			switch (columnIndex) {
				case 0 	: return IconKeeper.class; 
				case 1 	: return ThreeStateSwitchKeeper.class; 
				case 2 	: return ValueKeeper.class; 
				case 3 	: return ValueKeeper.class; 
				case 4 	: return ValueKeeper.class; 
				default	: return null;
			}
		}

		@Override
		public boolean isCellEditable(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0 	: return false; 
				case 1 	: return true; 
				case 2 	: return false; 
				case 3 	: return false; 
				case 4 	: return true; 
				default	: return false;
			}
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 0 	: return recs[rowIndex].getIcon(); 
				case 1 	: return recs[rowIndex].getState(); 
				case 2 	: return recs[rowIndex].getPrice(); 
				case 3 	: return recs[rowIndex].getAmount(); 
				case 4 	: return recs[rowIndex].getToSell(); 
				default	: return false;
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 1 	:
					recs[rowIndex].setState((SwitchState)aValue);
					fireTableRowsUpdated(rowIndex, rowIndex);
					break;
				case 4 	:  
					recs[rowIndex].setToSell(((Number)aValue).intValue());
					fireTableCellUpdated(rowIndex, columnIndex);
					break;
				default	: 
					break;
			}
		}
	}
	
	private class IconKeeperRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final Icon		icon = ((IconKeeper)value).getIcon();
			final JLabel	label = new JLabel(icon); 
			
			label.setOpaque(true);
			label.setBackground(Color.GRAY);
			label.setBorder(null);
			return label;
		}
	}

	private class ValueKeeperRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final ValueKeeper	vk = (ValueKeeper)value;
			
			switch (vk.getType()) {
				case AMOUNT	:
					final JLabel		amountLabel = new JLabel(""+vk.getValue(),JLabel.CENTER);
					
					amountLabel.setOpaque(true);
					amountLabel.setBackground(Color.GREEN);
					amountLabel.setBorder(null);
					return amountLabel;
				case PRICE	:
					final JLabel		priceLabel = new JLabel("***"+vk.getValue(),JLabel.CENTER);
					
					priceLabel.setOpaque(true);
					priceLabel.setBackground(Color.GREEN);
					priceLabel.setBorder(null);
					return priceLabel;
				case SLIDER	:
					final JPanel		panel = new JPanel(new BorderLayout());
					
					panel.setBackground(Color.GREEN);
					if (!vk.isHidden()) {
						final JLabel		val = new JLabel(""+vk.getValue());
						final JSlider		slider = new JSlider(vk.getMinValue(), vk.getMaxValue(), vk.getValue());
						
						slider.setBackground(Color.GREEN);
						panel.add(val, BorderLayout.WEST);
						panel.add(slider, BorderLayout.CENTER);
					}
					return panel;
				default:
					return null;
			}
		}
	}
	
	private class ThreeStateSwitchKeeperRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final ThreeStateSwitchKeeper	sk = (ThreeStateSwitchKeeper)value;
			final JPanel					panel = new JPanel(new BorderLayout());
			final  JLabel					leftOff = new JLabel(new ImageIcon(this.getClass().getResource("leftMin.png")));
			final  JLabel					rightOff = new JLabel(new ImageIcon(this.getClass().getResource("rightMin.png")));
			final  JLabel					leftOn = new JLabel(new ImageIcon(this.getClass().getResource("leftMax.png")));
			final  JLabel					rightOn = new JLabel(new ImageIcon(this.getClass().getResource("rightMax.png")));
			
			switch (sk.getState()) {
				case ALL_OFF	:
					panel.add(leftOff,BorderLayout.WEST);
					panel.add(rightOff,BorderLayout.EAST);
					break;
				case LEFT_ON	:
					panel.add(leftOn,BorderLayout.WEST);
					panel.add(rightOff,BorderLayout.EAST);
					break;
				case RIGHT_ON	:
					panel.add(leftOff,BorderLayout.WEST);
					panel.add(rightOn,BorderLayout.EAST);
					break;
				default:
					break;
			}
			panel.setBackground(Color.GREEN);
			return panel;
		}		
	}

	
	public static class ThreeStateSwitchEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
		private static final long serialVersionUID = 1L;
		
		protected static final String LEFT = "left";
		protected static final String RIGHT = "right";
		
		private SwitchState	currentState;
	
		public ThreeStateSwitchEditor() {}
		
	    public boolean isCellEditable(final EventObject e) {
	    	if (e instanceof MouseEvent) {
    			return true;
	    	}
	    	else {
	    		return false;
	    	}
	    }
		
		public void actionPerformed(final ActionEvent e) {
			switch (e.getActionCommand()) {
				case LEFT	:
					switch (currentState) {
						case ALL_OFF	:
							currentState = SwitchState.LEFT_ON;
							break;
						case LEFT_ON	:
							currentState = SwitchState.ALL_OFF;
							break;
						case RIGHT_ON	:
							currentState = SwitchState.LEFT_ON;
							break;
						default :
					}
					fireEditingStopped();
					break;
				case RIGHT	:
					switch (currentState) {
						case ALL_OFF	:
							currentState = SwitchState.RIGHT_ON;
							break;
						case LEFT_ON	:
							currentState = SwitchState.RIGHT_ON;
							break;
						case RIGHT_ON	:
							currentState = SwitchState.ALL_OFF;
							break;
						default :
					}
					fireEditingStopped();
					break;
				default :
					fireEditingCanceled();
					break;
			}
		}
	
		public Object getCellEditorValue() {
			return currentState;
		}
	
		public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
			final ThreeStateSwitchKeeper	sk = (ThreeStateSwitchKeeper)value;
			final JButton	buttonLeft = new JButton() {@Override public void paint(Graphics g) {}};
			final JButton	buttonRight = new JButton() {@Override public void paint(Graphics g) {}};
			final JButton	buttonRest = new JButton(" ") {@Override public void paint(Graphics g) {}};
			final JPanel	panel = new JPanel(new BorderLayout()); 
					
			buttonLeft.setActionCommand(LEFT);
			buttonLeft.addActionListener(this);
			buttonLeft.setBorderPainted(false);
			buttonLeft.setBackground(null);
			buttonLeft.setOpaque(false);
			
			buttonRight.setActionCommand(RIGHT);
			buttonRight.addActionListener(this);
			buttonRight.setBorderPainted(false);
			buttonRight.setBackground(null);
			buttonRight.setOpaque(false);

			buttonRest.setActionCommand("");
			buttonRest.addActionListener(this);
			buttonRest.setBorderPainted(false);
			buttonRest.setBackground(null);
			buttonRest.setOpaque(false);
			
			panel.setBackground(Color.GREEN);
			
			switch (sk.getState()) {
				case ALL_OFF	:
					buttonLeft.setText(">");
					buttonRight.setText("<");
					break;
				case LEFT_ON	:
					buttonLeft.setText("<--");
					buttonRight.setText("<");
					break;
				case RIGHT_ON	:
					buttonLeft.setText(">");
					buttonRight.setText("-->");
					break;
			}
			
			panel.add(buttonLeft,BorderLayout.WEST);
			panel.add(buttonRest,BorderLayout.CENTER);
			panel.add(buttonRight,BorderLayout.EAST);
			currentState = sk.getState();
			
			return panel;
		}
	}

	public static class ValueCellEditor extends AbstractCellEditor implements TableCellEditor {
		private static final long serialVersionUID = 1L;
		
		private int	currentValue;
	
		public ValueCellEditor() {}
		
	    public boolean isCellEditable(final EventObject e) {
	    	if (e instanceof MouseEvent) {
    			return ((MouseEvent)e).getX() > 24;
	    	}
	    	else {
	    		return false;
	    	}
	    }
		
		public Object getCellEditorValue() {
			return currentValue;
		}
	
		public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected, final int row, final int column) {
			final ValueKeeper	vk = (ValueKeeper)value;
			
			switch (vk.getType()) {
				case SLIDER	:
					final JPanel		panel = new JPanel(new BorderLayout());
					
					panel.setBackground(Color.GREEN);
					if (!vk.isHidden()) {
						final JLabel		val = new JLabel(""+vk.getValue());
						final JSlider		slider = new JSlider(vk.getMinValue(), vk.getMaxValue(), vk.getValue());
						
						slider.setBackground(Color.GREEN);
						slider.addChangeListener((e)->{
							if (slider.getValueIsAdjusting()) {
								val.setText(""+slider.getValue());
							}
							else {
								currentValue = slider.getValue();
								fireEditingStopped();
							}
						});
						panel.add(val, BorderLayout.WEST);
						panel.add(slider, BorderLayout.CENTER);
					}
					return panel;
				default		:
					final JButton	button = new JButton("...");
					
					button.addActionListener((e)->fireEditingCanceled());
					return new JButton();
			}
		}
	}
	
	private static class Record {
		private final Icon	icon;
		private int			price;
		private int			amount;
		private int			toSell;
		private SwitchState	state;
		
		public Record(final Icon icon, final int price, final int amount, final SwitchState state) {
			this.icon = icon;
			this.price = price;
			this.amount = amount;
			this.toSell = amount / 2;
			this.state = state;
		}
		
		public IconKeeper getIcon() {
			return new IconKeeper() {
				@Override public Icon getIcon() {return icon;}
			};
		}
		
		public ValueKeeper getPrice() {
			return new ValueKeeper() {
				@Override public ValueType getType() {return ValueType.PRICE;}
				@Override public int getValue() {return price;}
				@Override public boolean isHidden() {return false;}
			};
		}
		
		public ValueKeeper getAmount() {
			return new ValueKeeper() {
				@Override public ValueType getType() {return ValueType.AMOUNT;}
				@Override public int getValue() {return amount;}
				@Override public boolean isHidden() {return false;}
			};
		}
		
		public ValueKeeper getToSell()  {
			return new ValueKeeper() {
				@Override public ValueType getType() {return ValueType.SLIDER;}
				@Override public int getMinValue() {return 0;}
				@Override public int getValue() {return toSell;}
				@Override public int getMaxValue() {return amount;}
				@Override public boolean isHidden() {return state != SwitchState.RIGHT_ON;}
			};
		}
		
		public ThreeStateSwitchKeeper getState() {
			return new ThreeStateSwitchKeeper() {
				@Override public SwitchState getState() {return state;}
			};
		}
		
		public void setState(final SwitchState state) {
			this.state = state;
		}
		
		public void setToSell(final int toSell) {
			this.toSell = toSell;
		}
	}
}
