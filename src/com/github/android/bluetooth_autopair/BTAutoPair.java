package com.github.android.bluetooth_autopair;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.github.android.bluetooth_autopair.bt_module.AutoConnect;

public class BTAutoPair extends Activity {

    private static final String TAG = BTAutoPair.class.getSimpleName();

    private static final int REQUEST_SELECT_DEVICE = 1;

    private static final int REQUEST_ENABLE_BT = 2;

    private BluetoothDevice mBluetoothDevice = null;

    private ListView mDeviceListView;

    private ArrayAdapter<String> mDevicelistAdapter;

    private Button mConnectionButton;

    private final MyHandler mHandler = new MyHandler();
    
    private AutoConnect mAutoConnect = null;
    
    public static final int MSG_CONNECT_SUCCESS = 1;
    
    public static final int MSG_CONNECT_FAIL = -1;
    
    private class MyHandler extends Handler {

        @Override
        public void handleMessage(Message msg){
           switch(msg.what){
               case MSG_CONNECT_SUCCESS:
                   mDevicelistAdapter.clear();
                   mDevicelistAdapter.add("Connected device :"+mBluetoothDevice.getName()+ " ; "+mBluetoothDevice.getAddress());
                   mDevicelistAdapter.notifyDataSetChanged();
                   break;
               case MSG_CONNECT_FAIL:
                   mDevicelistAdapter.clear();
                   mDevicelistAdapter.add("FAIL to Connected device!");
                   mDevicelistAdapter.notifyDataSetChanged();
                   break;
           }
        }

    }
    
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if (BluetoothAdapter.getDefaultAdapter() == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        init();
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d(TAG, "onRestart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
            Log.i(TAG, "onResume - BT not enabled yet");
            final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }
    
    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
        if(mAutoConnect != null){
            try {
                mAutoConnect.destroy();
            } catch (final Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            mAutoConnect = null;
        }
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {

            case REQUEST_SELECT_DEVICE:
                if (resultCode == Activity.RESULT_OK && data != null) {
                    final String deviceAddress = data.getStringExtra(BluetoothDevice.EXTRA_DEVICE);
                    mBluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(deviceAddress);
                    if(mBluetoothDevice == null) return;
                    mDevicelistAdapter.clear();
                    mDevicelistAdapter.add("Connecting to device , plz wait~");
                    mDevicelistAdapter.notifyDataSetChanged();
                    mAutoConnect.startConnect(mBluetoothDevice);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == Activity.RESULT_OK) {
                    Toast.makeText(this, "Bluetooth has turned on ", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "BT not enabled");
                    Toast.makeText(this, "Problem in BT Turning ON ", Toast.LENGTH_SHORT).show();
                    finish();
                }
                break;
            default:
                Log.e(TAG, "wrong request code");
                break;
        }
    }

    private void init(){
        mDevicelistAdapter = new ArrayAdapter<String>(this, R.layout.device_detail);
        mDeviceListView = (ListView) findViewById(R.id.listDevice);
        mDeviceListView.setAdapter(mDevicelistAdapter);
        mDeviceListView.setDivider(null);
        mConnectionButton = (Button) findViewById(R.id.btn_select);

        // Handler Disconnect & Connect button
        mConnectionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                if (!BluetoothAdapter.getDefaultAdapter().isEnabled()) {
                    Log.i(TAG, "BT not enabled yet");
                    final Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                } else {

                    final Intent newIntent = new Intent(BTAutoPair.this, DeviceListActivity.class);
                    startActivityForResult(newIntent, REQUEST_SELECT_DEVICE);
                }
            }
        });
        mAutoConnect = new AutoConnect(this,mHandler);
    }
}
