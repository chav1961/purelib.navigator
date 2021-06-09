package chav1961.purelibnavigator;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTable;
import javax.swing.JViewport;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import chav1961.purelib.basic.subscribable.SubscribableInt;

public class TableTest extends JScrollPane {
	private static final long serialVersionUID = 1L;

	public TableTest(final SubscribableInt totalLimit, final Record... records) {
		final JTable	table = new JTable(new MyTableModel(records, totalLimit));
		
		table.setOpaque(false);
		table.setDefaultRenderer(Icon.class, new ImageRenderer());
		table.setDefaultRenderer(int.class, new IntRenderer());
		setViewportView(table);
	}

	@Override
	protected JViewport createViewport() {
		final JViewport	result = super.createViewport();
		
		result.setOpaque(false);
		return result;
	}
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
//		final SubscribableInt	si = new SubscribableInt(true);
//		final TableTest			tt = new TableTest(si,new Record(new ImageIcon(TableTest.class.getResource("pic.png")),1));
//		
//		si.set(10);
//		tt.setPreferredSize(new Dimension(200,200));
//		final JSlider	slider = new JSlider(0,100,50);
//		slider.setExtent(25);
//		slider.setPreferredSize(new Dimension(200,30));
//		JOptionPane.showMessageDialog(null, slider);
	}

	static class MyTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 1L;
		
		private final Record[]			records;
		private final SubscribableInt	totalLimit;
		private int						currentAmount = 0;

		MyTableModel(final Record[] records, final SubscribableInt totalLimit) {
			this.records = records;
			this.totalLimit = totalLimit;
		}
		
		@Override
		public int getRowCount() {
			return records == null ? 0 : records.length;
		}

		@Override
		public int getColumnCount() {
			return 2;
		}

		@Override
		public String getColumnName(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return "col1";
				case 1 : return "col2";
				default : throw new UnsupportedOperationException("Column index ["+columnIndex+"] is not supported yet"); 
			}
		}

		@Override
		public Class<?> getColumnClass(final int columnIndex) {
			switch (columnIndex) {
				case 0 : return Icon.class;
				case 1 : return int.class;
				default : throw new UnsupportedOperationException("Column index ["+columnIndex+"] is not supported yet"); 
			}
		}

		@Override
		public boolean isCellEditable(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return false;
				case 1 : return currentAmount < totalLimit.get();
				default : throw new UnsupportedOperationException("Column index ["+columnIndex+"] is not supported yet"); 
			}
		}

		@Override
		public Object getValueAt(final int rowIndex, final int columnIndex) {
			switch (columnIndex) {
				case 0 : return records[rowIndex].getImage();
				case 1 : return records[rowIndex].getValues();
				default : throw new UnsupportedOperationException("Column index ["+columnIndex+"] is not supported yet"); 
			}
		}

		@Override
		public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
			switch (columnIndex) {
				case 1 : 
					break;
				default : throw new UnsupportedOperationException("Column index ["+columnIndex+"] is not supported yet"); 
			}
		}
		
		public int getTotalLimit() {
			return totalLimit.get();
		}

		public int getCurrentAmount() {
			return currentAmount;
		}
	}
	
	static class Record {
		private final Icon	image;
		private int			values;
		
		public Record(final Icon image, final int values) {
			this.image = image;
			this.values = values;
		}
		
		public int getValues() {
			return values;
		}

		public void setValues(int values) {
			this.values = values;
		}

		public Icon getImage() {
			return image;
		}

		@Override
		public String toString() {
			return "Record [image=" + image + ", values=" + values + "]";
		}
	}
	
	static class ImageRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final Icon		icon = (Icon)value;
			final JLabel	result = new JLabel(icon);
			
			result.setPreferredSize(new Dimension(icon.getIconWidth(), icon.getIconHeight()));
			return result;
		}
		
	}

	static class IntRenderer implements TableCellRenderer {
		@Override
		public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected, final boolean hasFocus, final int row, final int column) {
			final MyTableModel	model = (MyTableModel)table.getModel();
			final int			maxValue = model.getTotalLimit()-model.getCurrentAmount(), curValue = ((Number)value).intValue();
			final JSlider		result = new JSlider(0,maxValue,curValue);

			result.setEnabled(maxValue != 0);
			result.setOpaque(false);
			return result;
		}
	}
}
