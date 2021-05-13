package com.lepu.lepuble.ble.cmd;

import com.lepu.lepuble.ble.utils.BleCRC;

public class Er1BleCmd {

    public static int RT_RRI = 0x07;

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
}
