package com.lepu.lepuble.ble.obj.bp2file;

public class EcgDiagnosis {
    // Regular ECG Rhythm
    private boolean isRegular = true;
    // Unable to analyze
    private boolean isPoorSignal = false;
    // Fast Heart Rate
    private boolean isFastHr = false;
    // Slow Heart Rate
    private boolean isSlowHr = false;
    // Irregular ECG Rhythm
    private boolean isIrregular = false;
    // Possible ventricular premature beats
    private boolean isPvcs = false;
    // Possible heart pause
    private boolean isHeartPause = false;
    // Possible Atrial fibrillation
    private boolean isFibrillation = false;
    // Wide QRS duration
    private boolean isWideQrs = false;
    // QTc is prolonged
    private boolean isProlongedQtc = false;
    // QTc is short
    private boolean isShortQtc = false;
    // ST segment elevation
    private boolean isStElevation = false;
    // ST segment depression
    private boolean isStDepression = false;

    // indicator
    private int indicator = 0;

    public EcgDiagnosis() {

    }

    public EcgDiagnosis(byte[] bytes) {

        if (bytes.length != 4) {
            return;
        }

        int result = (bytes[0]&0xFF) + ((bytes[1]&0xFF)<<8)  + ((bytes[2]&0xFF)<<16)  + ((bytes[1]&0xFF)<<24);

        if (result == 0) {
            isRegular = true;
        }
        if (result == 0xFFFFFFFF) {
            isPoorSignal = true;
        } else {
            if ((result & 0x00000001) == 0x00000001) {
                isFastHr = true;
            }
            if ((result & 0x00000002) == 0x00000002) {
                isSlowHr = true;
            }
            if ((result & 0x00000004) == 0x00000004) {
                isIrregular = true;
            }
            if ((result & 0x00000008) == 0x00000008) {
                isPvcs = true;
            }
            if ((result & 0x00000010) == 0x00000010) {
                isHeartPause = true;
            }
            if ((result & 0x00000020) == 0x00000020) {
                isFibrillation = true;
            }
            if ((result & 0x00000040) == 0x00000040) {
                isWideQrs = true;
            }
            if ((result & 0x00000080) == 0x00000080) {
                isProlongedQtc = true;
            }
            if ((result & 0x00000100) == 0x00000100) {
                isShortQtc = true;
            }
            if ((result & 0x00000200) == 0x00000200) {
                isStElevation = true;
            }
            if ((result & 0x00000400) == 0x00000400) {
                isStDepression = true;
            }
        }


        if (isPoorSignal) {
            indicator = 3;
        } else if (isIrregular || isPvcs || isHeartPause || isFibrillation || isWideQrs || isProlongedQtc || isShortQtc || isStElevation || isStDepression) {
            indicator = 2;
        } else if (isSlowHr || isFastHr) {
            indicator = 1;
        } else {
            indicator = 0;
        }

    }


    public boolean isRegular() {
        return isRegular;
    }

    public void setRegular(boolean regular) {
        isRegular = regular;
    }

    public boolean isStDepression() {
        return isStDepression;
    }

    public void setStDepression(boolean stDepression) {
        isStDepression = stDepression;
    }

    public boolean isStElevation() {
        return isStElevation;
    }

    public void setStElevation(boolean stElevation) {
        isStElevation = stElevation;
    }

    public boolean isShortQtc() {
        return isShortQtc;
    }

    public void setShortQtc(boolean shortQtc) {
        isShortQtc = shortQtc;
    }

    public boolean isProlongedQtc() {
        return isProlongedQtc;
    }

    public void setProlongedQtc(boolean prolongedQtc) {
        isProlongedQtc = prolongedQtc;
    }

    public boolean isWideQrs() {
        return isWideQrs;
    }

    public void setWideQrs(boolean wideQrs) {
        isWideQrs = wideQrs;
    }

    public boolean isFibrillation() {
        return isFibrillation;
    }

    public void setFibrillation(boolean fibrillation) {
        isFibrillation = fibrillation;
    }

    public boolean isHeartPause() {
        return isHeartPause;
    }

    public void setHeartPause(boolean heartPause) {
        isHeartPause = heartPause;
    }

    public boolean isPvcs() {
        return isPvcs;
    }

    public void setPvcs(boolean pvcs) {
        isPvcs = pvcs;
    }

    public boolean isIrregular() {
        return isIrregular;
    }

    public void setIrregular(boolean irregular) {
        isIrregular = irregular;
    }

    public boolean isSlowHr() {
        return isSlowHr;
    }

    public void setSlowHr(boolean slowHr) {
        isSlowHr = slowHr;
    }

    public boolean isFastHr() {
        return isFastHr;
    }

    public void setFastHr(boolean fastHr) {
        isFastHr = fastHr;
    }

    public boolean isPoorSignal() {
        return isPoorSignal;
    }

    public void setPoorSignal(boolean poorSignal) {
        isPoorSignal = poorSignal;
    }

    public int getIndicator() {
        return indicator;
    }

    public void setIndicator(int indicator) {
        this.indicator = indicator;
    }
}
