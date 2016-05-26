import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class LocalTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] columnNames = { "檔案名稱", "檔案大小", "檔案類型", "最後修改時間" };
	private final Class[] columnTypes = new Class[] { String.class, String.class, String.class, String.class };
	private Vector<Object[]> data;

	public LocalTableModel() {
		super();
		this.data = new Vector<Object[]>();
	}

	@Override
	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return false;
	}

	@Override
	public int getColumnCount() {
		return columnNames.length;
	}

	@Override
	public int getRowCount() {
		return data.size();
	}

	public String getColumnName(int col) {
		return columnNames[col];
	}

	public Class getColumnClass(int col) {
		return columnTypes[col];
	}

	@Override
	public Object getValueAt(int row, int col) {
		try {
			Object[] o = (Object[]) this.data.get(row);
			return (String) o[col];
		} catch (Exception e) {
			System.out.println("LocalTableModel getValueAt ERROR");
			e.printStackTrace();
		}
		return null;
	}

	public Object getRowData(int row) {
		try {
			return (Object) this.data.get(row);
		} catch (Exception e) {
			System.out.println("LocalTableModel getRowData ERROR");
			e.printStackTrace();
		}
		return null;
	}

	public void setValueAt(Object value, int row, int col) {
		Object[] o = this.data.get(row);
		o[col] = (String) value;
		fireTableCellUpdated(row, col);
	}

	public void setAllValueAt(Object obj, int row) {
		Object[] o = this.data.get(row);
		FileInfo f = (FileInfo) obj;
		o[0] = f.getName();
		o[1] = f.getSize();
		o[2] = f.getType();
		o[3] = f.getLastTime();
		fireTableDataChanged();
	}

	public void insertRowAt(int row, FileInfo f) {
		this.data.insertElementAt(new Object[] { f.getName(), f.getSize(), f.getType(), f.getLastTime() }, row);
		fireTableDataChanged();
	}

	public void insertData(FileInfo f) {
		this.data.addElement(new Object[] { f.getName(), f.getSize(), f.getType(), f.getLastTime() });
		fireTableDataChanged();
	}

	public void insertData(Vector<FileInfo> fs) {
		for (int i = 0; i < fs.size(); i++)
			this.data.addElement(new Object[] { fs.get(i).getName(), fs.get(i).getSize(), fs.get(i).getType(), fs.get(i).getLastTime() });
		fireTableDataChanged();
	}

	public void removeRow(int row) {
		data.removeElementAt(row);
		fireTableDataChanged();
	}

	public void removeAll() {
		data.clear();
		fireTableDataChanged();
	}
}
