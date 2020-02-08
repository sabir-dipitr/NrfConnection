package joseph.at.androidconnection;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import java.util.ArrayList;
import java.util.List;

import no.nordicsemi.android.ble.BleManagerCallbacks;
import no.nordicsemi.android.ble.BleServerManagerCallbacks;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanResult;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

public class MainActivity extends AppCompatActivity implements BleServerManagerCallbacks, CustomAdapter.OnAdapterItemListener, View.OnClickListener, MyBleManager.ManagerCallback {

    //    EA:A6:ED:F0:CC:91  self

    String TAG="MainActivity";
    String address ="F9:76:DD:8D:2B:EB"; //    EA:A6:ED:F0:CC:91
    MyBleManager manager;
    ServerManager serverManager;
    BluetoothLeScannerCompat scanner;
    Button btnAlert, btnUart, btnScan, btnConnect;
    static BluetoothManager mBluetoothManager;
    static BluetoothDevice dev;
    MyPref myPref;
    EditText edt;
    TextView tv;
    public static final int REQUEST_ENABLE_BT = 2;
    ListView listView;
    public CustomAdapter adapter;
    boolean scanning =false;
    private final Handler handler = new Handler();
    private final static long SCAN_DURATION = 10000;
    // BluetoothGattServer mBluetoothGattServer;
    MyService myService;



    public boolean bindService(){
        Intent bindIntent = new Intent(getApplicationContext(), MyService.class);
        boolean b = bindService(bindIntent, connection, Context.BIND_AUTO_CREATE);
        return b ;
    }

    ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            myService = ((MyService.LocalBinder) service).getService();
            myService.init(MainActivity.this);
            Log.d(TAG, "onServiceConnected: ");

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btnAlert = findViewById(R.id.btnAlert);
        btnConnect = findViewById(R.id.btnConnect);
        btnScan = findViewById(R.id.btnScan);
        btnUart = findViewById(R.id.btnUart);
        edt = findViewById(R.id.edt);
        tv = findViewById(R.id.tv);

        myPref = new MyPref(this);
        listView = findViewById(R.id.lv);
        manager = new MyBleManager(MainActivity.this);
        mBluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);

        // loadFragment(new ScannerFragment());
        //final ScannerFragment dialog = ScannerFragment.getInstance(UUID.randomUUID());
        //dialog.show(getSupportFragmentManager(), "scan_fragment");


        bindService();

        adapter = new CustomAdapter(this);
        listView.setAdapter(adapter);
       /// server code here

        Utils.letMeSleepfor(1000);

        btnAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                manager.sendAlert();
            }
        });

        Dexter.withActivity(this)
                .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION,
                        Manifest.permission.BLUETOOTH)
                .withListener(new BaseMultiplePermissionsListener(){

                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        super.onPermissionsChecked(report);
                        Log.d(TAG, "onPermissionsChecked: ");
                        showBLEDialog();
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        super.onPermissionRationaleShouldBeShown(permissions, token);
                        Log.d(TAG, "onPermissionRationaleShouldBeShown: ");
                    }
                }).check();

        btnUart.setOnClickListener(this);
        btnScan.setOnClickListener(this);
        btnConnect.setOnClickListener(this);
    }

    ScanCallback scanCallback = new ScanCallback() {

        @Override
        public void onBatchScanResults(@NonNull List<ScanResult> results) {
            super.onBatchScanResults(results);
            adapter.update(results);
            /*for(ScanResult r : results){
                if(r.getDevice().getAddress().equalsIgnoreCase(address)){

                   // Log.d(TAG, "onBatchScanResults: "+r.getDevice().getAddress());




                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    scanner.stopScan(scanCallback);


                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }






                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }




                }
            }*/
        }


        @Override
        public void onScanResult(int callbackType, @NonNull ScanResult result) {
            super.onScanResult(callbackType, result);
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.disconnect();
        serverManager.close();
    }

    @Override
    public void onServerReady() {
        Log.d(TAG, "onServerReady:## ");
    }


    @Override
    public void onDeviceConnectedToServer(@NonNull BluetoothDevice device) {
        Log.d(TAG, "onDeviceConnectedToServer:## "+device.getAddress());
        dev = device;




    }

    @Override
    public void onDeviceDisconnectedFromServer(@NonNull BluetoothDevice devicee) {
        Log.d(TAG, "onDeviceDisconnectedFromServer:### "+devicee.getAddress());
    }


    public void getDevice(){
    }

    protected void showBLEDialog() {
        final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
    }


    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        super.startActivityForResult(intent, requestCode);

        Log.d(TAG, "startActivityForResult: "+requestCode);


        if(requestCode == REQUEST_ENABLE_BT){

            scan();

        }

    }

    @Override
    public void click(BluetoothDevice devicee) {

        Log.d(TAG, "click: called");


        myPref.setPrefDeviceMac(devicee.getAddress());
        connect(devicee);
    }



    public void connect(BluetoothDevice devicee){
       ///connect call here

        myService.connection(devicee);
    }



    private void  scan(){
        scanner = BluetoothLeScannerCompat.getScanner();
        ScanSettings settings = new ScanSettings.Builder()
                .setLegacy(false)
                .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                .setReportDelay(1000)
                .setUseHardwareBatchingIfSupported(false)
                .build();
        List<ScanFilter> filters = new ArrayList<>();
        filters.add(new ScanFilter.Builder().setServiceUuid(ParcelUuid.
                fromString("00001802-0000-1000-8000-00805f9b34fb")).build());
        //  filters.add(new ScanFilter.Builder().setServiceUuid(mUuid).build());
        scanner.startScan(filters, settings, scanCallback);

        scanning = true;
        handler.postDelayed(() -> {
            if (scanning) {
                scanning = false;
                stopScan();
            }
        }, SCAN_DURATION);
    }

    private void stopScan() {
        if (scanning) {


            final BluetoothLeScannerCompat scanner = BluetoothLeScannerCompat.getScanner();
            scanner.stopScan(scanCallback);

            scanning = false;
        }
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();


        switch (id){

            case R.id.btnConnect:{

                if(myPref.getPrefDeviceMac().isEmpty()){
                    Toast.makeText(MainActivity.this,"No Saved device", Toast.LENGTH_SHORT).show();
                }else {
                    BluetoothDevice device = mBluetoothManager.getAdapter().getRemoteDevice(myPref.getPrefDeviceMac());
                    connect(device);
                }
            }
            break;
            case R.id.btnScan:{

                if(scanning){

                }else {
                    scan();
                }

            }
            break;
            case R.id.btnUart:{
                Log.d(TAG, "onClick: "+edt.getText().toString());
                manager.sendUart(edt.getText().toString());
            }
        }

    }

    @Override
    public void uart(String data) {
        if(!data.isEmpty()) {
            tv.setText(data);
        }else {
            Toast.makeText(MainActivity.this,"No uart data", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void battery(String level) {
        if(!level.isEmpty()) {
            tv.setText(level);
        }else {
            Toast.makeText(MainActivity.this,"No uart data", Toast.LENGTH_SHORT).show();
        }
    }

}
