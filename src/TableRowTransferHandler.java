import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.TransferHandler;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

public class TableRowTransferHandler extends TransferHandler {
	private final DataFlavor localObjectFlavor;

	public TableRowTransferHandler() {
		super();
		localObjectFlavor = new ActivationDataFlavor(Object[].class, DataFlavor.javaJVMLocalObjectMimeType, "Table rows");
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		// c is the source
		JTable st = (JTable) c;
		List<Object> list = new ArrayList<>();
		int[] idxs = st.getSelectedRows();
		if (st.getModel() instanceof LocalTableModel) {
			LocalTableModel model = (LocalTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			for (int i : idxs)
				list.add(model.getRowData(sorter.convertRowIndexToModel(i)));
		} else if (st.getModel() instanceof RemoteTableModel) {
			RemoteTableModel model = (RemoteTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			for (int i : idxs)
				list.add(model.getRowData(sorter.convertRowIndexToModel(i)));
		}

		Object[] transferedObjects = list.toArray();
		return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
	}

	@Override
	public boolean canImport(TransferSupport info) {
		// info is the destination
		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!(tdl instanceof JTable.DropLocation))
			return false;
		JTable.DropLocation dl = (JTable.DropLocation) tdl;
		int tidx = dl.getRow();

		JTable st = (JTable) info.getComponent();
		boolean isDirectory = false;
		if (st.getModel() instanceof LocalTableModel) {
			LocalTableModel model = (LocalTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			int max = model.getRowCount();
			if (tidx < 0 || tidx > max)
				tidx = -1;
			if (tidx != -1 && model.getValueAt(sorter.convertRowIndexToModel(tidx), 2).equals("目錄"))
				isDirectory = true;
		} else if (st.getModel() instanceof RemoteTableModel) {
			RemoteTableModel model = (RemoteTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			int max = model.getRowCount();
			if (tidx < 0 || tidx > max)
				tidx = -1;
			if (tidx != -1 && model.getValueAt(sorter.convertRowIndexToModel(tidx), 2).equals("目錄"))
				isDirectory = true;
		}

		boolean isDropable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor) && isDirectory;
		st.setCursor(isDropable ? DragSource.DefaultMoveDrop : DragSource.DefaultMoveNoDrop);
		return isDropable;
	}

	@Override
	public int getSourceActions(JComponent c) {
		return MOVE;
	}

	@Override
	public boolean importData(TransferSupport info) {
		if (!canImport(info))
			return false;

		TransferHandler.DropLocation tdl = info.getDropLocation();
		if (!(tdl instanceof JTable.DropLocation))
			return false;
		JTable.DropLocation dl = (JTable.DropLocation) tdl;

		JTable st = (JTable) info.getComponent();
		int destIndex = -1;

		if (st.getModel() instanceof LocalTableModel) {
			LocalTableModel model = (LocalTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			destIndex = sorter.convertRowIndexToModel(dl.getRow());
		} else if (st.getModel() instanceof RemoteTableModel) {
			RemoteTableModel model = (RemoteTableModel) st.getModel();
			TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
			destIndex = sorter.convertRowIndexToModel(dl.getRow());
		}

		// change the cursor icon
		st.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

		// get the source table data
		try {
			System.out.println("Destination Row: " + destIndex);
			Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
			System.out.println("Values Length: " + values.length);
			for (int i = 0; i < values.length; i++) {
				Object[] l = (Object[]) values[i];
				System.out.println(i + ": " + l[0]);
			}
			return true;
		} catch (UnsupportedFlavorException | IOException ex) {
			System.out.println("importData ERROR");
			ex.printStackTrace();
		}
		return false;
	}

	@Override
	protected void exportDone(JComponent c, Transferable data, int action) {
		c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		//		cleanup(c, action == MOVE);
	}
}