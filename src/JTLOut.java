
import java.io.*;
/**
 Console output using specific character encoding
*/
public class JTLOut {

    public static PrintStream out;
    public static PrintStream err;
    
    static {
        try {
            out = new PrintStream(System.out, true, "Cp850");
            err = new PrintStream(System.err, true, "Cp850");
        } catch (UnsupportedEncodingException ex) {
            ex.printStackTrace(System.err);
        }
    }
}
