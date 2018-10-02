package rest;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@WebServlet(name = "trie-get", urlPatterns = "/trie/get")
public class TrieGet extends TrieRouter {
    
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String key = req.getParameter("key");
        try {
            long startStepGet = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
            String value = new String(getInstance().read(key));
            long endStepGet = TimeUnit.NANOSECONDS.toMicros(System.nanoTime());
            String response =
                            "\"Value\" : \"" + value + "\", " +
                            "\"StartStepGet\" : \"" + startStepGet + "\", " +
                            "\"EndStepGet\" : \"" + endStepGet + "\", "
                    ;
            resp.getWriter().write(response);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}