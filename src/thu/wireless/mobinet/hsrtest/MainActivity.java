package thu.wireless.mobinet.hsrtest;

import java.io.IOException;
import java.util.Date;
import java.util.Iterator;

import com.baidu.mobstat.SendStrategyEnum;
import com.baidu.mobstat.StatService;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Criteria;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.telephony.CellLocation;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.telephony.cdma.CdmaCellLocation;
import android.telephony.gsm.GsmCellLocation;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author XQY 
 * �л���¼��������
 * �ֻ�������־���ܲ���¼
 */
public class MainActivity extends Activity implements OnClickListener {

	private MyPhoneStateListener myListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// ������Ļ����
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		// ���ðٶ�ͳ��
		StatService.setAppChannel(this, "Baidu Market", true);
		StatService.setSendLogStrategy(this, SendStrategyEnum.APP_START, 1, false);

		// �ֻ�״̬��ؿؼ�
		Config.start = (Button) findViewById(R.id.button_Start);
		Config.start.setOnClickListener(this);

		Config.serverConentEditText = (EditText) findViewById(R.id.editText_serverIP);
		Config.serverConentEditText.clearFocus();
		Config.serverTimeEditText = (EditText) findViewById(R.id.editText_serverTime);
		Config.bufferSizeEditText = (EditText) findViewById(R.id.editText_buffer);
		
		Config.asuTextView = (TextView) findViewById(R.id.signalText);
		Config.signalParameterTextView = (TextView) findViewById(R.id.signalParameterText);
		Config.basestationTextView = (TextView) findViewById(R.id.basestationText);
		Config.directionTextView = (TextView) findViewById(R.id.directionText);
		Config.speedTextView = (TextView) findViewById(R.id.speedText);
		Config.gpsTextView = (TextView) findViewById(R.id.gpsText);
		Config.satelliteTextView = (TextView) findViewById(R.id.satelliteText);
		Config.locationTextView = (TextView) findViewById(R.id.locationText);
		Config.typeTextView = (TextView) findViewById(R.id.typeText);
		Config.reportTextView = (TextView) findViewById(R.id.serverText);
		Config.handoffTextView = (TextView) findViewById(R.id.handoffText);
		Config.netTextView = (TextView) findViewById(R.id.netText);
		Config.pingTextView = (TextView) findViewById(R.id.pingText);
		Config.portTextView = (TextView) findViewById(R.id.tv_port);

		// ��ȡ�ֻ���Ϣ
		try {
			myListener = new MyPhoneStateListener();
			Config.tel = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			Config.tel.listen(myListener, Config.phoneEvents);
			Config.providerName = "No SIM";
			Config.phoneModel = Build.MODEL;
			Config.osVersion = Build.VERSION.RELEASE;
		
			String infoString = "PhoneModel=" + Build.MODEL 
					+ "\nsdkVersion=" + Build.VERSION.SDK_INT 
					+ "\nosVersion=" + Build.VERSION.RELEASE;
			if(Config.tel.getSimState() == TelephonyManager.SIM_STATE_READY) {
				String IMSI = Config.tel.getSubscriberId();			
				if (IMSI.startsWith("46000") || IMSI.startsWith("46002")
						|| IMSI.startsWith("46007")) {
					Config.providerName = "�й��ƶ�";
				} else if (IMSI.startsWith("46001")) {
					Config.providerName = "�й���ͨ";
				} else if (IMSI.startsWith("46003")) {
					Config.providerName = "�й�����";
				} else {
					Config.providerName = "�Ǵ�½�û�";
				}
			} else {
				Config.reportTextView.setText("No SIM Card");
			}
			
			ConnectivityManager connect = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
			NetworkInfo networkInfo = connect.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			infoString += "\nProviderName=" + Config.providerName;
			infoString += "\nDetailedState=" + networkInfo.getDetailedState();
			infoString += "\nReason=" + networkInfo.getReason();
			infoString += "\nSubtypeName=" + networkInfo.getSubtypeName();
			infoString += "\nExtraInfo=" + networkInfo.getExtraInfo();
			infoString += "\nTypeName=" + networkInfo.getTypeName();
			infoString += "\nIMEI=" + Config.tel.getDeviceId();
			infoString += "\nIMSI=" + Config.tel.getSubscriberId();
			infoString += "\nNetworkOperatorName=" + Config.tel.getNetworkOperatorName();
			infoString += "\nSimOperatorName=" + Config.tel.getSimOperatorName();
			infoString += "\nSimSerialNumber=" + Config.tel.getSimSerialNumber();
			Config.fosMobile.write(infoString.getBytes());
			Config.fosMobile.write(System.getProperty("line.separator").getBytes());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

		// �˿�����
		Config.setRemoteParameter();
		System.out.println(Config.testServerip);
//		Config.serverConentEditText.setText(Config.testServerip);
		Config.serverTimeEditText.setText(Config.testMeasuretime);
		Config.bufferSizeEditText.setText(String.valueOf(Config.bufferSize));
		Config.bufferSizeEditText.setEnabled(false);

		Spinner spinner = (Spinner) findViewById(R.id.measurementTypeSpinner);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, Config.measurementNames);
		// R.layout.spinner_dropdown
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner.setAdapter(adapter);
	    spinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				// TODO Auto-generated method stub
				Config.measurementID = arg2;				
				Config.serverConentEditText.setText(Config.testServerip);
				
				//0625:182.92.155.67
				//0719:182.92.104.73
				switch (arg2) {
				case 0:
					Config.serverConentEditText.setText("182.92.212.24");
					Config.portTextView.setText("Port:" + Config.tcpDownloadPort);
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(true);
					Config.bufferSizeEditText.setText("1024");
					break;
				case 1:
					Config.serverConentEditText.setText("182.92.212.24");
					Config.portTextView.setText("Port:" + Config.tcpUploadPort);
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(true);
					Config.bufferSizeEditText.setText("1024");
					break;
				case 2:
					Config.serverConentEditText.setText("182.92.212.24");
					Config.serverTimeEditText.setText("30");
					Config.portTextView.setText("Port:" + Config.udpDownloadPort);
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(true);
					Config.bufferSizeEditText.setText("1024");
					break;
				case 3:
					Config.serverConentEditText.setText("182.92.212.24");
					Config.portTextView.setText("Port:" + Config.udpUploadPort);//0626
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(true);
					Config.bufferSizeEditText.setText("1024");
					break;
				case 4:
					Config.serverConentEditText.setText("182.92.212.24");
					Config.portTextView.setText("");
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(true);
					Config.bufferSizeEditText.setText("");
					break;
				default:
					Config.portTextView.setText("");
					Config.serverConentEditText.setEnabled(true);
					Config.serverTimeEditText.setEnabled(false);
					Config.bufferSizeEditText.setText("-1");
					Config.bufferSizeEditText.setEnabled(false);
					break;
				}
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// TODO Auto-generated method stub
				
			}
		});
		
		startThread();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		menu.add(1, 1, 0, "��ͨ�Բ���");
		menu.add(1, 2, 0, "�汾����");
		menu.add(1, 3, 0, "��ȫ�˳�");
		return true;
	}
 
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		switch (item.getItemId()) {
		case 1:
//			Intent MyIntent = new Intent(Intent.ACTION_MAIN);
//			MyIntent.addCategory(Intent.CATEGORY_HOME);
//			startActivity(MyIntent);
			if (Config.wifiState.equals("Disconnected")
					&& Config.dataConnectionState.equals("Disconnected")) {
				Config.reportTextView.setText("�����ѶϿ���������������");
				Toast.makeText(getApplicationContext(), "�����ѶϿ���������������",
						Toast.LENGTH_SHORT).show();
				return false;
			}
			Config.reportTextView.setText("Testing...");
			handler4Ping.post(runnable4Ping);
			
			new Thread(){

				@Override
				public void run() {
					// TODO Auto-generated method stub
					super.run();
					Measurement.pingCmdTest(Config.addressSina, 10);
				}				
			}.start();
			
			break;
		case 2:
			String tmp = "MobiNet���������ֻ�������״̬\r\n֧���ƶ���ͨ����������ȫ����ʽ\r\nCopyright  2014  㡼ҷ�";
			Toast.makeText(getApplicationContext(), tmp, Toast.LENGTH_LONG).show();
			break;
		case 3:
			try {
				Config.fosMobile.close();
				Config.fosSignal.close();
				Config.fosSpeed.close();
				Config.fosCell.close();
				Config.fosUplink.close();
				Config.fosDownlink.close();
				Config.fosPing.close();
				handler4Speed.removeCallbacks(runnable4Speed);
				handler4GPS.removeCallbacks(runnable4GPS);
				handler4Ping.removeCallbacks(runnable4Ping);
				handler4Wifi.removeCallbacks(runnable4Wifi);
				handler4Show.removeCallbacks(runnable4Show);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// ����
			android.os.Process.killProcess(android.os.Process.myPid());
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		try {
			Config.tel.listen(myListener, Config.phoneEvents);
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Check your SIM card!",
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		try {
			Config.tel.listen(myListener, Config.phoneEvents);
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Check your SIM card!",
					Toast.LENGTH_LONG).show();
		}
		StatService.onPause(this);
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		try {
			Config.tel.listen(myListener, Config.phoneEvents);
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Check your SIM card!",
					Toast.LENGTH_LONG).show();
		}
		StatService.onResume(this);
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setIcon(R.drawable.ic_launcher);
			builder.setTitle("Exit");
			builder.setMessage("�˳�MobiNet?");
			builder.setPositiveButton("����",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							return;
						}
					});
			builder.setNegativeButton("ȷ��",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							android.os.Process.killProcess(android.os.Process.myPid());
						}
					});
			builder.show();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.equals(Config.start)) {
			Config.testMeasuretime = Config.serverTimeEditText.getText().toString();
			if (Config.wifiState.equals("Disconnected")
					&& Config.dataConnectionState.equals("Disconnected")) {
				Config.reportTextView.setText("�����ѶϿ���������������");
				Toast.makeText(getApplicationContext(), "�����ѶϿ���������������",
						Toast.LENGTH_SHORT).show();
				return;
			}

			Config.pingFlag = 0;
			String serverIPString = Config.serverConentEditText.getText().toString();
			String measureTimeString = Config.testMeasuretime;
			String measureIntervalString = Config.testInterval;
			Config.bufferSize = Integer.valueOf(Config.bufferSizeEditText.getText().toString());
			Config.bufferSizeEditText.setEnabled(false);
			
			switch (Config.measurementID) {
			case 0:
				Config.reportTextView.setText("TCP downlink testing...");
				Config.myTcpTest = new TCPTest(mHandler, serverIPString,
						measureTimeString, measureIntervalString,
						Config.fosDownlink, 1);
				handler4Show.post(runnable4Show);
				break;
			case 1:
				Config.reportTextView.setText("TCP uplink testing...");
				Config.myTcpTest = new TCPTest(mHandler, serverIPString,
						measureTimeString, measureIntervalString,
						Config.fosUplink, 2);
				handler4Show.post(runnable4Show);
				break;
			case 2:
				Config.reportTextView.setText("UDP downlink testing...");
				Config.start.setEnabled(false);
				Config.myUdpTest = new UDPTest(serverIPString,
						measureTimeString, Config.fosDownlink, 1);
				break;
			case 3:
				Config.reportTextView.setText("UDP uplink testing...");
				Config.start.setEnabled(false);
				Config.myUdpTest = new UDPTest(serverIPString,
						measureTimeString, Config.fosUplink, 2);
				break;
			case 4:
				Config.reportTextView.setText("Ping testing...");
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//				// ping 10 times
//				handler4Ping.removeCallbacks(runnable4Ping);
//				Config.start.setEnabled(false);
//				handler4Ping.post(runnable4Ping);
//				Measurement.pingCmdTest(serverIPString, 10);
				// ping long-lived
				handler4Ping.removeCallbacks(runnable4Ping);
				Config.start.setEnabled(false);
				handler4Ping.post(runnable4Ping);
				Measurement.pingCmdProfession(serverIPString);
				break;
			default:
				Config.reportTextView.setText("Test doesn't support");
				break;
			}
		}
	}

	Handler mHandler = new Handler() {
		public void handleMessage(android.os.Message msg) {
			if (msg.what == 0) {
				Config.start.setEnabled(false);
				Config.reportTextView.setText("Connecting...");
			} else if (msg.what == 1) {
				Config.reportTextView.setText("Client has connected to server");
			} else if (msg.what == 2) {
				Config.reportTextView.setText("Reconnecting...");
			} else if (msg.what == 3) {
				Config.reportTextView.setText("Client has closed connection");
				handler4Show.removeCallbacks(runnable4Show);
				Config.netTextView.setText("ƽ������:" + Config.myTcpTest.mAvgUplinkThroughput
						+ " ƽ������:" + Config.myTcpTest.mAvgDownlinkThroughput + " kbps");
				Config.start.setEnabled(true);
			} else if (msg.what == 4) {
				Config.reportTextView.setText("Server maybe have some error");
				Config.start.setEnabled(true);
			}
		};
	};

	private class MyPhoneStateListener extends PhoneStateListener {

		@Override
		public void onSignalStrengthsChanged(SignalStrength signalStrength) {
			// TODO Auto-generated method stub
			String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
			TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			Config.networkType = telManager.getNetworkType();
			SignalUtil.getCurrentNetworkType(Config.networkType);
			
			/**
			 * ��ȡ�ź�ǿ�Ȳ���
			 * http://www.oschina.net/code/explore/android-4.0.1/telephony/java/android/telephony/SignalStrength.java
			 * 0: GsmSignalStrength(0-31) GsmBitErrorRate(0-7)
			 * 2: CdmaDbm CdmaEcio EvdoDbm EvdoEcio EvdoSnr(0-8)
			 * 7: LteSignalStrength LteRsrp LteRsrq LteRssnr LteCqi ��4G��ȫΪ-1
			 * getGsmLevel getLteLevel getCdmaLevel getEvdoLevel
			 */
			String allSignal = signalStrength.toString();
			try {
				String[] parts = allSignal.split(" ");
				Config.gsmSignalStrength = parts[1];
				Config.cdmaDbm = parts[3];
				Config.cdmaEcio = parts[4];
				Config.evdoDbm = parts[5];
				Config.evdoEcio = parts[6];
				Config.evdoSnr = parts[7];
				Config.lteSignalStrength = parts[8];
				Config.lteRsrp = parts[9];
				Config.lteRsrq = parts[10];
				Config.lteRssnr = parts[11];
				Config.lteCqi = parts[12];
			} catch (Exception e) {
				// TODO: handle exception
			}

			/**
			 * asu��Level��ϵ
			 * Note3: 30��23��19
			 * Other: 11��7��4 
			 */			
//			int level = SignalUtil.getCurrentLevel(signalStrength.isGsm());
			
			/**
			 * ��¼ȫ���ź���Ϣ
			 */
			if (signalStrength.isGsm()) {
				allSignal = (Integer.parseInt(Config.gsmSignalStrength)*2-113) + " "
						+ (Integer.parseInt(Config.lteSignalStrength)*2-113) + " "
						+ Config.lteRsrp + " "
						+ Config.lteRsrq + " "
						+ Config.lteRssnr + " "
						+ Config.networkType + " " + Config.servingCid;
			} else {
				allSignal = (Integer.parseInt(Config.gsmSignalStrength)*2-113) + " "
						+ Config.cdmaDbm + " " + Config.cdmaEcio + " " 
						+ Config.evdoDbm + " " + Config.evdoEcio + " " + Config.evdoSnr + " "
						+ Config.networkType + " " + Config.servingCid;
			}

			if (allSignal.equals(Config.lastAddition)) {
				
			} else {
				Config.lastAddition = allSignal;
				try {
					Config.fosSignal.write((date + " " + allSignal).getBytes());
					Config.fosSignal.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			Config.typeTextView.setText(Config.providerName + "-"
					+ Config.networkTypeString + " " + Config.networkType
					+ " (" + Build.MODEL + "-" + Build.VERSION.RELEASE + ")");
			
			
			String cellContent = Config.networkType + " " + Config.servingCid;
			if (cellContent.equals(Config.lastlocationString)) {
				
			} else {
				Config.lastlocationString = cellContent;				
				cellContent = date + " " + cellContent + " " + Config.servingLac + " " + Config.servingPsc;
				try {
					Config.fosCell.write(cellContent.getBytes());
					Config.fosCell.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Config.handoffNumber++;
			}
			
			String parameter = "";
			switch (Config.networkType) {
			case 4:
			case 5:
			case 6:
			case 7:
			case 12:
				Config.asuShowString = "1x:" + Config.cdmaDbm + " 3G:"
						+ Config.evdoDbm;
				parameter = "Ecio:" + Config.cdmaEcio + "/" + Config.evdoEcio
						+ " SNR:" + Config.evdoSnr;
				break;
			case 13:
				Config.asuShowString = "2G:" + Config.gsmSignalStrength
						+ " 4G:" + Config.lteSignalStrength;
				parameter = "RSRP:" + Config.lteRsrp + " RSRQ:"
						+ Config.lteRsrq + " SNR:" + Config.lteRssnr;
				break;
			default:
				Config.asuShowString = Config.gsmSignalStrength;
				parameter = "NULL";
				break;
			}
			Config.asuTextView.setText(Config.asuShowString);
			Config.signalParameterTextView.setText(parameter);

			super.onSignalStrengthsChanged(signalStrength);
		}

		@Override
		public void onDataConnectionStateChanged(int state, int networkType) {
			// TODO Auto-generated method stub
			String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
			
			if (networkType == Config.lastNetworkType) {
				
			} else {
				Config.lastNetworkType = networkType;
				
				Config.networkType = networkType;
				SignalUtil.getCurrentNetworkType(networkType);
			}		
			
			switch (state) {
			case TelephonyManager.DATA_DISCONNECTED://0
				// ����Ͽ�		
				Config.dataConnectionState = "Disconnected";
				if (Config.lastConnect) {
					Config.disconnectNumber++;
					Config.lastConnect = false;
				}
				break;
			case TelephonyManager.DATA_CONNECTING://1
				// ������������
				Config.dataConnectionState = "Connecting";
				break;
			case TelephonyManager.DATA_CONNECTED://2
				// ����������
				Config.dataConnectionState = "Connected";
				Config.lastConnect = true;
				break;
			default:
				Config.dataConnectionState = "Unknown";
				break;
			}
			
			/**
			 * д����־
			 */
			String cellContent = Config.networkType + " " + Config.servingCid;
			if (cellContent.equals(Config.lastlocationString)) {
				
			} else {
				Config.lastlocationString = cellContent;				
				cellContent = date + " " + cellContent + " " + Config.servingLac + " " + Config.servingPsc;
				try {
					Config.fosCell.write(cellContent.getBytes());
					Config.fosCell.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Config.handoffNumber++;
			}
				
			if (Config.dataConnectionState.equals(Config.lastDataStateString)) {
				
			} else {
				Config.lastDataStateString = Config.dataConnectionState;
				String dataContent = date + " S: " + Config.dataConnectionState;
				try {
					Config.fosMobile.write(dataContent.getBytes());
					Config.fosMobile.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Config.directionTextView.setText(Config.dataConnectionState + " ����:" + Config.dataDirection);
			}
			
			Config.handoffTextView.setText("�л�:" + Config.handoffNumber
					+ " ����:" + Config.disconnectNumber);

			super.onDataConnectionStateChanged(state, networkType);
		}

		@Override
		public void onDataActivity(int direction) {
			// TODO Auto-generated method stub
			String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
			
			switch (direction) {
			case TelephonyManager.DATA_ACTIVITY_NONE://0
				// No IP Traffic
				Config.dataDirection = "NONE";
				break;
			case TelephonyManager.DATA_ACTIVITY_IN://1
				Config.dataDirection = "Active";
				break;
			case TelephonyManager.DATA_ACTIVITY_OUT://2
				Config.dataDirection = "Active";
				break;
			case TelephonyManager.DATA_ACTIVITY_INOUT://3
				Config.dataDirection = "Active";
				break;
			case TelephonyManager.DATA_ACTIVITY_DORMANT://4
				// Data connection is active, but physical link is down
				Config.dataDirection = "Dormant";
				break;
			default:
				Config.dataDirection = "Unknown";
				break;
			}
			/**
			 * д����־
			 */
			if (Config.dataDirection.equals(Config.lastDataDirectionString)) {
				
			} else {
				Config.lastDataDirectionString = Config.dataDirection;
				String dataContent = date + " D: " + Config.dataDirection;
				try {
					Config.fosMobile.write(dataContent.getBytes());
					Config.fosMobile.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Config.directionTextView.setText(Config.dataConnectionState + " ����:" + Config.dataDirection);
			}

			super.onDataActivity(direction);
		}

		@Override
		public void onCellLocationChanged(CellLocation location) {
			// TODO Auto-generated method stub
			String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));
			
			TelephonyManager telManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
			Config.networkType = telManager.getNetworkType();
			SignalUtil.getCurrentNetworkType(Config.networkType);
			switch (Config.networkType) {
			case 4:
			case 5:
			case 6:
			case 7:
			case 12:
				CdmaCellLocation cdmaCellLocation = (CdmaCellLocation) location;
				Config.servingCid = cdmaCellLocation.getBaseStationId();
				Config.servingLac = cdmaCellLocation.getNetworkId();
				Config.servingPsc = cdmaCellLocation.getSystemId();
				break;
			default:
				GsmCellLocation gsmCellLocation = (GsmCellLocation) location;
				Config.servingCid = gsmCellLocation.getCid() & 0xffff;
				Config.servingLac = gsmCellLocation.getLac();
				Config.servingPsc = gsmCellLocation.getPsc();
				break;
			}
			
			String cellContent = Config.networkType + " " + Config.servingCid;
			if (cellContent.equals(Config.lastlocationString)) {
				
			} else {
				Config.lastlocationString = cellContent;				
				cellContent = date + " " + cellContent + " " + Config.servingLac + " " + Config.servingPsc;
				try {
					Config.fosCell.write(cellContent.getBytes());
					Config.fosCell.write(System.getProperty("line.separator").getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				Config.handoffNumber++;
				Config.handoffTextView.setText("�л�:" + Config.handoffNumber
						+ " ����:" + Config.disconnectNumber);
			}

			Config.basestationTextView.setText(Config.servingCid + " " + Config.servingLac);
			
			super.onCellLocationChanged(location);
		}
	}
	
	private final LocationListener locationListener = new LocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
			// TODO Auto-generated method stub
			// When GPS state change, can capture immediately
			switch (status) {
			case LocationProvider.AVAILABLE:
				Config.gpsStateString = "Available";
				Config.gpsTextView.setText(Config.gpsStateString);
				break;
			case LocationProvider.OUT_OF_SERVICE:
				Config.gpsStateString = "OutOfService";
				Config.gpsTextView.setText(Config.gpsStateString);
				Config.mobilitySpeed = "Unknown";
				break;
			case LocationProvider.TEMPORARILY_UNAVAILABLE:
				Config.gpsStateString = "Unavailable";
				Config.gpsTextView.setText(Config.gpsStateString);
				Config.mobilitySpeed = "Unknown";
				break;
			}
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO Auto-generated method stub
			// GPS����ʱ����
			Config.loc = Config.locationManager.getLastKnownLocation(provider);
			Config.gpsStateString = "Enabled";
			Config.gpsTextView.setText(Config.gpsFixNumber + "/"
					+ Config.gpsAvailableNumber + " " + Config.gpsStateString);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO Auto-generated method stub
			Config.gpsStateString = "Disabled";
			Config.gpsTextView.setText(Config.gpsFixNumber + "/"
					+ Config.gpsAvailableNumber + " " + Config.gpsStateString);
			Config.prepareGPSFlag = false;
			Config.mobilitySpeed = "Unknown";
		}

		@Override
		public void onLocationChanged(Location location) {
			// TODO Auto-generated method stub
			String date = Config.contentDateFormat.format(new Date(System.currentTimeMillis()));

			/**
			 * ����
			 */
			Config.loc = Config.locationManager.getLastKnownLocation(Config.bestProvider);
			Config.speed = Config.loc.getSpeed();
			Config.latitude = Config.loc.getLatitude();
			Config.longitude = Config.loc.getLongitude();
			Config.accuracy = Config.loc.getAccuracy();
			Config.locationTextView.setText(Config.latitude + "," + Config.longitude);

			/**
			 * ������־����
			 */
			float speed2 = (float) (Config.speed * 3.6);
			Config.mobilitySpeed = String.valueOf(speed2);
			Config.speedTextView.setText(Config.mobilitySpeed + " km/h");
			Config.speedcontent = Config.gpsStateString + " " + speed2;

			if (Config.speedcontent.equals(Config.lastlocationString)) {

			} else {
				Config.lastlocationString = Config.speedcontent;
				Config.speedcontent = date + " " + Config.speedcontent + " "
						+ Config.latitude + " "
						+ Config.longitude + " "
						+ Config.accuracy;
				try {
					Config.fosSpeed.write(Config.speedcontent.getBytes());
					Config.fosSpeed.write(System.getProperty("line.separator")
							.getBytes());
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	};

	// ��ȡ��ǰ����GPS����
	private final GpsStatus.Listener statusListener = new GpsStatus.Listener() {

		@Override
		public void onGpsStatusChanged(int event) {
			// TODO Auto-generated method stub
			LocationManager locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			GpsStatus status = locManager.getGpsStatus(null); // ȡ��ǰ״̬

			switch (event) {
			case GpsStatus.GPS_EVENT_SATELLITE_STATUS:
				// ����״̬�ı�
				int maxSatellites = status.getMaxSatellites(); // ��ȡ���ǿ�����Ĭ�����ֵ
				Iterator<GpsSatellite> it = status.getSatellites().iterator();
				Config.gpsAvailableNumber = 0;
				Config.gpsFixNumber = 0;
				while (it.hasNext() && Config.gpsAvailableNumber <= maxSatellites) {
					GpsSatellite s = it.next();
					Config.gpsAvailableNumber++;
					if (s.usedInFix()) {
						Config.gpsFixNumber++;
					}
				}
				Config.satelliteTextView.setText(Config.gpsFixNumber + "/"
						+ Config.gpsAvailableNumber);
				break;
			case GpsStatus.GPS_EVENT_STARTED:
				Config.gpsStateString = "Start";
				Config.gpsTextView.setText(Config.gpsStateString);
				break;
			case GpsStatus.GPS_EVENT_STOPPED:
				Config.gpsStateString = "Stop";
				Config.gpsTextView.setText(Config.gpsStateString);
				break;
			case GpsStatus.GPS_EVENT_FIRST_FIX:
				Config.gpsStateString = "FirstFix";
				Config.gpsTextView.setText(Config.gpsStateString);
			default:
				break;
			}
		}
	};

	private void initLocation() {
		Config.locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
		if (Config.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
				|| Config.locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Config.criteria = new Criteria();
			Config.criteria.setAccuracy(Criteria.ACCURACY_FINE); // �߾���
			Config.criteria.setAltitudeRequired(true); // ��ʾ����
			Config.criteria.setBearingRequired(true); // ��ʾ����
			Config.criteria.setSpeedRequired(true); // ��ʾ�ٶ�
			Config.criteria.setCostAllowed(false); // �������л���
			Config.criteria.setPowerRequirement(Criteria.POWER_LOW); // �͹���
			Config.bestProvider = Config.locationManager.getBestProvider(Config.criteria, true);

			// locationManager����������λ��Ϣ�ĸı�
			Config.locationManager.requestLocationUpdates(Config.bestProvider, 100, 5,
					locationListener);
			Config.locationManager.addGpsStatusListener(statusListener);
			
			Location gpsLocation = Config.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(gpsLocation == null){     
				gpsLocation = Config.locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }
            if(gpsLocation != null){     
            	Config.latitude = gpsLocation.getLatitude(); //����     
            	Config.longitude = gpsLocation.getLongitude(); //γ��  
            	Config.locationTextView.setText(Config.latitude + "," + Config.longitude);
            }
		} 
		if (Config.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Config.prepareGPSFlag = true;
		} else {
			Config.gpsTextView.setText("Disabled");
			if (Config.prepareGPSFlag) {

			} else {
				showGPSDialog(this);
			}
		}
	}
	
	private Handler handler4Speed = new Handler();

	private Runnable runnable4Speed = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			initLocation();
		}
	};

	private Handler handler4Show = new Handler();

	private Runnable runnable4Show = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Show.postDelayed(runnable4Show, 1000);
			Config.netTextView.setText("����:" + Config.myTcpTest.mUplinkThroughput + " ����:"
					+ Config.myTcpTest.mDownlinkThroughput + " kbps");
		}
	};
	
	private Handler handler4Ping = new Handler();

	private Runnable runnable4Ping = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			handler4Ping.postDelayed(runnable4Ping, 1000);
			if (Config.pingFlag == 11) {
				Config.pingTextView.setText("Ping:" + Config.pingInfo
						+ " DNS:" + Config.dnsLookupInfo + " HTTP:" + Config.httpInfo);
				Config.start.setEnabled(true);
				Config.reportTextView.setText("Ping test finished");
				Config.pingFlag = 10;
			} else if (Config.pingFlag == 12) {
				Config.reportTextView.setText("Ping test failed");
				Config.pingFlag = 10;
				Config.start.setEnabled(true);
			} else if (Config.pingFlag == 13) {
				Config.reportTextView.setText("Ping test failed");
				Config.pingFlag = 10;
				AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
				builder.setIcon(R.drawable.ic_launcher);
				builder.setTitle("Test Failure");
		        builder.setMessage("���Ļ����ݲ�֧����ͨ�Բ��ԣ����ע�����汾");
				builder.setPositiveButton("ĬĬ�ȴ�", null);
				builder.show();
			} else if (Config.pingFlag == 21) {
				Config.pingTextView.setText("Ping:" + Config.pingInfo + " DNS:"
						+ Config.dnsLookupInfo + " HTTP:" + Config.httpInfo);
				Config.reportTextView.setText("DNS lookup test finished");
				Config.start.setEnabled(true);
				Config.pingFlag = 20;
			} else if (Config.pingFlag == 22) {
				Config.reportTextView.setText("DNS lookup test failed");
				Config.start.setEnabled(true);
				Config.pingFlag = 20;
			} else if (Config.pingFlag == 31) {
				Config.pingTextView.setText("Ping:" + Config.pingInfo + " DNS:"
						+ Config.dnsLookupInfo + " HTTP:" + Config.httpInfo);
				Config.reportTextView.setText("HTTP test finished");
				Config.start.setEnabled(true);
				Config.pingFlag = 30;
			} else if (Config.pingFlag == 32) {
				Config.reportTextView.setText("HTTP test failed");
				Config.start.setEnabled(true);
				Config.pingFlag = 30;
			}
		}
	};

	private Handler handler4Wifi = new Handler();

	private Runnable runnable4Wifi = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			/**
			 * �Ƿ�����Wifi
			 */
			try {
				ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
				NetworkInfo activeNetInfo = connectivityManager
						.getActiveNetworkInfo();
				String wifiContent = null;

				if (activeNetInfo != null
						&& activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {					
					WifiManager wifiManager = (WifiManager) getSystemService(WIFI_SERVICE);
					WifiInfo wifiInfo = wifiManager.getConnectionInfo();
					Config.wifiState = "Connected:" + wifiInfo.getSSID();
					wifiContent = Config.wifiState + " RSSI:"
							+ wifiInfo.getRssi() + " Speed:" + wifiInfo.getLinkSpeed();
				} else {
					Config.wifiState = "Disconnected";
					wifiContent = Config.wifiState;
				}

				if (wifiContent.equals(Config.lastWifiState)) {

				} else {
//					Config.wifiTextView.setText(wifiContent);
					Config.lastWifiState = wifiContent;
				}
			} catch (Exception e) {
				// TODO: handle exception
			}

			handler4Wifi.postDelayed(runnable4Wifi, 1000);
		}
	};
	
	private Handler handler4GPS = new Handler();

	private Runnable runnable4GPS = new Runnable() {

		@Override
		public void run() {
			// TODO Auto-generated method stub
			LocationManager locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				initLocation();
			} else {
				handler4GPS.postDelayed(runnable4GPS, 1000);
			}
		}
	};
		
	private void showGPSDialog(Context context) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);  
        builder.setIcon(R.drawable.ic_launcher);  
        builder.setTitle("GPS Failure");
        builder.setMessage("GPS��δ��������������˶��ٶ��빴ѡ");
        builder.setPositiveButton("�ݲ�����",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {  
                    	return;
                    }  
                });  
        builder.setNegativeButton("ȥ����",  
                new DialogInterface.OnClickListener() {  
                    public void onClick(DialogInterface dialog, int whichButton) {
            			Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            			startActivityForResult(intent, 0); 
                    }  
                });  
        builder.show();  
    }
	
	private void startThread() {
		try {
			// �����߳�
			handler4Wifi.post(runnable4Wifi);
			Thread.sleep(500);
			handler4GPS.post(runnable4GPS);
			Thread.sleep(500);
			handler4Speed.post(runnable4Speed);
		} catch (Exception e) {
			// TODO: handle exception
			Toast.makeText(getApplicationContext(), "Some mistake occured!",
					Toast.LENGTH_LONG).show();
		}
	}
}
