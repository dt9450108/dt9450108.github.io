import java.awt.Color;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class FtpClient {
	private boolean passive = true;
	private String HOST;
	private int CMD_PORT = 21;
	private Socket server;
	private PrintWriter serverOut;
	private BufferedReader serverIn;
	private ResponseGrabber responseGrabber;
	private Thread responseGrabberThread;

	public FtpClient() {

	}

	public FtpClient(String host, int port) {
		doOpen(host, port);
	}

	public void send(String cmd) {
		serverOut.print(cmd + "\r\n");
		serverOut.flush();
	}

	public Socket dataConnect(String cmd) {
		doType();
		doPwd();
		doBinary();

		Socket dataSocket = null;

		try {
			if (!this.passive) {
				// Active mode
				String localIp = InetAddress.getLocalHost().getHostAddress().replace('.', ',');
				String port = localIp + "," + (((server.getLocalPort()) / 256) & 0xff) + "," + (server.getLocalPort() & 0xff);
				send("PORT " + port);
				ServerSocket serverDataSocket = new ServerSocket(0, 1);
				send(cmd);
				dataSocket = serverDataSocket.accept();
				serverDataSocket.close();
			} else {
				// Passive mode
				send("PASV");
				while (this.responseGrabber.getGrab() == true);
				String port = this.responseGrabber.getResponse();

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

				send(cmd);
				this.responseGrabber.setGrab(true);
				dataSocket = new Socket(ip, dataPort);
			}
		} catch (Exception e) {
			System.out.println("dataConnect " + (!this.passive ? "active mode" : "passive mode") + " ERROR");
			e.printStackTrace();
		}
		return dataSocket;
	}

	public boolean doOpen(String host, int port) {
		this.HOST = host;
		try {
			if (port == 0) {
				// default port is 21
				this.server = new Socket(host, this.CMD_PORT);
			} else {
				this.server = new Socket(host, port);
				this.CMD_PORT = port;
			}
			this.serverOut = new PrintWriter(this.server.getOutputStream());
			this.serverIn = new BufferedReader(new InputStreamReader(this.server.getInputStream()));
			this.responseGrabber = new ResponseGrabber(this.serverIn);
			this.responseGrabberThread = new Thread(this.responseGrabber);
			this.responseGrabberThread.start();
		} catch (Exception e) {
			System.out.println("doOpen ERROR");
			e.printStackTrace();
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
		} catch (Exception e) {
			System.out.println("doLogin ERROR");
			e.printStackTrace();
			return false;
		}
		return true;
	}

	public void doLs() {
		Socket data = dataConnect("LIST");
		//		Socket data = dataConnect("MLSD");

		try {
			if (data != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream()));
				String line;
				while ((line = in.readLine()) != null) {
					System.out.println(line);
				}
				in.close();
				data.close();
			}
		} catch (Exception e) {
			System.out.println("doLs ERROR");
			e.printStackTrace();
		}
	}

	public void doNls() {
		Socket data = dataConnect("NLST");
		try {
			if (data != null) {
				BufferedReader in = new BufferedReader(new InputStreamReader(data.getInputStream()));
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

	public void doGet(String remote, String local) {
		try {
			int n;
			byte[] buff = new byte[1024];
			FileOutputStream outfile = new FileOutputStream(local + "/" + remote);
			Socket dataSocket = dataConnect("RETR " + remote);
			BufferedInputStream dataInput = new BufferedInputStream(dataSocket.getInputStream());
			while ((n = dataInput.read(buff)) > 0) {
				outfile.write(buff, 0, n);
			}
			dataSocket.close();
			outfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
		}
	}

	public void doPut(String local, String remote) {
		try {
			int n;
			byte[] buff = new byte[1024];
			FileInputStream sendfile = null;

			try {
				sendfile = new FileInputStream(local + "/" + remote);
			} catch (Exception e) {
				System.out.println("The file not found: " + local + "/" + remote);
				return;
			}

			Socket dataSocket = dataConnect("STOR " + remote);
			OutputStream outstr = dataSocket.getOutputStream();
			while ((n = sendfile.read(buff)) > 0) {
				outstr.write(buff, 0, n);
			}
			dataSocket.close();
			sendfile.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(1);
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
	}

	public void doType() {
		send("SYST");
	}

	private class ResponseGrabber implements Runnable {
		private BufferedReader in = null;
		private boolean grab;
		private boolean isRunning = true;
		private String passiveResponse;

		public ResponseGrabber(BufferedReader in) {
			this.in = in;
			this.grab = true;
		}

		@Override
		public void run() {
			try {
				while (isRunning) {
					if (this.grab) {
						String t = in.readLine();
						if (t.startsWith("227")) {
							passiveResponse = t;
							this.grab = false;
						}
						MainUI.append(MainUI.TaResponses, "回應: " + t + "\n", new Color(50, 127, 54));
						System.out.println("Response: " + t);
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

		public synchronized void setGrab(boolean grab) {
			this.grab = grab;
		}

		public synchronized boolean getGrab() {
			return this.grab;
		}

		public synchronized String getResponse() {
			return this.passiveResponse;
		}
	}
}
