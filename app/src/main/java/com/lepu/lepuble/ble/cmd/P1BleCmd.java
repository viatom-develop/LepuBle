package com.lepu.lepuble.ble.cmd;


import com.lepu.lepuble.ble.utils.P1CRC;

public class P1BleCmd {

    public static int P1_CMD_BT_ON = 0x01;  // 开关
    public static int P1_CMD_BATTERY = 0x02;
    public static int P1_CMD_HEAT = 0x03;   // 是否加热
    public static int P1_CMD_MODE = 0x04;   // 按摩模式
    public static int P1_CMD_STRENGTH = 0x05;  // 按摩强度
    public static int P1_CMD_DURATION = 0x06;  // 按摩时长
    public static int P1_CMD_GET_STATE = 0x80;
    public static int P1_CMD_STATE = 0x81;
    public static int P1_CMD_GET_SN = 0x90; // sn
    public static int P1_CMD_SN = 0x91;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 9999) {
            seqNo = 0;
        }
    }

    public static byte[] getSn() {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_GET_SN;
        buf[6] = (byte) len;
        buf[7] = 0x00; // null
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;

    }

    public static byte[] turnOn(boolean on) {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_BT_ON;
        buf[6] = (byte) len;
        buf[7] = (byte) (on ? 0x01 : 0x00);
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    public static byte[] getBattery() {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_BATTERY;
        buf[6] = (byte) len;
//        buf[7] = (byte) (on ? 0x01 : 0x00);
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    public static byte[] heatOn(boolean on) {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_HEAT;
        buf[6] = (byte) len;
        buf[7] = (byte) (on ? 0x01 : 0x00);
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    /**
     * MODE:
     * 0x00：活力模式
     * 0x01：动感模式
     * 0x02：捶击模式
     * 0x03：舒缓模式
     * 0x04：自动模式
     * @param mode
     * @return buf
     */
    public static byte[] setMode(int mode) {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_MODE;
        buf[6] = (byte) len;
        buf[7] = (byte) mode;
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    /*
     * @param strength range: 1~15
     * @return
     */
    public static byte[] setStrength(int strength) {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_STRENGTH;
        buf[6] = (byte) len;
        buf[7] = (byte) strength;
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    /**
     * 0x00： 15min
     * 0x01： 10min
     * 0x02： 5min
     * @param duration
     * @return
     */
    public static byte[] setDuration(int duration) {
        byte[] buf = new byte[10];

        int len = 1;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_DURATION;
        buf[6] = (byte) len;
        buf[7] = (byte) duration;
        buf[8] = P1CRC.CalCrc(buf);
        buf[9] = (byte) 0xfe;

        addNo();

        return buf;
    }

    public static byte[] getState() {
        byte[] buf = new byte[9];

        int len = 0;

        buf[0] = (byte) 0x55;
        buf[1] = (byte) 0xaa;
        buf[2] = (byte) (seqNo >> 8);
        buf[3] = (byte) seqNo;
        buf[4] = (byte) 0x00; // send cmd
        buf[5] = (byte) P1_CMD_GET_STATE;
        buf[6] = (byte) len;
        buf[7] = P1CRC.CalCrc(buf);
        buf[8] = (byte) 0xfe;

        addNo();

        return buf;
    }
}
