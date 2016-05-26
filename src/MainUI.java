import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.AttributeSet;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class MainUI {
	public static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	private JFrame frmHaoFtpClient;

	private JPanel LocalPanel;
	private JPanel RemoteTreePanel;
	private JPanel RemotePanel;
	private JPanel LocalTreePanel;
	private JPanel UpPanel;
	private JPanel InputPanel;

	private JSplitPane DownSplitPane;
	private JSplitPane UpSplitPane;
	private JSplitPane MidSplitPane;
	private JSplitPane LocalSplitPane;
	private JSplitPane RemoteSplitPane;

	private GridBagLayout gbl_LocalTreePanel;
	private GridBagLayout gbl_LocalPanel;
	private GridBagLayout gbl_RemoteTreePanel;
	private GridBagLayout gbl_RemotePanel;
	private GridBagLayout gbl_UpPanel;

	private GridBagConstraints gbc_LbLocalSite;
	private GridBagConstraints gbc_LocalTreeComboBox;
	private GridBagConstraints gbc_LocalTreeScrollPane;
	private GridBagConstraints gbc_RemoteTableScrollPane;
	private GridBagConstraints gbc_TfRemoteState;
	private GridBagConstraints gbc_ResponseScrollPane;
	private GridBagConstraints gbc_InputPanel;
	private GridBagConstraints gbc_LocalTableScrollPane;
	private GridBagConstraints gbc_TfLocalState;
	private GridBagConstraints gbc_LbRemoteSite;
	private GridBagConstraints gbc_RemoteTreeComboBox;
	private GridBagConstraints gbc_RemoteTreeScrollPane;

	private JScrollPane RemoteTreeScrollPane;
	private JScrollPane ResponseScrollPane;
	private JScrollPane RemoteTableScrollPane;
	private JScrollPane LocalTreeScrollPane;
	private JScrollPane LocalTableScrollPane;

	private JLabel LbLocalSite;
	private JLabel LbRemoteSite;
	private JLabel LbHost;
	private JLabel LbUsername;
	private JLabel LbPassword;
	private JLabel LbPort;

	private JTextField TfHost;
	private JTextField TfUsername;
	private JPasswordField TfPassword;
	private JTextField TfPort;
	private JTextField TfLocalState;
	private JTextField TfRemoteState;

	private DefaultTableCellRenderer TABLE_RIGHT_ALIGNMENT_RENDER = new DefaultTableCellRenderer();

	private LocalTableModel localTableModel;
	private TableRowSorter<TableModel> localTableSorter;
	private JTable LocalTable;

	private RemoteTableModel remoteTableModel;
	private TableRowSorter<TableModel> remoteTableSorter;
	private JTable RemoteTable;

	private JTree LocalTree;
	private JTree RemoteTree;

	private JComboBox<String> RemoteTreeComboBox;
	private JComboBox<String> LocalTreeComboBox;

	private JButton BtnConnection;
	public static JTextPane TaResponses;

	private Vector<FileInfo> localFiles;
	private Vector<FtpFile> remoteFiles;

	private FtpClient ftpc;
	private boolean CURRENT_LOCAL_DIRTORY_ROOT = false;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainUI window = new MainUI();
					window.frmHaoFtpClient.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public MainUI() {
		this.localFiles = new Vector<FileInfo>();
		this.remoteFiles = new Vector<FtpFile>();
		TABLE_RIGHT_ALIGNMENT_RENDER.setHorizontalAlignment(SwingConstants.RIGHT);
		ftpc = new FtpClient();
		initialize();
		getLocalList(getRoot());
	}

	private void getRemoteList(String dirStr, String parent) {
		Vector<FtpFile> ftpFiles = ftpc.doLs();
		for (int i = 0; i < ftpFiles.size(); i++) {
			ftpFiles.get(i).setParent(parent);
			//			System.out.println(ftpFiles.get(i).toString());
		}

		//		if (FtpClient.SERVER_ROOT_DIR.equals(dirStr))
		//			dirStr = FtpClient.SERVER_ROOT_DIR;

		dirStr = getRemoteDir(dirStr);
		int dirStrPos = ((DefaultComboBoxModel<String>) RemoteTreeComboBox.getModel()).getIndexOf(dirStr);
		if (dirStrPos == -1) {
			RemoteTreeComboBox.addItem(dirStr);
			RemoteTreeComboBox.setSelectedIndex(RemoteTreeComboBox.getItemCount() - 1);
		} else {
			RemoteTreeComboBox.setSelectedIndex(dirStrPos);
		}

		remoteFiles.clear();
		// add the first row
		remoteFiles.add(new FtpFile("..", "  ", "  ", "  ", "  ", "  "));
		remoteTableModel.removeAll();
		if (ftpFiles.size() != 0) {
			for (FtpFile af : ftpFiles) {
				if (af.isDirectory()) {
					af.setType("目錄");
					af.setSize("");
				} else {
					af.setType(readableFileType(af.getAbsolutePath(), false));
				}
				remoteFiles.addElement(af);
			}
		}
		this.remoteTableModel.insertData(remoteFiles);
	}

	private void getLocalList(String dirStr) {
		CURRENT_LOCAL_DIRTORY_ROOT = false;
		String root = getRoot();
		if (root.equals(dirStr)) {
			dirStr = root;
			CURRENT_LOCAL_DIRTORY_ROOT = true;
		}

		dirStr = getDirectory(dirStr);
		int dirStrPos = ((DefaultComboBoxModel<String>) LocalTreeComboBox.getModel()).getIndexOf(dirStr);
		if (dirStrPos == -1) {
			LocalTreeComboBox.addItem(dirStr);
			LocalTreeComboBox.setSelectedIndex(LocalTreeComboBox.getItemCount() - 1);
		} else {
			LocalTreeComboBox.setSelectedIndex(dirStrPos);
		}
		try {
			File dir = new File(dirStr);
			File[] files = dir.listFiles();
			String ft;
			String fs;
			localFiles.clear();
			if (!CURRENT_LOCAL_DIRTORY_ROOT)
				localFiles.addElement(new FileInfo("..", "  ", "  ", "  "));
			localTableModel.removeAll();
			if (files.length != 0) {
				for (File aFile : files) {
					if (aFile.isDirectory()) {
						ft = "目錄";
						fs = "";
					} else {
						ft = readableFileType(aFile.getAbsolutePath(), true);
						fs = readableFileSize(aFile.length());
					}
					localFiles.addElement(new FileInfo(aFile.getName(), fs, ft, DATE_TIME_FORMAT.format(aFile.lastModified())));
				}
			}
			this.localTableModel.insertData(localFiles);
		} catch (NullPointerException e) {
			localFiles.clear();
			localFiles.addElement(new FileInfo("..", "  ", "  ", "  "));
			localTableModel.removeAll();
			this.localTableModel.insertData(localFiles.get(0));
		}
	}

	public String readableFileType(String filename, boolean type) {
		if (filename == null) {
			return null;
		}
		int extensionPos = filename.lastIndexOf('.');
		int lastSeparator = filename.lastIndexOf(File.separator);
		int index = lastSeparator > extensionPos ? -1 : extensionPos;
		if (index == -1) {
			String fileType = "未知";
			if (type) {
				// local				
				try {
					fileType = Files.probeContentType(new File(filename).toPath());
				} catch (Exception e) {
					System.out.println("readableFileType ERROR");
					e.printStackTrace();
				}
			} else {
				fileType = "檔案";
			}
			return fileType;
		} else {
			return filename.substring(index + 1) + "-檔案";
		}
	}

	public static String readableFileSize(long size) {
		if (size <= 0)
			return "0 B   ";
		final String[] units = new String[] { "B", "KB", "MB", "GB", "TB" };
		int digitGroups = (int) (Math.log10(size) / Math.log10(1000));
		return new DecimalFormat("#,##0.#").format(size / Math.pow(1000, digitGroups)) + " " + units[digitGroups] + "   ";
	}

	private long realFileSize(String size) {
		if (size == null || size.length() == 0)
			return 0;
		int sp = size.indexOf(' ');
		int digit;
		String u = size.substring(sp + 1);
		String n = size.substring(0, sp);
		double t = 0;
		try {
			t = new DecimalFormat("#,##0.#").parse(n).doubleValue();
		} catch (ParseException e) {
			e.printStackTrace();
		}

		switch (u.charAt(0)) {
			case 'K':
				digit = 1;
				break;
			case 'M':
				digit = 2;
				break;
			case 'G':
				digit = 3;
				break;
			case 'T':
				digit = 4;
				break;
			default:
				digit = 0;
		}
		return (long) (t * Math.pow(1000, digit));
	}

	private String getParentDir(String cur) {
		int lastSeparator = cur.lastIndexOf(File.separator, cur.length() - 2);
		return cur.substring(0, lastSeparator + 1);
	}

	private String getDirectory(String cur) {
		if (cur.charAt(cur.length() - 1) != File.separatorChar)
			cur += File.separator;
		return cur;
	}

	private String getRemoteParentDir(String cur) {
		int lastSeparator = cur.lastIndexOf("/", cur.length() - 2);
		return cur.substring(0, lastSeparator + 1);
	}

	private String getRemoteDir(String cur) {
		if (cur.charAt(cur.length() - 1) != '/')
			cur += "/";
		return cur;
	}

	private String getRoot() {
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		String root = userDir.substring(0, userDir.indexOf(File.separator) + 1);
		return root;
	}

	private void initialize() {
		frmHaoFtpClient = new JFrame();
		frmHaoFtpClient.setTitle("Hao Ftp Client");
		frmHaoFtpClient.setBounds(100, 100, 990, 600);
		frmHaoFtpClient.setMinimumSize(new Dimension(200, 600));
		frmHaoFtpClient.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmHaoFtpClient.getContentPane().setBackground(Color.white);
		frmHaoFtpClient.getContentPane().setLayout(null);
		frmHaoFtpClient.setLocationRelativeTo(null);
		frmHaoFtpClient.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				int h = frmHaoFtpClient.getHeight();
				int w = frmHaoFtpClient.getWidth();

				MidSplitPane.setDividerLocation(w >> 1);
				DownSplitPane.setSize(w, h);
				DownSplitPane.setDividerLocation((int) (h * 0.8));
				DownSplitPane.updateUI();
			}
		});

		DownSplitPane = new JSplitPane();
		DownSplitPane.setBounds(0, 0, 990, 600);
		DownSplitPane.setContinuousLayout(true);
		DownSplitPane.setOneTouchExpandable(true);
		DownSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		DownSplitPane.setDividerLocation(480);
		frmHaoFtpClient.getContentPane().add(DownSplitPane);

		UpSplitPane = new JSplitPane();
		UpSplitPane.setContinuousLayout(true);
		UpSplitPane.setOneTouchExpandable(true);
		DownSplitPane.setLeftComponent(UpSplitPane);
		UpSplitPane.setDividerLocation(140);
		UpSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);

		MidSplitPane = new JSplitPane();
		MidSplitPane.setOneTouchExpandable(true);
		MidSplitPane.setContinuousLayout(true);
		MidSplitPane.setDividerLocation(495);
		UpSplitPane.setRightComponent(MidSplitPane);

		LocalSplitPane = new JSplitPane();
		LocalSplitPane.setContinuousLayout(true);
		LocalSplitPane.setOneTouchExpandable(true);
		LocalSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		LocalSplitPane.setDividerLocation(120);
		MidSplitPane.setLeftComponent(LocalSplitPane);

		LocalTreePanel = new JPanel();
		LocalSplitPane.setLeftComponent(LocalTreePanel);
		gbl_LocalTreePanel = new GridBagLayout();
		gbl_LocalTreePanel.columnWidths = new int[] { 60, 0, 0 };
		gbl_LocalTreePanel.rowHeights = new int[] { 25, 0, 0 };
		gbl_LocalTreePanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_LocalTreePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		LocalTreePanel.setLayout(gbl_LocalTreePanel);

		LbLocalSite = new JLabel("本地站台:");
		gbc_LbLocalSite = new GridBagConstraints();
		gbc_LbLocalSite.insets = new Insets(0, 5, 0, 0);
		gbc_LbLocalSite.fill = GridBagConstraints.BOTH;
		gbc_LbLocalSite.gridx = 0;
		gbc_LbLocalSite.gridy = 0;
		LocalTreePanel.add(LbLocalSite, gbc_LbLocalSite);
		LbLocalSite.setFont(new Font("新細明體", Font.PLAIN, 12));

		LocalTreeComboBox = new JComboBox<String>();
		gbc_LocalTreeComboBox = new GridBagConstraints();
		gbc_LocalTreeComboBox.fill = GridBagConstraints.BOTH;
		gbc_LocalTreeComboBox.weightx = 1.0;
		gbc_LocalTreeComboBox.gridx = 1;
		gbc_LocalTreeComboBox.gridy = 0;
		LocalTreeComboBox.setEditable(true);
		LocalTreeComboBox.setFont(new Font("新細明體", Font.PLAIN, 12));
		LocalTreeComboBox.addItemListener(new ItemListener() {
			private String current = "";

			public void itemStateChanged(ItemEvent e) {
				if (e.getStateChange() == ItemEvent.SELECTED) {
					String td = getDirectory(e.getItem().toString());

					if (new File(td).exists()) {
						current = td;
					} else {
						JOptionPane.showMessageDialog(frmHaoFtpClient, "\"" + td + "\" 不存在或是無法存取.");
					}
					getLocalList(current);
					getLocalTreeView(current);
				}
			}
		});
		LocalTreePanel.add(LocalTreeComboBox, gbc_LocalTreeComboBox);

		LocalTreeScrollPane = new JScrollPane();
		gbc_LocalTreeScrollPane = new GridBagConstraints();
		gbc_LocalTreeScrollPane.gridwidth = 2;
		gbc_LocalTreeScrollPane.weighty = 1.0;
		gbc_LocalTreeScrollPane.weightx = 1.0;
		gbc_LocalTreeScrollPane.fill = GridBagConstraints.BOTH;
		gbc_LocalTreeScrollPane.gridx = 0;
		gbc_LocalTreeScrollPane.gridy = 1;
		LocalTreePanel.add(LocalTreeScrollPane, gbc_LocalTreeScrollPane);

		FileTreeModel LocalTreeModel = new FileTreeModel(new File(getRoot()));

		LocalTree = new JTree();
		LocalTreeScrollPane.setViewportView(LocalTree);
		LocalTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		LocalTree.setModel(LocalTreeModel);
		LocalTree.addTreeSelectionListener(new TreeSelectionListener() {
			@Override
			public void valueChanged(TreeSelectionEvent e) {
				String ap = getDirectory(((File) e.getNewLeadSelectionPath().getLastPathComponent()).getAbsolutePath());
				getLocalList(ap);
			}
		});
		LocalTree.setFont(new Font("新細明體", Font.PLAIN, 12));

		LocalPanel = new JPanel();
		LocalSplitPane.setRightComponent(LocalPanel);
		gbl_LocalPanel = new GridBagLayout();
		gbl_LocalPanel.columnWidths = new int[] { 0, 0 };
		gbl_LocalPanel.rowHeights = new int[] { 0, 25, 0 };
		gbl_LocalPanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_LocalPanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		LocalPanel.setLayout(gbl_LocalPanel);

		LocalTableScrollPane = new JScrollPane();
		gbc_LocalTableScrollPane = new GridBagConstraints();
		gbc_LocalTableScrollPane.weighty = 1.0;
		gbc_LocalTableScrollPane.weightx = 1.0;
		gbc_LocalTableScrollPane.fill = GridBagConstraints.BOTH;
		gbc_LocalTableScrollPane.gridx = 0;
		gbc_LocalTableScrollPane.gridy = 0;
		LocalPanel.add(LocalTableScrollPane, gbc_LocalTableScrollPane);

		// local site table 
		localTableModel = new LocalTableModel();
		localTableSorter = new TableRowSorter<TableModel>(localTableModel) {
			@Override
			public void toggleSortOrder(int column) {
				List<? extends SortKey> sortKeys = getSortKeys();
				if (sortKeys.size() > 0) {
					// if sort key is descending, set sort key to null, then the column is unsorted
					if (sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
						setSortKeys(null);
						return;
					}
				}
				super.toggleSortOrder(column);
			}
		};
		localTableSorter.setComparator(1, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int modifier = localTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
				if (o1.equals("  "))
					return -modifier;
				else if (o2.equals("  "))
					return modifier;
				if (o1.length() == 0)
					return -1;
				else if (o2.length() == 0)
					return 1;
				else {
					long os1 = realFileSize(o1);
					long os2 = realFileSize(o2);

					if (os1 < os2)
						return -1;
					else if (os1 > os2)
						return 1;
					return 0;
				}
			}
		});

		localTableSorter.setComparator(3, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int modifier = localTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
				if (o1.equals("  "))
					return -modifier;
				else if (o2.equals("  "))
					return modifier;
				else {
					Date d1, d2;
					try {
						d1 = DATE_TIME_FORMAT.parse(o1);
						d2 = DATE_TIME_FORMAT.parse(o2);
					} catch (ParseException e) {
						System.out.println("localTableSorter.setComparator(3 ERROR");
						e.printStackTrace();
						return 0;
					}
					return d1.compareTo(d2);
				}
			}
		});

		for (int i = 0; i < 3; i++) {
			if (i != 1)
				localTableSorter.setComparator(i, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						int modifier = localTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
						if (o1.equals("  ") || o1.equals(".."))
							return -modifier;
						else if (o2.equals("  ") || o2.equals(".."))
							return modifier;
						else {
							return o1.compareTo(o2);
						}
					}
				});
		}

		LocalTable = new JTable();
		LocalTable.setFillsViewportHeight(true);
		LocalTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		LocalTable.setFont(new Font("新細明體", Font.PLAIN, 12));
		LocalTable.setModel(localTableModel);
		LocalTable.setSelectionBackground(new Color(76, 175, 80));
		LocalTable.setSelectionForeground(Color.white);
		LocalTable.setRowHeight(25);
		LocalTable.setShowGrid(false);
		LocalTable.getTableHeader().setReorderingAllowed(false);
		LocalTable.setTransferHandler(new TableRowTransferHandler());
		LocalTable.setDropMode(DropMode.ON);
		LocalTable.setDragEnabled(true);
		LocalTable.setRowSorter(localTableSorter);
		LocalTable.getColumnModel().getColumn(1).setCellRenderer(TABLE_RIGHT_ALIGNMENT_RENDER);
		LocalTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					int fileCount = 0;
					int dirCount = 0;
					long total = 0;

					int[] selectedRow;
					if (LocalTable.getSelectedRow() == 0 && !CURRENT_LOCAL_DIRTORY_ROOT) {
						selectedRow = new int[LocalTable.getRowCount() - 1];
						for (int i = 1; i < LocalTable.getRowCount(); i++)
							selectedRow[i - 1] = i;
					} else {
						selectedRow = LocalTable.getSelectedRows();
					}

					for (int i = 0; i < selectedRow.length; i++) {
						if (LocalTable.getValueAt(selectedRow[i], 2) == "目錄")
							dirCount++;
						else
							fileCount++;
						total += realFileSize((String) LocalTable.getValueAt(selectedRow[i], 1));
					}

					String out;
					if (fileCount > 0 && dirCount == 0)
						out = "  選取 " + fileCount + " 個檔案. 總共大小: " + readableFileSize(total);
					else if (fileCount == 0 && dirCount > 0)
						out = "  選取 " + dirCount + " 個目錄.";
					else
						out = "  選取 " + fileCount + " 個檔案與 " + dirCount + " 個目錄. 總共大小: " + readableFileSize(total);
					TfLocalState.setText(out);
				}
			}
		});
		LocalTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && localTableModel.getRowCount() > 0) {
					// cd parent directory
					if (LocalTable.getSelectedRow() == 0 && !CURRENT_LOCAL_DIRTORY_ROOT) {
						String parent = getParentDir(LocalTreeComboBox.getSelectedItem().toString());
						getLocalTreeView(parent);
					} else if (localTableModel.getValueAt(localTableSorter.convertRowIndexToModel(LocalTable.getSelectedRow()), 2).equals("目錄")) {
						String dirStr = LocalTreeComboBox.getSelectedItem().toString() + localTableModel.getValueAt(localTableSorter.convertRowIndexToModel(LocalTable.getSelectedRow()), 0);
						getLocalTreeView(dirStr);
					}
				}
			}
		});
		LocalTableScrollPane.setViewportView(LocalTable);

		TfLocalState = new JTextField();
		gbc_TfLocalState = new GridBagConstraints();
		gbc_TfLocalState.fill = GridBagConstraints.BOTH;
		gbc_TfLocalState.weightx = 1.0;
		gbc_TfLocalState.gridx = 0;
		gbc_TfLocalState.gridy = 1;
		LocalPanel.add(TfLocalState, gbc_TfLocalState);
		TfLocalState.setEditable(false);
		TfLocalState.setFont(new Font("新細明體", Font.PLAIN, 12));
		TfLocalState.setBorder(new LineBorder(new Color(180, 180, 180)));
		TfLocalState.setColumns(10);

		RemoteSplitPane = new JSplitPane();
		RemoteSplitPane.setResizeWeight(0.0);
		RemoteSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		RemoteSplitPane.setOneTouchExpandable(true);
		RemoteSplitPane.setEnabled(true);
		RemoteSplitPane.setContinuousLayout(true);
		RemoteSplitPane.setDividerLocation(120);
		MidSplitPane.setRightComponent(RemoteSplitPane);

		RemoteTreePanel = new JPanel();
		RemoteSplitPane.setLeftComponent(RemoteTreePanel);
		gbl_RemoteTreePanel = new GridBagLayout();
		gbl_RemoteTreePanel.columnWidths = new int[] { 60, 0, 0 };
		gbl_RemoteTreePanel.rowHeights = new int[] { 25, 0, 0 };
		gbl_RemoteTreePanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_RemoteTreePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		RemoteTreePanel.setLayout(gbl_RemoteTreePanel);

		LbRemoteSite = new JLabel("遠端站台:");
		gbc_LbRemoteSite = new GridBagConstraints();
		gbc_LbRemoteSite.insets = new Insets(0, 5, 0, 0);
		gbc_LbRemoteSite.fill = GridBagConstraints.BOTH;
		gbc_LbRemoteSite.gridx = 0;
		gbc_LbRemoteSite.gridy = 0;
		RemoteTreePanel.add(LbRemoteSite, gbc_LbRemoteSite);
		LbRemoteSite.setFont(new Font("新細明體", Font.PLAIN, 12));

		RemoteTreeComboBox = new JComboBox<String>();
		gbc_RemoteTreeComboBox = new GridBagConstraints();
		gbc_RemoteTreeComboBox.weightx = 1.0;
		gbc_RemoteTreeComboBox.fill = GridBagConstraints.BOTH;
		gbc_RemoteTreeComboBox.gridx = 1;
		gbc_RemoteTreeComboBox.gridy = 0;
		RemoteTreePanel.add(RemoteTreeComboBox, gbc_RemoteTreeComboBox);
		RemoteTreeComboBox.setEditable(true);
		RemoteTreeComboBox.setFont(new Font("新細明體", Font.PLAIN, 12));
		RemoteTreeComboBox.addItemListener(new ItemListener() {
			private String current = "";
			private boolean firstTime = false;

			public void itemStateChanged(ItemEvent e) {
				if (firstTime) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						String td = getDirectory(e.getItem().toString());
						ftpc.doCd(td);
						String re = ftpc.getResponseGrabber().getResponse();
						if (re.startsWith("550")) {
							ftpc.sendMsgPane("無法取得目錄列表", FtpClient.MSG_TYPE.ERROR);
						} else {
							current = td;
							getRemoteList(current, getRemoteParentDir(current));
						}
					}
				} else {
					firstTime = true;
				}
			}
		});

		RemoteTreeScrollPane = new JScrollPane();
		gbc_RemoteTreeScrollPane = new GridBagConstraints();
		gbc_RemoteTreeScrollPane.gridwidth = 2;
		gbc_RemoteTreeScrollPane.weighty = 1.0;
		gbc_RemoteTreeScrollPane.weightx = 1.0;
		gbc_RemoteTreeScrollPane.fill = GridBagConstraints.BOTH;
		gbc_RemoteTreeScrollPane.gridx = 0;
		gbc_RemoteTreeScrollPane.gridy = 1;
		RemoteTreePanel.add(RemoteTreeScrollPane, gbc_RemoteTreeScrollPane);

		RemoteTree = new JTree();
		RemoteTreeScrollPane.setViewportView(RemoteTree);
		RemoteTree.setFont(new Font("新細明體", Font.PLAIN, 12));
		RemoteTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

		RemotePanel = new JPanel();
		RemoteSplitPane.setRightComponent(RemotePanel);
		gbl_RemotePanel = new GridBagLayout();
		gbl_RemotePanel.columnWidths = new int[] { 0, 0 };
		gbl_RemotePanel.rowHeights = new int[] { 0, 25, 0 };
		gbl_RemotePanel.columnWeights = new double[] { 0.0, Double.MIN_VALUE };
		gbl_RemotePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		RemotePanel.setLayout(gbl_RemotePanel);

		RemoteTableScrollPane = new JScrollPane();
		gbc_RemoteTableScrollPane = new GridBagConstraints();
		gbc_RemoteTableScrollPane.weighty = 1.0;
		gbc_RemoteTableScrollPane.weightx = 1.0;
		gbc_RemoteTableScrollPane.fill = GridBagConstraints.BOTH;
		gbc_RemoteTableScrollPane.insets = new Insets(0, 0, 0, 0);
		gbc_RemoteTableScrollPane.gridx = 0;
		gbc_RemoteTableScrollPane.gridy = 0;
		RemotePanel.add(RemoteTableScrollPane, gbc_RemoteTableScrollPane);

		// remote site table 
		remoteTableModel = new RemoteTableModel();
		remoteTableSorter = new TableRowSorter<TableModel>(remoteTableModel) {
			@Override
			public void toggleSortOrder(int column) {
				List<? extends SortKey> sortKeys = getSortKeys();
				if (sortKeys.size() > 0) {
					// if sort key is descending, set sort key to null, then the column is unsorted
					if (sortKeys.get(0).getSortOrder() == SortOrder.DESCENDING) {
						setSortKeys(null);
						return;
					}
				}
				super.toggleSortOrder(column);
			}
		};
		remoteTableSorter.setComparator(1, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int modifier = remoteTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
				if (o1.equals("  "))
					return -modifier;
				else if (o2.equals("  "))
					return modifier;
				if (o1.length() == 0)
					return -1;
				else if (o2.length() == 0)
					return 1;
				else {
					long os1 = realFileSize(o1);
					long os2 = realFileSize(o2);

					if (os1 < os2)
						return -1;
					else if (os1 > os2)
						return 1;
					return 0;
				}
			}
		});
		remoteTableSorter.setComparator(3, new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				int modifier = remoteTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
				if (o1.equals("  "))
					return -modifier;
				else if (o2.equals("  "))
					return modifier;
				else {
					Date d1, d2;
					try {
						d1 = DATE_TIME_FORMAT.parse(o1);
						d2 = DATE_TIME_FORMAT.parse(o2);
					} catch (ParseException e) {
						System.out.println("remoteTableSorter.setComparator(3 ERROR");
						e.printStackTrace();
						return 0;
					}
					return d1.compareTo(d2);
				}
			}
		});
		for (int i = 0; i < 6; i++) {
			if (i != 1 && i != 3) {
				remoteTableSorter.setComparator(i, new Comparator<String>() {
					@Override
					public int compare(String o1, String o2) {
						int modifier = remoteTableSorter.getSortKeys().get(0).getSortOrder() == SortOrder.ASCENDING ? 1 : -1;
						if (o1.equals("  ") || o1.equals(".."))
							return -modifier;
						else if (o2.equals("  ") || o2.equals(".."))
							return modifier;
						else {
							return o1.compareTo(o2);
						}
					}
				});
			}
		}
		RemoteTable = new JTable();
		RemoteTable.setFillsViewportHeight(true);
		RemoteTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		RemoteTable.setFont(new Font("新細明體", Font.PLAIN, 12));
		RemoteTable.setModel(remoteTableModel);
		RemoteTable.setSelectionBackground(new Color(76, 175, 80));
		RemoteTable.setSelectionForeground(Color.white);
		RemoteTable.setRowHeight(25);
		RemoteTable.setShowGrid(false);
		RemoteTable.getTableHeader().setReorderingAllowed(false);
		RemoteTable.setTransferHandler(new TableRowTransferHandler());
		RemoteTable.setDropMode(DropMode.ON);
		RemoteTable.setDragEnabled(true);
		RemoteTable.setRowSorter(remoteTableSorter);
		RemoteTable.getColumnModel().getColumn(1).setCellRenderer(TABLE_RIGHT_ALIGNMENT_RENDER);
		RemoteTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				if (e.getValueIsAdjusting()) {
					int fileCount = 0;
					int dirCount = 0;
					long total = 0;

					int[] selectedRow;
					if (RemoteTable.getSelectedRow() == 0) {
						selectedRow = new int[RemoteTable.getRowCount() - 1];
						for (int i = 1; i < RemoteTable.getRowCount(); i++)
							selectedRow[i - 1] = i;
					} else {
						selectedRow = RemoteTable.getSelectedRows();
					}

					for (int i = 0; i < selectedRow.length; i++) {
						if (RemoteTable.getValueAt(selectedRow[i], 2) == "目錄")
							dirCount++;
						else
							fileCount++;
						total += realFileSize((String) RemoteTable.getValueAt(selectedRow[i], 1));
					}

					String out;
					if (fileCount > 0 && dirCount == 0)
						out = "  選取 " + fileCount + " 個檔案. 總共大小: " + readableFileSize(total);
					else if (fileCount == 0 && dirCount > 0)
						out = "  選取 " + dirCount + " 個目錄.";
					else
						out = "  選取 " + fileCount + " 個檔案與 " + dirCount + " 個目錄. 總共大小: " + readableFileSize(total);
					TfRemoteState.setText(out);
				}
			}
		});
		RemoteTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2 && remoteTableModel.getRowCount() > 0) {
					// cd parent directory
					String current = RemoteTreeComboBox.getSelectedItem().toString();
					String parent = getRemoteParentDir(current);
					if (parent.length() == 0)
						parent = FtpClient.SERVER_ROOT_DIR;

					if (RemoteTable.getSelectedRow() == 0) {
						ftpc.doCd(parent);
						getRemoteList(parent, getRemoteParentDir(parent));
					} else if (remoteTableModel.getValueAt(remoteTableSorter.convertRowIndexToModel(RemoteTable.getSelectedRow()), 2).equals("目錄")) {
						String dirStr = current + remoteTableModel.getValueAt(remoteTableSorter.convertRowIndexToModel(RemoteTable.getSelectedRow()), 0);
						ftpc.doCd(dirStr);
						getRemoteList(dirStr, parent);
					}
				}
			}
		});
		RemoteTableScrollPane.setViewportView(RemoteTable);

		TfRemoteState = new JTextField();
		gbc_TfRemoteState = new GridBagConstraints();
		gbc_TfRemoteState.weightx = 1.0;
		gbc_TfRemoteState.ipadx = 1;
		gbc_TfRemoteState.fill = GridBagConstraints.BOTH;
		gbc_TfRemoteState.gridx = 0;
		gbc_TfRemoteState.gridy = 1;
		RemotePanel.add(TfRemoteState, gbc_TfRemoteState);
		TfRemoteState.setEditable(false);
		TfRemoteState.setFont(new Font("新細明體", Font.PLAIN, 12));
		TfRemoteState.setBorder(new LineBorder(new Color(180, 180, 180)));
		TfRemoteState.setColumns(10);

		UpPanel = new JPanel();
		UpSplitPane.setLeftComponent(UpPanel);
		UpPanel.setBorder(new LineBorder(new Color(0, 0, 0)));
		gbl_UpPanel = new GridBagLayout();
		gbl_UpPanel.columnWidths = new int[] { 0, 0, 0 };
		gbl_UpPanel.rowHeights = new int[] { 50, 0, 0 };
		gbl_UpPanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_UpPanel.rowWeights = new double[] { 0.0, 0.0, 0.0 };
		UpPanel.setLayout(gbl_UpPanel);

		InputPanel = new JPanel();
		InputPanel.setBorder(new MatteBorder(0, 0, 1, 0, (Color) new Color(0, 0, 0)));
		gbc_InputPanel = new GridBagConstraints();
		gbc_InputPanel.weightx = 1.0;
		gbc_InputPanel.fill = GridBagConstraints.BOTH;
		gbc_InputPanel.gridx = 0;
		gbc_InputPanel.gridy = 0;
		UpPanel.add(InputPanel, gbc_InputPanel);
		InputPanel.setLayout(null);

		LbHost = new JLabel("主機(H):");
		LbHost.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		LbHost.setBounds(5, 10, 60, 30);
		InputPanel.add(LbHost);

		LbUsername = new JLabel("使用者名稱(U):");
		LbUsername.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		LbUsername.setBounds(235, 10, 105, 30);
		InputPanel.add(LbUsername);

		LbPassword = new JLabel("密碼(W):");
		LbPassword.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		LbPassword.setBounds(475, 10, 60, 30);
		InputPanel.add(LbPassword);

		LbPort = new JLabel("連接埠(P):");
		LbPort.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		LbPort.setBounds(670, 10, 70, 30);
		InputPanel.add(LbPort);

		TfHost = new JTextField();
		TfHost.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		TfHost.setBounds(70, 10, 150, 30);
		InputPanel.add(TfHost);
		TfHost.setColumns(10);

		TfUsername = new JTextField();
		TfUsername.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		TfUsername.setBounds(340, 10, 120, 30);
		InputPanel.add(TfUsername);
		TfUsername.setColumns(10);

		TfPassword = new JPasswordField();
		TfPassword.setBounds(540, 10, 120, 30);
		InputPanel.add(TfPassword);

		TfPort = new JTextField();
		TfPort.setFont(new Font("Times New Roman", Font.PLAIN, 14));
		TfPort.setColumns(10);
		TfPort.setBounds(745, 10, 120, 30);
		InputPanel.add(TfPort);

		TfHost.setText("haoecec.ddns.net");
		TfUsername.setText("haoecec-ftp");
		TfPassword.setText("GCL6M3VU62K7EC2FTP");

		//		TfHost.setText("120.114.186.4");
		//		TfUsername.setText("summer");
		//		TfPassword.setText("nutncsie");

		BtnConnection = new JButton("連線");
		BtnConnection.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		BtnConnection.setBounds(880, 10, 100, 30);
		InputPanel.add(BtnConnection);
		BtnConnection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				String host = TfHost.getText();
				String user = TfUsername.getText();
				String pw = new String(TfPassword.getPassword());
				String port = TfPort.getText();

				if (host.length() == 0) {
					JOptionPane.showMessageDialog(frmHaoFtpClient, "無法解析伺服器位置:\n未填入主機, 請輸入主機名稱.", "語法錯誤", JOptionPane.WARNING_MESSAGE);
				} else {
					if (user.length() == 0) {
						user = "anonymous";
						TfUsername.setText(user);
					}
					ftpc.doOpen(host, 0);
					ftpc.doLogin(user, pw);
					ftpc.doPwd();
					RemoteTreeComboBox.removeAllItems();
					getRemoteList(FtpClient.SERVER_ROOT_DIR, FtpClient.SERVER_ROOT_DIR);
					//					ftpc.doNls();
					//					File uptest = new File("/media/hao/WD500/Linux_Download/ftptest/123");
					//					uploadProcessor(uptest, "/networkTest/ftptest/");
					//					ftpc.doCd("/networkTest/ftptest/" + uptest.getName());
					//					getRemoteList("/networkTest/ftptest/" + uptest.getName(), "/networkTest/ftptest/");

					//					ftpc.doCd("/networkTest/ftptest/");
					//					Vector<FtpFile> fl = ftpc.doLs();
					//					FtpFile tf = null;
					//					for (int i = 0; i < fl.size(); i++)
					//						if (fl.get(i).getName().equals("123")) {
					//							tf = fl.get(i);
					//							break;
					//						}
					//					if (tf != null) {
					//						String lcd = LocalTreeComboBox.getSelectedItem().toString();
					//						downloadProcessor(tf, "/networkTest/ftptest/", lcd);
					//					} else {
					//						System.out.println("123 not found");
					//					}
				}
			}
		});

		ResponseScrollPane = new JScrollPane();
		gbc_ResponseScrollPane = new GridBagConstraints();
		gbc_ResponseScrollPane.weighty = 1.0;
		gbc_ResponseScrollPane.fill = GridBagConstraints.BOTH;
		gbc_ResponseScrollPane.weightx = 1.0;
		gbc_ResponseScrollPane.gridx = 0;
		gbc_ResponseScrollPane.gridy = 1;
		UpPanel.add(ResponseScrollPane, gbc_ResponseScrollPane);
		ResponseScrollPane.setAutoscrolls(true);

		TaResponses = new JTextPane();
		TaResponses.setEditable(false);
		TaResponses.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
		ResponseScrollPane.setViewportView(TaResponses);
	}

	public static void append(JTextPane tp, String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		try {
			Document doc = tp.getDocument();
			doc.insertString(doc.getLength(), msg, aset);
			tp.setCaretPosition(doc.getLength());
			//			tp.paintImmediately(tp.getBounds());
		} catch (Exception e1) {
			System.out.println("MainUI.append ERROR");
			e1.printStackTrace();
		}
	}

	public void getLocalTreeView(String dir) {
		Vector<File> fs = new Vector<File>();
		fs.addElement(new File(dir));
		String parent = getParentDir(dir);
		while (parent.length() > 0) {
			fs.insertElementAt(new File(parent), 0);
			parent = getParentDir(parent);
		}
		TreePath tps = new TreePath(fs.toArray());
		LocalTree.setSelectionPath(tps);
		LocalTree.scrollPathToVisible(tps);
	}

	// root is the file or the directory attempted to upload
	// remoteRoot is the directory of the server
	public void uploadProcessor(File root, String remoteRoot) {
		if (root.isFile()) {
			ftpc.doPut(root, remoteRoot);
			if (!ftpc.getResponseGrabber().getResponse().startsWith("226"))
				ftpc.sendMsgPane("檔案傳輸成功, 已傳輸 " + readableFileSize(root.length()), FtpClient.MSG_TYPE.STATUS);
		} else {
			File[] fl = root.listFiles();
			for (int i = 0; i < fl.length; i++) {
				String nd = remoteRoot + fl[i].getName();
				if (fl[i].isDirectory()) {
					// mkdir
					if (!ftpc.getResponseGrabber().getResponse().startsWith("250"))
						ftpc.doMkd(remoteRoot + fl[i].getName());
					uploadProcessor(fl[i], getRemoteDir(nd));
				} else {
					// upload file
					ftpc.doPut(fl[i], remoteRoot);
					if (!ftpc.getResponseGrabber().getResponse().startsWith("226"))
						ftpc.sendMsgPane("檔案傳輸成功, 已傳輸 " + readableFileSize(fl[i].length()), FtpClient.MSG_TYPE.STATUS);
				}
			}
		}
	}

	//	tf is the target file to download
	//	remoteRoot is the directory where the target file is
	public void downloadProcessor(FtpFile tf, String remoteRoot, String localRoot) {
		if (tf.isFile()) {
			// doGet to download the file
			ftpc.doGet(tf.getName(), localRoot, remoteRoot);
		} else {
			String scd = getRemoteDir(remoteRoot + tf.getName());
			String lcd = getDirectory(localRoot + tf.getName());
			// make the directory in localRoot
			File tmpCreate = new File(localRoot + tf.getName());
			if (!tmpCreate.exists())
				tmpCreate.mkdirs();

			// change the remote directory
			ftpc.doCd(remoteRoot + tf.getName());

			// get the changed directory list
			Vector<FtpFile> tfl = ftpc.doLs();
			for (int i = 0; i < tfl.size(); i++) {
				if (tfl.get(i).isDirectory()) {
					// if directory, download the files or directories belong to the directory
					downloadProcessor(tfl.get(i), scd, lcd);
				} else {
					// directly download the file
					ftpc.doGet(tfl.get(i).getName(), lcd, scd);
				}
			}
		}
	}
}
