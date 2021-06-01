package com.lepu.lepuble.file;

import android.content.Context;
import android.text.TextUtils;
import android.util.TypedValue;

import com.blankj.utilcode.util.LogUtils;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class DataConvert {

    public static final byte COM_MAX_VAL = 127;
    public static final byte COM_MIN_VAL = -127;
    public static final short COM_EXTEND_MAX_VAL = 382;
    public static final short COM_EXTEND_MIN_VAL = -382;

    public static final byte COM_RET_ORIGINAL = -128;
    public static final byte COM_RET_POSITIVE = 127;
    public static final byte COM_RET_NEGATIVE = -127;

    public static final short UNCOM_RET_INVALI = -32768;

    public byte unCompressNum;
    public int lastCompressData;

    public DataConvert() {
        this.unCompressNum = 0;
        this.lastCompressData = 0;
    }

    public byte getUnCompressNum() {
        return unCompressNum;
    }

    public int getLastCompressData() {
        return lastCompressData;
    }

    public short unCompressAlgECG(byte compressData) {
        int ecgData = 0;
        //标志位
        switch (unCompressNum) {
            case 0:
                if (compressData == COM_RET_ORIGINAL) {
                    unCompressNum = 1;
                    ecgData = UNCOM_RET_INVALI;
                } else if (compressData == COM_RET_POSITIVE) {        //正
                    unCompressNum = 3;
                    ecgData = UNCOM_RET_INVALI;
                } else if (compressData == COM_RET_NEGATIVE) {        //负
                    unCompressNum = 4;
                    ecgData = UNCOM_RET_INVALI;
                } else {
                    ecgData = lastCompressData + compressData;
                    lastCompressData = ecgData;
                }
                break;
            case 1:            // 原始数据字节低位
//                lastCompressData = compressData & 0xFFFF;
                lastCompressData = compressData & 0xFF;
                unCompressNum = 2;
                ecgData = UNCOM_RET_INVALI;
                break;
            case 2:            //原始数据字节高位
                ecgData = lastCompressData + (compressData << 8);
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            case 3:
                ecgData = COM_MAX_VAL + (lastCompressData + (compressData & 0xFF));
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            case 4:
                ecgData = COM_MIN_VAL + (lastCompressData - (compressData & 0xFF));
                lastCompressData = ecgData;
                unCompressNum = 0;
                break;
            default:
                break;
        }
        return (short) ecgData;
    }

    public static int[] unCompressAlgECG(byte[] tmpDataArray) {
        LogUtils.d("DataConvert unCompressAlgECG");
        int[] tmpInt = new int[tmpDataArray.length];
        DataConvert convert = new DataConvert();
        for (int i = 0; i < tmpDataArray.length; i++) {
            short tmp = convert.unCompressAlgECG(tmpDataArray[i]);
            tmpInt[i] = tmp == 32767 ? 0 : tmp;
        }

        /*tmpInt = J8Arrays.stream(tmpInt)
             .filter(x -> x != -32768 )
             .map(new IntUnaryOperator() {
                @Override
                public int applyAsInt(int operand) {
                    if(operand == 32767) {
                        return 0;
                    } else {
                        return operand;
                    }
                }
            }).toArray();*/
//        tmpInt = J8Arrays.stream(tmpInt).map(new IntUnaryOperator() {
//            @Override
//            public int applyAsInt(int operand) {
//                if(operand == 32767) {
//                    return 0;
//                } else {
//                    return operand;
//                }
//            }
//        }).toArray();
        return tmpInt;
    }

    /**
     * dp 2 px
     *
     * @param dpVal
     */
    public static int dp2px(Context context, int dpVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                dpVal, context.getResources().getDisplayMetrics());
    }

    /**
     * sp 2 px
     *
     * @param spVal
     * @return
     */
    public static int sp2px(Context context, int spVal) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP,
                spVal, context.getResources().getDisplayMetrics());

    }

    public static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public static String getHexUppercase(byte b) {
        StringBuilder sb = new StringBuilder();
        int lh = b & 0x0f;
        int fh = (b & 0xf0) >> 4;
        sb.append("0x");
        sb.append(HEX_ARRAY[fh]);
        sb.append(HEX_ARRAY[lh]);
        return sb.toString();
    }

    public static String getHexLowercase(byte b) {
        return getHexUppercase(b).toLowerCase();
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String getBtNamePrefix(String btName) {
        return TextUtils.isEmpty(btName) ? "" : btName.split(" ")[0];
    }

    /**
     * BLE Scan record parsing
     * inspired by:
     * http://stackoverflow.com/questions/22016224/ble-obtain-uuid-encoded-in-advertising-packet
     */
    public static Map<Integer, String> parseRecord(byte[] scanRecord) {
        Map<Integer, String> ret = new HashMap<Integer, String>();
        int index = 0;
        while (index < scanRecord.length) {
            int length = scanRecord[index++];
            //Zero value indicates that we are done with the record now
            if (length == 0) break;

            int type = scanRecord[index];
            //if the type is zero, then we are pass the significant section of the data,
            // and we are thud done
            if (type == 0) break;

            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);
            if (data != null && data.length > 0) {
                StringBuilder hex = new StringBuilder(data.length * 2);
                //                // the data appears to be there backwards
                //                for (int bb = data.length- 1; bb >= 0; bb--){
                //                    hex.append(String.format("%02X", data[bb]));
                //                }
                for (int bb = 0; bb < data.length; bb++) {
                    hex.append(String.format("%02X", data[bb]));
                }
                ret.put(type, hex.toString());
            }
            index += length;
        }

        return ret;
    }

    public static int[] convertToIntArray(byte[] input) {
        if (input == null) {
            return new int[0];
        }
        int[] ret = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            ret[i] = input[i] & 0xff; // Range 0 to 255, not -128 to 127
        }
        return ret;
    }

    public static List<Integer> range(int min, int max) {
        List<Integer> list = new LinkedList<>();
        for (int i = min; i <= max; i++) {
            list.add(i);
        }

        return list;
    }
    public static short[] toShortArray(byte[] src) {

        int count = src.length >> 1;
        short[] dest = new short[count];
        for (int i = 0; i < count; i++) {
            dest[i] = (short) (src[i * 2] << 8 | src[2 * i + 1] & 0xff);
        }
        return dest;
    }

    public static char[] getChars(byte[] bytes) {
        Charset cs = Charset.forName("UTF-8");
        ByteBuffer bb = ByteBuffer.allocate(bytes.length);
        bb.put(bytes);
        bb.flip();
        CharBuffer cb = cs.decode(bb);
        return cb.array();
    }

    public static int toSignedShort(byte b1, byte b2) {
        return (b1 & 255) + ((b2 & 255) << 8);
    }

    public static int toShort(byte b1) {
        return b1 & 255;
    }
}
