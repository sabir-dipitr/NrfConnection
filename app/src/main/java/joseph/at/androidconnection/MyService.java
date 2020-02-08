package joseph.at.androidconnection;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.BleServerManagerCallbacks;

public class MyService extends Service implements BleManagerCallbacks, BleServerManagerCallbacks {

    MyBleManager manager;
    ServerManager serverManager;
    Context context;
    String TAG ="Service";
    private final IBinder mBinder = new LocalBinder();



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public class LocalBinder extends Binder {
        public    MyService getService() {
            return MyService.this;
        }
    }



   public void init(Context context){
       this.context = context;
       manager = new MyBleManager(context);
       serverManager = new ServerManager(context);
       serverManager.setManagerCallbacks((MainActivity)context);
       serverManager.open();
       manager.useServer(serverManager);
   }

  public void connection(BluetoothDevice devicee){

      manager.setGattCallbacks(new BleManagerCallbacks() {
          @Override
          public void onDeviceConnecting(@NonNull BluetoothDevice device) {
              Log.d(TAG, "onDeviceConnecting: ");
          }

          @Override
          public void onDeviceConnected(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onDeviceConnected: ");

          }

          @Override
          public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onDeviceDisconnecting: ");
          }

          @Override
          public void onDeviceDisconnected(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onDeviceDisconnected: ");
          }

          @Override
          public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onLinkLossOccurred: ");
          }

          @Override
          public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

              Log.d(TAG, "onServicesDiscovered: ");
          }

          @Override
          public void onDeviceReady(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onDeviceReady: ");





          }

          @Override
          public void onBondingRequired(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onBondingRequired: ");
          }

          @Override
          public void onBonded(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onBonded: ");
          }

          @Override
          public void onBondingFailed(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onBondingFailed: ");
          }

          @Override
          public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

              Log.d(TAG, "onError: "+message+" "+errorCode);

              manager.refresh();

          }

          @Override
          public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

              Log.d(TAG, "onDeviceNotSupported: ");
          }

          @Override
          public void onBatteryValueReceived(@NonNull BluetoothDevice device, int value) {
              Log.d(TAG, "onBatteryValueReceived: "+value);
          }
      });
      manager.connect(devicee)
              .timeout(100000)
              .useAutoConnect(true)
              .retry(3, 200)
              .done(device -> Log.i(TAG, "Device initiated"))
              .enqueue();

  }


    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onLinkLossOccurred(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onServicesDiscovered(@NonNull BluetoothDevice device, boolean optionalServicesFound) {

    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBondingRequired(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBonded(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onBondingFailed(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onError(@NonNull BluetoothDevice device, @NonNull String message, int errorCode) {

    }

    @Override
    public void onDeviceNotSupported(@NonNull BluetoothDevice device) {

    }


    @Override
    public void onServerReady() {

    }

    @Override
    public void onDeviceConnectedToServer(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnectedFromServer(@NonNull BluetoothDevice device) {

    }





}
