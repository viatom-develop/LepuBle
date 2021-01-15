package com.lepu.lepuble.ble.cmd;

import androidx.annotation.IntDef;

import com.lepu.lepuble.ble.utils.BleCRC;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class Bp2BleCmd {

    public static int RT_PARAM = 0x06;
    public static int RT_WAVE = 0x07;
    public static int RT_DATA = 0x08;
    public static int SWITCH_STATE = 0x09;

    public static final int STATE_START_BP = 0x00;
    public static final int STATE_START_ECG = 0x01;
    public static final int STATE_HISTORY = 0x02;
    public static final int STATE_READY = 0x03;
    public static final int STATE_OFF = 0x04;

    @IntDef({STATE_START_BP, STATE_START_ECG, STATE_HISTORY, STATE_READY, STATE_OFF})
    @Retention(RetentionPolicy.SOURCE)
    public @interface STATE {

    }

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getRtParam() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_PARAM;
        cmd[2] = (byte) ~RT_PARAM;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len >> 8);
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getRtWave() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) RT_WAVE;
        cmd[2] = (byte) ~RT_WAVE;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len >> 8);
        cmd[7] = BleCRC.calCRC8(cmd);

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
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len >> 8);
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] switchState(int state) {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SWITCH_STATE;
        cmd[2] = (byte) ~SWITCH_STATE;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) state;
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }
}
