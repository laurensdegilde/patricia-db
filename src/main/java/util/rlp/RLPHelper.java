package util.rlp;

import trie.Node;

public class RLPHelper {
    
    
    public static Node decode(byte[] data) {
        if (data != null && data.length != 0) {
            return new Node(RLP.decode(data, 0).getDecoded());
        }
        return null;
    }
    
    public static byte[] encode(Node node) {
        return RLP.encode(node);
    }
}
