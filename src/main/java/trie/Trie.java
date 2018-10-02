package trie;

import org.bouncycastle.util.encoders.Hex;
import util.NibbleHelper;
import util.Util;
import util.encrypt.EncryptHelper;
import util.encrypt.EncryptedNode;
import util.rlp.RLPHelper;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Trie {
    
    private static byte PAIR_SIZE = 2;
    private static byte LIST_SIZE = 17;
    private Object root;
    private DatabaseExposure databaseExposure;
    
    public Trie() throws IOException, NoSuchAlgorithmException {
        this.databaseExposure = new DatabaseExposure();
        this.root = setRoot();
    }
    
    public static Trie getTrie() throws IOException, NoSuchAlgorithmException {
        return new Trie();
    }
    
   
    
    public Object setRoot() throws NoSuchAlgorithmException, IOException {
        byte[] value = this.databaseExposure.get(this.getRoot());
        if (value == null) {
            return new byte[]{};
        }
        Node node = RLPHelper.decode(value);
        return node;
    }
    
    public byte[] read(String key) {
        byte[] k = NibbleHelper.binToNibbles(key.getBytes());
        Node node = new Node(this.read(this.root, k));
        return (node == null) ? null : node.asBytes();
    }
    
    private Object read(Object node, byte[] key) {
        if (key.length == 0 || isEmptyNode(node)) {
            return node;
        }
        
        Node currentNode = this.getNode(node);

        if (currentNode == null) return null;
        
        if (currentNode.length() == PAIR_SIZE) {
            
            byte[] k = NibbleHelper.unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();
            
            if (key.length >= k.length && Arrays.equals(k, Arrays.copyOfRange(key, 0, k.length))) {
                return this.read(v, Arrays.copyOfRange(key, k.length, key.length));
            } else {
                return "";
            }
        } else {
            return this.read(currentNode.get(key[0]).asObj(), Arrays.copyOfRange(key, 1, key.length));
        }
    }
    
    public Long [] write(String key, String value) throws NoSuchAlgorithmException {
        long startStepGet;
        long endStepGet;
        byte[] _key = NibbleHelper.binToNibbles(key.getBytes());;
        synchronized (this){
            startStepGet = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
            root = this.write(root, _key, value);
            endStepGet = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
            EncryptedNode encryptedNode = EncryptHelper.encryptNode(root);
            this.databaseExposure.put(encryptedNode);
            Util.writeToFile(Util.ROOTHASH_FILE_PATH, Hex.toHexString(this.getRoot().getEncryptedKey()));
        }
        return new Long[]{startStepGet, endStepGet};
    
    }
    
    private Object write(Object node, byte[] key, Object value) throws NoSuchAlgorithmException {
        if (key.length == 0) {
            return value;
        }
        
        if (isEmptyNode(node)) {
            Object[] newNode = new Object[]{NibbleHelper.packNibbles(key), value};
            return newNode;
        }
        
        Node currentNode = this.getNode(node);
        
        if (currentNode.length() == PAIR_SIZE) {
            byte[] k = NibbleHelper.unpackToNibbles(currentNode.get(0).asBytes());
            Object v = currentNode.get(1).asObj();
            
            if (Arrays.equals(k, key)) {
                Object[] newNode = new Object[]{NibbleHelper.packNibbles(key), value};
                return newNode;
            }
            
            Object newRoot;
            int matchingLength = NibbleHelper.matchingNibbleLength(key, k);
            if (matchingLength == k.length) {
                
                byte[] remainingKeypart = Arrays.copyOfRange(key, matchingLength, key.length);
                newRoot = this.write(v, remainingKeypart, value);
                
            } else {
                
                Object oldNode = this.write("", Arrays.copyOfRange(k, matchingLength + 1, k.length), v);
                Object newNode = this.write("", Arrays.copyOfRange(key, matchingLength + 1, key.length), value);
                
                Object[] scaledSlice = emptyStringSlice(17);
                
                scaledSlice[k[matchingLength]] = oldNode;
                scaledSlice[key[matchingLength]] = newNode;
                
                newRoot = scaledSlice;
            }
            
            if (matchingLength == 0) {
                return newRoot;
            } else {
                Object[] newNode = new Object[]{NibbleHelper.packNibbles(Arrays.copyOfRange(key, 0, matchingLength)), newRoot};
                return newNode;
            }
        } else {
            Object[] newNode = copyNode(currentNode);
            
            newNode[key[0]] = this.write(currentNode.get(key[0]).asObj(), Arrays.copyOfRange(key, 1, key.length), value);
            return newNode;
        }
        
    }
    
    private Node getNode(Object node) {
        Node val = new Node(node);
        
        if (!val.isBytes()) {
            return val;
        }
        
        byte[] keyBytes = val.asBytes();
        if (keyBytes.length == 0) {
            return val;
        } else if (keyBytes.length < 32) {
            return new Node(keyBytes);
        }
        return null;
    }
    
    private boolean isEmptyNode(Object node) {
        Node n = new Node(node);
        return (node == null || (n.isString() && (n.asString() == "" || n.get(0).isNull())) || n.length() == 0);
    }
    
    private Object[] copyNode(Node currentNode) {
        Object[] itemList = emptyStringSlice(LIST_SIZE);
        for (int i = 0; i < LIST_SIZE; i++) {
            Object cpy = currentNode.get(i).asObj();
            if (cpy != null)
                itemList[i] = cpy;
        }
        return itemList;
    }
    
    private Object[] emptyStringSlice(int l) {
        Object[] slice = new Object[l];
        for (int i = 0; i < l; i++) {
            slice[i] = "";
        }
        return slice;
    }
    
    public EncryptedNode getRoot() throws NoSuchAlgorithmException {
        if (root == null
                || (root instanceof byte[] && ((byte[]) root).length == 0)
                || (root instanceof String && "".equals((String) root))) {
            try {
                return new EncryptedNode(Hex.decode(Util.readFile(Util.ROOTHASH_FILE_PATH)), null);
            } catch (Exception ee) {
                return new EncryptedNode(new byte[]{}, null);
            }
        } else if (root instanceof byte[]) {
            return new EncryptedNode((byte[]) this.root, null);
        } else {
            Node node = new Node(this.root);
            byte[] val = RLPHelper.encode(node);
            return new EncryptedNode(EncryptHelper.getSHA256().digest(val), null);
        }
    }
    
    private void scanTree(EncryptedNode encryptedNode, ScanAction scanAction) throws IOException {
        byte[] data = this.databaseExposure.get(encryptedNode);
        Node node = RLPHelper.decode(data);
        if (node == null) return;
        
        if (node.isList()) {
            List<Object> siblings = node.asList();
            if (siblings.size() == PAIR_SIZE) {
                Node val = new Node(siblings.get(1));
                if (val.isHashCode())
                    scanTree(new EncryptedNode(val.asBytes(), null), scanAction);
            } else {
                for (int j = 0; j < LIST_SIZE; ++j) {
                    Node val = new Node(siblings.get(j));
                    if (val.isHashCode())
                        scanTree(new EncryptedNode(val.asBytes(), null), scanAction);
                }
            }
            scanAction.doOnNode(encryptedNode, node);
        }
    }
    
    public String getTrieDump() throws NoSuchAlgorithmException, IOException {
        
        String root = "";
        TraceAllNodes traceAction = new TraceAllNodes();
        this.scanTree(this.getRoot(), traceAction);
        
        if (this.root instanceof Node) {
            root = "root: " + Hex.toHexString(getRoot().getEncryptedKey()) + " => " + this.root + "\n";
        } else {
            root = "root: " + Hex.toHexString(getRoot().getEncryptedKey()) + "\n";
        }
        return root + traceAction.getOutput();
    }
    
    public void dumpTrie() throws IOException, NoSuchAlgorithmException {
        Util.writeToFile(Util.TREEDUMP_FILE_PATH, this.getTrieDump());
    }
    
    public interface ScanAction {
        public void doOnNode(EncryptedNode encryptedNode, Node node);
    }
}
