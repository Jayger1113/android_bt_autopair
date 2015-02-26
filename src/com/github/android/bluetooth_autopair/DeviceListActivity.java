/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.android.bluetooth_autopair;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class DeviceListActivity extends Activity {

    private static final String TAG = DeviceListActivity.class.getSimpleName();

    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    private List<BluetoothDevice> mDeviceList;

    private DeviceAdapter mDeviceAdapter;

    private volatile boolean mScanning;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        init();
    }

    private void init() {
        getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.title_bar);
        setContentView(R.layout.device_list);
        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available", Toast.LENGTH_LONG).show();
            finish();
            return;
        }
        initList();
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                if (mScanning == false)
                    scanBTDevice(true);
                else
                    finish();
            }
        });

    }

    final BroadcastReceiver mBluetoothDeviceFoundReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(final Context context, final Intent intent) {
            try {
                final String action = intent.getAction();
                Log.v(TAG, "onReceive");

                if (action.equals(BluetoothDevice.ACTION_FOUND)) {
                    final BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (device != null && device.getName() != null) {
                        Log.v(TAG, "device name = " + device.getName() + " device bond state = " + device.getBondState());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                addDevice(device);
                            }
                        });
                    }
                } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
                    Log.v(TAG, "ACTION_DISCOVERY_FINISHED");
                }
            } catch (final Exception e) {
                e.printStackTrace();
            }
        }
    };

    private void initList() {
        Log.d(TAG, "initList");
        mDeviceList = new ArrayList<BluetoothDevice>();
        mDeviceAdapter = new DeviceAdapter(this, mDeviceList);

        final ListView newDevicesListView = (ListView) findViewById(R.id.new_devices);
        newDevicesListView.setAdapter(mDeviceAdapter);
        newDevicesListView.setOnItemClickListener(mDeviceClickListener);

        scanBTDevice(true);

    }

    private void scanBTDevice(final boolean enable) {
        Log.v(TAG, "scanBTDevice");
        final Button cancelButton = (Button) findViewById(R.id.btn_cancel);
        if (enable) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mScanning = true;
            final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mBluetoothDeviceFoundReceiver, filter);
            mBluetoothAdapter.startDiscovery();
            cancelButton.setText(R.string.cancel);
        } else {
            unregisterReceiver(mBluetoothDeviceFoundReceiver);
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mScanning = false;
            cancelButton.setText(R.string.scan);
        }
    }

    private void addDevice(final BluetoothDevice device) {
        boolean deviceFound = false;

        for (final BluetoothDevice listDev : mDeviceList) {
            if (listDev.getAddress().equals(device.getAddress())) {
                deviceFound = true;
                break;
            }
        }

        if (!deviceFound) {
            mDeviceList.add(device);
            mDeviceAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private final OnItemClickListener mDeviceClickListener = new OnItemClickListener() {

        @Override
        public void onItemClick(final AdapterView<?> parent, final View view, final int position, final long id) {
            final BluetoothDevice device = mDeviceList.get(position);

            final Bundle b = new Bundle();
            b.putString(BluetoothDevice.EXTRA_DEVICE, mDeviceList.get(position).getAddress());

            final Intent result = new Intent();
            result.putExtras(b);
            setResult(Activity.RESULT_OK, result);
            finish();

        }
    };

    @Override
    protected void onPause() {
        super.onPause();
        scanBTDevice(false);
    }

    class DeviceAdapter extends BaseAdapter {
        Context context;

        List<BluetoothDevice> mDeviceList;

        LayoutInflater inflater;

        public DeviceAdapter(final Context context, final List<BluetoothDevice> devices) {
            this.context = context;
            inflater = LayoutInflater.from(context);
            this.mDeviceList = devices;
        }

        @Override
        public int getCount() {
            return mDeviceList.size();
        }

        @Override
        public Object getItem(final int position) {
            return mDeviceList.get(position);
        }

        @Override
        public long getItemId(final int position) {
            return position;
        }

        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            ViewGroup vg;

            if (convertView != null) {
                vg = (ViewGroup) convertView;
            } else {
                vg = (ViewGroup) inflater.inflate(R.layout.device_element, null);
            }

            final BluetoothDevice device = mDeviceList.get(position);
            final TextView tvadd = ((TextView) vg.findViewById(R.id.address));
            final TextView tvname = ((TextView) vg.findViewById(R.id.name));
            final TextView tvpaired = (TextView) vg.findViewById(R.id.paired);

            tvname.setText(device.getName());
            tvadd.setText(device.getAddress());
            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                Log.i(TAG, "device::" + device.getName());
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setTextColor(Color.GRAY);
                tvpaired.setVisibility(View.VISIBLE);
                tvpaired.setText(R.string.paired);
            } else {
                tvname.setTextColor(Color.WHITE);
                tvadd.setTextColor(Color.WHITE);
                tvpaired.setVisibility(View.GONE);
            }
            return vg;
        }
    }

}
