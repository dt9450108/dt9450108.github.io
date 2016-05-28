import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DragSource;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Vector;

import javax.activation.ActivationDataFlavor;
import javax.activation.DataHandler;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DropMode;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SortOrder;
import javax.swing.SwingConstants;
import javax.swing.TransferHandler;
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
	public final static SimpleDateFormat DATE_TIME_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public final static ImageIcon icon = new ImageIcon(MainUI.class.getResource("/icon.png"));
	private JFrame frmHaoFtpClient;

	private JPanel LocalPanel;
	private JPanel RemotePanel;
	private JPanel LocalTreePanel;
	private JPanel UpPanel;
	private JPanel InputPanel;

	private JSplitPane DownSplitPane;
	private JSplitPane UpSplitPane;
	private JSplitPane MidSplitPane;
	private JSplitPane LocalSplitPane;

	private GridBagLayout gbl_LocalTreePanel;
	private GridBagLayout gbl_LocalPanel;
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
	private GridBagConstraints gbc_RemoteComboBox;

	public static JScrollPane ResponseScrollPane;
	private JScrollPane RemoteTableScrollPane;
	private JScrollPane LocalTreeScrollPane;
	private JScrollPane LocalTableScrollPane;
	public static JScrollPane UpdownScrollpane;

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

	private JComboBox<String> RemoteComboBox;
	private JComboBox<String> LocalTreeComboBox;

	private JButton BtnConnection;
	private JButton BtnDisconnection;
	public static JTextPane TaResponses;
	public static JTextArea UpdownTextarea;

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
		for (int i = 0; i < ftpFiles.size(); i++)
			ftpFiles.get(i).setParent(parent);

		dirStr = getRemoteDir(dirStr);
		int dirStrPos = ((DefaultComboBoxModel<String>) RemoteComboBox.getModel()).getIndexOf(dirStr);
		if (dirStrPos == -1) {
			RemoteComboBox.addItem(dirStr);
			RemoteComboBox.setSelectedIndex(RemoteComboBox.getItemCount() - 1);
		} else {
			RemoteComboBox.setSelectedIndex(dirStrPos);
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
		return cur.substring(0, lastSeparator + 1).replaceAll("\\/+", "/").replaceAll("\\\\+", "\\\\");
	}

	private String getDirectory(String cur) {
		if (cur.charAt(cur.length() - 1) != File.separatorChar)
			cur += File.separator;
		return cur.replaceAll("\\/+", "/").replaceAll("\\\\+", "\\\\");
	}

	private String getRemoteParentDir(String cur) {
		int lastSeparator = cur.lastIndexOf("/", cur.length() - 2);
		return cur.substring(0, lastSeparator + 1).replaceAll("\\/+", "/");
	}

	private String getRemoteDir(String cur) {
		if (cur.charAt(cur.length() - 1) != '/')
			cur += "/";
		return cur.replaceAll("\\/+", "/");
	}

	private String getRoot() {
		String userDir = new File(System.getProperty("user.dir")).getAbsolutePath();
		String root = userDir.substring(0, userDir.indexOf(File.separator) + 1);
		return root;
	}

	private void initialize() {
		frmHaoFtpClient = new JFrame();
		frmHaoFtpClient.setTitle("Hao Ftp Client");
		frmHaoFtpClient.setIconImage(icon.getImage());
		frmHaoFtpClient.setSize(1150, 600);
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
				DownSplitPane.setSize(w - 15, h - 40);
				DownSplitPane.setDividerLocation((int) (h * 0.8));
				DownSplitPane.updateUI();
			}
		});

		DownSplitPane = new JSplitPane();
		DownSplitPane.setBounds(0, 0, 1150, 600);
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

					if (new File(td).exists())
						current = td;
					else
						JOptionPane.showMessageDialog(frmHaoFtpClient, "\"" + td + "\" 不存在或是無法存取.");
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
				if (e.getClickCount() == 2 && localTableModel.getRowCount() > 0 && LocalTable.getSelectedRow() >= 0) {
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

		ActionMap LocalTableMap = LocalTable.getActionMap();
		AbstractAction dummy = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		};
		LocalTableMap.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
		LocalTableMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
		LocalTableMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);
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

		RemotePanel = new JPanel();
		MidSplitPane.setRightComponent(RemotePanel);
		gbl_RemotePanel = new GridBagLayout();
		gbl_RemotePanel.columnWidths = new int[] { 60, 0, 0 };
		gbl_RemotePanel.rowHeights = new int[] { 25, 0, 25, 0 };
		gbl_RemotePanel.columnWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		gbl_RemotePanel.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		RemotePanel.setLayout(gbl_RemotePanel);

		LbRemoteSite = new JLabel("遠端站台:");
		gbc_LbRemoteSite = new GridBagConstraints();
		gbc_LbRemoteSite.insets = new Insets(0, 5, 0, 0);
		gbc_LbRemoteSite.fill = GridBagConstraints.BOTH;
		gbc_LbRemoteSite.gridx = 0;
		gbc_LbRemoteSite.gridy = 0;
		RemotePanel.add(LbRemoteSite, gbc_LbRemoteSite);
		LbRemoteSite.setFont(new Font("新細明體", Font.PLAIN, 12));

		RemoteComboBox = new JComboBox<String>();
		gbc_RemoteComboBox = new GridBagConstraints();
		gbc_RemoteComboBox.weightx = 1.0;
		gbc_RemoteComboBox.fill = GridBagConstraints.BOTH;
		gbc_RemoteComboBox.gridx = 1;
		gbc_RemoteComboBox.gridy = 0;
		RemoteComboBox.setEditable(false);
		RemoteComboBox.setEnabled(false);
		RemoteComboBox.setFont(new Font("新細明體", Font.PLAIN, 12));
		RemoteComboBox.addItemListener(new ItemListener() {
			private String current = "";
			private boolean firstTime = false;

			public void itemStateChanged(ItemEvent e) {
				if (firstTime) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						String td = getRemoteDir(e.getItem().toString());
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
		RemotePanel.add(RemoteComboBox, gbc_RemoteComboBox);

		RemoteTableScrollPane = new JScrollPane();
		gbc_RemoteTableScrollPane = new GridBagConstraints();
		gbc_RemoteTableScrollPane.gridwidth = 2;
		gbc_RemoteTableScrollPane.weighty = 1.0;
		gbc_RemoteTableScrollPane.weightx = 1.0;
		gbc_RemoteTableScrollPane.fill = GridBagConstraints.BOTH;
		gbc_RemoteTableScrollPane.insets = new Insets(0, 0, 0, 0);
		gbc_RemoteTableScrollPane.gridx = 0;
		gbc_RemoteTableScrollPane.gridy = 1;
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
		RemoteTable.setEnabled(false);
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
				if (e.getClickCount() == 2 && remoteTableModel.getRowCount() > 0 && RemoteTable.getSelectedRow() >= 0) {
					// cd parent directory
					String current = RemoteComboBox.getSelectedItem().toString();
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
		ActionMap RemoteTableMap = RemoteTable.getActionMap();
		RemoteTableMap.put(TransferHandler.getCutAction().getValue(Action.NAME), dummy);
		RemoteTableMap.put(TransferHandler.getCopyAction().getValue(Action.NAME), dummy);
		RemoteTableMap.put(TransferHandler.getPasteAction().getValue(Action.NAME), dummy);
		RemoteTable.getInputMap().put(KeyStroke.getKeyStroke("DELETE"), "deleteRow");
		RemoteTable.getActionMap().put("deleteRow", new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				int response = JOptionPane.showConfirmDialog(frmHaoFtpClient, "真的要從伺服器刪除選擇之檔案與其內容嗎？", "需要確認", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {
					JTable t = (JTable) e.getSource();
					RemoteTableModel model = (RemoteTableModel) t.getModel();
					TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) t.getRowSorter();
					String remoteCurrentDir = RemoteComboBox.getSelectedItem().toString();
					int[] rows = t.getSelectedRows();
					for (int i = rows.length - 1; i >= 0; i--) {
						if (rows[i] != 0) {
							FtpFile tf = new FtpFile();
							Object[] o = (Object[]) model.getRowData(sorter.convertRowIndexToModel(rows[i]));
							tf.setName((String) o[0]);
							tf.setDirectory(o[2].equals("目錄"));
							deleteProcessor(tf, remoteCurrentDir);
						}
					}
					ftpc.doCd(remoteCurrentDir);
					getRemoteList(remoteCurrentDir, getRemoteParentDir(remoteCurrentDir));
				}
			}
		});
		RemoteTableScrollPane.setViewportView(RemoteTable);

		TfRemoteState = new JTextField();
		gbc_TfRemoteState = new GridBagConstraints();
		gbc_TfRemoteState.gridwidth = 2;
		gbc_TfRemoteState.weightx = 1.0;
		gbc_TfRemoteState.fill = GridBagConstraints.BOTH;
		gbc_TfRemoteState.gridx = 0;
		gbc_TfRemoteState.gridy = 2;
		TfRemoteState.setEditable(false);
		TfRemoteState.setFont(new Font("新細明體", Font.PLAIN, 12));
		TfRemoteState.setBorder(new LineBorder(new Color(180, 180, 180)));
		TfRemoteState.setColumns(10);
		RemotePanel.add(TfRemoteState, gbc_TfRemoteState);

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
		TfPort.setText("21");
		InputPanel.add(TfPort);

		//		TfHost.setText("haoecec.ddns.net");
		//		TfUsername.setText("haoecec-ftp");
		//		TfPassword.setText("GCL6M3VU62K7EC2FTP");

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

				if (host.length() == 0) {
					JOptionPane.showMessageDialog(frmHaoFtpClient, "無法解析伺服器位置:\n未填入主機, 請輸入主機名稱.", "語法錯誤", JOptionPane.WARNING_MESSAGE);
				} else {
					TaResponses.setText("");
					UpdownTextarea.setText("");

					if (user.length() == 0) {
						user = "anonymous";
						TfUsername.setText(user);
					}
					ftpc.doOpen(host, 0);
					if (FtpClient.CONNECTION_STATE) {
						if (ftpc.getResponseGrabber().getResponse().startsWith("220")) {
							RemoteComboBox.setEditable(true);
							RemoteComboBox.setEnabled(true);
							RemoteTable.setEnabled(true);
						}
						if (ftpc.doLogin(user, pw)) {
							ftpc.doOpts();
							ftpc.doPwd();
							RemoteComboBox.removeAllItems();
							getRemoteList(FtpClient.SERVER_ROOT_DIR, FtpClient.SERVER_ROOT_DIR);
						} else {
							ftpc.sendMsgPane("無法連線到伺服器", FtpClient.MSG_TYPE.ERROR);
						}
					}
				}
			}
		});

		BtnDisconnection = new JButton("結束連線");
		BtnDisconnection.setFont(new Font("微軟正黑體", Font.BOLD, 14));
		BtnDisconnection.setBounds(1000, 10, 100, 30);
		InputPanel.add(BtnDisconnection);
		BtnDisconnection.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (FtpClient.CONNECTION_STATE) {
					TfHost.setText("");
					TfUsername.setText("");
					TfPassword.setText("");
					TfPort.setText("21");
					TfRemoteState.setText("");
					CURRENT_LOCAL_DIRTORY_ROOT = false;
					RemoteComboBox.setEnabled(false);
					RemoteComboBox.setEditable(false);
					RemoteComboBox.removeAllItems();
					RemoteTable.setEnabled(false);
					remoteTableModel.removeAll();
					ftpc.doQuit();
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

		UpdownScrollpane = new JScrollPane();
		DownSplitPane.setRightComponent(UpdownScrollpane);

		UpdownTextarea = new JTextArea();
		UpdownTextarea.setEditable(false);
		UpdownTextarea.setTabSize(4);
		UpdownTextarea.setLineWrap(true);
		UpdownTextarea.setFont(new Font("微軟正黑體", Font.PLAIN, 13));
		UpdownScrollpane.setViewportView(UpdownTextarea);
	}

	public static void appendTextPane(String msg, Color c) {
		StyleContext sc = StyleContext.getDefaultStyleContext();
		AttributeSet aset = sc.addAttribute(SimpleAttributeSet.EMPTY, StyleConstants.Foreground, c);

		try {
			Document doc = TaResponses.getDocument();
			doc.insertString(doc.getLength(), msg, aset);
			TaResponses.setCaretPosition(doc.getLength() - 1);
		} catch (Exception e1) {
			System.out.println("MainUI.appendTextPane ERROR");
			e1.printStackTrace();
		}
	}

	public static void appendTextArea(String msg) {
		UpdownTextarea.setText(UpdownTextarea.getText() + msg + "\n");
		UpdownTextarea.setCaretPosition(UpdownTextarea.getText().length() - 1);
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
			if (ftpc.getResponseGrabber().getResponse().startsWith("226")) {
				appendTextArea("上傳：" + root.getAbsolutePath() + " >>> " + remoteRoot + root.getName());
				ftpc.sendMsgPane("檔案傳輸成功, 已傳輸 " + readableFileSize(root.length()), FtpClient.MSG_TYPE.STATUS);
			}
		} else {
			// make root directory
			String ndir = remoteRoot + root.getName();
			ftpc.doMkd(ndir);
			File[] fl = root.listFiles();
			for (int i = 0; i < fl.length; i++) {
				uploadProcessor(fl[i], getRemoteDir(ndir));
			}
		}
	}

	//	tf is the target file to download
	//	remoteRoot is the directory where the target file is
	public void downloadProcessor(FtpFile tf, String remoteRoot, String localRoot) {
		if (tf.isFile()) {
			// doGet to download the file
			appendTextArea("下載：" + localRoot + tf.getName() + " <<< " + remoteRoot + tf.getName());
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
					appendTextArea("下載：" + lcd + tfl.get(i).getName() + " <<< " + scd + tfl.get(i).getName());
					ftpc.doGet(tfl.get(i).getName(), lcd, scd);
				}
			}
		}
	}

	// tf is the file or directory
	// remoteRoot is the directory where tf is
	public void deleteProcessor(FtpFile tf, String remoteRoot) {
		if (tf.isFile()) {
			appendTextArea("刪除：" + remoteRoot + tf.getName() + " 檔案");
			ftpc.doDelete(remoteRoot + tf.getName());
		} else {
			String d = getRemoteDir(remoteRoot + tf.getName());
			ftpc.doCd(d);
			Vector<FtpFile> tfl = ftpc.doLs();
			for (int i = 0; i < tfl.size(); i++) {
				if (tfl.get(i).isDirectory()) {
					deleteProcessor(tfl.get(i), d);
				} else {
					appendTextArea("刪除：" + d + tfl.get(i).getName() + " 檔案");
					ftpc.doDelete(d + tfl.get(i).getName());
				}
			}
			appendTextArea("刪除：" + d + " 資料夾");
			ftpc.doRmd(d);
		}
	}

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
					if (i != 0)
						list.add(model.getRowData(sorter.convertRowIndexToModel(i)));
			} else if (st.getModel() instanceof RemoteTableModel) {
				RemoteTableModel model = (RemoteTableModel) st.getModel();
				TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
				for (int i : idxs)
					if (i != 0)
						list.add(model.getRowData(sorter.convertRowIndexToModel(i)));
			}

			Object[] transferedObjects = list.toArray();
			return new DataHandler(transferedObjects, localObjectFlavor.getMimeType());
		}

		@Override
		public boolean canImport(TransferSupport info) {
			// info is the destination
			JTable st = (JTable) info.getComponent();
			boolean isDropable = info.isDrop() && info.isDataFlavorSupported(localObjectFlavor);
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
			int destIndex = dl.getRow();
			int destination = 0;
			int source = 0;
			String localCurrentDir = LocalTreeComboBox.getSelectedItem().toString();
			String remoteCurrentDir = RemoteComboBox.getSelectedItem().toString();
			String destDir = "";

			if (st.getModel() instanceof LocalTableModel) {
				LocalTableModel model = (LocalTableModel) st.getModel();
				TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
				if (destIndex > 0 && destIndex <= model.getRowCount()) {
					destIndex = sorter.convertRowIndexToModel(destIndex);
					if (model.getValueAt(destIndex, 2).equals("目錄")) {
						destDir = getDirectory(localCurrentDir + model.getValueAt(destIndex, 0));
					} else {
						destDir = localCurrentDir;
					}
				} else {
					destDir = localCurrentDir;
				}
				destination = 1;
			} else if (st.getModel() instanceof RemoteTableModel) {
				RemoteTableModel model = (RemoteTableModel) st.getModel();
				TableRowSorter<TableModel> sorter = (TableRowSorter<TableModel>) st.getRowSorter();
				if (destIndex > 0 && destIndex <= model.getRowCount()) {
					destIndex = sorter.convertRowIndexToModel(destIndex);
					if (model.getValueAt(destIndex, 2).equals("目錄")) {
						destDir = getRemoteDir(remoteCurrentDir + model.getValueAt(destIndex, 0));
					} else {
						destDir = remoteCurrentDir;
					}
				} else {
					destDir = remoteCurrentDir;
				}
				destination = 2;
			}

			// change the cursor icon
			st.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));

			// get the source table data
			try {
				Object[] values = (Object[]) info.getTransferable().getTransferData(localObjectFlavor);
				source = ((Object[]) values[0]).length == 4 ? 1 : 2;

				if (source == 1 && destination == 2) {
					for (int i = 0; i < values.length; i++) {
						Object[] l = (Object[]) values[i];
						File uf = new File(localCurrentDir + l[0]);
						uploadProcessor(uf, destDir);
					}
					ftpc.doCd(destDir);
					getRemoteList(destDir, getRemoteParentDir(destDir));
				} else if (source == 2 && destination == 1) {
					for (int i = 0; i < values.length; i++) {
						Object[] l = (Object[]) values[i];
						FtpFile df = new FtpFile((String) l[0], "", "", "", "", "");
						df.setDirectory(l[2].toString().equals("目錄"));
						downloadProcessor(df, remoteCurrentDir, destDir);
					}
					getLocalList(destDir);
					getLocalTreeView(destDir);
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
		}
	}
}
