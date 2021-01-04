package com.lepu.lepuble.objs;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import com.blankj.utilcode.util.LogUtils;

import java.util.ArrayList;
import java.util.Optional;

public class BluetoothController {

    private final static String TAG = "BLE";
    private final static String UPDATER = "Updater";

    public static ArrayList<Integer> getModelList() {
        return modelList;
    }

    public static void setModelList(ArrayList<Integer> modelList) {
        BluetoothController.modelList = modelList;
    }

    private static ArrayList<String> connectedList = new ArrayList<String>();
    private static ArrayList<Bluetooth> bleDevices = new ArrayList<Bluetooth>();
    private static ArrayList<Bluetooth> connectedDevices = new ArrayList<Bluetooth>();
    private static ArrayList<Integer> modelList = new ArrayList<Integer>();

    public void setConnectedList(ArrayList<String> list) {
        connectedList = list;
    }

    synchronized public static boolean addDevice(Bluetooth b) {
        boolean needNotify = false;
//        Log.d(TAG, b.getName() + " mac: " + b.getMacAddr());

        if (!bleDevices.contains(b)) {
            bleDevices.add(b);
            needNotify = true;
        }
        if (!modelList.contains(b.getModel())) {
            modelList.add(b.getModel());
            needNotify = true;
        }

        return needNotify;
    }

    synchronized static public void clear() {
        bleDevices = new ArrayList<Bluetooth>();
        connectedDevices = new ArrayList<Bluetooth>();
        modelList = new ArrayList<Integer>();
    }

    synchronized public static ArrayList<Bluetooth> getDevices() {
        return bleDevices;
    }

    synchronized public static ArrayList<Bluetooth> getDevices(@Bluetooth.MODEL int model) {
        ArrayList<Bluetooth> list = new ArrayList<Bluetooth>();
        for (Bluetooth b : bleDevices) {
            if (b.getModel() == model) {
                list.add(b);
            }
        }
//        LogUtils.d("get device: " + model + " -> " + list.size());
        return list;
    }

    synchronized public static ArrayList<Bluetooth> getConnectedDevices() {
        return connectedDevices;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    synchronized public static String getDeviceName(String address) {
        Optional<Bluetooth> optional = bleDevices.stream().filter(b -> b.getMacAddr().equals(address))
                .findFirst();
//        if(optional.isPresent()) {
//            return optional.get().getName();
//        } else {
//            return null;
//        }

        return optional.map(Bluetooth::getName).orElse(null);
//        for (Bluetooth b : bleDevices) {
//            if (b.getMacAddr().equals(address)) {
//                return b.getName();
//            }
//        }
//        return null;
    }
}
