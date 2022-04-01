#include "ecg_compress.h"

//************************************************
//单通道压缩格式
//原始数据：压缩标记(1Byte)+原始数据
//扩展数据：扩展标记(1Byte)+扩展差分
//压缩数据：差分压缩数据
//多通道压缩格式
//原始数据：压缩标记(1Byte)+位标记(1Byte)+原始数据
//压缩数据：差分压缩数据
//************************************************


#define COM_RET_ORIGINAL	(-128)		//需要保存原始值返回值
#define	COM_RET_POSITIVE	(127)		//需要保存扩展数为正数返回值
#define	COM_RET_NEGATIVE	(-127)		//需要保存扩展数为负数返回值

#define	COM_MAX_VAL			(127)			//压缩最大值
#define	COM_MIN_VAL			(-127)			//压缩最小值
#define	COM_EXTEND_MAX_VAL	(382)			//压缩扩展最大值
#define	COM_EXTEND_MIN_VAL	(-382)			//压缩扩展最小值

#define ECG_CHANNEL_MAX_NUM				8

static unsigned char m_channel_num;			//通道数


//解压相关变量
static char m_uncompress_step;				//解压步骤	0x00:公用 0x01~0x0F:单通道  0x11~:多通道

//************************************************************************
// 函数名称: ecg_uncompress_init 
//
// 函数说明: ECG差分解压算法初始化
//
// 输入变量: 
//			 unsigned char channel_num: 压缩数据通道数 根据实际压缩参数来 最大支持8通道
//
// 输出变量: NULL
//
// 历    史: Created by chenzhuangli : 2021/11/19
//************************************************************************
void ecg_uncompress_init(unsigned char channel_num)
{
	m_uncompress_step = 0;
	m_channel_num = channel_num;
	return;
}

//************************************************************************
// 函数名称: ecg_uncompress_alg 
//
// 函数说明: ECG差分解压函数
//
// 输入变量:signed char compress_data 压缩数据单个字节参数
//			unsigned char *p_output_data 原始数据单个采样点输出指针
// 输出变量: unsigned char:返回值 解压标记
//
// 历    史: Created by chenzhuangli : 2021/11/19
//************************************************************************
unsigned char ecg_uncompress_alg(signed char compress_data, short *p_output_data)
{
	static short last_data[ECG_CHANNEL_MAX_NUM] = { 0 };		//最后一次解压数据
	static short uncompress_data[ECG_CHANNEL_MAX_NUM] = { 0 };	//当前采样点解压缓存  多通道有效
	static unsigned char uncompress_len;				//当前采样点解压缓存已解压长度 多通道有效
	unsigned char uncompress_ret = UNCOM_RET_INVALI;	//解压返回值，解压标记
	static unsigned char original_bitmask = 0x00;	//原始数据位标记

	switch (m_uncompress_step) {
		case 0x00:			//正常数据解析
			if (m_channel_num == 1) {		//单通道处理				
				if (compress_data == COM_RET_ORIGINAL) {
					m_uncompress_step = 0x01;		//下一步解析原始数据
				}else if (compress_data == COM_RET_POSITIVE) {		//正
					m_uncompress_step = 0x03;
				}else if (compress_data == COM_RET_NEGATIVE) {		//负
					m_uncompress_step = 0x04;
				}else {
					p_output_data[0] = last_data[0] + compress_data;
					last_data[0] = p_output_data[0];
					uncompress_ret = UNCOM_RET_VALID;
				}
			}else {				//多通道处理				
				if (compress_data == COM_RET_ORIGINAL) {
					m_uncompress_step = 0x11;		//下一步解析原始数据
					original_bitmask = 0x00;
					uncompress_len = 0;
				} else {
					uncompress_data[uncompress_len] = last_data[uncompress_len] + compress_data;
					last_data[uncompress_len] = uncompress_data[uncompress_len];
					if (++uncompress_len >= m_channel_num) {
						for (unsigned char i = 0; i < m_channel_num; i++) {
							p_output_data[i] = uncompress_data[i];
						}
						uncompress_len = 0;
						uncompress_ret = UNCOM_RET_VALID;
					}
				}
			}			
			break;
		case 0x01:			//原始数据字节低位
			last_data[0] = (unsigned char)compress_data;
			m_uncompress_step = 0x02;
			break;
		case 0x02:			//原始数据字节高位
			p_output_data[0] = last_data[0] + (compress_data << 8);
			last_data[0] = p_output_data[0];
			m_uncompress_step = 0x00;
			uncompress_ret = UNCOM_RET_VALID;
			break;
		case 0x03:
			p_output_data[0] = COM_MAX_VAL + (last_data[0] + (unsigned char)compress_data);
			last_data[0] = p_output_data[0];
			m_uncompress_step = 0x00;
			uncompress_ret = UNCOM_RET_VALID;
			break;
		case 0x04:
			p_output_data[0] = COM_MIN_VAL + (last_data[0] - (unsigned char)compress_data);
			last_data[0] = p_output_data[0];
			m_uncompress_step = 0x00;
			uncompress_ret = UNCOM_RET_VALID;
			break;
		case 0x11:
			original_bitmask = (unsigned char)compress_data;
			if (original_bitmask != 0xFF && original_bitmask) {
				uncompress_data[0] = 0;
			}
			m_uncompress_step = 0x12;
			break;
		case 0x12:		//原始数据字节低位
			if (original_bitmask & (1 << uncompress_len)) {			//为压缩数据则继续
				last_data[uncompress_len] = (unsigned char)compress_data;
				m_uncompress_step = 0x13;				
			} else {
				uncompress_data[uncompress_len] = last_data[uncompress_len] + compress_data;
				last_data[uncompress_len] = uncompress_data[uncompress_len];
				if (++uncompress_len >= m_channel_num) {
					for (unsigned char i = 0; i < m_channel_num; i++) {
						p_output_data[i] = uncompress_data[i];
					}
					m_uncompress_step = 0x00;
					uncompress_len = 0;
					uncompress_ret = UNCOM_RET_VALID;
				}
			}
			break;
		case 0x13:		//原始数据字节高位			
			uncompress_data[uncompress_len] = last_data[uncompress_len] | (compress_data << 8);
			last_data[uncompress_len] = uncompress_data[uncompress_len];
			m_uncompress_step = 0x12;
			if (++uncompress_len >= m_channel_num) {
				for (unsigned char i = 0; i < m_channel_num; i++) {
					p_output_data[i] = uncompress_data[i];
				}
				m_uncompress_step = 0x00;
				uncompress_len = 0;
				uncompress_ret = UNCOM_RET_VALID;
			}
			break;
		default:
			break;
	}
	return uncompress_ret;
}