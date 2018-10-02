package rest;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@WebServlet(name = "trie-dump", urlPatterns = "/trie/dump")
public class TrieDump extends TrieRouter {
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        
        try {
            resp.getWriter().write("Dumping trie...");
            getInstance().dumpTrie();
            resp.getWriter().write("Done dumping trie");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}