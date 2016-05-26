import java.util.Vector;

import javax.swing.table.AbstractTableModel;

public class RemoteTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private final String[] columnNames = { "檔案名稱", "檔案大小", "檔案類型", "最後修改時間", "權限", "擁有人/群組" };
	private final Class[] columnTypes = new Class[] { String.class, String.class, String.class, String.class, String.class, String.class };
	private Vector<Object[]> data;

	public RemoteTableModel() {
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
			System.out.println("RemoteTableModel getValueAt ERROR");
			e.printStackTrace();
		}
		return new String();
	}

	public Object getRowData(int row) {
		try {
			return (Object) this.data.get(row);
		} catch (Exception e) {
			System.out.println("RemoteTableModel getRowData ERROR");
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
		FtpFile f = (FtpFile) obj;
		o[0] = f.getName();
		o[1] = f.getSize();
		o[2] = f.getType();
		o[3] = f.getLastTime();
		o[4] = f.getAuth();
		o[5] = f.getOwner() + " " + f.getGroup();
		fireTableDataChanged();
	}

	public void insertRowAt(int row, FtpFile f) {
		this.data.insertElementAt(new Object[] { f.getName(), f.getSize(), f.getType(), f.getLastTime(), f.getAuth(), f.getOwner() + " " + f.getGroup() }, row);
		fireTableDataChanged();
	}

	public void insertData(FtpFile f) {
		this.data.addElement(new Object[] { f.getName(), f.getSize(), f.getType(), f.getLastTime(), f.getAuth(), f.getOwner() + " " + f.getGroup() });
		fireTableDataChanged();
	}

	public void insertData(Vector<FtpFile> fs) {
		for (int i = 0; i < fs.size(); i++)
			this.data.addElement(new Object[] { fs.get(i).getName(), fs.get(i).getSize(), fs.get(i).getType(), fs.get(i).getLastTime(), fs.get(i).getAuth(), fs.get(i).getOwner() + " " + fs.get(i).getGroup() });
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
