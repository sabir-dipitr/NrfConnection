package joseph.at.androidconnection;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import no.nordicsemi.android.ble.BleServerManager;
import no.nordicsemi.android.ble.BleServerManagerCallbacks;

import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import androidx.annotation.NonNull;

import java.sql.Time;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static joseph.at.androidconnection.MainActivity.mBluetoothManager;
import static joseph.at.androidconnection.TimeProfile.CLIENT_CONFIG;
import static joseph.at.androidconnection.TimeProfile.CURRENT_TIME;
import static joseph.at.androidconnection.TimeProfile.LOCAL_TIME_INFO;
import static joseph.at.androidconnection.TimeProfile.TIME_SERVICE;


public class ServerManager extends BleServerManager  {
	
	
	String TAG="ServerManger";

	ServerManager(@NonNull final Context context) {
		super(context);
	}

	BluetoothGattDescriptor configDescriptor = new BluetoothGattDescriptor(CLIENT_CONFIG,
			//Read/write descriptor
			BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);



	@NonNull
	@Override
	protected List<BluetoothGattService> initializeServer() {

		Log.d(TAG, "initializeServer: ############");
		final List<BluetoothGattService> services = new ArrayList<>();


		BluetoothGattService service = new BluetoothGattService(TIME_SERVICE,
				BluetoothGattService.SERVICE_TYPE_PRIMARY);

		// Current Time characteristic
		BluetoothGattCharacteristic currentTime = new BluetoothGattCharacteristic(CURRENT_TIME,
				//Read-only characteristic, supports notifications
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ);

		// Local Time Information characteristic
		BluetoothGattCharacteristic localTime = new BluetoothGattCharacteristic(LOCAL_TIME_INFO,
				//Read-only characteristic
				BluetoothGattCharacteristic.PROPERTY_READ,
				BluetoothGattCharacteristic.PERMISSION_READ);

		service.addCharacteristic(currentTime);
		service.addCharacteristic(localTime);

		services.add(service);



		BluetoothAdapter bluetoothAdapter = mBluetoothManager.getAdapter();
		BluetoothLeAdvertiser mBluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
		if (mBluetoothLeAdvertiser == null) {
			Log.w(TAG, "Failed to create advertiser");
		}

		AdvertiseSettings settings = new AdvertiseSettings.Builder()
				.setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)
				.setConnectable(true)
				.setTimeout(0)
				.setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
				.build();

		AdvertiseData data = new AdvertiseData.Builder()
				.setIncludeDeviceName(true)
				.setIncludeTxPowerLevel(false)
				.addServiceUuid(new ParcelUuid(TimeProfile.TIME_SERVICE))
				.build();

		mBluetoothLeAdvertiser.startAdvertising(settings, data, new AdvertiseCallback() {
			/**
			 * Callback to receive information about the advertisement process.
			 */
			@Override
			public void onStartSuccess(AdvertiseSettings settingsInEffect) {
				Log.e(TAG, "LE Advertise Started.");

			}

			@Override
			public void onStartFailure(int errorCode) {
				Log.e(TAG, "LE Advertise Failed: " + errorCode);

			}
		});

		/*services.add(service(TIME_SERVICE,
				characteristic(CURRENT_TIME,
						 BluetoothGattCharacteristic.PROPERTY_READ,
						 BluetoothGattCharacteristic.PERMISSION_READ,configDescriptor)
				));

		services.add(service(TIME_SERVICE,
				characteristic(LOCAL_TIME_INFO,
						 BluetoothGattCharacteristic.PROPERTY_READ,
						BluetoothGattCharacteristic.PERMISSION_READ,configDescriptor)
		));
*/

		return services;
	}



}