package com.lepu.lepuble.file.uncompress;

public class Er3Decompress {

    public static int COM_RET_ORIGINAL  =   -128;  //需要保存原始值返回值
    public static int COM_RET_POSITIVE  =   127;   //需要保存扩展数为正数返回值
    public static int COM_RET_NEGATIVE  =   -127;  //需要保存扩展数为负数返回值

    public static int COM_MAX_VAL   =   127;    //压缩最大值
    public static int COM_MIN_VAL   =   -127;   //压缩最小值
    public static int COM_EXTEND_MAX_VAL    =   382;    //压缩扩展最大值
    public static int COM_EXTEND_MIN_VAL    =   -382;   //压缩扩展最小值

    public static int ECG_CHANNEL_MAX_NUM   =   8;

    static int mChannelNum;
    static int mUncompressStep; //解压步骤	0x00:公用 0x01~0x0F:单通道  0x11~:多通道

    /**
     * //************************************************************************
     * // 函数名称: ecg_uncompress_init
     * //
     * // 函数说明: ECG差分解压算法初始化
     * //
     * // 输入变量:
     * //			 unsigned char channel_num: 压缩数据通道数 根据实际压缩参数来 最大支持8通道
     * //
     * // 输出变量: NULL
     * //
     * // 历    史: Created by chenzhuangli : 2021/11/19
     * android: wangjiang 2022/04/01
     * //************************************************************************
     */

    /**
     * init
     */
    public Er3Decompress(int channelNum) {
        mUncompressStep = 0;
        mChannelNum = channelNum;

        lastData = new int[mChannelNum];
        uncompressData = new int[mChannelNum];
    }

    /**
     * //************************************************************************
     * // 函数名称: ecg_uncompress_alg
     * //
     * // 函数说明: ECG差分解压函数
     * //
     * // 输入变量:signed char compress_data 压缩数据单个字节参数
     * //			unsigned char *p_output_data 原始数据单个采样点输出指针
     * // 输出变量: unsigned char:返回值 解压标记
     * //
     * // 历    史: Created by chenzhuangli : 2021/11/19
     * android: wangjiang 2022/04/01
     * //************************************************************************
     */

    /**
     * decompress output
     */
    public static class Er3DecompressObj {
        boolean valid;
        int[] output;

        public Er3DecompressObj(boolean valid, int[] output) {
            this.valid = valid;
            this.output = output;
        }
    }

    static int[] lastData = null; //最后一次解压数据
    static int[] uncompressData = null; //当前采样点解压缓存  多通道有效
    static int uncompressLen; //当前采样点解压缓存已解压长度 多通道有效

    public static byte original_bitmask = 0x00; //原始数据位标记

    public int[] Decompress(byte compressData) {
        boolean compressRet = false;
        int[] output = new int[mChannelNum];

        switch (mUncompressStep) {
            case 0x00:  //正常数据解析
                if (mChannelNum == 1) {// 单通道
                    if (compressData == COM_RET_ORIGINAL) {
                        mUncompressStep = 0x01; //下一步解析原始数据
                    } else if (compressData == COM_RET_POSITIVE) {
                        mUncompressStep = 0x03;
                    } else if (compressData == COM_RET_NEGATIVE) {
                        mUncompressStep = 0x04;
                    } else {
                        output[0] = lastData[0] + compressData;
                        lastData[0] = output[0];
                        compressRet = true;
                    }

                } else {// 多通道
                    if (compressData == COM_RET_ORIGINAL) {
                        mUncompressStep = 0x11; //下一步解析原始数据
                        original_bitmask = 0x00;
                        uncompressLen = 0;
                    } else {
                        uncompressData[uncompressLen] = lastData[uncompressLen] + compressData;
                        lastData[uncompressLen] = uncompressData[uncompressLen];
                        if (++uncompressLen >= mChannelNum) {
                            System.arraycopy(uncompressData, 0, output, 0, mChannelNum);
                            uncompressLen = 0;
                            compressRet = true;
                        }
                    }
                }
                break;
            case 0x01: //原始数据字节低位
                lastData[0] = compressData & 0xff;
                mUncompressStep = 0x02;
                break;
            case 0x02: //原始数据字节高位
                output[0] = lastData[0] + (compressData << 8);
                lastData[0] = output[0];
                mUncompressStep = 0x00;
                compressRet = true;
                break;
            case 0x03:
                output[0] = COM_MAX_VAL + (lastData[0] + (compressData & 0xff));
                lastData[0] = output[0];
                mUncompressStep = 0x00;
                compressRet = true;
                break;
            case 0x04:
                output[0] = COM_MIN_VAL + (lastData[0] + (compressData & 0xff));
                lastData[0] = output[0];
                mUncompressStep = 0x00;
                compressRet = true;
                break;
            case 0x11:
                original_bitmask = compressData;
                if (original_bitmask != ((byte) 0xff) && original_bitmask != 0x00) {
                    uncompressData[0] = 0;
                }
                mUncompressStep = 0x12;
                break;
            case 0x12: //原始数据字节低位
                if ((original_bitmask & (1 << uncompressLen)) != 0) {
                    lastData[uncompressLen] = compressData & 0xff;
                    mUncompressStep = 0x13;
                } else {
                    uncompressData[uncompressLen] = lastData[uncompressLen] + compressData;
                    lastData[uncompressLen] = uncompressData[uncompressLen];
                    if (++uncompressLen >= mChannelNum) {
                        System.arraycopy(uncompressData, 0, output, 0, mChannelNum);
                        mUncompressStep = 0x00;
                        uncompressLen = 0;
                        compressRet = true;
                    }
                }
                break;
            case 0x13: //原始数据字节高位
                uncompressData[uncompressLen] = lastData[uncompressLen] | (compressData << 8);
                lastData[uncompressLen] = uncompressData[uncompressLen];
                mUncompressStep = 0x12;
                if (++uncompressLen >= mChannelNum) {
                    System.arraycopy(uncompressData, 0, output, 0, mChannelNum);
                    mUncompressStep = 0x00;
                    uncompressLen = 0;
                    compressRet = true;
                }
                break;
            default:
                break;


        }

        if (compressRet) {
            return output;
        } else {
            return null;
        }
    }
}
