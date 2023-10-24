package ru.sir.ymodem;

public interface OnChangeListener {
    void post(String message);
    void progress(int progress);
}
