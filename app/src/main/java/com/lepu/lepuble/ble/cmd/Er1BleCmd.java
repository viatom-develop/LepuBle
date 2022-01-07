package com.lepu.lepuble.ble.cmd;

import com.blankj.utilcode.util.LogUtils;
import com.lepu.lepuble.ble.utils.BleCRC;
import com.lepu.lepuble.utils.ByteArrayKt;

public class Er1BleCmd {

    public static int RT_RRI = 0x07;
    public static int SET_VIBRATE = 0x04;
    public static int GET_VIBRATE_CONFIG = 0x00;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getRtRri() {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_RRI;
        cmd[2] = (byte) ~RT_RRI;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0xFA;  //
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] setVibrate(boolean on1,int threshold1, int threshold2) {
        int len = 3;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SET_VIBRATE;
        cmd[2] = (byte) ~SET_VIBRATE;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x03;
        cmd[6] = (byte) 0x00;
        if(on1){
            cmd[7] = (byte) 0x01;
        }else{
            cmd[7] = (byte) 0x00;
        }

        cmd[8] = (byte) threshold1;
        cmd[9] = (byte) threshold2;
        cmd[10] = BleCRC.calCRC8(cmd);
        addNo();

//        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] getVibrateConfig() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_VIBRATE_CONFIG;
        cmd[2] = (byte) ~GET_VIBRATE_CONFIG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
}
