package com.lepu.lepuble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lepu.lepuble.ble.cmd.UniversalBleResponse;
import com.lepu.lepuble.ble.utils.BleCRC;
import com.lepu.lepuble.ble.cmd.Er1BleResponse;
import com.lepu.lepuble.ble.cmd.UniversalBleCmd;
import com.lepu.lepuble.objs.BleLogItem;
import com.lepu.lepuble.utils.ByteUtils;
import com.lepu.lepuble.vals.EventMsgConst;

import java.util.Arrays;
import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;


public class LepuBleManager extends BleManager {
    public final static UUID service_uuid =
            UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339");
    public final static UUID write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    public final static UUID notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private BluetoothGattCharacteristic write_char, notify_char;

    private onNotifyListener listener;
    private byte[] pool = null;

    public void setNotifyListener(onNotifyListener listener) {
        this.listener = listener;
    }

    public LepuBleManager(@NonNull final Context context) {
        super(context);
    }

    @NonNull
    @Override
    protected BleManagerGattCallback getGattCallback() {
        return new MyManagerGattCallback();
    }

    /**
     * BluetoothGatt callbacks object.
     */
    private class MyManagerGattCallback extends BleManagerGattCallback {

        @Override
        public boolean isRequiredServiceSupported(@NonNull final BluetoothGatt gatt) {
            final BluetoothGattService service = gatt.getService(service_uuid);
            if (service != null) {
                write_char = service.getCharacteristic(write_uuid);
                notify_char = service.getCharacteristic(notify_uuid);
            }
            // Validate properties
            boolean notify = false;
            if (notify_char != null) {
                final int properties = notify_char.getProperties();
                notify = (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
            }
            boolean writeRequest = false;
            if (write_char != null) {
                final int properties = write_char.getProperties();
                writeRequest = (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
                write_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            }
            // Return true if all required services have been found
            return write_char != null && notify_char != null
                    && notify && writeRequest;
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
            // You may enqueue multiple operations. A queue ensures that all operations are
            // performed one after another, but it is not required.
            beginAtomicRequestQueue()
                    .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                            .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                            .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
//                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
//                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
                    .add(enableNotifications(notify_char))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();
            // You may easily enqueue more operations here like such:

            //                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
            setNotificationCallback(notify_char)
//                    .with(LepuBleManager.this::onNotify);
                    .with((device, data) -> {
//                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
                        listener.onNotify(device, data);
                        LiveEventBus.get(EventMsgConst.EventBleLog).post(new BleLogItem(BleLogItem.Companion.getRECEIVE(), data.getValue()));
                    });

            // sync time
            syncTime();

            // get info
            getInfo();

        }

        @Override
        protected void onDeviceDisconnected() {
            // Device disconnected. Release your references here.
            write_char = null;
            notify_char = null;
        }

        @Override
        protected void onServicesInvalidated() {

        }
    }

    private void getInfo() {
        sendCmd(UniversalBleCmd.getInfo());
    }

    private void syncTime() {

    }

    public void sendCmd(byte[] bytes) {
        LiveEventBus.get(EventMsgConst.EventBleLog).post(new BleLogItem(BleLogItem.Companion.getSEND(), bytes));

        writeCharacteristic(write_char, bytes)
                .split()
                .done(device -> {
//                    LogUtils.d(device.getName() + " send: " + ByteArrayKt.bytesToHex(bytes));
                })
                .enqueue();
    }

    public interface onNotifyListener {
        void onNotify(BluetoothDevice device, Data data);
    }

//    public interface onNotifyListener {
//        void onResponse(UniversalBleResponse.LepuResponse response);
//    }
//
//    private void onNotify(BluetoothDevice device, Data data) {
//        if (data != null) {
//            pool = ByteUtils.add(pool, data.getValue());
//        }
//        if (pool != null) {
//            pool = hsaResponse(pool);
//        }
//    }
//
//    private byte[] hsaResponse(byte[] bytes) {
//        byte[] bytesLeft = bytes;
//
//        if (bytes == null || bytes.length <= 0) {
//            return bytes;
//        }
//
//        for (int i = 0; i < bytes.length-8; i++) {
//            if (bytes[i] != 0xa5 || bytes[i+1] != ~bytes[i+2]) {
//                continue;
//            }
//
//            // content length
//            int len = (bytes[i+5] & 0xff) + ((bytes[i+6] &0xff) << 8);
//            if (i+8+len > bytes.length) {
//                continue;
//            }
//
//            byte[] tmp = Arrays.copyOfRange(bytes, i, i+8+len);
//            if (tmp[-1] == BleCRC.calCRC8(tmp)) {
//                UniversalBleResponse.LepuResponse response = new UniversalBleResponse.LepuResponse(tmp);
//                listener.onResponse(response);
//
//                bytesLeft = (i+8+len == bytes.length) ? null : (Arrays.copyOfRange(bytes, i+8+len, bytes.length));
//
//                hsaResponse(bytesLeft);
//
//            }
//        }
//        return bytesLeft;
//    }

    @Override
    public void log(final int priority, @NonNull final String message) {
//        if (Build.DEBUG || priority == Log.ERROR) {
//            Log.println(priority, "MyBleManager", message);
//        }
//        LogUtils.d(message);
    }
}
