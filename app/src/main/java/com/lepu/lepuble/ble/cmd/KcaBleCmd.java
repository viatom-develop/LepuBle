package com.lepu.lepuble.ble.cmd;

import com.blankj.utilcode.util.LogUtils;
import com.lepu.lepuble.utils.ByteArrayKt;

import java.util.ArrayList;
import java.util.Calendar;

public class KcaBleCmd {

    public static int seqNo = 0;

    /**
     * command id
     */
    public static int CMD_CONFIG = 0x01;
        public static int KEY_NIGHT_PERIOD = 0x01;
        public static int KEY_NIGHT_PERIOD_RES = 0x02;
        public static int KEY_INTERVAL = 0x03;
        public static int KEY_INTERVAL_RES = 0x04;
        public static int KEY_VOLUME = 0x05;
        public static int KEY_VOLUME_RES = 0x06;
        public static int KEY_TIME = 0x07;
        public static int KEY_TIME_RES = 0x08;

    public static int CMD_STATE = 0x02;
        public static int KEY_MEASURE_START = 0x01;
        public static int KEY_MEASURING = 0x02;
        public static int KEY_MEASURE_RESULT = 0x03;
        public static int KEY_MEASURE_RESULT_RES = 0x04;

    public static int CMD_DATA = 0x03;
        public static int KEY_SN = 0x01;
        public static int KEY_SN_RES = 0x02;
        public static int KEY_BATTERY = 0x03;
        public static int KEY_BATTERY_RES = 0x04;
        public static int KEY_HISTORY = 0x05;
        public static int KEY_HISTORY_RES = 0x06;
        public static int KEY_HISTORY_END = 0x07;
        public static int KEY_DELETE = 0x08;
        public static int KEY_DELETE_RES = 0x09;

    public static byte[] syncTimeCmd() {
        Calendar c = Calendar.getInstance();
        byte[] bs = new byte[6];
        bs[0] = (byte) (c.get(Calendar.YEAR) - 2000);
        bs[1] = (byte) (c.get(Calendar.MONTH)+1);
        bs[2] = (byte) c.get(Calendar.DAY_OF_MONTH);
        bs[3] = (byte) c.get(Calendar.HOUR_OF_DAY);
        bs[4] = (byte) c.get(Calendar.MINUTE);
        bs[5] = (byte) c.get(Calendar.SECOND);

        LogUtils.d(c.toString(), ByteArrayKt.toHex(bs));
        return getKcaCmd(CMD_CONFIG, KEY_TIME, bs);
    }

    public static byte[] getSnCmd() {

        return getKcaCmd(CMD_DATA, KEY_SN, new byte[0]);
    }

    public static byte[] getBattery() {
        return getKcaCmd(CMD_DATA, KEY_BATTERY, new byte[0]);
    }

    /**
     *
     * @param stH  start hour
     * @param stM  start minute
     * @param edH  end hour
     * @param edM  end minute
     * @return
     */
    public static byte[] setNightPeriod(int stH, int stM, int edH, int edM) {
        byte[] c = new byte[4];
        c[0] = (byte) stH;
        c[1] = (byte) stM;
        c[2] = (byte) edH;
        c[3] = (byte) edM;

        return getKcaCmd(CMD_CONFIG, KEY_NIGHT_PERIOD, c);
    }

    public static byte[] setInterval(int dayInt, int nightInt) {
        byte[] c = new byte[2];
        c[0] = (byte) dayInt;
        c[1] = (byte) nightInt;

        return getKcaCmd(CMD_CONFIG, KEY_INTERVAL, c);
    }

    public static byte[] getKcaCmd(int cmd, int key, byte[] keyVal) {
        int l2Len = 2+1+2+keyVal.length;
        byte[] c = new byte[8+l2Len];
        c[0] = 0x5A;
        // 1 -> error, ACK, version
        c[2] = (byte) (l2Len >> 8);
        c[3] = (byte) l2Len;
        // crc 4, 5

        // 6 7 seq index
        int seq = getSeqNo();
        c[6] = (byte) (seq >> 8);
        c[7] = (byte) seq;

        byte[] l2 = new byte[l2Len];
        l2[0] = (byte) cmd;
        // 9 -> version
        l2[2] = (byte) key;
        l2[3] = (byte) ((keyVal.length) >> 8);
        l2[4] = (byte) keyVal.length;
        if (keyVal.length != 0) {
            System.arraycopy(keyVal, 0, l2, 5, keyVal.length);
        }
        LogUtils.d(ByteArrayKt.toHex(keyVal), "l2: "+ ByteArrayKt.toHex(l2));
        int crc = KcaBleCrc.calCrc16(l2);
        c[4] = (byte) (crc >> 8);
        c[5] = (byte) crc;

        System.arraycopy(l2, 0, c, 8, l2Len);

        LogUtils.d("send cmd: "+ ByteArrayKt.toHex(c));
        return c;
    }

    private static int getSeqNo() {
        seqNo++;
        return seqNo;
    }

    public static class KcaPackage {
        // header -> 8 bytes
        public byte header = 0x5A;
        public boolean errFlag;
        public boolean ackFlag;
        public String version;
        public int contentLen;
        public int crc;
        public boolean crcHasErr = true;
        public int seqId;
        // content -> 0~504 bytes
        public byte[] content;

        public KcaPackage(byte[] bytes) {
            if (bytes == null || bytes.length < 8) {
                return;
            }
            if (bytes[0] != 0x5A) {
                return;
            }

            errFlag = (bytes[1] | 0xdf) == 0xff;
            ackFlag = (bytes[1] | 0xef) == 0xff;
            contentLen = ((bytes[2] & 0xff) << 8) + ((bytes[3]) & 0xff);
            crc = ((bytes[4] & 0xff) << 8) + ((bytes[5]) & 0xff);

            seqId = ((bytes[6] & 0xff) << 8) + ((bytes[7]) & 0xff);

            if (contentLen + 8 != bytes.length) {
                LogUtils.e("content 长度不匹配: " + contentLen + "~" + bytes.length);
                content = null;
            } else {
                content = new byte[contentLen];
                System.arraycopy(bytes, 8, content, 0, contentLen);

                if (crc != KcaBleCrc.calCrc16(content)) {
                    LogUtils.e("crc 错误: " + ByteArrayKt.toHex(content));
                } else {
//                    LogUtils.d(ByteArrayKt.toHex(content));
                    crcHasErr = false;
                }
            }
        }
    }

    public static class KcaContent {
        public int cmd;
        public String version;
        public int objLen;
        public ArrayList<KeyObj> keyObjs = new ArrayList<KeyObj>();

        public KcaContent(byte[] bytes) {
            if (bytes == null || bytes.length < 5) {
                return;
            }
            cmd = bytes[0] & 0xff;

            byte[] byteLeft = new byte[bytes.length - 2];
            System.arraycopy(bytes, 2, byteLeft, 0, bytes.length-2);
            hasTempObj(byteLeft);
        }

        byte[] hasTempObj(byte[] bytes) {

            TempObj obj = new TempObj(bytes);
            keyObjs.add(obj.obj);
            objLen++;

            if (obj.hasMore) {
                return hasTempObj(obj.bytesLeft);
            }

            return obj.bytesLeft;
        }
    }

    public static class KeyObj {
        public int key;
        public int valLen;
        public byte[] val;
        public int keyLen;

        public KeyObj(byte[] bytes) {
            if (bytes == null || bytes.length < 3) {
                return;
            }

            key = bytes[0] & 0xff;
            valLen = ((bytes[1] & 0x01) << 8) + ((bytes[2]) & 0xff);
            keyLen = valLen+3;
            val = new byte[valLen];
            System.arraycopy(bytes, 3, val, 0, valLen);
        }
    }

    public static class TempObj {
        public KeyObj obj;
        public boolean hasMore;
        public byte[] bytesLeft;

        public TempObj(byte[] bytes) {
            if (bytes.length < 3) {
                return;
            }
            obj = new KeyObj(bytes);
//            LogUtils.d("KeyObj: " + obj.key + "--" + ByteArrayKt.toHex(obj.val));
            hasMore = bytes.length - obj.keyLen > 3;
            bytesLeft = new byte[bytes.length - obj.keyLen];
            System.arraycopy(bytes, obj.keyLen, bytesLeft, 0, bytes.length - obj.keyLen);
        }
    }


}
