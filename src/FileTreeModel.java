import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

public class FileTreeModel implements TreeModel {
	private File root;
	private Vector listeners = new Vector();
	private FilenameFilter directoryFilter = new FilenameFilter() {
		@Override
		public boolean accept(File file, String name) {
			return new File(file, name).isDirectory();
		}
	};

	public FileTreeModel(File rootDirectory) {
		root = rootDirectory;
	}

	@Override
	public Object getRoot() {
		return root;
	}

	@Override
	public Object getChild(Object parent, int index) {
		File directory = (File) parent;
		String[] children = directory.list(directoryFilter);

		return new FileTreeModel.TreeFile(directory, children[index]);
	}

	@Override
	public int getChildCount(Object parent) {
		File file = (File) parent;
		if (file.isDirectory()) {
			String[] fileList = file.list(directoryFilter);

			if (fileList != null) {
				return fileList.length;
			}
		}
		return 0;
	}

	@Override
	public boolean isLeaf(Object node) {
		File file = (File) node;
		return file.isFile();
	}

	@Override
	public int getIndexOfChild(Object parent, Object child) {
		File directory = (File) parent;
		File file = (File) child;
		String[] children = directory.list(directoryFilter);
		for (int i = 0; i < children.length; i++) {
			if (file.getName().equals(children[i])) {
				return i;
			}
		}
		return -1;
	}

	@Override
	public void valueForPathChanged(TreePath path, Object value) {
		File oldFile = (File) path.getLastPathComponent();
		String fileParentPath = oldFile.getParent();
		String newFileName = (String) value;
		File targetFile = new File(fileParentPath, newFileName);
		oldFile.renameTo(targetFile);
		File parent = new File(fileParentPath);
		int[] changedChildrenIndices = { getIndexOfChild(parent, targetFile) };
		Object[] changedChildren = { targetFile };
		fileTreeNodesChanged(path.getParentPath(), changedChildrenIndices, changedChildren);

	}

	private void fileTreeNodesChanged(TreePath parentPath, int[] indices, Object[] children) {
		TreeModelEvent event = new TreeModelEvent(this, parentPath, indices, children);
		Iterator iterator = listeners.iterator();
		TreeModelListener listener = null;
		while (iterator.hasNext()) {
			listener = (TreeModelListener) iterator.next();
			listener.treeNodesChanged(event);
		}
	}

	@Override
	public void addTreeModelListener(TreeModelListener listener) {
		listeners.add(listener);
	}

	@Override
	public void removeTreeModelListener(TreeModelListener listener) {
		listeners.remove(listener);
	}

	public static class TreeFile extends File {
		public TreeFile(File parent, String child) {
			super(parent, child);
		}

		@Override
		public String toString() {
			return getName();
		}
	}
}
