package trie;


import org.bouncycastle.util.encoders.Hex;
import util.encrypt.EncryptedNode;

public class TraceAllNodes implements Trie.ScanAction {
    
    StringBuilder output = new StringBuilder();
    
    @Override
    public void doOnNode(EncryptedNode node_, Node node) {
        output.append(Hex.toHexString(node_.getEncryptedKey())).append(" ==> ").append(node.toString()).append("\n");
    }
    
    public String getOutput() {
        return output.toString();
    }
}
