package thu.wireless.mobinet.hsrtest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Date;
import android.os.Handler;

public class TCPTest {
	private Handler mHandler;
	public String mUplinkThroughput = "0";
	public String mDownlinkThroughput = "0";
	public String mAvgUplinkThroughput = "0";
	public String mAvgDownlinkThroughput = "0";

	public TCPTest(Handler _mHandler, String serverIP, String measuretime,
			String interval, FileOutputStream fos, int mode) {

		measureIP = serverIP;
		measureTime = measuretime;
		measureInterval = interval;
		if (mode == 2) {
			fosUplink = fos;
		} else if (mode == 1) {
			fosDownlink = fos;
		} else if (mode == 3) { // TCP Flow
			fosDownlink = fos;
		} else if (mode == 4) {
			fosDownlink = fos;
		} else if (mode == 5) { // MutiTCP
			fosDownlink = fos;
		} else if (mode == 6) { // MutiTCP
			fosUplink = fos;
		} else if (mode == 7) { // TCP UL Flow
			fosUplink = fos;
		}
		testmode = mode;

		this.mHandler = _mHandler;

		(new myThread()).start();
	}

	public TCPTest(String serverIP, String measuretime, String interval,
			FileOutputStream fos, int mode) {

		measureIP = serverIP;
		measureTime = measuretime;
		measureInterval = interval;
		if (mode == 2) {
			fosUplink = fos;
		} else if (mode == 1) {
			fosDownlink = fos;
		} else if (mode == 3) { // TCP Flow
			fosDownlink = fos;
		} else if (mode == 4) {
			fosDownlink = fos;
		} else if (mode == 5) { // MutiTCP
			fosDownlink = fos;
		} else if (mode == 6) { // MutiTCP
			fosUplink = fos;
		} else if (mode == 7) { // TCP UL Flow
			fosUplink = fos;
		}
		testmode = mode;

		(new myThread()).start();
	}

	class myThread extends Thread {

		@Override
		public void run() {
			if (testmode == 2) {
				connect2server();
			} else if (testmode == 1) {
				server2client();
			} else if (testmode == 3) {
				while (true) {
					server2clientFlow();
				}
			} else if (testmode == 4) {
				while (true) {
					server2clientFlowAlternate();
				}
			} else if (testmode == 5) { // MutiTCP
				server2MultiClient(Config.tcpDownloadPort + 4);
			} else if (testmode == 6) { // MutiTCP
				multipleConnect2server(Config.tcpUploadPort + 4);
			} else if (testmode == 7) { // TCP UL Flow
				connect2serverFlow();
			}
		}
	}

	/*
	 * Uplink
	 */
	private static long mStartTime;
	private static long mEndTime; // 测量结束时间
	private static long mTime; // 测量时间
	private static long mInterval; // 报告周期

	private static long packetTime;
	private static long mLastTime; // 上一次吞吐量报告时间
	private static long mNextTime; // 下一次吞吐量报告时间
	private static long mTotalTime; // 从开始到写入最后一个buf的时间

	private static long mTotalLen; // 客户机向TCP窗口（缓存）中写入的总数据量，在mTotalTime时间段中，TCP窗口（缓存）中可能还有些数据没有发送出去;测量时间越长，误差越小
	private static long mLastTotalLen;
	private static NumberFormat numF;

	/*
	 * Downlink
	 */
	private static long mStartTimed;
	private static long mEndTimed; // 测量结束时间

	private static long packetTimed;
	private static long mLastTimed; // 上一次吞吐量报告时间
	private static long mNextTimed; // 下一次吞吐量报告时间
	private static long mTotalTimed; // 从开始到写入最后一个buf的时间

	private static long mTotalLend; // 客户机向TCP窗口（缓存）中写入的总数据量，在mTotalTime时间段中，TCP窗口（缓存）中可能还有些数据没有发送出去;测量时间越长，误差越小
	private static long mLastTotalLend;

	private static Socket clientSocketDown;

	// ////////////////////////////////

	private static String measureIP;
	private static String measureTime;
	private static String measureInterval;
	private static Socket clientSocketUp;
	private static String disconnectTime;
	private static int testmode = 0;

	FileOutputStream fosUplink = null;
	FileOutputStream fosDownlink = null;

	static boolean boolthd = false;

	private void connect2server() {

		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			try {
				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				if (clientSocketUp == null) {
					while (true) {
						try {
							clientSocketUp = new Socket(measureIP,
									Config.tcpUploadPort);
							if (clientSocketUp != null)
								break;
						} catch (Exception e) {
							continue;
						}
					}
				}

				// send 1
				mHandler.sendEmptyMessage(1);

				mTotalLen = 0;
				mLastTotalLen = 0;

				String connectTimeString = Config.contentDateFormat
						.format(new Date());
				String local = " Local "
						+ clientSocketUp.getLocalAddress().getHostAddress()
						+ " port " + clientSocketUp.getLocalPort();
				String peer = clientSocketUp.getRemoteSocketAddress()
						.toString();
				fosUplink.write((" ConnectTime: " + connectTimeString + local
						+ " connected to " + peer + "\n").getBytes());

				// 每次向套接字中写入buf的数据，长度为4K,大小为8KB，内容为全'1'
				// int bufLen = 1 * 1024;
				int bufLen = Config.bufferSize;
				// 每次写入的字节数
				int currLen = bufLen * 2;
				String buf = "";
				for (int i = 0; i < bufLen; i++)
					buf += '1';

				DataOutputStream outToServer = new DataOutputStream(
						clientSocketUp.getOutputStream());

				mStartTime = System.currentTimeMillis();
				mEndTime = mStartTime + mTime;
				mLastTime = mStartTime;
				mNextTime = mStartTime + mInterval;

				do {
					outToServer.writeChars(buf);
					packetTime = System.currentTimeMillis();
					disconnectTime = Config.contentDateFormat
							.format(new Date());

					if (packetTime >= mNextTime) {
						long inBytes = mTotalLen - mLastTotalLen;
						long inStart = mLastTime - mStartTime;
						long inStop = mNextTime - mStartTime;

						// 1KB = 1024B; 1kbps = 1000bps
						double throughput = (double) inBytes * 8
								/ (mInterval / 1000) / 1000;
						String rate = numF.format(throughput);
						fosUplink.write((inStart / 1000 + "-" + inStop / 1000
								+ " sec " + inBytes / 1024 + " KB " + rate
								+ " kbps" + "\n").getBytes());

						mUplinkThroughput = String.valueOf((int) throughput);// 回传

						mLastTime = mNextTime;
						mNextTime += mInterval;
						mLastTotalLen = mTotalLen;

						while (packetTime > mNextTime) {
							// ReportPeriodicBW();
							inBytes = mTotalLen - mLastTotalLen;
							inStart = mLastTime - mStartTime;
							inStop = mNextTime - mStartTime;
							// 1KB = 1024B; 1kbps = 1000bps
							throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							rate = numF.format(throughput);
							fosUplink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps" + "\n").getBytes());

							mUplinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTime = mNextTime;
							mNextTime += mInterval;
							mLastTotalLen = mTotalLen;
						}
					}

					// 没有抛出IOException的话，说明写入成功
					mTotalLen += currLen;
				} while (packetTime <= mEndTime);// add by XQY

				// 报告整个测量期间的数据传输量和吞吐量
				mTotalTime = packetTime - mStartTime;
				double throughput = (double) mTotalLen * 8
						/ (mTotalTime / 1000) / 1000;
				String rate = numF.format(throughput);
				String content = "TotalTime	Transfer Throughput uplink:" + "0-"
						+ mTotalTime / 1000 + " sec " + mTotalLen / 1024
						+ " KB " + rate + " kbps" + "\n";

				mAvgUplinkThroughput = String.valueOf((int) throughput);// 回传

				try {
					fosUplink.write(content.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 关闭套接字和连接
				clientSocketUp.close();

				while (true) {
					if (boolthd) {
						break;
					} else {
						Thread.sleep(1000);// add by XQY
						continue;
					}
				}

				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				try {
					fosUplink.write((disconnectTime + " disconnected " + "\n")
							.getBytes());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// send 2
				mHandler.sendEmptyMessage(2);

				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				while (true) {
					try {
						clientSocketUp = new Socket(measureIP,
								Config.tcpUploadPort);
						if (clientSocketUp != null) {
							break;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
				}
				continue;
			}
		}
		// send 3
		mHandler.sendEmptyMessage(3);
		// tvReport.setText("Client has closed up&downlink connection.");
		// btnSend.setEnabled(true);
		// btnClose.setEnabled(true);
	}

	private void connect2serverFlow() {

		// send 0
		mHandler.sendEmptyMessage(0);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			while (true) {

				try {
					// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
					if (clientSocketUp == null) {
						while (true) {
							try {
								clientSocketUp = new Socket(measureIP,
										Config.tcpUploadPort);
								if (clientSocketUp != null)
									break;
							} catch (Exception e) {
								continue;
							}
						}
					}

					// send 1
					mHandler.sendEmptyMessage(1);

					mTotalLen = 0;
					mLastTotalLen = 0;

					String connectTimeString = Config.contentDateFormat
							.format(new Date());
					String local = " Local "
							+ clientSocketUp.getLocalAddress().getHostAddress()
							+ " port " + clientSocketUp.getLocalPort();
					String peer = clientSocketUp.getRemoteSocketAddress()
							.toString();
					fosUplink.write((" ConnectTime: " + connectTimeString
							+ local + " connected to " + peer + "\n")
							.getBytes());

					// 每次向套接字中写入buf的数据，长度为4K,大小为8KB，内容为全'1'
					// int bufLen = 1 * 1024;
					int bufLen = Config.bufferSize;
					// 每次写入的字节数
					int currLen = bufLen * 2;
					String buf = "";
					for (int i = 0; i < bufLen; i++)
						buf += '1';

					DataOutputStream outToServer = new DataOutputStream(
							clientSocketUp.getOutputStream());

					mStartTime = System.currentTimeMillis();
					mEndTime = mStartTime + mTime;
					mLastTime = mStartTime;
					mNextTime = mStartTime + mInterval;

					do {
						outToServer.writeChars(buf);
						packetTime = System.currentTimeMillis();
						disconnectTime = Config.contentDateFormat
								.format(new Date());

						if (packetTime >= mNextTime) {
							long inBytes = mTotalLen - mLastTotalLen;
							long inStart = mLastTime - mStartTime;
							long inStop = mNextTime - mStartTime;

							// 1KB = 1024B; 1kbps = 1000bps
							double throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							String rate = numF.format(throughput);
							fosUplink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps" + "\n").getBytes());

							mUplinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTime = mNextTime;
							mNextTime += mInterval;
							mLastTotalLen = mTotalLen;

							while (packetTime > mNextTime) {
								// ReportPeriodicBW();
								inBytes = mTotalLen - mLastTotalLen;
								inStart = mLastTime - mStartTime;
								inStop = mNextTime - mStartTime;
								// 1KB = 1024B; 1kbps = 1000bps
								throughput = (double) inBytes * 8
										/ (mInterval / 1000) / 1000;
								rate = numF.format(throughput);
								fosUplink.write((inStart / 1000 + "-" + inStop
										/ 1000 + " sec " + inBytes / 1024
										+ " KB " + rate + " kbps" + "\n")
										.getBytes());

								mUplinkThroughput = String
										.valueOf((int) throughput);// 回传

								mLastTime = mNextTime;
								mNextTime += mInterval;
								mLastTotalLen = mTotalLen;
							}
						}

						// 没有抛出IOException的话，说明写入成功
						mTotalLen += currLen;
					} while (packetTime <= mEndTime);// add by XQY

					// 报告整个测量期间的数据传输量和吞吐量
					mTotalTime = packetTime - mStartTime;
					double throughput = (double) mTotalLen * 8
							/ (mTotalTime / 1000) / 1000;
					String rate = numF.format(throughput);
					String content = "TotalTime	Transfer Throughput uplink:"
							+ "0-" + mTotalTime / 1000 + " sec " + mTotalLen
							/ 1024 + " KB " + rate + " kbps" + "\n";

					mAvgUplinkThroughput = String.valueOf((int) throughput);// 回传

					try {
						fosUplink.write(content.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					// 关闭套接字和连接
					clientSocketUp.close();

					while (true) {
						if (boolthd) {
							break;
						} else {
							Thread.sleep(1000);// add by XQY
							continue;
						}
					}

					break;
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					try {
						fosUplink
								.write((disconnectTime + " disconnected " + "\n")
										.getBytes());
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}

					// send 2
					mHandler.sendEmptyMessage(2);

					// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
					while (true) {
						try {
							clientSocketUp = new Socket(measureIP,
									Config.tcpUploadPort);
							if (clientSocketUp != null) {
								break;
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							continue;
						}
					}
					continue;
				}

			}
		}
	}

	private void multipleConnect2server(int port) {

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;
		Socket clientSocketUp = null;

		while (true) {
			try {
				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				if (clientSocketUp == null) {
					while (true) {
						try {
							clientSocketUp = new Socket(measureIP, port);
							if (clientSocketUp != null)
								break;
						} catch (Exception e) {
							continue;
						}
					}
				}

				mTotalLen = 0;
				mLastTotalLen = 0;

				String connectTimeString = Config.contentDateFormat
						.format(new Date());
				String local = " Local "
						+ clientSocketUp.getLocalAddress().getHostAddress()
						+ " port " + clientSocketUp.getLocalPort();
				String peer = clientSocketUp.getRemoteSocketAddress()
						.toString();
				fosUplink.write((" ConnectTime: " + connectTimeString + local
						+ " connected to " + peer + "\n").getBytes());

				// 每次向套接字中写入buf的数据，长度为4K,大小为8KB，内容为全'1'
				int bufLen = 1 * 1024;
				// int bufLen = Config.bufferSize;
				// 每次写入的字节数
				int currLen = bufLen * 2;
				String buf = "";
				for (int i = 0; i < bufLen; i++)
					buf += '1';

				DataOutputStream outToServer = new DataOutputStream(
						clientSocketUp.getOutputStream());

				mStartTime = System.currentTimeMillis();
				mEndTime = mStartTime + mTime;
				mLastTime = mStartTime;
				mNextTime = mStartTime + mInterval;

				do {
					outToServer.writeChars(buf);
					packetTime = System.currentTimeMillis();
					disconnectTime = Config.contentDateFormat
							.format(new Date());

					if (packetTime >= mNextTime) {
						long inBytes = mTotalLen - mLastTotalLen;
						long inStart = mLastTime - mStartTime;
						long inStop = mNextTime - mStartTime;

						// 1KB = 1024B; 1kbps = 1000bps
						double throughput = (double) inBytes * 8
								/ (mInterval / 1000) / 1000;
						String rate = numF.format(throughput);
						fosUplink.write((inStart / 1000 + "-" + inStop / 1000
								+ " sec " + inBytes / 1024 + " KB " + rate
								+ " kbps" + "\n").getBytes());

						mUplinkThroughput = String.valueOf((int) throughput);// 回传

						mLastTime = mNextTime;
						mNextTime += mInterval;
						mLastTotalLen = mTotalLen;

						while (packetTime > mNextTime) {
							// ReportPeriodicBW();
							inBytes = mTotalLen - mLastTotalLen;
							inStart = mLastTime - mStartTime;
							inStop = mNextTime - mStartTime;
							// 1KB = 1024B; 1kbps = 1000bps
							throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							rate = numF.format(throughput);
							fosUplink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps" + "\n").getBytes());

							mUplinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTime = mNextTime;
							mNextTime += mInterval;
							mLastTotalLen = mTotalLen;
						}
					}

					// 没有抛出IOException的话，说明写入成功
					mTotalLen += currLen;
				} while (packetTime <= mEndTime);// add by XQy

				// 报告整个测量期间的数据传输量和吞吐量
				mTotalTime = packetTime - mStartTime;
				double throughput = (double) mTotalLen * 8
						/ (mTotalTime / 1000) / 1000;
				String rate = numF.format(throughput);
				String content = "TotalTime	Transfer Throughput uplink:" + "0-"
						+ mTotalTime / 1000 + " sec " + mTotalLen / 1024
						+ " KB " + rate + " kbps" + "\n";

				mAvgUplinkThroughput = String.valueOf((int) throughput);// 回传

				try {
					fosUplink.write(content.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// 关闭套接字和连接
				clientSocketUp.close();

				while (true) {
					if (boolthd) {
						break;
					} else {
						Thread.sleep(1000);// add by XQY
						continue;
					}
				}

				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				try {
					fosUplink.write((disconnectTime + " disconnected " + "\n")
							.getBytes());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				while (true) {
					try {
						clientSocketUp = new Socket(measureIP, port);
						if (clientSocketUp != null) {
							break;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
				}
				continue;
			}
		}
	}

	private void server2client() {
		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			try {
				// 建立连接
				if (clientSocketDown == null) {
					while (true) {
						try {
							clientSocketDown = new Socket(measureIP,
									Config.tcpDownloadPort);
							if (clientSocketDown != null)
								break;
						} catch (Exception ce) {
							continue;
						}
					}
				}

				// send 1
				mHandler.sendEmptyMessage(1);

				mTotalLend = 0;
				mLastTotalLend = 0;

				String connectTimeString = Config.contentDateFormat
						.format(new Date());
				String local = " Local "
						+ clientSocketDown.getLocalAddress().getHostAddress()
						+ " port " + clientSocketDown.getLocalPort();
				String peer = clientSocketDown.getRemoteSocketAddress()
						.toString();
				try {
					fosDownlink.write((" ConnectTime: " + connectTimeString
							+ local + " connect to " + peer + "\n").getBytes());
				} catch (Exception e) {
					// TODO: handle exception
				}

				// 每次从套接字读入数据到buf，buf的长度由bufLen指定为4K
				// int bufLen = 1 * 1024;
				int bufLen = Config.bufferSize;
				char buf[] = new char[bufLen];

				// 每次读入的字节数
				int currLen = 0;

				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocketDown.getInputStream()));

				mStartTimed = System.currentTimeMillis();
				mEndTimed = mStartTimed + mTime; // add by XQY
				mLastTimed = mStartTimed;
				mNextTimed = mStartTimed + mInterval;

				do {
					currLen = inFromServer.read(buf);
					// currLen = -1 means reaching the end of the stream
					if (currLen == -1)
						break;
					packetTimed = System.currentTimeMillis();

					// 周期性地报告带宽
					if (packetTimed >= mNextTimed) {
						long inBytes = mTotalLend - mLastTotalLend;
						long inStart = mLastTimed - mStartTimed;
						long inStop = mNextTimed - mStartTimed;

						// 1KB = 1024B; 1kbps = 1000bps
						double throughput = (double) inBytes * 8
								/ (mInterval / 1000) / 1000;
						String rate = numF.format(throughput);
						try {
							fosDownlink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps\n").getBytes());
						} catch (Exception e) {
							// TODO: handle exception
						}

						mDownlinkThroughput = String.valueOf((int) throughput);// 回传

						mLastTimed = mNextTimed;
						mNextTimed += mInterval;
						mLastTotalLend = mTotalLend;

						while (packetTimed > mNextTimed) {
							inBytes = mTotalLend - mLastTotalLend;
							inStart = mLastTimed - mStartTimed;
							inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							rate = numF.format(throughput);
							try {
								fosDownlink.write((inStart / 1000 + "-"
										+ inStop / 1000 + " sec " + inBytes
										/ 1024 + " KB " + rate + " kbps\n")
										.getBytes());
							} catch (Exception e) {
								// TODO: handle exception
							}

							mDownlinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;
						}
					}

					// 没有抛出IOException的话，说明写入成功
					mTotalLend += currLen;
				} while (packetTimed <= mEndTimed);// add by XQY

				// 报告整个测量期间的数据传输量和吞吐量
				mTotalTimed = packetTimed - mStartTimed;
				double throughput = (double) mTotalLend * 8
						/ (mTotalTimed / 1000) / 1000;
				String rate = numF.format(throughput);
				String content = "TotalTime Transfer Throughput downlink:"
						+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
						/ 1024 + " KB " + rate + " kbps" + "\n";

				mAvgDownlinkThroughput = String.valueOf((int) throughput);// 回传

				try {
					fosDownlink.write(content.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				clientSocketDown.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// send 2
				mHandler.sendEmptyMessage(2);

				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				while (true) {
					try {
						// clientSocketDown = new Socket(measureIP, port);
						clientSocketDown = new Socket(measureIP,
								Config.tcpDownloadPort);
						if (clientSocketDown != null) {
							break;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
				}
				continue;
			}
		}
	}

	private void server2client30() {
		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			try {
				// 建立连接
				if (clientSocketDown == null) {
					while (true) {
						try {
							clientSocketDown = new Socket(measureIP,
									Config.tcpDownloadPort + 4);
							if (clientSocketDown != null)
								break;
						} catch (Exception ce) {
							continue;
						}
					}
				}

				// send 1
				mHandler.sendEmptyMessage(1);

				mTotalLend = 0;
				mLastTotalLend = 0;

				String connectTimeString = Config.contentDateFormat
						.format(new Date());
				String local = " Local "
						+ clientSocketDown.getLocalAddress().getHostAddress()
						+ " port " + clientSocketDown.getLocalPort();
				String peer = clientSocketDown.getRemoteSocketAddress()
						.toString();
				try {
					fosDownlink.write((" ConnectTime: " + connectTimeString
							+ local + " connect to " + peer + "\n").getBytes());
				} catch (Exception e) {
					// TODO: handle exception
				}

				// 每次从套接字读入数据到buf，buf的长度由bufLen指定为4K
				int bufLen = 1 * 1024;
				// int bufLen = Config.bufferSize;
				char buf[] = new char[bufLen];

				// 每次读入的字节数
				int currLen = 0;

				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(clientSocketDown.getInputStream()));

				mStartTimed = System.currentTimeMillis();
				mEndTimed = mStartTimed + mTime; // add by XQY
				mLastTimed = mStartTimed;
				mNextTimed = mStartTimed + mInterval;

				do {
					currLen = inFromServer.read(buf);
					// currLen = -1 means reaching the end of the stream
					if (currLen == -1)
						break;
					packetTimed = System.currentTimeMillis();

					// 周期性地报告带宽
					if (packetTimed >= mNextTimed) {
						long inBytes = mTotalLend - mLastTotalLend;
						long inStart = mLastTimed - mStartTimed;
						long inStop = mNextTimed - mStartTimed;

						// 1KB = 1024B; 1kbps = 1000bps
						double throughput = (double) inBytes * 8
								/ (mInterval / 1000) / 1000;
						String rate = numF.format(throughput);
						try {
							fosDownlink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps\n").getBytes());
						} catch (Exception e) {
							// TODO: handle exception
						}

						mDownlinkThroughput = String.valueOf((int) throughput);// 回传

						mLastTimed = mNextTimed;
						mNextTimed += mInterval;
						mLastTotalLend = mTotalLend;

						while (packetTimed > mNextTimed) {
							inBytes = mTotalLend - mLastTotalLend;
							inStart = mLastTimed - mStartTimed;
							inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							rate = numF.format(throughput);
							try {
								fosDownlink.write((inStart / 1000 + "-"
										+ inStop / 1000 + " sec " + inBytes
										/ 1024 + " KB " + rate + " kbps\n")
										.getBytes());
							} catch (Exception e) {
								// TODO: handle exception
							}

							mDownlinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;
						}
					}

					// 没有抛出IOException的话，说明写入成功
					mTotalLend += currLen;
				} while (packetTimed <= mEndTimed);// add by XQY

				// 报告整个测量期间的数据传输量和吞吐量
				mTotalTimed = packetTimed - mStartTimed;
				double throughput = (double) mTotalLend * 8
						/ (mTotalTimed / 1000) / 1000;
				String rate = numF.format(throughput);
				String content = "TotalTime Transfer Throughput downlink:"
						+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
						/ 1024 + " KB " + rate + " kbps" + "\n";

				mAvgDownlinkThroughput = String.valueOf((int) throughput);// 回传

				try {
					fosDownlink.write(content.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				clientSocketDown.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// send 2
				mHandler.sendEmptyMessage(2);

				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				while (true) {
					try {
						clientSocketDown = new Socket(measureIP,
								Config.tcpDownloadPort + 4);
						if (clientSocketDown != null) {
							break;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
				}
				continue;
			}
		}
	}

	private void server2clientFlow() {
		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			while (true) {
				try {
					// 建立连接
					if (clientSocketDown == null) {
						while (true) {
							try {
								clientSocketDown = new Socket(measureIP,
										Config.tcpFlowPort);
								if (clientSocketDown != null)
									break;
							} catch (Exception ce) {
								continue;
							}
						}
					}

					// send 1
					mHandler.sendEmptyMessage(1);

					mTotalLend = 0;
					mLastTotalLend = 0;

					String connectTimeString = Config.contentDateFormat
							.format(new Date());
					String local = " Local "
							+ clientSocketDown.getLocalAddress()
									.getHostAddress() + " port "
							+ clientSocketDown.getLocalPort();
					String peer = clientSocketDown.getRemoteSocketAddress()
							.toString();
					try {
						fosDownlink.write((" ConnectTime: " + connectTimeString
								+ local + " connect to " + peer + "\n")
								.getBytes());
					} catch (Exception e) {
						// TODO: handle exception
					}

					// 每次从套接字读入数据到buf，buf的长度由bufLen指定为4K
					int bufLen = 1 * 1024;
					// int bufLen = Config.bufferSize;
					char buf[] = new char[bufLen];

					// 每次读入的字节数
					int currLen = 0;

					BufferedReader inFromServer = new BufferedReader(
							new InputStreamReader(
									clientSocketDown.getInputStream()));

					mStartTimed = System.currentTimeMillis();
					mEndTimed = mStartTimed + mTime; // add by XQY
					mLastTimed = mStartTimed;
					mNextTimed = mStartTimed + mInterval;

					do {
						currLen = inFromServer.read(buf);
						// currLen = -1 means reaching the end of the stream
						if (currLen == -1)
							break;
						packetTimed = System.currentTimeMillis();

						// 周期性地报告带宽
						if (packetTimed >= mNextTimed) {
							long inBytes = mTotalLend - mLastTotalLend;
							long inStart = mLastTimed - mStartTimed;
							long inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							double throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							String rate = numF.format(throughput);
							try {
								fosDownlink.write((inStart / 1000 + "-"
										+ inStop / 1000 + " sec " + inBytes
										/ 1024 + " KB " + rate + " kbps\n")
										.getBytes());
							} catch (Exception e) {
								// TODO: handle exception
							}

							mDownlinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;

							while (packetTimed > mNextTimed) {
								inBytes = mTotalLend - mLastTotalLend;
								inStart = mLastTimed - mStartTimed;
								inStop = mNextTimed - mStartTimed;

								// 1KB = 1024B; 1kbps = 1000bps
								throughput = (double) inBytes * 8
										/ (mInterval / 1000) / 1000;
								rate = numF.format(throughput);
								try {
									fosDownlink.write((inStart / 1000 + "-"
											+ inStop / 1000 + " sec " + inBytes
											/ 1024 + " KB " + rate + " kbps\n")
											.getBytes());
								} catch (Exception e) {
									// TODO: handle exception
								}

								mDownlinkThroughput = String
										.valueOf((int) throughput);// 回传

								mLastTimed = mNextTimed;
								mNextTimed += mInterval;
								mLastTotalLend = mTotalLend;
							}
						}

						// 没有抛出IOException的话，说明写入成功
						mTotalLend += currLen;
					} while (packetTimed <= mEndTimed);// add by XQY

					// 报告整个测量期间的数据传输量和吞吐量
					mTotalTimed = packetTimed - mStartTimed;
					double throughput = (double) mTotalLend * 8
							/ (mTotalTimed / 1000) / 1000;
					String rate = numF.format(throughput);
					String content = "TotalTime Transfer Throughput downlink:"
							+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
							/ 1024 + " KB " + rate + " kbps" + "\n";

					mAvgDownlinkThroughput = String.valueOf((int) throughput);// 回传

					try {
						fosDownlink.write(content.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					clientSocketDown.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					// send 2
					mHandler.sendEmptyMessage(2);

					// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
					while (true) {
						try {
							clientSocketDown = new Socket(measureIP,
									Config.tcpFlowPort);
							if (clientSocketDown != null) {
								break;
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							continue;
						}
					}
					continue;
				}
			}
		}
	}

	private void server2clientFlowAlternate() {
		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);

		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;
		// 李立添加
		int flowPortNow = Config.tcpFlowPort;
		while (true) {
			while (true) {
				try {
					// 建立连接
					if (clientSocketDown == null) {
						while (true) {
							try {
								clientSocketDown = new Socket(measureIP,
										flowPortNow);
								if (clientSocketDown != null) {
									// 李立添加
									if (flowPortNow == Config.tcpFlowPort) {
										flowPortNow = Config.tcpFlowPort + 1;
									} else {
										flowPortNow = Config.tcpFlowPort;
									}

									break;
								}

							} catch (Exception ce) {
								continue;
							}
						}
					}

					// send 1
					mHandler.sendEmptyMessage(1);

					mTotalLend = 0;
					mLastTotalLend = 0;

					String connectTimeString = Config.contentDateFormat
							.format(new Date());
					String local = " Local "
							+ clientSocketDown.getLocalAddress()
									.getHostAddress() + " port "
							+ clientSocketDown.getLocalPort();
					String peer = clientSocketDown.getRemoteSocketAddress()
							.toString();
					try {
						fosDownlink.write((" ConnectTime: " + connectTimeString
								+ local + " connect to " + peer + "\n")
								.getBytes());
					} catch (Exception e) {
						// TODO: handle exception
					}

					// 每次从套接字读入数据到buf，buf的长度由bufLen指定为4K
					int bufLen = 1 * 1024;
					// int bufLen = Config.bufferSize;
					char buf[] = new char[bufLen];

					// 每次读入的字节数
					int currLen = 0;

					BufferedReader inFromServer = new BufferedReader(
							new InputStreamReader(
									clientSocketDown.getInputStream()));

					mStartTimed = System.currentTimeMillis();
					mEndTimed = mStartTimed + mTime; // add by XQY
					mLastTimed = mStartTimed;
					mNextTimed = mStartTimed + mInterval;

					do {
						currLen = inFromServer.read(buf);
						// currLen = -1 means reaching the end of the stream
						if (currLen == -1)
							break;
						packetTimed = System.currentTimeMillis();

						// 周期性地报告带宽
						if (packetTimed >= mNextTimed) {
							long inBytes = mTotalLend - mLastTotalLend;
							long inStart = mLastTimed - mStartTimed;
							long inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							double throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							String rate = numF.format(throughput);
							try {
								fosDownlink.write((inStart / 1000 + "-"
										+ inStop / 1000 + " sec " + inBytes
										/ 1024 + " KB " + rate + " kbps\n")
										.getBytes());
							} catch (Exception e) {
								// TODO: handle exception
							}

							mDownlinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;

							while (packetTimed > mNextTimed) {
								inBytes = mTotalLend - mLastTotalLend;
								inStart = mLastTimed - mStartTimed;
								inStop = mNextTimed - mStartTimed;

								// 1KB = 1024B; 1kbps = 1000bps
								throughput = (double) inBytes * 8
										/ (mInterval / 1000) / 1000;
								rate = numF.format(throughput);
								try {
									fosDownlink.write((inStart / 1000 + "-"
											+ inStop / 1000 + " sec " + inBytes
											/ 1024 + " KB " + rate + " kbps\n")
											.getBytes());
								} catch (Exception e) {
									// TODO: handle exception
								}

								mDownlinkThroughput = String
										.valueOf((int) throughput);// 回传

								mLastTimed = mNextTimed;
								mNextTimed += mInterval;
								mLastTotalLend = mTotalLend;
							}
						}

						// 没有抛出IOException的话，说明写入成功
						mTotalLend += currLen;
					} while (packetTimed <= mEndTimed);// add by XQY

					// 报告整个测量期间的数据传输量和吞吐量
					mTotalTimed = packetTimed - mStartTimed;
					double throughput = (double) mTotalLend * 8
							/ (mTotalTimed / 1000) / 1000;
					String rate = numF.format(throughput);
					String content = "TotalTime Transfer Throughput downlink:"
							+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
							/ 1024 + " KB " + rate + " kbps" + "\n";

					mAvgDownlinkThroughput = String.valueOf((int) throughput);// 回传

					try {
						fosDownlink.write(content.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					clientSocketDown.close();

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

					// send 2
					mHandler.sendEmptyMessage(2);

					// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
					while (true) {
						try {
							clientSocketDown = new Socket(measureIP,
									flowPortNow);
							if (clientSocketDown != null) {
								// 李立添加
								if (flowPortNow == Config.tcpFlowPort) {
									flowPortNow = Config.tcpFlowPort + 1;
								} else {
									flowPortNow = Config.tcpFlowPort;
								}

								break;
							}
						} catch (Exception e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
							continue;
						}
					}
					continue;
				}
			}
		}
	}

	private void server2MultiClient(int port) {
		// 速率报告保留0位小数
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// 测量时间由参数argv[2]指定，单位为min
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// 参数argv[3]指定带宽报告周期，单位为s
		mInterval = Integer.parseInt(measureInterval) * 1000;
		Socket clientSocketDown2 = null;
		while (true) {
			try {
				// 建立连接
				if (clientSocketDown2 == null) {
					while (true) {
						try {
							clientSocketDown2 = new Socket(measureIP, port);
							if (clientSocketDown2 != null)
								break;
						} catch (Exception ce) {
							continue;
						}
					}
				}

				mTotalLend = 0;
				mLastTotalLend = 0;

				String connectTimeString = Config.contentDateFormat
						.format(new Date());
				String local = " Local "
						+ clientSocketDown2.getLocalAddress().getHostAddress()
						+ " port " + clientSocketDown2.getLocalPort();
				String peer = clientSocketDown2.getRemoteSocketAddress()
						.toString();
				try {
					fosDownlink.write((" ConnectTime: " + connectTimeString
							+ local + " connect to " + peer + "\n").getBytes());
				} catch (Exception e) {
					// TODO: handle exception
				}

				// 每次从套接字读入数据到buf，buf的长度由bufLen指定为4K
				int bufLen = 1 * 1024;
				// int bufLen = Config.bufferSize;
				char buf[] = new char[bufLen];

				// 每次读入的字节数
				int currLen = 0;

				BufferedReader inFromServer = new BufferedReader(
						new InputStreamReader(
								clientSocketDown2.getInputStream()));

				mStartTimed = System.currentTimeMillis();
				mEndTimed = mStartTimed + mTime; // add by XQY
				mLastTimed = mStartTimed;
				mNextTimed = mStartTimed + mInterval;

				do {
					currLen = inFromServer.read(buf);
					// currLen = -1 means reaching the end of the stream
					if (currLen == -1)
						break;
					packetTimed = System.currentTimeMillis();

					// 周期性地报告带宽
					if (packetTimed >= mNextTimed) {
						long inBytes = mTotalLend - mLastTotalLend;
						long inStart = mLastTimed - mStartTimed;
						long inStop = mNextTimed - mStartTimed;

						// 1KB = 1024B; 1kbps = 1000bps
						double throughput = (double) inBytes * 8
								/ (mInterval / 1000) / 1000;
						String rate = numF.format(throughput);
						try {
							fosDownlink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps\n").getBytes());
						} catch (Exception e) {
							// TODO: handle exception
						}

						mDownlinkThroughput = String.valueOf((int) throughput);// 回传

						mLastTimed = mNextTimed;
						mNextTimed += mInterval;
						mLastTotalLend = mTotalLend;

						while (packetTimed > mNextTimed) {
							inBytes = mTotalLend - mLastTotalLend;
							inStart = mLastTimed - mStartTimed;
							inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							rate = numF.format(throughput);
							try {
								fosDownlink.write((inStart / 1000 + "-"
										+ inStop / 1000 + " sec " + inBytes
										/ 1024 + " KB " + rate + " kbps\n")
										.getBytes());
							} catch (Exception e) {
								// TODO: handle exception
							}

							mDownlinkThroughput = String
									.valueOf((int) throughput);// 回传

							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;
						}
					}

					// 没有抛出IOException的话，说明写入成功
					mTotalLend += currLen;
				} while (packetTimed <= mEndTimed);// add by XQY

				// 报告整个测量期间的数据传输量和吞吐量
				mTotalTimed = packetTimed - mStartTimed;
				double throughput = (double) mTotalLend * 8
						/ (mTotalTimed / 1000) / 1000;
				String rate = numF.format(throughput);
				String content = "TotalTime Transfer Throughput downlink:"
						+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
						/ 1024 + " KB " + rate + " kbps" + "\n";

				mAvgDownlinkThroughput = String.valueOf((int) throughput);// 回传

				try {
					fosDownlink.write(content.getBytes());
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				clientSocketDown2.close();

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

				// 建立连接，命令行参数argv[0]指示服务器的IP地址，服务器使用5001号端口监听
				while (true) {
					try {
						clientSocketDown2 = new Socket(measureIP, port);
						// clientSocketDown2 = new Socket(measureIP,
						// Config.tcpDownloadPort);
						if (clientSocketDown2 != null) {
							break;
						}
					} catch (Exception e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
						continue;
					}
				}
				continue;
			}
		}
	}

}
