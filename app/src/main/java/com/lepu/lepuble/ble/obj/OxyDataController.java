package com.lepu.lepuble.ble.obj;

public class OxyDataController {

    public static int index = 0;

    public static int maxIndex;
    public static float mm2px;

    public static int[] defaultIs = new int[]{121,121,121,121,121};

    public static int[] iniDataSrc(int size) {
        int[] ints = new int[size];
        for (int i =0; i<size; i++) {
            ints[i] = 121;
        }

        return ints;
    }

    // received from device
    public static int[] dataRec = new int[0];

    public static int[] feed(int[] src, int[] fs) {
        if (fs == null || fs.length == 0) {
            fs = new int[5];
        }

        if (src == null) {
            src = defaultIs;
        }

        for (int i = 0; i<fs.length; i++) {
            int tempIndex = (index + i) % src.length;
            src[tempIndex] = fs[i];
        }

        index = (index + fs.length) % src.length;

        return src;
    }

    synchronized public static void receive(int[] fs) {
        if (fs == null || fs.length == 0) {
            return;
        }

        int[] temp = new int[dataRec.length + fs.length];
        System.arraycopy(dataRec, 0, temp, 0, dataRec.length);
        System.arraycopy(fs, 0, temp, dataRec.length, fs.length);

        dataRec = temp;

    }

    synchronized public static int[] draw(int n) {
        if (n == 0 || n > dataRec.length) {
            return null;
        }

        int[] res = new int[n];
        int[] temp = new int[dataRec.length - n];
        System.arraycopy(dataRec, 0, res, 0, n);
        System.arraycopy(dataRec, n, temp, 0, dataRec.length-n);

        dataRec = temp;

        return res;
    }

    synchronized public static void clear() {
        index = 0;
        dataRec = new int[0];
    }
}
