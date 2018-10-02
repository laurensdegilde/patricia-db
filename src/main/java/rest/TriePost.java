package rest;


import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

@WebServlet(name = "trie-post", urlPatterns = "/trie/post")
public class TriePost extends TrieRouter {
    
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String key = req.getParameter("key");
        String value = req.getParameter("value");
        try {
            long start = System.currentTimeMillis();
            Long [] timeSet = getInstance().write(key, value);
            long end = System.currentTimeMillis();
            
            String response =
                            "\"StartStepGet\" : \"" + start + "\", " +
                            "\"EndStepGet\" : \"" + end + "\", " +
                            "\"StartStepInsert\" : \"" + timeSet[0] + "\", " +
                            "\"EndStepInsert\" : \"" + timeSet[1] + "\", "
                    ;
            resp.getWriter().write(response);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
}
