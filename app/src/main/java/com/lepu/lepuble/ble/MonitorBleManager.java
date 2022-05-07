package com.lepu.lepuble.ble;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import java.util.UUID;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.jeremyliao.liveeventbus.LiveEventBus;
import com.lepu.lepuble.objs.BleLogItem;
import com.lepu.lepuble.utils.ByteArrayKt;
import com.lepu.lepuble.vals.EventMsgConst;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.data.Data;


public class MonitorBleManager extends BleManager{

    public final static UUID service_uuid =
            UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339");
    public final static UUID write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    public final static UUID notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private BluetoothGattCharacteristic write_char, notify_char;

    private MonitorBleManager.onNotifyListener listener;

    public void setNotifyListener(onNotifyListener listener) {
        this.listener = listener;
    }

    public MonitorBleManager(@NonNull final Context context) {
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
    private class MyManagerGattCallback extends BleManager.BleManagerGattCallback {

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
                write_char.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
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
                            .fail((device, status) -> log(Log.WARN, ": " + status)))
//                    .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
//                            .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
//                    .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
//                    .add(sleep(500))
                    .add(enableNotifications(notify_char))
                    .done(device -> log(Log.INFO, "Target initialized"))
                    .enqueue();
            // You may easily enqueue more operations here like such:

//            setNotificationCallback(notify_char)
//                    .with((device, data) -> {
////                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
//                        listener.onNotify(device, data);
//                    });
            setNotify();

//            writeCharacteristic(write_char, "Hello World!".getBytes())
//                    .done(device -> log(Log.INFO, "Greetings sent"))
//                    .enqueue();
            // Set a callback for your notifications. You may also use waitForNotification(...).
            // Both callbacks will be called when notification is received.
//            waitForNotification(notify_char);

            // If you need to send very long data using Write Without Response, use split()
            // or define your own splitter in split(DataSplitter splitter, WriteProgressCallback cb).
//            writeCharacteristic(write_char, "Very, very long data that will no fit into MTU")
//                    .split()
//                    .enqueue();
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

    public void setNotify() {
        setNotificationCallback(notify_char)
                .with((device, data) -> {
//                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
                    listener.onNotify(device, data);
                    LiveEventBus.get(EventMsgConst.EventBleLog).post(new BleLogItem(BleLogItem.Companion.getRECEIVE(), data.getValue()));
                });
    }

    public void sendCmd(byte[] bytes) {

        LogUtils.d("send: " + ByteArrayKt.bytesToHex(bytes));
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

    @Override
    public void log(final int priority, @NonNull final String message) {
//        if (Build.DEBUG || priority == Log.ERROR) {
//            Log.println(priority, "MyBleManager", message);
//        }
//        LogUtils.d(message);
    }
}
