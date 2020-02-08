package joseph.at.androidconnection;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;

import java.nio.charset.StandardCharsets;
import java.sql.Time;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.callback.DataReceivedCallback;
import no.nordicsemi.android.ble.data.Data;

class 	MyBleManager extends BleManager<BleManagerCallbacks> {



	String TAG = "TAG";
                                                                      //"00001802-0000-1000-8000-00805f9b34fb"
	//uart
	public static final UUID UART_SERVICE_UUID = UUID.fromString("6e400001-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID TX_CHAR_UUID = UUID.fromString("6e400002-b5a3-f393-e0a9-e50e24dcca9e");
	public static final UUID RX_CHAR_UUID = UUID.fromString("6e400003-b5a3-f393-e0a9-e50e24dcca9e");

	// Immediate Alert
	public static final UUID IMMEDIATE_ALERT_SERVICE_UUID = UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	// private static final UUID ALERT_LEVEL_UUID = UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");
	public static final UUID CHARACTERISTIC_IMMEDIATE_ALERT= UUID.fromString("00002A06-0000-1000-8000-00805f9b34fb");

	//Battery Levels
	private static final UUID BATTERY_SERVICE = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
	private static final UUID BATTERY_LEVEL_CHARACTERISTIC = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

	//Time profile
	private static final UUID SERVICE_UUID = UUID.fromString("00001805-0000-1000-8000-00805f9b34fb");
	private static final UUID CURRENT_TIME_CHAR_UUID = UUID.fromString("00002A2B-0000-1000-8000-00805f9b34fb");



	public  final byte[] HIGH_ALERT = { 0x02 };
	public   final byte[] MILD_ALERT = { 0x01 };
	public   final byte[] NO_ALERT = { 0x00 };
	public   final byte[] Enable = { 0x07 };


	ManagerCallback managerCallback;

	// Client characteristics
	private BluetoothGattCharacteristic rxCharacteristic, txCharacteristic, alertCharacteristic,
			batteryCharacteristic, serverCharacteristics;

	MyBleManager(@NonNull final Context context) {
		super(context);
		managerCallback = (ManagerCallback) context;
	}

	@NonNull
	@Override
	protected BleManagerGattCallback getGattCallback() {
		return new MyManagerGattCallback();
	}




	@Override
	public void log(final int priority, @NonNull final String message) {
		// Please, don't log in production.
		if (priority == Log.ERROR)
			Log.println(priority, "MyBleManager", message);
	}

	/**
	 * BluetoothGatt callbacks object.
	 */
	private class MyManagerGattCallback extends BleManagerGattCallback {

		// This method will be called when the device is connected and services are discovered.
		// You need to obtain references to the characteristics and descriptors that you will use.
		// Return true if all required services are found, false otherwise.
		@Override
		public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
			final BluetoothGattService service = gatt.getService(UART_SERVICE_UUID);

			Log.d(TAG, "isRequiredServiceSupported: "+( service!=null	));

			if (service != null) {
				rxCharacteristic = service.getCharacteristic(RX_CHAR_UUID);
				txCharacteristic = service.getCharacteristic(TX_CHAR_UUID);
			}

			// Validate properties
			boolean notify = false;
			if (rxCharacteristic != null) {

				final int properties = rxCharacteristic.getProperties();
				notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
				Log.d(TAG, "isRequiredServiceSupported: notify rx"+notify);

			}

			boolean writeRequest = false;
			if (txCharacteristic != null) {

				final int properties = txCharacteristic.getProperties();
				writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
				txCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
				Log.d(TAG, "isRequiredServiceSupported: write tx"+writeRequest);

			}

			// Return true if all required services have been found

			Log.d(TAG, "isRequiredServiceSupported: "+(rxCharacteristic != null
					&& txCharacteristic != null && notify && writeRequest));

			final BluetoothGattService alertService = gatt.getService(IMMEDIATE_ALERT_SERVICE_UUID);
			Log.d(TAG, "isRequiredServiceSupported: alert service "+alertService);

			if (alertService != null) {
				alertCharacteristic = alertService.getCharacteristic(CHARACTERISTIC_IMMEDIATE_ALERT);
			}
			if(alertCharacteristic!=null){
				Log.d(TAG, "isRequiredServiceSupported: alert characteristics not null");
			}

			final BluetoothGattService batteryService = gatt.getService(BATTERY_SERVICE);

			if (batteryService != null) {
				batteryCharacteristic = batteryService.getCharacteristic(BATTERY_LEVEL_CHARACTERISTIC);
			}


			if(batteryCharacteristic!=null){
				Log.d(TAG, "isRequiredServiceSupported: battery characteristics not null");
			}

			return (rxCharacteristic != null && txCharacteristic != null && notify && writeRequest);
			//return true;
		}

		// If you have any optional services, allocate them here. Return true only if
		// they are found. 
		@Override
		protected boolean isOptionalServiceSupported(@NonNull final BluetoothGatt gatt) {
			return super.isOptionalServiceSupported(gatt);
		}

		// Initialize your device here. Often you need to enable notifications and set required
		// MTU or write some initial data. Do it here.
		@Override
		protected void initialize() {

			//uart
			/*writeCharacteristic(txCharacteristic, "Hello World!".getBytes())
					.done(device -> Log.d(TAG, "Greeting sent"))
					.enqueue();*/

			setNotificationCallback(rxCharacteristic)
					.with((device, data) -> {
						 String text = "";
						for(int i=0; i< data.size();i++){
							int value = Byte.valueOf(data.getValue()[i]).intValue();
							text = text+value+" ";
							Log.d(TAG, "\"" + value + "\" received");
						}
						managerCallback.uart(text);

					});
			requestMtu(260).enqueue();
			enableNotifications(rxCharacteristic).enqueue();


			// battery
			setNotificationCallback(batteryCharacteristic).with(((device, data) -> {
				Log.d(TAG, "battery: "+data.getIntValue(Data.FORMAT_UINT8, 0));
				managerCallback.battery(""+data.getIntValue(Data.FORMAT_UINT8, 0));
			}));

			enableNotifications(batteryCharacteristic)
					.done(device -> Log.d(TAG, "Battery Level notifications enabled"))
					.fail((device, status) -> log(Log.WARN, "Battery Level characteristic not found"))
					.enqueue();


			// Immediate ALert
			/*byte[] b = new byte[1];
			b[0] = 7;
			writeCharacteristic(alertCharacteristic,Enable)
					.done(device -> Log.d(TAG, "alert sent"+device.getName()))
					.fail(((device, status) -> Log.d(TAG, "alert failed: ")))
					.enqueue();*/
		}

		@Override
		protected void onDeviceReady() {
			super.onDeviceReady();
		}

		@Override
		protected void onManagerReady() {
			super.onManagerReady();
		}

		@Override
		protected void onServerReady(@NonNull BluetoothGattServer server) {
			super.onServerReady(server);
			//List<BluetoothDevice> d = server.getConnectedDevices();
			Log.d(TAG, "onServerReady: MyBleManger ######"	);




			/*serverCharacteristics = server.getService(TimeProfile.TIME_SERVICE).getCharacteristic(TimeProfile.CURRENT_TIME);


			long now = System.currentTimeMillis();


			if (TimeProfile.CURRENT_TIME.equals(serverCharacteristics.getUuid())){
				Log.d(TAG, "onServerReady: current time "+
						new String(TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE), StandardCharsets.UTF_8));


				server.sendResponse(MainActivity.dev, 1, BluetoothGatt.GATT_SUCCESS, 0,
						TimeProfile.exactTime256WithUpdateReason(Calendar.getInstance(), TimeProfile.UPDATE_REASON_UNKNOWN));

//				server.sendResponse(MainActivity.dev,
//						1,
//						BluetoothGatt.GATT_SUCCESS,
//						0,
//						TimeProfile.getExactTime(now, TimeProfile.ADJUST_NONE));

			}else if(TimeProfile.LOCAL_TIME_INFO.equals(serverCharacteristics.getUuid())){

				Log.d(TAG, "onServerReady: local time"+new String(TimeProfile.getLocalTimeInfo(now), StandardCharsets.UTF_8));

				server.sendResponse(MainActivity.dev, 1, BluetoothGatt.GATT_SUCCESS, 0,
						TimeProfile.timezoneWithDstOffset(Calendar.getInstance()));


//				server.sendResponse(MainActivity.dev,
//						1,
//						BluetoothGatt.GATT_SUCCESS,
//						0,
//						TimeProfile.getLocalTimeInfo(now));

			}else {

				Log.d(TAG, "onServerReady: No time");

			}*/


		}

		@Override
		protected void onDescriptorRead(@NonNull BluetoothGatt gatt, @NonNull BluetoothGattDescriptor descriptor) {
			super.onDescriptorRead(gatt, descriptor);
		}

		@Override
		protected void onDeviceDisconnected() {
			// Device disconnected. Release your references here.
			rxCharacteristic = null;
			txCharacteristic = null;
		}
	};
	
	// Define your API.
	public void sendAlert(){
		/*writeCharacteristic(txCharacteristic, "Hello World! again".getBytes())
				.done(device -> Log.d(TAG, "Greeting sent"))
				.enqueue();*/
		// Immediate ALert

		byte[] b = new byte[1];
		b[0] = 7;
		writeCharacteristic(alertCharacteristic,Enable)
				.done(device -> Log.d(TAG, "alert sent"))
				.fail(((device, status) -> Log.d(TAG, "alert failed: ")))
				.enqueue();
	}

	public void sendUart(String msg){

		writeCharacteristic(txCharacteristic, msg.getBytes())
				.done(device -> Log.d(TAG, "msg sent"))
				.enqueue();

	}



	public void refresh(){
		refreshDeviceCache();
	}


	DataReceivedCallback callbackBattery = (device, data) ->
			Log.d(TAG, "onDataReceived: "+ data.getIntValue(Data.FORMAT_UINT8, 0));


	interface  ManagerCallback{

		void uart(String data);

		void battery(String level);

	}

}