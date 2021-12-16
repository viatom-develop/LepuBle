package com.lepu.lepuble.ble.cmd;

import com.lepu.lepuble.ble.utils.BleCRC;

public class AedBleCmd {

    public static int AED_CONFIG = 0x21;

    private static int seqNo = 0;
    private static void addNo() {
        seqNo++;
        if (seqNo >= 255) {
            seqNo = 0;
        }
    }

    public static byte[] aedConfig(int a1, int a2, int a3, int c1, int c2, int c3) {
        int len = 32;

        byte[] cmd = new byte[8+len];
        int i = 0;
        cmd[i] = (byte) 0xA5;
        i++;
        cmd[i] = (byte) AED_CONFIG;
        i++;
        cmd[i] = (byte) ~AED_CONFIG;
        i++;
        cmd[i] = (byte) 0x00;
        i++;
        cmd[i] = (byte) seqNo;
        i++;
        cmd[i] = (byte) len;
        i++;
        cmd[i] = (byte) (len>>8);
        i++;

        // config
        cmd[i] = (byte) 0x00; //read_flag;
        i++;
        cmd[i] = (byte) 0x00; //is_auto_shock;  //配置，是否自动电击
        i++;
        cmd[i] = (byte) 0x00; //is_screen_version;      //配置，是否带屏幕
        i++;
        cmd[i] = (byte) 0x00; //language;       //语言 0中文 1英语 2西语
        i++;
        cmd[i] = (byte) 0x00; //record_func;    //是否开启录音功能
        i++;
        cmd[i] = (byte) 0x00; //oxi_func;       //配置，是否开启血氧功能
        i++;
        cmd[i] = (byte) 0x00; //disp_wave;      //是否显示波形
        i++;
        cmd[i] = (byte) 0x00; //CPR_mode;       //CPR模式 0仅按压 1 30:2 2 15:2
        i++;
        cmd[i] = (byte) 0x00; //CPR_time;       //CPR时间
        i++;
        cmd[i] = (byte) a1; //能量序列 成人
        cmd[i+1] = (byte) (a1 >> 8);
        cmd[i+2] = (byte) a2;
        cmd[i+3] = (byte) (a2 >> 8);
        cmd[i+4] = (byte) a3;
        cmd[i+5] = (byte) (a3 >> 8);
        i+=6;

        cmd[i] = (byte) c1; //能量序列 小儿
        cmd[i+1] = (byte) (c1 >> 8);
        cmd[i+2] = (byte) c2;
        cmd[i+3] = (byte) (c2 >> 8);
        cmd[i+4] = (byte) c3;
        cmd[i+5] = (byte) (c3 >> 8);
        i+=6;

        cmd[i] = (byte) 0x00; //power_mode;     //能量设定方式 0固定序列，1灵活序列
        i++;
        cmd[i] = (byte) 0x00; //self_check_mode;        //自检周期 0每日 1每周 2每月
        i++;
        cmd[i] = (byte) 0x00; //self_check_time_hour;   //自检时间点：小时
        i++;
        cmd[i] = (byte) 0x00; //self_check_time_min;    //自检时间点：分钟
        i++;
        cmd[i] = (byte) 0x00; //self_check_time_sec;    //自检时间点：秒
        i++;
        cmd[i] = (byte) 0x00; //report_updata_mode;     //自检报告发送周期 0关闭 1每日 2每周 3每月
        i++;
        cmd[i] = (byte) 0x00; //reserved[5];
        i+=5;

        cmd[i] = BleCRC.calCRC8(cmd);
        // crc
        addNo();
        return cmd;
    }
}
