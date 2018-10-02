package trie;

import org.bouncycastle.util.encoders.Hex;
import util.NibbleHelper;

import java.util.Arrays;
import java.util.List;

public class Node {
    
    private Object value;
    
    public Node(Object obj) {
        
        if (obj == null) return;
        
        if (obj instanceof Node) {
            this.value = ((Node) obj).asObj();
        } else {
            this.value = obj;
        }
    }
    
    public Object asObj() {
        return value;
    }
    
    public List<Object> asList() {
        Object[] valueArray = (Object[]) value;
        return Arrays.asList(valueArray);
    }
    
    public String asString() {
        if (isBytes()) {
            return new String((byte[]) value);
        } else if (isString()) {
            return (String) value;
        }
        return "";
    }
    
    public byte[] asBytes() {
        if (isBytes()) {
            return (byte[]) value;
        } else if (isString()) {
            return asString().getBytes();
        }
        return NibbleHelper.EMPTY_BYTE_ARRAY;
    }
    
    public Node get(int index) {
        if (isList()) {
            if (asList().size() <= index) {
                return new Node(null);
            }
            if (index < 0) {
                throw new RuntimeException("Negative index not allowed");
            }
            return new Node(asList().get(index));
        }
        return new Node(null);
    }
    
    public boolean isList() {
        return value != null && value.getClass().isArray() && !value.getClass().getComponentType().isPrimitive();
    }
    
    public boolean isString() {
        return value instanceof String;
    }
    
    public boolean isBytes() {
        return value instanceof byte[];
    }
    
    public boolean isNull() {
        return value == null;
    }
    
    public boolean isHashCode() {
        return this.asBytes().length == 32;
    }
    
    public int length() {
        if (isList()) {
            return asList().size();
        } else if (isBytes()) {
            return asBytes().length;
        } else if (isString()) {
            return asString().length();
        }
        return 0;
    }
    
    public String toString() {
        
        StringBuffer buffer = new StringBuffer();
        
        if (isList()) {
            
            Object[] list = (Object[]) value;
            
            // special case - key/value node
            if (list.length == 2) {
                
                buffer.append("{ \n \t");
                
                Node key = new Node(list[0]);
                
                byte[] keyNibbles = NibbleHelper.binToNibblesNoTerminator(key.asBytes());
                String keyString = NibbleHelper.nibblesToPrettyString(keyNibbles);
                String readableString = NibbleHelper.nibblesToReadableString(keyNibbles);
    
                buffer.append('"' + keyString + '"');
                
                buffer.append(":");
                
                Node val = new Node(list[1]);
                buffer.append(val.toString());
                
                buffer.append(" } \n ");
                return buffer.toString();
            }
            buffer.append(" [ \n \t");
            
            for (int i = 0; i < list.length; ++i) {
                Node val = new Node(list[i]);
                if (val.isString() || val.isEmpty()) {
                    buffer.append('{').append(val.toString()).append('}');
                } else {
                    buffer.append(val.toString());
                }
                if (i < list.length - 1)
                    buffer.append(", ");
            }
            buffer.append("] \n ");
            
            return buffer.toString();
        } else if (isEmpty()) {
            return "";
        } else if (isBytes()) {
            
            StringBuffer output = new StringBuffer();
            if (isHashCode()) {
                output.append(Hex.toHexString(asBytes()));
            } else if (isReadbleString()) {
                output.append('"');
                for (byte oneByte : asBytes()) {
                    if (oneByte < 16) {
                        output.append("x").append(NibbleHelper.oneByteToHexString(oneByte));
                    } else {
                        output.append(Character.valueOf((char) oneByte));
                    }
                }
                output.append('"');
                return output.toString();
            }
            return Hex.toHexString(this.asBytes());
        } else if (isString()) {
            return asString();
        }
        return "Unexpected type";
    }
    public boolean isEmpty() {
        if (isNull()) return true;
        if (isBytes() && asBytes().length == 0) return true;
        if (isList() && asList().isEmpty()) return true;
        if (isString() && asString().equals("")) return true;
        
        return false;
    }
    
    public boolean isReadbleString() {
        
        int readableChars = 0;
        byte[] data = (byte[]) value;
        
        if (data.length == 1 && data[0] > 31 && data[0] < 126) {
            return true;
        }
        
        for (int i = 0; i < data.length; ++i) {
            if (data[i] > 32 && data[i] < 126) ++readableChars;
        }
        
        if ((double) readableChars / (double) data.length > 0.55)
            return true;
        else
            return false;
    }
}
