package thu.wireless.mobinet.kingyoung;

/**
 * @author YJY
 * 2014.1.20
 */

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.text.NumberFormat;
import java.util.Date;
import android.os.Handler;

public class SingleSender {
	private Handler mHandler;
	public String mUplinkThroughput = "0";
	public String mDownlinkThroughput = "0";
	public String mAvgUplinkThroughput = "0";
	public String mAvgDownlinkThroughput = "0";

	public SingleSender(Handler _mHandler, String serverIP, String measuretime,
			String interval, FileOutputStream fosDown, FileOutputStream fosUp) {

		measureIP = serverIP;
		measureTime = measuretime;
		measureInterval = interval;
		fosDownlink = fosDown;
		fosUplink = fosUp;

		this.mHandler = _mHandler;

		(new myThread()).start();
	}

	class myThread extends Thread {

		@Override
		public void run() {
			connect2server();
		}
	}

	/*
	 * Uplink
	 */
	private static long mStartTime;
	private static long mEndTime; // ��������ʱ��
	private static long mTime; // ����ʱ��
	private static long mInterval; // ��������

	private static long packetTime;
	private static long mLastTime; // ��һ������������ʱ��
	private static long mNextTime; // ��һ������������ʱ��
	private static long mTotalTime; // �ӿ�ʼ��д�����һ��buf��ʱ��

	private static long mTotalLen; // �ͻ�����TCP���ڣ����棩��д���������������mTotalTimeʱ����У�TCP���ڣ����棩�п��ܻ���Щ����û�з��ͳ�ȥ;����ʱ��Խ�������ԽС
	private static long mLastTotalLen;
	private static NumberFormat numF;

	/*
	 * Downlink
	 */
	private static long mStartTimed;
	private static long mEndTimed; // ��������ʱ��

	private static long packetTimed;
	private static long mLastTimed; // ��һ������������ʱ��
	private static long mNextTimed; // ��һ������������ʱ��
	private static long mTotalTimed; // �ӿ�ʼ��д�����һ��buf��ʱ��

	private static long mTotalLend; // �ͻ�����TCP���ڣ����棩��д���������������mTotalTimeʱ����У�TCP���ڣ����棩�п��ܻ���Щ����û�з��ͳ�ȥ;����ʱ��Խ�������ԽС
	private static long mLastTotalLend;

	// ////////////////////////////////

	private static String measureIP;
	private static String measureTime;
	private static String measureInterval;
	private static Socket clientSocketUp;
	private static Socket clientSocketDown;
	private static String disconnectTime;

	FileOutputStream fosUplink = null;
	static FileOutputStream fosDownlink = null;

	static boolean boolthd = false;

	private void connect2server() {

		// send 0
		mHandler.sendEmptyMessage(0);
		// btnSend.setEnabled(false);
		// btnClose.setEnabled(false);
		final long t1 = System.currentTimeMillis();

		// ���ʱ��汣��0λС��
		numF = NumberFormat.getInstance();
		numF.setMaximumFractionDigits(0);

		// ����ʱ���ɲ���argv[2]ָ������λΪmin
		mTime = Integer.parseInt(measureTime) * 60 * 1000;
		// ����argv[3]ָ�����������ڣ���λΪs
		mInterval = Integer.parseInt(measureInterval) * 1000;

		while (true) {
			try {
				// �������ӣ������в���argv[0]ָʾ��������IP��ַ��������ʹ��5001�Ŷ˿ڼ���
//				if (clientSocketUp == null) {
//					while (true) {
//						try {
//							new Thread(){
//
//								@Override
//								public void run() {
//									// TODO Auto-generated method stub
//									super.run();
//									long t2 = System.currentTimeMillis();
//									if ((t2-t1)>20000 && clientSocketUp == null) {
//										mHandler.sendEmptyMessage(4);
//									}
//								}							
//							}.start();
//							clientSocketUp = new Socket(measureIP, 5001);							
//							if (clientSocketUp != null)
//								break;
//						} catch (Exception ce) {
//							continue;
//						}
//					}
//				}

				if (clientSocketDown == null) {
					while (true) {
						try {
							clientSocketDown = new Socket(measureIP, 5002);
							if (clientSocketDown != null)
								break;
						} catch (Exception ce) {
							continue;
						}
					}
				}

				// send 1
				mHandler.sendEmptyMessage(1);
				
				mTotalLen = 0;
				mLastTotalLen = 0;
				mTotalLend = 0;
				mLastTotalLend = 0;
				
				invokeDown(clientSocketDown);//add by XQY
				
//				String connectTimeString = Config.contentDateFormat.format(new Date());
//				String local = " Local "
//						+ clientSocketUp.getLocalAddress().getHostAddress()
//						+ " port " + clientSocketUp.getLocalPort();
//				String peer = clientSocketUp.getRemoteSocketAddress()
//						.toString();
//				fosUplink.write((" ConnectTime: " + connectTimeString + local
//						+ " connected to " + peer + "\n").getBytes());

//				// uplink
//				// ÿ�����׽�����д��buf�����ݣ�����Ϊ4K,��СΪ8KB������Ϊȫ'1'
//				int bufLen = 1 * 1024;
//				// ÿ��д����ֽ���
//				int currLen = bufLen * 2;
//				String buf = "";
//				for (int i = 0; i < bufLen; i++)
//					buf += '1';
//
//				DataOutputStream outToServer = new DataOutputStream(clientSocketUp.getOutputStream());
//
//				mStartTime = System.currentTimeMillis();
//				mEndTime = mStartTime + mTime;
//				mLastTime = mStartTime;
//				mNextTime = mStartTime + mInterval;
//
//				do {
//					outToServer.writeChars(buf);
//					packetTime = System.currentTimeMillis();
//					disconnectTime = Config.contentDateFormat.format(new Date());
//
//					if (packetTime >= mNextTime) {
//						long inBytes = mTotalLen - mLastTotalLen;
//						long inStart = mLastTime - mStartTime;
//						long inStop = mNextTime - mStartTime;
//
//						// 1KB = 1024B; 1kbps = 1000bps
//						double throughput = (double) inBytes * 8
//								/ (mInterval / 1000) / 1000;
//						String rate = numF.format(throughput);
//						fosUplink.write((inStart / 1000 + "-" + inStop / 1000
//								+ " sec " + inBytes / 1024 + " KB " + rate
//								+ " kbps" + "\n").getBytes());
//						 
//						mUplinkThroughput = String.valueOf((int)throughput);//�ش�
//
//						mLastTime = mNextTime;
//						mNextTime += mInterval;
//						mLastTotalLen = mTotalLen;
//
//						while (packetTime > mNextTime) {
//							// ReportPeriodicBW();
//							inBytes = mTotalLen - mLastTotalLen;
//							inStart = mLastTime - mStartTime;
//							inStop = mNextTime - mStartTime;
//							// 1KB = 1024B; 1kbps = 1000bps
//							throughput = (double) inBytes * 8 / (mInterval / 1000) / 1000;
//							rate = numF.format(throughput);
//							fosUplink.write((inStart / 1000 + "-" + inStop
//									/ 1000 + " sec " + inBytes / 1024 + " KB "
//									+ rate + " kbps" + "\n").getBytes());
//							
//							mUplinkThroughput = String.valueOf((int)throughput);//�ش�
//							
//							mLastTime = mNextTime;
//							mNextTime += mInterval;
//							mLastTotalLen = mTotalLen;
//						}
//					}
//
//					// û���׳�IOException�Ļ���˵��д��ɹ�
//					mTotalLen += currLen;
//				} while (packetTime <= mEndTime);//add by XQy
//
//				// �������������ڼ�����ݴ�������������
//				mTotalTime = packetTime - mStartTime;
//				double throughput = (double) mTotalLen * 8
//						/ (mTotalTime / 1000) / 1000;
//				String rate = numF.format(throughput);
//				String content = "TotalTime	Transfer Throughput uplink:" + "0-"
//						+ mTotalTime / 1000 + " sec " + mTotalLen / 1024
//						+ " KB " + rate + " kbps" + "\n";
//
//				mAvgUplinkThroughput = String.valueOf((int)throughput);//�ش�
//				
//				try {
//					fosUplink.write(content.getBytes());
//				} catch (Exception e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				// �ر��׽��ֺ�����
//				clientSocketUp.close();
//
//				while (true) {
//					if (boolthd) {
//						break;
//					}
//					else {
//						Thread.sleep(1000);//add by XQY
//						continue;
//					}
//				}

				break;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

//				try {
//					fosUplink.write((disconnectTime + " disconnected " + "\n")
//							.getBytes());
//				} catch (Exception e1) {
//					// TODO Auto-generated catch block
//					e1.printStackTrace();
//				}
//				
				// send 2
				mHandler.sendEmptyMessage(2);

				// �������ӣ������в���argv[0]ָʾ��������IP��ַ��������ʹ��5001�Ŷ˿ڼ���
				while (true) {
					try {
//						clientSocketUp = new Socket(measureIP, 5001);
						clientSocketDown = new Socket(measureIP, 5002);
						if (clientSocketDown != null) {
							break;
						}
//						if (clientSocketUp != null && clientSocketDown != null) {
//							break;
//						}
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

	//private static void invokeDown(final Socket serverSocket)
	private void invokeDown(final Socket serverSocket) throws Exception {
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try {
					String connectTimeString = Config.contentDateFormat.format(new Date());
					String local = " Local "
							+ clientSocketDown.getLocalAddress().getHostAddress()
							+ " port " + clientSocketDown.getLocalPort();
					String peer = clientSocketDown.getRemoteSocketAddress()
							.toString();
					fosDownlink.write((" ConnectTime: " + connectTimeString + local
							+ " connect to " + peer + "\n").getBytes());

					// ÿ�δ��׽��ֶ������ݵ�buf��buf�ĳ�����bufLenָ��Ϊ4K
					int bufLen = 1 * 1024;
					char buf[] = new char[bufLen];

					// ÿ�ζ�����ֽ���
					int currLen = 0;

					BufferedReader inFromServer = new BufferedReader(
							new InputStreamReader(serverSocket.getInputStream()));

					mStartTimed = System.currentTimeMillis();
					mEndTimed = mStartTimed + mTime; //add by XQY
					mLastTimed = mStartTimed;
					mNextTimed = mStartTimed + mInterval;

					do {
						currLen = inFromServer.read(buf);
						// currLen = -1 means reaching the end of the stream
						if (currLen == -1)
							break;
						packetTimed = System.currentTimeMillis();

						// �����Եر������
						if (packetTimed >= mNextTimed) {
							long inBytes = mTotalLend - mLastTotalLend;
							long inStart = mLastTimed - mStartTimed;
							long inStop = mNextTimed - mStartTimed;

							// 1KB = 1024B; 1kbps = 1000bps
							double throughput = (double) inBytes * 8
									/ (mInterval / 1000) / 1000;
							String rate = numF.format(throughput);
							fosDownlink.write((inStart / 1000 + "-" + inStop
									/ 1000 + " sec " + inBytes / 1024 + " KB "
									+ rate + " kbps\n").getBytes());
												
							mDownlinkThroughput = String.valueOf((int)throughput);//�ش�
							
							mLastTimed = mNextTimed;
							mNextTimed += mInterval;
							mLastTotalLend = mTotalLend;

							while (packetTimed > mNextTimed) {
								inBytes = mTotalLend - mLastTotalLend;
								inStart = mLastTimed - mStartTimed;
								inStop = mNextTimed - mStartTimed;

								// 1KB = 1024B; 1kbps = 1000bps
								throughput = (double) inBytes * 8 / (mInterval / 1000) / 1000;
								rate = numF.format(throughput);
								fosDownlink.write((inStart / 1000 + "-" + inStop
												/ 1000 + " sec " + inBytes
												/ 1024 + " KB " + rate
												+ " kbps\n").getBytes());
								
								mDownlinkThroughput = String.valueOf((int)throughput);//�ش�
								
								mLastTimed = mNextTimed;
								mNextTimed += mInterval;
								mLastTotalLend = mTotalLend;
							}
						}

						// û���׳�IOException�Ļ���˵��д��ɹ�
						mTotalLend += currLen;
//					} while (true);
					}while (packetTimed <= mEndTimed);//add by XQY

					// �������������ڼ�����ݴ�������������
					mTotalTimed = packetTimed - mStartTimed;
					double throughput = (double) mTotalLend * 8
							/ (mTotalTimed / 1000) / 1000;
					String rate = numF.format(throughput);
					String content = "TotalTime Transfer Throughput downlink:"
							+ "0-" + mTotalTimed / 1000 + " sec " + mTotalLend
							/ 1024 + " KB " + rate + " kbps" + "\n";

					mAvgDownlinkThroughput = String.valueOf((int)throughput);//�ش�
					
					try {
						fosDownlink.write(content.getBytes());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					serverSocket.close();
					boolthd = true;

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
