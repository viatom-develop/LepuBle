package com.lepu.lepuble.ble.cmd;

import com.lepu.lepuble.ble.utils.BleCRC;

public class Er3BleCmd {

    public static int GET_CONFIG = 0X00;
    public static int SET_CONFIG = 0x04;
    public static int ER3_RT_DATA = 0x05;
    public static int RT_DATA = 0x06;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getConfig() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) GET_CONFIG;
        cmd[2] = (byte) ~GET_CONFIG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    /**
     * 心电测量模式：
     * 0   监护模式
     * 1   手术模式
     * 2   ST模式
     */
    public static byte[] setConfig(int mode) {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SET_CONFIG;
        cmd[2] = (byte) ~SET_CONFIG;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) mode;
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getEr3RtData() {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) ER3_RT_DATA;
        cmd[2] = (byte) ~ER3_RT_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x7D;  // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getRtData() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_DATA;
        cmd[2] = (byte) ~RT_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }
}
