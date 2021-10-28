package com.lepu.lepuble.ble.cmd;

import androidx.annotation.NonNull;

public class AmResponse {

    /**
     * 肌肉刺激参数
     */
    public static class IntensityParam {
        int freq_a;
        int bandwidth_a;
        float raise_a;
        float fall_a;
        int duration_a;
        int rest_a;
        int freq_b;
        int bandwidth_b;
        float raise_b;
        float fall_b;
        int duration_b;
        int rest_b;

        public IntensityParam(byte[] buf) {
            if (buf.length < 16) {
                return;
            }
            int i = 0;
            freq_a = ((buf[i] << 8) + buf[i+1]);
            i += 2;
            bandwidth_a = ((buf[i] << 8) + buf[i+1]);
            i += 2;
            raise_a = buf[i] & 0xff;
            i++;
            duration_a = buf[i] & 0xff;
            i++;
            fall_a = buf[i] & 0xff;
            i++;
            rest_a = buf[i] & 0xff;
            i++;
            freq_b = ((buf[i] << 8) + buf[i+1]);
            i += 2;
            bandwidth_b = ((buf[i] << 8) + buf[i+1]);
            i += 2;
            raise_b = buf[i] & 0xff;
            i++;
            duration_b = buf[i] & 0xff;
            i++;
            fall_b = buf[i] & 0xff;
            i++;
            rest_b = buf[i] & 0xff;
            i++;
        }

        @NonNull
        @Override
        public String toString() {
            return "肌肉刺激参数" + "\n" +
                    "Freq: " + freq_a + " ~ " + freq_b + " Hz\n" +
                    "Pulse: " + bandwidth_a + " ~ " + bandwidth_b + " us\n" +
                    "RaiseTime: " + raise_a + " ~ " + raise_b + " s\n" +
                    "StimulTime: " + duration_a + " ~ " + duration_b + " s\n" +
                    "FallTime: " + fall_a + " ~ " + fall_b + " s\n" +
                    "RestTime: " + rest_a + " ~ " + rest_b + " s\n";
        }
    }

    /**
     * 刺激强度
     */
    public static class Intensity {
        int valueA;
        int valueB;

        public Intensity(byte[] buf) {
            if (buf.length < 2) {
                return;
            }

            valueA = buf[0] & 0xff;
            valueB = buf[1] & 0xff;
        }

        @NonNull
        @Override
        public String toString() {
            return "刺激强度" + "\n" +
                    "A: " + valueA + "\n" +
                    "B: " + valueB;
        }
    }

    /**
     * 下位机工作状态
     */
    public static class WorkingStatus{
        String statusA;
        String statusB;

        public WorkingStatus(byte[] buf) {
            statusA = getStatus(buf[0]);
            statusB = getStatus(buf[1]);
        }

        @NonNull
        @Override
        public String toString() {
            return "下位机状态" + "\n" +
                    "A: " + statusA + "\n" +
                    "B: " + statusB;
        }
    }

    static String getStatus(byte b) {
        switch (b) {
            case (byte) 0x00: return "空闲";
            case (byte) 0x01: return "评估/反馈训练(EMG上传)";
            case (byte) 0x02: return "肌肉刺激";
            case (byte) 0x03: return "肌电触发刺激";
            case (byte) 0x04: return "调试模式(EMG原始数据上传)";
            case (byte) 0x05: return "充电中";
            case (byte) 0x06: return "EMG硬件增益定标";
            case (byte) 0xf1: return "硬件故障";
            default: return "undefined";
        }
    }
}
