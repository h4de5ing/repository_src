package ru.sir.ymodem;

public class CRC8 implements CRC {
    @Override
    public int getCRCLength() {
        return 1;
    }

    @Override
    public long calcCRC(byte[] block) {
        byte checkSumma = 0;
        for (byte b : block) {
            checkSumma += b;
        }
        return checkSumma;
    }
}
