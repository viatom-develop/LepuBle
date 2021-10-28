package com.lepu.lepuble.ble.cmd;

import androidx.annotation.NonNull;

public class RunStatus {
    int status;
//    int preStatus;

    RunStatus(byte b) {
        status = b & 0x0f;
//        preStatus = (b >> 4) & 0x0f;
    }

    static String getStatusMsg(int s) {
        switch (s) {
            case 0x00: return "空闲待机(导联脱落)";
            case 0x01: return "测量准备(主机丢弃前段波形阶段)";
            case 0x02: return "记录中";
            case 0x03: return "分析存储中";
            case 0x04: return "已存储成功(满时间测量结束后一直停留此状态直到回空闲状态)";
            case 0x05: return "记录小于30s(记录中状态直接切换至此状态)";
            case 0x06: return "重测已达6次，进入待机";
            case 0x07: return "导联断开";
            default: return "undefined";
        }

    }

    @NonNull
    @Override
    public String toString() {
        return "Current Status: " + status + " " + getStatusMsg(status);
//                "Previous Status: " + preStatus + " " + getStatusMsg(preStatus);
    }
}
