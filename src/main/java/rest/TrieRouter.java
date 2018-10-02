package rest;

import trie.Trie;

import javax.servlet.http.HttpServlet;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public class TrieRouter extends HttpServlet {
    protected static Trie trie;
    
    
    protected static Trie getInstance() throws IOException, NoSuchAlgorithmException {
        if (trie == null) {
            trie = Trie.getTrie();
        }
        return trie;
    }
}
