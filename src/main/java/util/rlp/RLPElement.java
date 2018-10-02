package util.rlp;

import java.io.Serializable;

public interface RLPElement extends Serializable {
    
    public byte[] getRLPData();
}
