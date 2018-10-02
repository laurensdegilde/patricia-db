package util.encrypt;

import trie.Node;
import util.rlp.RLPHelper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class EncryptHelper {
    
    
    public static EncryptedNode encryptNode(Object node) throws NoSuchAlgorithmException {
        Node value = new Node(node);
        byte[] v = RLPHelper.encode(value);
        byte[] k = getSHA256().digest(v);
        return new EncryptedNode(k, v);
        
    }
    
    public static MessageDigest getSHA256() throws NoSuchAlgorithmException {
        return MessageDigest.getInstance("SHA-256");
    }
}
