
/**
 * Compiles jtl file (Java template language) into java file.
 *
 *
 */
import java.io.*;

public class JTLC {

    JTLDefinitionParser defparser;
    
    /// name of template file
    String tname;
    RandomAccessFile templateReader;
    int linenr;
    int skippedLines;
    boolean lastCheckOK;
    boolean inCodeBlock;
    String classFileTemplate;
    String javaTemplateFile;
    PrintStream pout;

    // Escape sequences
    static final String CS_B = "@<"; // code section start
    static final String CS_E = "@>"; // ... end
    static final String CW_B = "@["; // code word start
    static final String CW_E = "]@"; // ... end
    static final String CL = "@";  // code line

    /// all template output shall be done using this method
    void tout(String s) {
        pout.println(s);
    }

    /// Constructor requires name of template file (jtl file)
    public JTLC(String tname) {
        this.tname = tname;

    }

    public String getJavaTemplateFile()
    {
        return javaTemplateFile;
    }

    /// Checks if line starts with particular string (front). If yes "front" will be replaced by string "repl".
    protected String checkReplace(String line, String front, String repl) {
        String ts = line.trim();
        lastCheckOK = ts.startsWith(front);
        if (lastCheckOK) {
            return repl + line.substring(line.indexOf(front) + front.length());
        }

        return line;
    }

    /// Each created java file will have this header
    protected void printTemplateHeader() {

        tout("import java.io.*;");
        tout("import java.util.*;");
		tout("import java.text.*;");

        RandomAccessFile headerReader;
        try {
            headerReader = new RandomAccessFile(classFileTemplate + ".jtl_header", "r");
            String line = headerReader.readLine();
            while (line != null) {
                tout(line);
                line = headerReader.readLine();
            }
        } catch (FileNotFoundException e) {
            JTLOut.out.println("No jtl_header file found. Proceed with default header");
        } catch (IOException e) {
            JTLOut.out.println(e.getLocalizedMessage());
            e.printStackTrace(JTLOut.err);
        }

        tout("public class " + classFileTemplate + " extends JTLTemplate");
        tout("{");
        tout("  static public void main(String[] args) {");
        tout("      JTLTemplate._run(args,new " + classFileTemplate + "(new JTLContext()),\"" + classFileTemplate + "\");");
        tout("  }");
        tout("  public " + classFileTemplate + "(JTLContext ctxIn) { ctx(ctxIn); }");
        tout("  @Override");
        tout("  protected void process(Object object)  throws Exception {");
        tout("  JTLEntity entity=null; if ( object instanceof JTLEntity) entity=(JTLEntity)object;");
        tout("  JTLEntity root=entity;");
        tout("  // Code from jtl file follows");
    }

    /// Each created java file will have this footer
    protected void printTemplateFooter() {
        tout("} // end of process");
        tout("} // end of class");
        tout("");
    }

    /// print java code line 
    protected void print_code(String s) {
        if (s.trim().endsWith(";")) {
            String fill = "";
            for (int i = s.length(); i < 80; i++) {
                fill += " ";
            }
            tout(s + fill + "_line(" + linenr + ");");
        } else {
            tout(s);
        }
    }

    /// print no-code line but replace code-words (=java statements)
    protected void print_nocode(String s) throws Exception {

		s = s.replace("\\", "\\\\");
		//s = s.replace("\"", "\\\"");
		
        String res = "";
        boolean inCode = false;
        boolean inStr = false;
        for (int i = 0; i < s.length(); i++) {

            String t;
            if (i < s.length() - 1) {
                t = s.substring(i, i + 2);
            } else {
                t = s.substring(i, i + 1);
            }

            if (t.equals(CW_B)) {
                if (!inCode) {
                    res += "\"+";
                    inCode = true;
                    i++;
                } else {
                    throw (new Exception("Nested code sections not allowed : " + s));
                }
            } else if (t.equals(CW_E)) {
                if (inCode) {
                    res += "+\"";
                    inCode = false;
                    i++;
                } else {
                    throw (new Exception("Unmatched " + CW_E + " symbol in :" + s));
                }
            } else if (t.charAt(0) == '"' && !inCode) {
                res += "\\\"";
                inStr = !inStr;
            } else {
                res += t.charAt(0);
            }
        }

        if (inStr) {
            throw (new Exception("Unclosed string"));
        }

        // now we create java code line by using println
        String codestr = "println(\"" + res + "\");";
        print_code(codestr); // reuse the print_code method 
    }

    /// process a single line in jtl file
    protected void processTemplateLine(String line) throws Exception {
        String ts = line;
        ts = checkReplace(line, CS_B, "");
        if (lastCheckOK) {
            if (!inCodeBlock) {
                inCodeBlock = true;
                print_code(ts);
                return;
            } else {
                JTLOut.err.print("Error: Found nested " + CS_B + " in line ");
                JTLOut.err.println(linenr);
                throw new Exception("Error: Found nested " + CS_B + " in: " + line);
            }
        }

        ts = checkReplace(line, CS_E, "");
        if (lastCheckOK) {
            if (inCodeBlock) {
                inCodeBlock = false;
                // it probably was not intended to print some empty text after closing bracket. 
                if (ts.trim().length() != 0) {
                    print_nocode(ts);
                }
                return;
            } else {
                JTLOut.err.print("Error: Found unmatched " + CS_E + " in line ");
                JTLOut.err.println(linenr);
                throw new Exception("Error: unmatched " + CS_E + " in :" + line);
            }
        }

        // check for expression at the beginning, since it has same start as code line
        checkReplace(line, CW_B, CW_B);
        if (lastCheckOK) {
            print_nocode(line);
            return;
        }

        // check for single line code sequence
        ts = checkReplace(line, CL, "");
        if (lastCheckOK) {
            print_code(ts);
            return;
        }

        // if in code block print code
        if (inCodeBlock) {
            print_code(line);
            return;
        }

        // otherwise in no-code sections. print with replaced expressions
        print_nocode(line);
    }

    /// Process a jtl template file line by line. TemplateReader was already setup to read the jtl file
    protected void processTemplate() {
        JTLOut.out.println("Generating java file: " + javaTemplateFile);

        linenr = 0;
        printTemplateHeader();
        String line = "";
        try {
            skippedLines = 0;
            line = templateReader.readLine();
            linenr++;
            while (line != null) {
                // = line.trim();
                processTemplateLine(line);
                line = templateReader.readLine();
                linenr++;
            }

        } catch (Exception e) {

            JTLOut.err.print("Template Line ");
            JTLOut.err.print(linenr);
            if (line != null) {
                JTLOut.out.println(": " + line.trim());
            }
            JTLOut.err.println(e.getMessage());
            e.printStackTrace(JTLOut.err);
        }
        printTemplateFooter();
        JTLOut.out.println("Succesfully generated java file: " + javaTemplateFile);
        JTLOut.out.println("Compile it and run with your definition file");
    }

    /// Runs the JTLC compiler for jtl file provided in tname
    /// Creates file readers and writers, setup of class names, etc
    public void run() {
        JTLOut.out.println("JTL file: " + tname);

        try {
            File file = new File(tname);
            templateReader = new RandomAccessFile(tname, "r");
            classFileTemplate = file.getName().replace(".jtl", "");
            javaTemplateFile = file.getAbsolutePath().replace(".jtl", ".java"); 
            pout = new PrintStream(new FileOutputStream(javaTemplateFile), true, "UTF8");
            processTemplate();
            pout.close();
        } catch (FileNotFoundException e) {
            JTLOut.out.println(e.getLocalizedMessage());
        } catch (UnsupportedEncodingException e) {
            JTLOut.out.println(e.getLocalizedMessage());
            e.printStackTrace(JTLOut.err);
        }

    }

    /// function called from command line
    public static void main(String[] args) throws Exception {
        JTLC jtlc;

        JTLOut.out.println("JTCL: Java Template Language Compiler. Version "+JTLContext.majorVersion+"."+JTLContext.minorVersion);

        if (args.length == 0) {
            JTLOut.out.println("Usage: JTCL <template_file_1.jtl> <template_file_2.jtl> ... ");
        } else {

            for (String jtl_fname : args) {
                jtlc = new JTLC(jtl_fname);
                jtlc.run();
            }
        }

    }
}
