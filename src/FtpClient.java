import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JOptionPane;

public class FtpClient {
	public static boolean CONNECTION_STATE = false;
	public static boolean LIST_CMD_TYPE = false;
	public static String SERVER_ROOT_DIR = "/";
	public static String SERVER_CURRENT_DIR = "/";
	public final static Pattern UNIX_FTP_REGEX = Pattern.compile("(?<dir>[\\-d])(?<permission>([-rwxst]){9})\\s+\\d+\\s+(?<owner>\\d+)+\\s+(?<group>\\d+)+\\s+(?<size>\\d+)\\s+(?<timestamp>\\w+\\s+\\d+\\s+\\d{1,2}:\\d{2})\\s+(?<name>.+)");
	public final static Pattern WIN_FTP_REGEX = Pattern.compile("type=(?<dir>[\\w]{3,4});modify=(?<timestamp>\\d+);((?:size=(?<size>\\d+));)?\\s+(?<name>.+)");
	private int CMD_PORT = 21;
	private Socket server;
	private PrintWriter serverOut;
	private BufferedReader serverIn;
	private ResponseGrabber responseGrabber;
	private Thread responseGrabberThread;

	public static enum MSG_TYPE {
		CMD, RESPONSE, ERROR, STATUS;
	}

	public FtpClient() {

	}

	public FtpClient(String host, int port) {
		doOpen(host, port);
	}

	public void send(String cmd) {
		this.responseGrabber.setOneresponse(true);
		this.responseGrabber.setTworesponse(true);
		if (cmd != null) {
			serverOut.print(cmd + "\n");
			serverOut.flush();
			if (cmd.startsWith("PASS")) {
				cmd = "**************";
			}
			sendMsgPane(cmd, MSG_TYPE.CMD);
			while (this.responseGrabber.getOneresponse() && this.responseGrabber.getTworesponse());
		} else {
			while (this.responseGrabber.getTworesponse());
		}
	}

	public Socket dataConnect(String cmd) {
		doBinary();
		Socket dataSocket = null;
		try {
			// Passive mode
			send("PASV");
			String port = this.responseGrabber.getResponse();
			if (port.startsWith("227")) {
				int start = port.indexOf('(');
				int end = port.indexOf(')');
				port = port.substring(start + 1, end);
				int a = port.indexOf(',');
				int b = port.indexOf(',', a + 1);
				int c = port.indexOf(',', b + 1);
				int d = port.indexOf(',', c + 1);
				int e = port.indexOf(',', d + 1);
				String ip = port.substring(0, a) + "." + port.substring(a + 1, b) + "." + port.substring(b + 1, c) + "." + port.substring(c + 1, d);
				int upper = Integer.parseInt(port.substring(d + 1, e));
				int lower = Integer.parseInt(port.substring(e + 1));
				int dataPort = upper * 256 + lower;

				dataSocket = new Socket(ip, dataPort);
				send(cmd);
			} else {
				// Active mode
				String localIp = InetAddress.getLocalHost().getHostAddress().replace('.', ',');
				String activePort = localIp + "," + (((server.getLocalPort()) / 256) & 0xff) + "," + (server.getLocalPort() & 0xff);
				send("PORT " + activePort);
				ServerSocket serverDataSocket = new ServerSocket(0, 1);
				send(cmd);
				dataSocket = serverDataSocket.accept();
				serverDataSocket.close();
				if (!this.responseGrabber.getResponse().startsWith("200")) {
					// local active mode
					localIp = InetAddress.getLoopbackAddress().getHostAddress().replace('.', ',');
					activePort = localIp + "," + (((server.getLocalPort()) / 256) & 0xff) + "," + (server.getLocalPort() & 0xff);
					send("PORT " + activePort);
					serverDataSocket = new ServerSocket(0, 1);
					send(cmd);
					dataSocket = serverDataSocket.accept();
					serverDataSocket.close();
					if (!this.responseGrabber.getResponse().startsWith("200")) {
						dataSocket = null;
						sendMsgPane("主動與被動模式皆無法運行，請確認伺服器是否正確", MSG_TYPE.ERROR);
					}
				}
			}
		} catch (Exception e) {
			System.out.println("dataConnect ERROR");
			e.printStackTrace();
		}
		return dataSocket;
	}

	public boolean doOpen(String host, int port) {
		try {
			String ip = host;
			if (!host.matches("(?:[0-9]{1,3}\\.){3}[0-9]{1,3}")) {
				sendMsgPane("正在解析 " + host + " 的 IP Address", FtpClient.MSG_TYPE.STATUS);
				ip = InetAddress.getByName(host).getHostAddress();
			}

			if (port == 0) {
				// default port is 21
				port = this.CMD_PORT;
			} else {
				this.CMD_PORT = port;
			}
			sendMsgPane("正在連線到 " + ip + ":" + port + "...", FtpClient.MSG_TYPE.STATUS);

			this.server = new Socket(ip, port);
			this.serverOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(this.server.getOutputStream(), "UTF-8")), true);
			this.serverIn = new BufferedReader(new InputStreamReader(this.server.getInputStream(), "UTF-8"));
			this.responseGrabber = new ResponseGrabber(this.serverIn);
			this.responseGrabberThread = new Thread(this.responseGrabber);
			this.responseGrabberThread.start();
			sendMsgPane("連線已建立, 正在等候歡迎訊息...", FtpClient.MSG_TYPE.STATUS);
			send(null);
			String r = this.responseGrabber.getResponse();
			if (r.startsWith("220"))
				CONNECTION_STATE = true;
			else {
				sendMsgPane("無法連線到伺服器", FtpClient.MSG_TYPE.ERROR);
				JOptionPane.showMessageDialog(null, "請確認主機是否正確", "連接錯誤", JOptionPane.ERROR_MESSAGE);
			}
		} catch (Exception e) {
			sendMsgPane("無法連線到伺服器", FtpClient.MSG_TYPE.ERROR);
			System.out.println("doOpen ERROR");
			JOptionPane.showMessageDialog(null, "請確認主機是否正確", "連接錯誤", JOptionPane.ERROR_MESSAGE);
			//			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean doQuit() {
		try {
			send("QUIT");
			this.responseGrabber.stop();
			this.responseGrabberThread.interrupt();
			this.server.close();
			this.CMD_PORT = 21;
			CONNECTION_STATE = false;
			LIST_CMD_TYPE = false;
			SERVER_ROOT_DIR = "/";
			SERVER_CURRENT_DIR = "/";
		} catch (Exception e) {
			System.out.println("doQuit ERROR");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public boolean doLogin(String name, String password) {
		try {
			send("USER " + name);
			send("PASS " + password);
			if (this.responseGrabber.getResponse().startsWith("530")) {
				this.sendMsgPane("嚴重錯誤", MSG_TYPE.ERROR);
				return false;
			}
			doType();
			doFeat();
		} catch (Exception e) {
			System.out.println("doLogin ERROR");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public Vector<FtpFile> doLs() {
		sendMsgPane("正在取得目錄列表...", FtpClient.MSG_TYPE.STATUS);
		Vector<FtpFile> lists = new Vector<FtpFile>();
		String cmd = (LIST_CMD_TYPE) ? "MLSD" : "LIST";
		Socket data = dataConnect(cmd);
		try {
			if (data != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream(), "UTF-8"));
				String line;
				while ((line = in.readLine()) != null) {
					lists.addElement(getFtpFile(line));
				}
				in.close();
				data.close();
				while (this.responseGrabber.getTworesponse());
				sendMsgPane("成功取得目錄列表", FtpClient.MSG_TYPE.STATUS);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return lists;
	}

	public void doNls() {
		Socket data = dataConnect("NLST");
		try {
			if (data != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream(), "UTF-8"));
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
				data.close();
			}
		} catch (Exception e) {
			System.out.println("doNls ERROR");
			e.printStackTrace();
		}
	}

	public void doAscii() {
		send("TYPE A");
	}

	public void doBinary() {
		send("TYPE I");
	}

	public void doOpts() {
		send("OPTS UTF8 ON");
	}

	public void doGet(String f, String local, String remote) {
		try {
			int n;
			byte[] buff = new byte[1024];
			FileOutputStream outfile = new FileOutputStream(local + f);
			Socket dataSocket = dataConnect("RETR " + remote + f);
			BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
			while ((n = dataInput.read(buff)) > 0) {
				outfile.write(buff, 0, n);
			}
			dataSocket.close();
			outfile.close();
			while (this.responseGrabber.getTworesponse());
		} catch (Exception e) {
			System.out.println("FtpClient doGet ERROR");
			e.printStackTrace();
		}
	}

	public void doPut(File f, String remote) {
		try {
			int n;
			byte[] buff = new byte[1024];
			FileInputStream sendfile = null;

			try {
				sendfile = new FileInputStream(f);
			} catch (Exception e) {
				System.out.println("The file not found: " + f.getAbsolutePath());
				return;
			}

			Socket dataSocket = dataConnect("STOR " + remote + f.getName());
			OutputStream outstr = dataSocket.getOutputStream();
			while ((n = sendfile.read(buff)) > 0) {
				outstr.write(buff, 0, n);
			}
			dataSocket.close();
			sendfile.close();
			while (this.responseGrabber.getTworesponse());
		} catch (Exception e) {
			System.out.println("FtpClient doPut ERROR");
			e.printStackTrace();
		}
	}

	public void doDelete(String remote) {
		send("DELE " + remote);
	}

	public void doCd(String remote) {
		send("CWD " + remote);
	}

	public void doRmd(String remote) {
		send("RMD " + remote);
	}

	public void doMkd(String remote) {
		send("MKD " + remote);
	}

	public void doPwd() {
		send("PWD");
		String t = this.responseGrabber.getResponse();
		if (t.startsWith("257")) {
			int s = t.indexOf("\"");
			int e = t.indexOf("\"", s + 1);
			if (!CONNECTION_STATE)
				SERVER_ROOT_DIR = t.substring(s + 1, e);
			else
				SERVER_CURRENT_DIR = t.substring(s + 1, e);
		}
	}

	public void doType() {
		send("SYST");
	}

	public void doFeat() {
		send("FEAT");
	}

	public void sendMsgPane(String msg, MSG_TYPE type) {
		String t;
		Color c;
		switch (type) {
			case CMD:
				t = "指令: ";
				c = new Color(0, 0, 204);
				break;
			case RESPONSE:
				t = "回應: ";
				c = new Color(50, 127, 54);
				break;
			case ERROR:
				t = "錯誤: ";
				c = Color.red;
				break;
			case STATUS:
				t = "狀態: ";
				c = Color.black;
				break;
			default:
				t = "Unknown: ";
				c = Color.black;
		}
		MainUI.appendTextPane(t + msg + "\n", c);
	}

	private FtpFile getFtpFile(String file) {
		FtpFile f = new FtpFile();
		if (!LIST_CMD_TYPE) {
			// unix
			SimpleDateFormat udf = new SimpleDateFormat("yyyy MMM dd HH:mm", Locale.ENGLISH);
			Matcher u1 = UNIX_FTP_REGEX.matcher(file);
			if (!u1.matches()) {
				System.out.println(file + " NOT MATCH");
				return null;
			}

			// set is a directory or a file
			f.setDirectory(u1.group(1).equals("d"));
			// set auth
			f.setAuth(u1.group(2));
			// set owner
			f.setOwner(u1.group(4));
			// set group
			f.setGroup(u1.group(5));
			// set size
			f.setSize(MainUI.readableFileSize(Long.parseLong(u1.group(6))));
			// set lasttime
			try {
				Date lt = udf.parse(Calendar.getInstance().get(Calendar.YEAR) + " " + u1.group(7));
				f.setLastTime(MainUI.DATE_TIME_FORMAT.format(lt));
			} catch (ParseException e) {
				System.out.println("getFtpFile parse unix time Error");
				e.printStackTrace();
			}
			// set name
			f.setName(u1.group(8));
		} else {
			// win
			SimpleDateFormat wdf = new SimpleDateFormat("yyyyMMddHHmmss");
			Matcher w1 = WIN_FTP_REGEX.matcher(file);
			if (!w1.matches()) {
				System.out.println(file + " NOT MATCHw");
				return null;
			}

			// if a directory or a file
			f.setDirectory(w1.group(1).equals("dir"));
			// set name
			f.setName(w1.group(5));
			// set last time
			try {
				Date lt = wdf.parse(w1.group(2));
				f.setLastTime(MainUI.DATE_TIME_FORMAT.format(lt));
			} catch (ParseException e) {
				System.out.println("getFtpFile parse win time Error");
				e.printStackTrace();
			}
			// set size
			if (f.isFile())
				f.setSize(MainUI.readableFileSize(Long.parseLong(w1.group(4))));
		}
		return f;
	}

	public ResponseGrabber getResponseGrabber() {
		return responseGrabber;
	}

	class ResponseGrabber implements Runnable {
		private BufferedReader in = null;
		private boolean tworesponse;
		private boolean oneresponse;
		private boolean isRunning = true;
		private String passiveResponse;

		public ResponseGrabber(BufferedReader in) {
			this.in = in;
			this.oneresponse = true;
			this.tworesponse = false;
		}

		@Override
		public void run() {
			try {
				while (isRunning) {
					// waiting possible
					String t = in.readLine();
					if (t != null) {
						this.passiveResponse = t;
						System.out.println("Response: " + t);
						sendMsgPane(t, MSG_TYPE.RESPONSE);

						if (t.indexOf("MLSD") != -1)
							LIST_CMD_TYPE = true;
						else if (t.startsWith("421")) {
							CMD_PORT = 21;
							CONNECTION_STATE = false;
							SERVER_ROOT_DIR = "/";
							SERVER_CURRENT_DIR = "/";
							sendMsgPane("由伺服器關閉連線", MSG_TYPE.ERROR);
							responseGrabber.stop();
							responseGrabberThread.interrupt();
						}

						if (t.charAt(0) == '1') {
							this.oneresponse = false;
						} else if (t.charAt(0) == '2' && t.charAt(3) == '-') {
							this.tworesponse = true;
						} else if (t.charAt(0) == '2' || t.charAt(0) == '3' || t.charAt(0) == '4' || t.charAt(0) == '5') {
							this.oneresponse = true;
							this.tworesponse = false;
						}
					}
				}
			} catch (Exception e) {
				System.out.println("ResponseGrabber run ERROR");
				e.printStackTrace();
			}
		}

		public void stop() {
			try {
				this.isRunning = false;
				this.in.close();
			} catch (IOException e) {
				System.out.println("ResponseGrabber stop ERROR");
				e.printStackTrace();
			}
		}

		public synchronized void setOneresponse(boolean oneresponse) {
			this.oneresponse = oneresponse;
		}

		public synchronized void setTworesponse(boolean tworesponse) {
			this.tworesponse = tworesponse;
		}

		public synchronized boolean getOneresponse() {
			return this.oneresponse;
		}

		public synchronized boolean getTworesponse() {
			return this.tworesponse;
		}

		public synchronized String getResponse() {
			return this.passiveResponse;
		}
	}
}
