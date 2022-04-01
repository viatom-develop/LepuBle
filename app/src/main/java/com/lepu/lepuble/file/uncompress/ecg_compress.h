#ifndef __ECG_COMPRESS_H__
#define	__ECG_COMPRESS_H__

#define	UNCOM_RET_INVALI	(0)		//解压无需处理返回值
#define	UNCOM_RET_VALID		(1)		//解压输出有效数据

void ecg_uncompress_init(unsigned char channel_num);
unsigned char ecg_uncompress_alg(signed char compress_data, short *p_output_data);

#endif // !__ECG_COMPRESS_H__
