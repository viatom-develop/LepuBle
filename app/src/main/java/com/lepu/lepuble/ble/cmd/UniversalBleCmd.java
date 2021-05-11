package com.lepu.lepuble.ble.cmd;

import com.blankj.utilcode.util.LogUtils;
import com.lepu.lepuble.ble.utils.BleCRC;
import com.lepu.lepuble.utils.ByteArrayKt;

import java.util.Calendar;

/**
 * universal command for Viatom devices
 */
public class UniversalBleCmd {

    public static int GET_INFO = 0xE1;
    public static int RESET = 0xE2;
    public static int FACTORY_RESET = 0xE3;
    public static int BURN_FACTORY_INFO = 0xEA;
    public static int BURN_LOCK_FLASH = 0xEB;
    public static int SYNC_TIME = 0xEC;
    public static int RT_DATA = 0x03;
    public static int VIBRATE_CONFIG = 0x00;
    public static int READ_FILE_LIST = 0xF1;
    public static int READ_FILE_START = 0xF2;
    public static int READ_FILE_DATA = 0xF3;
    public static int READ_FILE_END = 0xF4;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] getRtData() {
        int len = 1;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x03;
        cmd[2] = (byte) ~0x03;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x01;
        cmd[6] = (byte) 0x00;
        cmd[7] = (byte) 0x7D;  // 0 -> 125hz;  1-> 62.5hz
        cmd[8] = BleCRC.calCRC8(cmd);

        addNo();
        return cmd;
    }

    public static byte[] getInfo() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0xE1;
        cmd[2] = (byte) ~0xE1;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0;
        cmd[6] = (byte) 0;
        cmd[7] = BleCRC.calCRC8(cmd);

        addNo();

        return cmd;
    }

    public static byte[] syncTime() {
        int len = 7;

        byte cmd[] = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) SYNC_TIME;
        cmd[2] = (byte) ~SYNC_TIME;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) len;
        cmd[6] = (byte) (len>>8);

        Calendar c = Calendar.getInstance();
        LogUtils.d(c.toString());

        cmd[7] = (byte) (c.get(Calendar.YEAR));
        cmd[8] = (byte) ((c.get(Calendar.YEAR) >> 8));
        cmd[9] = (byte) (c.get(Calendar.MONTH) +1);
        cmd[10] = (byte) (c.get(Calendar.DAY_OF_MONTH));
        cmd[11] = (byte) (c.get(Calendar.HOUR_OF_DAY));
        cmd[12] = (byte) (c.get(Calendar.MINUTE));
        cmd[13] = (byte) (c.get(Calendar.SECOND));

        cmd[14] = BleCRC.calCRC8(cmd);

        addNo();

        return cmd;
    }

    public static byte[] setVibrate(boolean on1,int threshold1, int threshold2) {
        int len = 3;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) 0x04;
        cmd[2] = (byte) ~0x04;
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
        cmd[1] = (byte) 0x00;
        cmd[2] = (byte) ~0x00;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
    public static byte[] getFileList() {
        int len = 0;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_LIST;
        cmd[2] = (byte) ~READ_FILE_LIST;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

//        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] readFileStart(byte[] name,int offset) {
        int len = 20;

        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_START;
        cmd[2] = (byte) ~READ_FILE_START;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x14;
        cmd[6] = (byte) 0x00;
        int k=0;
        for(k=0;k<Math.min(15, name.length);k++){
            cmd[7+k]=name[k];
        }
        cmd[23] = (byte) (offset);
        cmd[24] = (byte) (offset >> 8);
        cmd[25] = (byte) (offset >> 16);
        cmd[26] = (byte) (offset >> 24);
        cmd[27] = BleCRC.calCRC8(cmd);
        addNo();

//        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }

    public static byte[] readFileData(int offset) {
        int len = 4;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_DATA;
        cmd[2] = (byte) ~READ_FILE_DATA;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x04;
        cmd[6] = (byte) 0x00;
        int k;
        cmd[7] = (byte) (offset);
        cmd[8] = (byte) (offset >> 8);
        cmd[9] = (byte) (offset >> 16);
        cmd[10] = (byte) (offset >> 24);

        cmd[11] = BleCRC.calCRC8(cmd);
        addNo();
//        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
    public static byte[] readFileEnd() {
        int len = 0;
        byte[] cmd = new byte[8+len];
        cmd[0] = (byte) 0xA5;
        cmd[1] = (byte) READ_FILE_END;
        cmd[2] = (byte) ~READ_FILE_END;
        cmd[3] = (byte) 0x00;
        cmd[4] = (byte) seqNo;
        cmd[5] = (byte) 0x00;
        cmd[6] = (byte) 0x00;
        cmd[7] = BleCRC.calCRC8(cmd);
        addNo();

//        LogUtils.d(ByteArrayKt.bytesToHex(cmd));
        return cmd;
    }
}
