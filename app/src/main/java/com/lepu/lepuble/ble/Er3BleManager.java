package com.lepu.lepuble.ble;

import static no.nordicsemi.android.ble.ConnectionPriorityRequest.CONNECTION_PRIORITY_HIGH;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.blankj.utilcode.util.LogUtils;
import com.lepu.lepuble.ble.cmd.UniversalBleCmd;
import com.lepu.lepuble.vals.RunVarsKt;

import java.util.UUID;

import no.nordicsemi.android.ble.BleManager;
import no.nordicsemi.android.ble.PhyRequest;
import no.nordicsemi.android.ble.data.Data;


public class Er3BleManager extends BleManager {
    public final static UUID service_uuid =
            UUID.fromString("14839ac4-7d7e-415c-9a42-167340cf2339");
    public final static UUID write_uuid =
            UUID.fromString("8B00ACE7-EB0B-49B0-BBE9-9AEE0A26E1A3");
    public final static UUID notify_uuid =
            UUID.fromString("0734594A-A8E7-4B1A-A6B1-CD5243059A57");

    private BluetoothGattCharacteristic write_char, notify_char;

    private onNotifyListener listener;

    public void setNotifyListener(onNotifyListener listener) {
        this.listener = listener;
    }

    public Er3BleManager(@NonNull final Context context) {
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
            if (RunVarsKt.getSupport2MPhy()) {
                beginAtomicRequestQueue()
                        .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                                .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                                .fail((device, status) -> log(Log.INFO, "Requested MTU not supported: " + status)))
                        .add(setPreferredPhy(PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_LE_2M_MASK, PhyRequest.PHY_OPTION_NO_PREFERRED)
                                .fail((device, status) -> log(Log.WARN, "Requested PHY not supported: " + status)))
                        .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
                        .add(enableNotifications(notify_char))
                        .done(device -> log(Log.INFO, "Target initialized"))
                        .enqueue();
            } else {
                beginAtomicRequestQueue()
                        .add(requestMtu(247) // Remember, GATT needs 3 bytes extra. This will allow packet size of 244 bytes.
                                .with((device, mtu) -> log(Log.INFO, "MTU set to " + mtu))
                                .fail((device, status) -> log(Log.WARN, "Requested MTU not supported: " + status)))
                        .add(requestConnectionPriority(CONNECTION_PRIORITY_HIGH))
                        .add(enableNotifications(notify_char))
                        .done(device -> log(Log.INFO, "Target initialized"))
                        .enqueue();
            }
            // You may easily enqueue more operations here like such:

            setNotificationCallback(notify_char)
                    .with((device, data) -> {
//                        LogUtils.d(device.getName() + " received: " + ByteArrayKt.bytesToHex(data.getValue()));
                        listener.onNotify(device, data);
                    });

            // sync time
            syncTime();

            // get info
            getInfo();

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

    private void getInfo() {
        sendCmd(UniversalBleCmd.getInfo());
    }

    private void syncTime() {
        sendCmd(UniversalBleCmd.syncTime());
    }

    public void sendCmd(byte[] bytes) {

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

    // Define your API.

//    private abstract class DataCallback implements DataReceivedCallback {
//        @Override
//        public void onDataReceived(@NonNull final BluetoothDevice device, @NonNull final Data data) {
//            // Some validation?
//            listener.onNotify(device, data);
//            LogUtils.d("onNotify", device.getName(), data.getValue() == null ? null : data.getValue().length);
//            onNotifySet();
//        }
//
//        abstract void onNotifySet();
//    }

//    /** Initialize time machine. */
//    public void enableFluxCapacitor(final int year) {
//        waitForNotification(notify_char)
//                .trigger(
//                        writeCharacteristic(write_char, new FluxJumpRequest(year))
//                                .done(device -> log(Log.INDO, "Power on command sent"))
//                )
//                .with(new FluxHandler() {
//                    public void onFluxCapacitorEngaged() {
//                        log(Log.WARN, "Flux Capacitor enabled! Going back to the future in 3 seconds!");
//
//                        sleep(3000).enqueue();
//                        write(write_char, "Hold on!".getBytes())
//                                .done(device -> log(Log.WARN, "It's " + year + "!"))
//                                .fail((device, status) -> "Not enough flux? (status: " + status + ")")
//                                .enqueue();
//                    }
//                })
//                .enqueue();
//    }
//
//    /**
//     * Aborts time travel. Call during 3 sec after enabling Flux Capacitor and only if you don't
//     * like 2020.
//     */
//    public void abort() {
//        cancelQueue();
//    }

    @Override
    public void log(final int priority, @NonNull final String message) {
//        if (Build.DEBUG || priority == Log.ERROR) {
//            Log.println(priority, "MyBleManager", message);
//        }
//        LogUtils.d(message);
    }
}
