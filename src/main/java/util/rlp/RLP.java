package util.rlp;

import trie.Node;
import util.NibbleHelper;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;

import static java.util.Arrays.copyOfRange;
import static org.bouncycastle.pqc.math.linearalgebra.ByteUtils.concatenate;
import static org.bouncycastle.util.BigIntegers.asUnsignedByteArray;

public class RLP {
    
    private static double MAX_ITEM_LENGTH = Math.pow(256, 8);
    private static int SIZE_THRESHOLD = 56;
    private static int OFFSET_SHORT_ITEM = 0x80;
    private static int OFFSET_LONG_ITEM = 0xb7;
    private static int OFFSET_SHORT_LIST = 0xc0;
    private static int OFFSET_LONG_LIST = 0xf7;
    
    
    private static byte decodeOneByteItem(byte[] data, int index) {
        // null item
        if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM) {
            return (byte) (data[index] - OFFSET_SHORT_ITEM);
        }
        // single byte item
        if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {
            return (byte) (data[index]);
        }
        // single byte item
        if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM + 1) {
            return (byte) (data[index + 1]);
        }
        return 0;
    }
    
    public static int decodeInt(byte[] data, int index) {
        
        int value = 0;
        
        if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            byte pow = (byte) (length - 1);
            for (int i = 1; i <= length; ++i) {
                value += data[index + i] << (8 * pow);
                pow--;
            }
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }
    
    private static short decodeShort(byte[] data, int index) {
        
        short value = 0;
        
        if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            value = ByteBuffer.wrap(data, index, length).getShort();
        } else {
            value = data[index];
        }
        return value;
    }
    
    private static long decodeLong(byte[] data, int index) {
        
        long value = 0;
        
        if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            byte length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            byte pow = (byte) (length - 1);
            for (int i = 1; i <= length; ++i) {
                value += data[index + i] << (8 * pow);
                pow--;
            }
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }
    
    private static String decodeStringItem(byte[] data, int index) {
        
        String value = null;
        
        if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            int length = calcLengthRaw(lengthOfLength, data, index);
            value = new String(data, index + lengthOfLength + 1, length);
            
        } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            byte length = (byte) ((data[index] & 0xFF) - OFFSET_SHORT_ITEM);
            value = new String(data, index + 1, length);
            
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        return value;
    }
    
    private static byte[] decodeItemBytes(byte[] data, int index) {
        
        byte[] value = null;
        int length = 0;
        
        if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            length = calcLengthRaw(lengthOfLength, data, index);
            
        } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        byte[] valueBytes = new byte[length];
        System.arraycopy(data, index, valueBytes, 0, length);
        value = valueBytes;
        return value;
    }
    
    public static BigInteger decodeBigInteger(byte[] data, int index) {
        
        BigInteger value = null;
        int length = 0;
        
        if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            length = calcLengthRaw(lengthOfLength, data, index);
            
        } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        byte[] valueBytes = new byte[length];
        System.arraycopy(data, index, valueBytes, 0, length);
        value = new BigInteger(1, valueBytes);
        return value;
    }
    
    private static byte[] decodeByteArray(byte[] data, int index) {
        
        byte[] value = null;
        int length = 0;
        
        if ((data[index] & 0xFF) >= OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            length = calcLengthRaw(lengthOfLength, data, index);
            
        } else if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) < OFFSET_LONG_ITEM) {
            
            length = (byte) (data[index] - OFFSET_SHORT_ITEM);
            
        } else {
            throw new RuntimeException("wrong decode attempt");
        }
        byte[] valueBytes = new byte[length];
        System.arraycopy(data, index, valueBytes, 0, length);
        value = valueBytes;
        return value;
    }
    
    private static int nextItemLength(byte[] data, int index) {
        
        if (index >= data.length)
            return -1;
        
        if ((data[index] & 0xFF) >= OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_LIST);
            
            int length = calcLength(lengthOfLength, data, index);
            return length;
        }
        if ((data[index] & 0xFF) >= OFFSET_SHORT_LIST
                && (data[index] & 0xFF) < OFFSET_LONG_LIST) {
            
            byte length = (byte) ((data[index] & 0xFF) - OFFSET_SHORT_LIST);
            return length;
        }
        if ((data[index] & 0xFF) > OFFSET_LONG_ITEM
                && (data[index] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (data[index] - OFFSET_LONG_ITEM);
            int length = calcLength(lengthOfLength, data, index);
            return length;
        }
        if ((data[index] & 0xFF) > OFFSET_SHORT_ITEM
                && (data[index] & 0xFF) <= OFFSET_LONG_ITEM) {
            
            byte length = (byte) ((data[index] & 0xFF) - OFFSET_SHORT_ITEM);
            return length;
        }
        if ((data[index] & 0xFF) == OFFSET_SHORT_ITEM) {
            return 1;
        }
        if ((data[index] & 0xFF) < OFFSET_SHORT_ITEM) {
            return 1;
        }
        return -1;
    }
    
    public static byte[] decodeIP4Bytes(byte[] data, int index) {
        
        int length = (data[index] & 0xFF) - OFFSET_SHORT_LIST;
        int offset = 1;
        
        byte aByte = decodeOneByteItem(data, index + offset);
        
        if ((data[index + offset] & 0xFF) > OFFSET_SHORT_ITEM)
            offset = offset + 2;
        else
            offset = offset + 1;
        byte bByte = decodeOneByteItem(data, index + offset);
        
        if ((data[index + offset] & 0xFF) > OFFSET_SHORT_ITEM)
            offset = offset + 2;
        else
            offset = offset + 1;
        byte cByte = decodeOneByteItem(data, index + offset);
        
        if ((data[index + offset] & 0xFF) > OFFSET_SHORT_ITEM)
            offset = offset + 2;
        else
            offset = offset + 1;
        byte dByte = decodeOneByteItem(data, index + offset);
        
        // return IP address
        return new byte[]{aByte, bByte, cByte, dByte};
    }
    
    public static int getFirstListElement(byte[] payload, int pos) {
        
        if (pos >= payload.length)
            return -1;
        
        if ((payload[pos] & 0xFF) >= OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
            return pos + lengthOfLength + 1;
        }
        if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST
                && (payload[pos] & 0xFF) < OFFSET_LONG_LIST) {
            return pos + 1;
        }
        if ((payload[pos] & 0xFF) >= OFFSET_LONG_ITEM
                && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
            return pos + lengthOfLength + 1;
        }
        return -1;
    }
    
    public static int getNextElementIndex(byte[] payload, int pos) {
        
        if (pos >= payload.length)
            return -1;
        
        if ((payload[pos] & 0xFF) >= OFFSET_LONG_LIST) {
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_LIST);
            int length = calcLength(lengthOfLength, payload, pos);
            return pos + lengthOfLength + length + 1;
        }
        if ((payload[pos] & 0xFF) >= OFFSET_SHORT_LIST
                && (payload[pos] & 0xFF) < OFFSET_LONG_LIST) {
            
            byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_LIST);
            return pos + 1 + length;
        }
        if ((payload[pos] & 0xFF) >= OFFSET_LONG_ITEM
                && (payload[pos] & 0xFF) < OFFSET_SHORT_LIST) {
            
            byte lengthOfLength = (byte) (payload[pos] - OFFSET_LONG_ITEM);
            int length = calcLength(lengthOfLength, payload, pos);
            return pos + lengthOfLength + length + 1;
        }
        if ((payload[pos] & 0xFF) > OFFSET_SHORT_ITEM
                && (payload[pos] & 0xFF) < OFFSET_LONG_ITEM) {
            
            byte length = (byte) ((payload[pos] & 0xFF) - OFFSET_SHORT_ITEM);
            return pos + 1 + length;
        }
        if ((payload[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
            return pos + 1;
        }
        if ((payload[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
            return pos + 1;
        }
        return -1;
    }
    
    /**
     * Get exactly one message payload
     */
    public static void fullTraverse(byte[] msgData, int level, int startPos,
                                    int endPos, int levelToIndex, Queue<Integer> index) {
        
        try {
            
            if (msgData == null || msgData.length == 0)
                return;
            int pos = startPos;
            
            while (pos < endPos) {
                
                if (level == levelToIndex)
                    index.add(new Integer(pos));
                
                // It's a list with a payload more than 55 bytes
                // data[0] - 0xF7 = how many next bytes allocated
                // for the length of the list
                if ((msgData[pos] & 0xFF) >= OFFSET_LONG_LIST) {
                    
                    byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_LIST);
                    int length = calcLength(lengthOfLength, msgData, pos);
                    
                    // now we can parse an item for data[1]..data[length]
                    System.out.println("-- level: [" + level
                            + "] Found big list length: " + length);
                    
                    fullTraverse(msgData, level + 1, pos + lengthOfLength + 1,
                            pos + lengthOfLength + length, levelToIndex, index);
                    
                    pos += lengthOfLength + length + 1;
                    continue;
                }
                // It's a list with a payload less than 55 bytes
                if ((msgData[pos] & 0xFF) >= OFFSET_SHORT_LIST
                        && (msgData[pos] & 0xFF) < OFFSET_LONG_LIST) {
                    
                    byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_LIST);
                    
                    System.out.println("-- level: [" + level
                            + "] Found small list length: " + length);
                    
                    fullTraverse(msgData, level + 1, pos + 1, pos + length + 1,
                            levelToIndex, index);
                    
                    pos += 1 + length;
                    continue;
                }
                // It's an item with a payload more than 55 bytes
                // data[0] - 0xB7 = how much next bytes allocated for
                // the length of the string
                if ((msgData[pos] & 0xFF) >= OFFSET_LONG_ITEM
                        && (msgData[pos] & 0xFF) < OFFSET_SHORT_LIST) {
                    
                    byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_ITEM);
                    int length = calcLength(lengthOfLength, msgData, pos);
                    
                    // now we can parse an item for data[1]..data[length]
                    System.out.println("-- level: [" + level
                            + "] Found big item length: " + length);
                    pos += lengthOfLength + length + 1;
                    
                    continue;
                }
                // It's an item less than 55 bytes long,
                // data[0] - 0x80 == length of the item
                if ((msgData[pos] & 0xFF) > OFFSET_SHORT_ITEM
                        && (msgData[pos] & 0xFF) < OFFSET_LONG_ITEM) {
                    
                    byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_ITEM);
                    
                    System.out.println("-- level: [" + level
                            + "] Found small item length: " + length);
                    pos += 1 + length;
                    continue;
                }
                // null item
                if ((msgData[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
                    System.out.println("-- level: [" + level
                            + "] Found null item: ");
                    pos += 1;
                    continue;
                }
                // single byte item
                if ((msgData[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
                    System.out.println("-- level: [" + level
                            + "] Found single item: ");
                    pos += 1;
                    continue;
                }
            }
        } catch (Throwable th) {
            throw new RuntimeException("rlp wrong encoding",
                    th.fillInStackTrace());
        }
    }
    
    private static int calcLength(int lengthOfLength, byte[] msgData, int pos) {
        byte pow = (byte) (lengthOfLength - 1);
        int length = 0;
        for (int i = 1; i <= lengthOfLength; ++i) {
            length += (msgData[pos + i] & 0xFF) << (8 * pow);
            pow--;
        }
        return length;
    }
    
    private static int calcLengthRaw(int lengthOfLength, byte[] msgData, int index) {
        byte pow = (byte) (lengthOfLength - 1);
        int length = 0;
        for (int i = 1; i <= lengthOfLength; ++i) {
            length += msgData[index + i] << (8 * pow);
            pow--;
        }
        return length;
    }
    
    public static byte getCommandCode(byte[] data) {
        byte command = 0;
        int index = getFirstListElement(data, 0);
        command = data[index];
        command = ((int) (command & 0xFF) == OFFSET_SHORT_ITEM) ? 0 : command;
        return command;
    }
    
    /**
     * Parse wire byte[] message into rlp elements
     *
     * @param msgData - raw rlp data
     * @return rlpList
     * - outcome of recursive rlp structure
     */
    public static RLPList decode2(byte[] msgData) {
        RLPList rlpList = new RLPList();
        fullTraverse(msgData, 0, 0, msgData.length, 1, rlpList);
        return rlpList;
    }
    
    /**
     * Get exactly one message payload
     */
    private static void fullTraverse(byte[] msgData, int level, int startPos,
                                     int endPos, int levelToIndex, RLPList rlpList) {
        
        try {
            if (msgData == null || msgData.length == 0)
                return;
            int pos = startPos;
            
            while (pos < endPos) {
                // It's a list with a payload more than 55 bytes
                // data[0] - 0xF7 = how many next bytes allocated
                // for the length of the list
                if ((msgData[pos] & 0xFF) > OFFSET_LONG_LIST) {
                    
                    byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_LIST);
                    int length = calcLength(lengthOfLength, msgData, pos);
                    
                    byte[] rlpData = new byte[lengthOfLength + length + 1];
                    System.arraycopy(msgData, pos, rlpData, 0, lengthOfLength
                            + length + 1);
                    
                    RLPList newLevelList = new RLPList();
                    newLevelList.setRLPData(rlpData);
                    
                    fullTraverse(msgData, level + 1, pos + lengthOfLength + 1,
                            pos + lengthOfLength + length + 1, levelToIndex,
                            newLevelList);
                    rlpList.add(newLevelList);
                    
                    pos += lengthOfLength + length + 1;
                    continue;
                }
                // It's a list with a payload less than 55 bytes
                if ((msgData[pos] & 0xFF) >= OFFSET_SHORT_LIST
                        && (msgData[pos] & 0xFF) <= OFFSET_LONG_LIST) {
                    
                    byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_LIST);
                    
                    byte[] rlpData = new byte[length + 1];
                    System.arraycopy(msgData, pos, rlpData, 0, length + 1);
                    
                    RLPList newLevelList = new RLPList();
                    newLevelList.setRLPData(rlpData);
                    
                    if (length > 0)
                        fullTraverse(msgData, level + 1, pos + 1, pos + length
                                + 1, levelToIndex, newLevelList);
                    rlpList.add(newLevelList);
                    
                    pos += 1 + length;
                    continue;
                }
                // It's an item with a payload more than 55 bytes
                // data[0] - 0xB7 = how much next bytes allocated for
                // the length of the string
                if ((msgData[pos] & 0xFF) > OFFSET_LONG_ITEM
                        && (msgData[pos] & 0xFF) < OFFSET_SHORT_LIST) {
                    
                    byte lengthOfLength = (byte) (msgData[pos] - OFFSET_LONG_ITEM);
                    int length = calcLength(lengthOfLength, msgData, pos);
                    
                    // now we can parse an item for data[1]..data[length]
                    byte[] item = new byte[length];
                    System.arraycopy(msgData, pos + lengthOfLength + 1, item,
                            0, length);
                    
                    byte[] rlpPrefix = new byte[lengthOfLength + 1];
                    System.arraycopy(msgData, pos, rlpPrefix, 0,
                            lengthOfLength + 1);
                    
                    RLPItem rlpItem = new RLPItem(item);
                    rlpList.add(rlpItem);
                    pos += lengthOfLength + length + 1;
                    
                    continue;
                }
                // It's an item less than 55 bytes long,
                // data[0] - 0x80 == length of the item
                if ((msgData[pos] & 0xFF) > OFFSET_SHORT_ITEM
                        && (msgData[pos] & 0xFF) <= OFFSET_LONG_ITEM) {
                    
                    byte length = (byte) ((msgData[pos] & 0xFF) - OFFSET_SHORT_ITEM);
                    
                    byte[] item = new byte[length];
                    System.arraycopy(msgData, pos + 1, item, 0, length);
                    
                    byte[] rlpPrefix = new byte[2];
                    System.arraycopy(msgData, pos, rlpPrefix, 0, 2);
                    
                    RLPItem rlpItem = new RLPItem(item);
                    rlpList.add(rlpItem);
                    pos += 1 + length;
                    
                    continue;
                }
                // null item
                if ((msgData[pos] & 0xFF) == OFFSET_SHORT_ITEM) {
                    byte[] item = NibbleHelper.EMPTY_BYTE_ARRAY;
                    RLPItem rlpItem = new RLPItem(item);
                    rlpList.add(rlpItem);
                    pos += 1;
                    continue;
                }
                // single byte item
                if ((msgData[pos] & 0xFF) < OFFSET_SHORT_ITEM) {
                    
                    byte[] item = {(byte) (msgData[pos] & 0xFF)};
                    
                    RLPItem rlpItem = new RLPItem(item);
                    rlpList.add(rlpItem);
                    pos += 1;
                    continue;
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("rlp wrong encoding", e);
        }
    }
    
    /**
     * Reads any rlp encoded byte-array and returns all objects as byte-array or list of byte-arrays
     *
     * @param data rlp encoded byte-array
     * @param pos  position in the array to start reading
     * @return DecodeResult encapsulates the decoded items as a single Object and the final read position
     */
    public static DecodeResult decode(byte[] data, int pos) {
        if (data == null || data.length < 1) {
            return null;
        }
        int prefix = data[pos] & 0xFF;
        if (prefix == OFFSET_SHORT_ITEM) {
            return new DecodeResult(pos + 1, ""); // means no length or 0
        } else if (prefix < OFFSET_SHORT_ITEM) {
            return new DecodeResult(pos + 1, new byte[]{data[pos]}); // byte is its own rlp encoding
        } else if (prefix < OFFSET_LONG_ITEM) {
            int len = prefix - OFFSET_SHORT_ITEM; // length of the encoded bytes
            return new DecodeResult(pos + 1 + len, copyOfRange(data, pos + 1, pos + 1 + len));
        } else if (prefix < OFFSET_SHORT_LIST) {
            int lenlen = prefix - OFFSET_LONG_ITEM; // length of length the encoded bytes
            int lenbytes = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
            return new DecodeResult(pos + 1 + lenlen + lenbytes, copyOfRange(data, pos + 1 + lenlen, pos + 1 + lenlen + lenbytes));
        } else if (prefix <= OFFSET_LONG_LIST) {
            int len = prefix - OFFSET_SHORT_LIST; // length of the encoded list
            int prevPos = pos;
            pos++;
            return decodeList(data, pos, prevPos, len);
        } else if (prefix < 0xFF) {
            int lenlen = prefix - OFFSET_LONG_LIST; // length of length the encoded list
            int lenlist = byteArrayToInt(copyOfRange(data, pos + 1, pos + 1 + lenlen)); // length of encoded bytes
            pos = pos + lenlen + 1; // start at position of first element in list
            int prevPos = lenlist;
            return decodeList(data, pos, prevPos, lenlist);
        } else {
            throw new RuntimeException("Only byte values between 0x00 and 0xFF are supported, but got: " + prefix);
        }
    }
    
    public static int byteArrayToInt(byte[] b) {
        if (b == null || b.length == 0)
            return 0;
        return new BigInteger(1, b).intValue();
    }
    
    private static DecodeResult decodeList(byte[] data, int pos, int prevPos, int len) {
        List<Object> slice = new ArrayList<>();
        for (int i = 0; i < len; ) {
            // Get the next item in the data list and append it
            DecodeResult result = decode(data, pos);
            slice.add(result.getDecoded());
            // Increment pos by the amount bytes in the previous read
            prevPos = result.getPos();
            i += (prevPos - pos);
            pos = prevPos;
        }
        return new DecodeResult(pos, slice.toArray());
    }
    
    /* ******************************************************
     * 						ENCODING						*
     * ******************************************************/
    
    /**
     * Turn Object into its rlp encoded equivalent of a byte-array
     * Support for String, Integer, BigInteger and Lists of any of these types.
     *
     * @param input as object or List of objects
     * @return byte[] rlp encoded
     */
    public static byte[] encode(Object input) {
        Node val = new Node(input);
        if (val.isList()) {
            List<Object> inputArray = val.asList();
            if (inputArray.size() == 0) {
                return encodeLength(inputArray.size(), OFFSET_SHORT_LIST);
            }
            byte[] output = NibbleHelper.EMPTY_BYTE_ARRAY;
            for (Object object : inputArray) {
                output = concatenate(output, encode(object));
            }
            byte[] prefix = encodeLength(output.length, OFFSET_SHORT_LIST);
            return concatenate(prefix, output);
        } else {
            byte[] inputAsBytes = toBytes(input);
            if (inputAsBytes.length == 1) {
                return inputAsBytes;
            } else {
                byte[] firstByte = encodeLength(inputAsBytes.length, OFFSET_SHORT_ITEM);
                return concatenate(firstByte, inputAsBytes);
            }
        }
    }
    
    /**
     * Integer limitation goes up to 2^31-1 so length can never be bigger than MAX_ITEM_LENGTH
     */
    public static byte[] encodeLength(int length, int offset) {
        if (length < SIZE_THRESHOLD) {
            byte firstByte = (byte) (length + offset);
            return new byte[]{firstByte};
        } else if (length < MAX_ITEM_LENGTH) {
            byte[] binaryLength;
            if (length > 0xFF)
                binaryLength = BigInteger.valueOf(length).toByteArray();
            else
                binaryLength = new byte[]{(byte) length};
            byte firstByte = (byte) (binaryLength.length + offset + SIZE_THRESHOLD - 1);
            return concatenate(new byte[]{firstByte}, binaryLength);
        } else {
            throw new RuntimeException("Input too long");
        }
    }
    
    public static byte[] encodeByte(byte singleByte) {
        if ((singleByte & 0xFF) == 0) {
            return new byte[]{(byte) OFFSET_SHORT_ITEM};
        } else if ((singleByte & 0xFF) < 0x7F) {
            return new byte[]{singleByte};
        } else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 1), singleByte};
        }
    }
    
    public static byte[] encodeShort(short singleShort) {
        if (singleShort <= 0xFF)
            return encodeByte((byte) singleShort);
        else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 2),
                    (byte) (singleShort >> 8 & 0xFF),
                    (byte) (singleShort >> 0 & 0xFF)};
        }
    }
    
    public static byte[] encodeInt(int singleInt) {
        if (singleInt <= 0xFF)
            return encodeByte((byte) singleInt);
        else if (singleInt <= 0xFFFF)
            return encodeShort((short) singleInt);
        else if (singleInt <= 0xFFFFFF)
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 3),
                    (byte) (singleInt >>> 16),
                    (byte) (singleInt >>> 8),
                    (byte) singleInt};
        else {
            return new byte[]{(byte) (OFFSET_SHORT_ITEM + 4),
                    (byte) (singleInt >>> 24),
                    (byte) (singleInt >>> 16),
                    (byte) (singleInt >>> 8),
                    (byte) singleInt};
        }
    }
    
    public static byte[] encodeString(String srcString) {
        return encodeElement(srcString.getBytes());
    }
    
    public static byte[] encodeBigInteger(BigInteger srcBigInteger) {
        if (srcBigInteger == BigInteger.ZERO)
            return encodeByte((byte) 0);
        else
            return encodeElement(asUnsignedByteArray(srcBigInteger));
    }
    
    public static byte[] encodeElement(byte[] srcData) {
        
        if (srcData == null)
            return new byte[]{(byte) OFFSET_SHORT_ITEM};
        else if (srcData.length == 1 && (srcData[0] & 0xFF) < 0x80) {
            return srcData;
        } else if (srcData.length < SIZE_THRESHOLD) {
            // length = 8X
            byte length = (byte) (OFFSET_SHORT_ITEM + srcData.length);
            byte[] data = Arrays.copyOf(srcData, srcData.length + 1);
            System.arraycopy(data, 0, data, 1, srcData.length);
            data[0] = length;
            
            return data;
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = srcData.length;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }
            byte[] lenBytes = new byte[byteNum];
            for (int i = 0; i < byteNum; ++i) {
                lenBytes[byteNum - 1 - i] = (byte) ((srcData.length >> (8 * i)) & 0xFF);
            }
            // first byte = F7 + bytes.length
            byte[] data = Arrays.copyOf(srcData, srcData.length + 1 + byteNum);
            System.arraycopy(data, 0, data, 1 + byteNum, srcData.length);
            data[0] = (byte) (OFFSET_LONG_ITEM + byteNum);
            System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);
            
            return data;
        }
    }
    
    public static byte[] encodeList(byte[]... elements) {
        
        int totalLength = 0;
        for (int i = 0; i < elements.length; ++i) {
            totalLength += elements[i].length;
        }
        
        byte[] data;
        int copyPos = 0;
        if (totalLength < SIZE_THRESHOLD) {
            
            data = new byte[1 + totalLength];
            data[0] = (byte) (OFFSET_SHORT_LIST + totalLength);
            copyPos = 1;
        } else {
            // length of length = BX
            // prefix = [BX, [length]]
            int tmpLength = totalLength;
            byte byteNum = 0;
            while (tmpLength != 0) {
                ++byteNum;
                tmpLength = tmpLength >> 8;
            }
            tmpLength = totalLength;
            byte[] lenBytes = new byte[byteNum];
            for (int i = 0; i < byteNum; ++i) {
                lenBytes[byteNum - 1 - i] = (byte) ((tmpLength >> (8 * i)) & 0xFF);
            }
            // first byte = F7 + bytes.length
            data = new byte[1 + lenBytes.length + totalLength];
            data[0] = (byte) (OFFSET_LONG_LIST + byteNum);
            System.arraycopy(lenBytes, 0, data, 1, lenBytes.length);
            
            copyPos = lenBytes.length + 1;
        }
        for (int i = 0; i < elements.length; ++i) {
            System.arraycopy(elements[i], 0, data, copyPos, elements[i].length);
            copyPos += elements[i].length;
        }
        return data;
    }
    
    /*
     *	Utility function to convert Objects into byte arrays
     */
    private static byte[] toBytes(Object input) {
        if (input instanceof byte[]) {
            return (byte[]) input;
        } else if (input instanceof String) {
            String inputString = (String) input;
            return inputString.getBytes();
        } else if (input instanceof Long) {
            Long inputLong = (Long) input;
            return (inputLong == 0) ? NibbleHelper.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputLong));
        } else if (input instanceof Integer) {
            Integer inputInt = (Integer) input;
            return (inputInt == 0) ? NibbleHelper.EMPTY_BYTE_ARRAY : asUnsignedByteArray(BigInteger.valueOf(inputInt.intValue()));
        } else if (input instanceof BigInteger) {
            BigInteger inputBigInt = (BigInteger) input;
            return (inputBigInt == BigInteger.ZERO) ? NibbleHelper.EMPTY_BYTE_ARRAY : asUnsignedByteArray(inputBigInt);
        } else if (input instanceof Node) {
            Node val = (Node) input;
            return toBytes(val.asObj());
        }
        throw new RuntimeException("Unsupported type: Only accepting String, Integer and BigInteger for now");
    }
}
