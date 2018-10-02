package util;

import java.io.*;

public class Util {
    public static final String ROOTHASH_FILE_PATH = "./trie/root-hash.txt";
    public static final String TREEDUMP_FILE_PATH = "./trie/trie-dump.json";
    public static final String DATABASE_DIR_PATH = "./trie/leveldb-persistance";
    
    public static String readFile(String filePath) {
        try{
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            return reader.readLine();
        }catch(FileNotFoundException fe){
            File f = new File(filePath);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
        return null;
    }
    
    public static void writeToFile(String filePath, String dump) {
        try{
            BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));
            writer.write(dump);
            writer.close();
        }catch(FileNotFoundException fe){
            File f = new File(filePath);
        }catch(IOException ioe){
            ioe.printStackTrace();
        }
    }
}

