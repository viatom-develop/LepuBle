package com.lepu.lepuble.ble.cmd;

import android.media.session.MediaSession;

import androidx.annotation.BinderThread;
import androidx.annotation.NonNull;

import com.lepu.lepuble.ble.utils.Am300bCRC;

import org.jetbrains.annotations.NotNull;

public class Am300bBleCmd {

    public static int TOKEN_UNIVERSAL = 0xF0;
    public static int CMD_VERSION = 0x81;
    public static int CMD_SN = 0x82;
    public static int CMD_BATTERY = 0x83;

    public static int ACK_VERSION = 0x01;
    public static int ACK_SN = 0x02;
    public static int ACK_BATTERY = 0x03;

    /*-----------------------------------------*/

    public static int TOKEN_KF= 0x69;
    public static int CMD_EMG_START = 0x81;
    public static int CMD_EMG_END = 0x82;
    public static int CMD_STIMULATE_CONFIG = 0x91;  //肌肉刺激参数设置
    public static int CMD_STIMULATE_CONFIG_QUERY = 0x9D;  //肌肉刺激参数查询
    public static int CMD_INTENSITY_CONFIG = 0x92;  //刺激强度设置
    public static int CMD_INTENSITY_QUERY = 0x9E;  //刺激强度查询
    public static int CMD_STIMULATE_START = 0x93;  //肌肉刺激治疗开始
    public static int CMD_STIMULATE_PAUSE = 0x94;  //肌肉刺激治疗暂停
    public static int CMD_STIMULATE_END = 0x95;  //肌肉刺激治疗停止
    public static int CMD_TRIGGER_START = 0x97;  //肌电触发刺激治疗开始
    public static int CMD_TRIGGER_PAUSE = 0x98;  //肌电触发刺激治疗暂停
    public static int CMD_TRIGGER_END = 0x99;  //肌电触发刺激治疗停止
    public static int CMD_MUSCLE_SINGLE = 0x9A;  //肌电触发刺激进行单次刺激
    public static int CMD_STATUS = 0x9C;  //查询下位机工作状态
    public static int CMD_SET_SN = 0xA6; // 设置SN
    public static int CMD_SET_HWGAIN = 0xA7; // 设置硬件增益

    public static int ACK_EMG_START = 0x01;
    public static int ACK_EMG_END = 0x02;
    public static int ACK_EMG_PKG = 0x03;
    public static int ACK_EMG_LEAD = 0x04;
    public static int ACK_STIMULATE_CONFIG = 0x11;  //肌肉刺激参数设置
    public static int ACK_STIMULATE_CONFIG_QUERY = 0x1D;  //肌肉刺激参数查询
    public static int ACK_INTENSITY_CONFIG = 0x12;  //刺激强度设置
    public static int ACK_INTENSITY_QUERY = 0x1E;  //刺激强度查询
    public static int ACK_STIMULATE_START = 0x13;  //肌肉刺激治疗开始
    public static int ACK_STIMULATE_PAUSE = 0x14;  //肌肉刺激治疗暂停
    public static int ACK_STIMULATE_END = 0x15;  //肌肉刺激治疗停止
    public static int ACK_STIMULATE_PKG = 0x16;
    public static int ACK_TRIGGER_START = 0x17;  //肌电触发刺激治疗开始
    public static int ACK_TRIGGER_PAUSE = 0x18;  //肌电触发刺激治疗暂停
    public static int ACK_TRIGGER_END = 0x19;  //肌电触发刺激治疗停止
    public static int ACK_MUSCLE_SINGLE = 0x1A;  //肌电触发刺激进行单次刺激
    public static int ACK_BATTERY_LOW = 0x1B;  //电池电量低，治疗终止
    public static int ACK_STATUS = 0x1C;  //查询下位机工作状态
    public static int ACK_SET_SN = 0x26; // 设置SN
    public static int ACK_SET_HWGAIN = 0xA8; // 设置硬件增益

    /*--------------------------------------------*/

    public static byte[] getVersion() {
        BleCmd cmd = new BleCmd(TOKEN_UNIVERSAL, CMD_VERSION, null);
        return cmd.toBytes();
    }
    public static byte[] getSn() {
        BleCmd cmd = new BleCmd(TOKEN_UNIVERSAL, CMD_SN, null);
        return cmd.toBytes();
    }
    public static byte[] getBattery() {
        BleCmd cmd = new BleCmd(TOKEN_UNIVERSAL, CMD_BATTERY, null);
        return cmd.toBytes();
    }

    public static byte[] EmgStart() {
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_EMG_START, null);
        return cmd.toBytes();
    }
    public static byte[] EmgEnd() {
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_EMG_END, null);
        return cmd.toBytes();
    }

    public static byte[] intensityStart(int channel) {
        byte[] content = new byte[1];
        content[0] = (byte) channel;
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_STIMULATE_START, content);
        return cmd.toBytes();
    }

    public static byte[] intensityEnd(int channel) {
        byte[] content = new byte[1];
        content[0] = (byte) channel;
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_STIMULATE_END, content);
        return cmd.toBytes();
    }

    public static byte[] setIntensityParam(
            int channel,
            int freq,
            int bandwidth,
            float raise,
            float fall,
            int duration,
            int rest
    ) {
        byte[] content = new byte[9];
        content[0] = (byte) (freq);
        content[1] = (byte) (freq >> 8);
        content[2] = (byte) (bandwidth);
        content[3] = (byte) (bandwidth >> 8);
        content[4] = (byte) (raise*10);
        content[5] = (byte) duration;
        content[6] = (byte) (fall*10);
        content[7] = (byte) rest;
        content[8] = (byte) channel;

        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_STIMULATE_CONFIG, content);
        return cmd.toBytes();
    }

    /**
     * 肌肉刺激参数查询
     * @return
     */
    public static byte[] queryIntensityParam() {
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_STIMULATE_CONFIG_QUERY, null);
        return cmd.toBytes();
    }

    public static byte[] setIntensity(
            int value,
            int channel
    ) {
        byte[] content = new byte[3];
        content[0] = (byte) value;
        content[1] = (byte) 3;
        content[2] = (byte) channel;

        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_INTENSITY_CONFIG, content);
        return cmd.toBytes();
    }

    /**
     * 刺激强度查询
     */
    public static byte[] queryIntensity() {
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_INTENSITY_QUERY, null);
        return cmd.toBytes();
    }

    /**
     * 下位机工作状态查询
     */
    public static byte[] queryWorkingStatus() {
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_STATUS, null);
        return cmd.toBytes();
    }

    /**
     * 设置SN
     */
    public static byte[] setSn(String sn) {
        byte[] bs = new byte[4];
        for (int i = 0; i<Math.min(sn.length(), 4); i++) {
            bs[i] = (byte) sn.charAt(i);
        }

        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_SET_SN, bs);
        return cmd.toBytes();
    }

    /**
     * 设置硬件增益
     */
    public static byte[] setHwGain(int gain, int channel) {
        byte[] content = new byte[3];
        content[0] = (byte) gain;
        content[1] = (byte) (gain >> 8);

        content[2] = (byte) channel;
        BleCmd cmd = new BleCmd(TOKEN_KF, CMD_SET_HWGAIN, content);
        return cmd.toBytes();
    }

    public static class BleCmd {
//        byte header1 = (byte) 0xAA;
//        byte header2 = (byte) 0x55;
        public byte token;
        public int len;
        public byte cmd;
        public byte[] content;
        public byte crc;

        public BleCmd(int token, int cmd, byte[] content) {
            len = content == null ? 2 : content.length+2;
            this.token = (byte) token;
            this.cmd = (byte) cmd;
            this.content = content;
        }

        public BleCmd(byte[] bytes) {
            if (bytes.length < 6) {
                return;
            }
            int index = 2;
            token = bytes[index];
            index++;
            len = bytes[index] & 0xff;
            index++;
            cmd = bytes[index];
            index++;
            if (len > 2) {
                content = new byte[len-2];
                System.arraycopy(bytes, index, content, 0, len-2);
            }

        }

        @NonNull
        public byte[] toBytes() {
            byte[] buf = new byte[4+len];
            int index = 0;
            buf[index] = (byte) 0xAA;
            buf[index+1] = (byte) 0x55;
            index += 2;
            buf[index] = token;
            index++;
            buf[index] = (byte) len;
            index++;
            buf[index] = cmd;
            index++;
            if (content != null && content.length != 0) {
                System.arraycopy(content, 0, buf, index, content.length);
                index += content.length;
            }
            buf[index] = Am300bCRC.calCRC8(buf);
            return buf;
        }

        @NotNull
        @Override
        public String toString() {
            return "token: " + token + " \n"
                    + "len: " + len + " \n"
                    + "cmd: " + cmd;
        }
    }
}
