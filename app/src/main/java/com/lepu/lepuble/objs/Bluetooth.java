package com.lepu.lepuble.objs;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Bluetooth implements Parcelable {

//    public static final String BT_NAME_O2 = "O2";
//    public static final String BT_NAME_SNO2 = "O2BAND";
//    public static final String BT_NAME_SPO2 = "SleepO2";
//    public static final String BT_NAME_O2RING = "O2Ring";
//    public static final String BT_NAME_WEARO2 = "WearO2";
//    public static final String BT_NAME_SLEEPU = "SleepU";
    public static final String BT_NAME_ER1 = "ER1";
    public static final String BT_NAME_DUOEK = "DuoEK";
    public static final String BT_NAME_ER2 = "ER2";
//    public static final String BT_NAME_PULSEBIT_EX = "Pulsebit";
    public static final String BT_NAME_OXY_LINK = "Oxylink";
    public static final String BT_NAME_KIDS_O2 = "KidsO2";
    public static final String BT_NAME_FETAL = "MD1000AF4";
    public static final String BT_NAME_BP2 = "BP2";
    public static final String BT_NAME_BP2W = "BP2W";
    public static final String BT_NAME_BP2A = "BP2A";
    public static final String BT_NAME_BP2T = "BP2T";
    public static final String BT_NAME_RINGO2 = "O2NCI";
    public static final String BT_NAME_KCA = "KCA"; // 康康血压计
    public static final String BT_NAME_O2MAX = "O2M"; // O2 Max
    public static final String BT_NAME_ER3 = "ER3";
    public static final String BT_NAME_S1 = "le S1";
    public static final String BT_NAME_P1 = "LEM1"; // HOSO
    public static final String BT_NAME_300B = "AM300"; // HOSO
    public static final String BT_NAME_AED = "UrTransfer"; // HOSO
    public static final String BT_NAME_MONITOR = "Checkme"; // Checkme Monitor


    public static final int MODEL_UNRECOGNIZED = 0;
    public static final int MODEL_CHECKO2 = 1;
    public static final int MODEL_SNOREO2 = 2;
    public static final int MODEL_SLEEPO2 = 3;
    public static final int MODEL_O2RING = 4;
    public static final int MODEL_WEARO2 = 5;
    public static final int MODEL_SLEEPU = 6;
    public static final int MODEL_ER1 = 7;
    public static final int MODEL_ER2 = 8;
    public static final int MODEL_PULSEBITEX = 9;
    public static final int MODEL_OXYLINK = 10;
    public static final int MODEL_KIDSO2 = 11;
    public static final int MODEL_FETAL = 12;
    public static final int MODEL_BP2 = 13;
    public static final int MODEL_RINGO2 = 14;
    public static final int MODEL_KCA = 15;
    public static final int MODEL_O2MAX = 16;
    public static final int MODEL_ER3 = 17;
    public static final int MODEL_AIRBP = 18;
    public static final int MODEL_S1= 19;
    public static final int MODEL_P1 = 20;
    public static final int MODEL_BP2A = 21;
    public static final int MODEL_300B = 22;
    public static final int MODEL_BP2w = 23;
    public static final int MODEL_AED = 24;
    public static final int MODEL_MONITOR = 25;

    @IntDef({MODEL_UNRECOGNIZED, MODEL_CHECKO2, MODEL_SNOREO2, MODEL_SLEEPO2, MODEL_O2RING, MODEL_WEARO2, MODEL_SLEEPU, MODEL_ER1, MODEL_ER2, MODEL_PULSEBITEX, MODEL_OXYLINK, MODEL_KIDSO2, MODEL_FETAL, MODEL_BP2, MODEL_RINGO2, MODEL_KCA, MODEL_O2MAX, MODEL_ER3, MODEL_AIRBP, MODEL_S1, MODEL_P1, MODEL_BP2A, MODEL_300B, MODEL_BP2w, MODEL_AED, MODEL_MONITOR})
    @Retention(RetentionPolicy.SOURCE)
    public @interface MODEL {

    }

    public @MODEL
    static int getDeviceModel(String deviceName) {

        if (deviceName == null || deviceName.length() == 0) {
            return MODEL_UNRECOGNIZED;
        }

        if (deviceName.split(" ").length == 0)
            return MODEL_UNRECOGNIZED;

        String deviceNamePrefix = deviceName.split(" ")[0];
        switch (deviceNamePrefix) {
            case BT_NAME_ER1:
            case BT_NAME_ER2:
            case BT_NAME_DUOEK:
                return MODEL_ER1;
            case BT_NAME_ER3:
                return MODEL_ER3;
            case BT_NAME_OXY_LINK:
                return MODEL_OXYLINK;
            case BT_NAME_BP2A:
            case BT_NAME_BP2:
            case BT_NAME_BP2W:
            case BT_NAME_BP2T:
                return MODEL_BP2;
            case BT_NAME_MONITOR:
                return MODEL_MONITOR;
            default:
                if (deviceNamePrefix.contains("O2"))
                    return MODEL_CHECKO2;
                if (deviceName.startsWith(BT_NAME_S1))
                    return MODEL_S1;
                if (deviceNamePrefix.startsWith(BT_NAME_KCA))
                    return MODEL_KCA;
                if (deviceNamePrefix.startsWith(BT_NAME_P1))
                    return MODEL_P1;
                if (deviceNamePrefix.startsWith(BT_NAME_300B))
                    return MODEL_300B;
                if (deviceName.startsWith(BT_NAME_AED))
                    return MODEL_AED;
                return MODEL_UNRECOGNIZED;
        }
    }


    @MODEL
    private int model;
    private String name;
    private BluetoothDevice device;
    private String macAddr;
    private int rssi;

    public Bluetooth(@MODEL int model, String name, BluetoothDevice device, int rssi) {
        this.model = model;
        this.name = name == null ? "" : name;
        this.device = device;
        this.macAddr = device.getAddress();
        this.rssi = rssi;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bluetooth) {
            Bluetooth b = (Bluetooth) obj;
            return (this.macAddr.equals(b.getMacAddr()));
        }
        return false;
    }

    public int describeContents() {
        return 0;
    }
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(model);
        out.writeString(name);
        out.writeParcelable(device, flags);
        out.writeString(macAddr);
        out.writeInt(rssi);
    }
    public static final Creator<Bluetooth> CREATOR = new Creator<Bluetooth>() {
        public Bluetooth createFromParcel(Parcel in) {
            return new Bluetooth(in);
        }
        public Bluetooth[] newArray(int size) {
            return new Bluetooth[size];
        }
    };

    private Bluetooth(Parcel in) {
        model = in.readInt();
        name = in.readString();
        device = in.readParcelable(Bluetooth.class.getClassLoader());
        macAddr = in.readString();
        rssi = in.readInt();
    }

    public String getMacAddr() {
        return macAddr;
    }

    public void setMacAddr(String macAddr) {
        this.macAddr = macAddr;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @MODEL
    public int getModel() {
        return model;
    }

    public void setModel(@MODEL int model) {
        this.model = model;
    }

    public int getRssi() {
        return rssi;
    }

    public void setRssi(int rssi) {
        this.rssi = rssi;
    }
}
