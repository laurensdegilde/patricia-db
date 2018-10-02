package trie;

import org.iq80.leveldb.DB;
import org.iq80.leveldb.Options;
import util.Util;
import util.encrypt.EncryptedNode;

import java.io.File;
import java.io.IOException;

import static org.iq80.leveldb.impl.Iq80DBFactory.factory;

public class DatabaseExposure {
    
    private DB database;
    
    public DatabaseExposure() throws IOException {
        this.database = factory.open(new File(Util.DATABASE_DIR_PATH), new Options());
    }
    
    public void put(EncryptedNode node) {
        this.database.put(node.getEncryptedKey(), node.getEncodedValue());
    }
    
    public byte[] get(EncryptedNode node) {
        return this.database.get(node.getEncryptedKey());
    }
}
