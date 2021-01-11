package bsi;

import android.app.Application;

public class PasswordWalletApplication extends Application {
    private byte[] masterPasswordHash;

    public byte[] getMasterPasswordHash() {
        return masterPasswordHash;
    }

    public void setMasterPasswordHash(byte[] masterPasswordHash) {
        this.masterPasswordHash = masterPasswordHash;
    }
}
