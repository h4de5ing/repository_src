package ru.sir.ymodem;

public interface CRC {
    int getCRCLength();

    long calcCRC(byte[] block);
}
