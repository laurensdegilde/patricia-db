package util.encrypt;

public class EncryptedNode {
    
    private byte[] encryptedKey;
    private byte[] encodedValue;
    
    public EncryptedNode(byte[] k, byte[] v) {
        this.encryptedKey = k;
        this.encodedValue = v;
    }
    
    public byte[] getEncryptedKey() {
        return encryptedKey;
    }
    
    public byte[] getEncodedValue() {
        return encodedValue;
    }
}
