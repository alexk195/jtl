
/**
 * Definition parser. Read files in format showed below.
 * Result is a tree with entities. Each entity can have parameters.
 *
 * File format:
 *
 * entity (param1,param2,...)
 * {
 *  entity(...)
 *  {
 *   ...
 *  }
 *  ...
 * }
 *
 *
 */
import java.io.*;

public class JTLDefinitionParser {

    StreamTokenizer tokenizer;
    JTLEntity root;
    FileInputStream fis;
    InputStreamReader isr;
    boolean jsonMode = false;
    boolean csvMode = false;
    boolean defMode = false;
    
    public static final char PARAM_BEGIN = '(';
    public static final char PARAM_END = ')';
    public static final char PARAM_SEPAR = ',';
    public static final char BLOCK_BEGIN = '{';
    public static final char BLOCK_END = '}';
    public static final char QUOTE_CHAR = '"';

    public static final char JSON_OBJ_SEPAR = ',';
    public static final char JSON_KEY_VALUE_DELIM = ':';
    public static final char JSON_ARRAY_BEGIN = '[';
    public static final char JSON_ARRAY_END = ']';
    public static final String JSON_ELEM_NAME = "_elem_";
    public static final String JSON_ARRAY_NAME = "_array_";

    public static final char CSV_DELIM = ';';
    public static final String CSV_ELEM_NAME = "_elem_";
    
    // parse current entity in e
    // current token is the entity name
    protected void parseParams(JTLEntity e) throws Exception {
        tokenizer.nextToken();
        while (tokenizer.ttype != PARAM_END) {
            if (tokenizer.ttype == QUOTE_CHAR) {
                e.params.addElement(tokenizer.sval);
            } else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
                e.params.addElement(tokenizer.sval);
            } else {
                throw new Exception("Parameter expected. Found: " + tokenizer.toString());
            }
            tokenizer.nextToken();
            if (tokenizer.ttype == PARAM_SEPAR) {
                tokenizer.nextToken();
            }
        }
        tokenizer.nextToken();
    }

    protected void parseBlock(JTLEntity e) throws Exception {
        tokenizer.nextToken();
        while (tokenizer.ttype != BLOCK_END) {
            if ((tokenizer.ttype == StreamTokenizer.TT_WORD) || (tokenizer.ttype == QUOTE_CHAR)) {
                JTLEntity newentity = new JTLEntity();
                parseEntity(newentity);
                e.addChild(newentity);
            } else {
                throw new Exception("Entity expected. Found: " + tokenizer.toString());
            }

        }
        tokenizer.nextToken();// eat the '}'
    }

    protected void parseJsonArray(JTLEntity e) throws Exception {
        tokenizer.nextToken(); // eat [
        while (tokenizer.ttype != ']') {
            JTLEntity rs = new JTLEntity();
            rs.name = JSON_ELEM_NAME;
            e.addChild(rs);
            parseJsonRightSite(rs);

            tokenizer.nextToken(); // expect , or ]
            if ((tokenizer.ttype != JSON_ARRAY_END) && (tokenizer.ttype != JSON_OBJ_SEPAR)) {
                throw new Exception("Array end or obj separator expected. Found: " + tokenizer.toString());
            }
            if (tokenizer.ttype == JSON_OBJ_SEPAR) {
                tokenizer.nextToken();//eat ,
            }
        }
    }

    // Parse the right site of :
    protected void parseJsonRightSite(JTLEntity e) throws Exception {

       // JTLOut.out.println("p0: " + tokenizer.ttype);

        if (tokenizer.ttype == QUOTE_CHAR) {
            e.params.addElement(tokenizer.sval);
        } else if (tokenizer.ttype == StreamTokenizer.TT_WORD) {
            e.params.addElement(tokenizer.sval);
        } else if (tokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            e.params.addElement(tokenizer.sval);
        } else if (tokenizer.ttype == JSON_ARRAY_BEGIN) {
            e.addParam(JSON_ARRAY_NAME);
            parseJsonArray(e);
        } else if (tokenizer.ttype == BLOCK_BEGIN) {
            parseJsonObject(e);
        } else {
            throw new Exception("Unexpected. Found: " + tokenizer.toString());
        }
    }

    /**
     * Starts with '{' ends with '}' separated with ','
     */
    protected void parseJsonObject(JTLEntity e) throws Exception {

        if (tokenizer.ttype != BLOCK_BEGIN) {
            throw new Exception("Object expected. Found: " + tokenizer.toString());
        }
        tokenizer.nextToken(); // eat {

        boolean parseNextKeyValue = true;

        while (parseNextKeyValue) {
            // either we have empty list -> }
            // or key-value pair
            if (tokenizer.ttype == QUOTE_CHAR || tokenizer.ttype == StreamTokenizer.TT_WORD) {
                // key found
                JTLEntity keyval = new JTLEntity();
                e.addChild(keyval);
                keyval.name = tokenizer.sval;
                tokenizer.nextToken();

                // expect :
                if (tokenizer.ttype != JSON_KEY_VALUE_DELIM) {
                    throw new Exception("Symbol : expected. Found: " + tokenizer.toString());
                }

                tokenizer.nextToken(); // eat :

                // parse right site (value/object)
                parseJsonRightSite(keyval);

                tokenizer.nextToken(); // expect , or end
				
                parseNextKeyValue = (tokenizer.ttype == JSON_OBJ_SEPAR);

                if (parseNextKeyValue) {
                    tokenizer.nextToken(); //eat ,
                }
            }
			else
			{
				throw new Exception("Key expected / unneeded separator. Before: " + tokenizer.toString());
			}
        }
        if (tokenizer.ttype != BLOCK_END) {
            throw new Exception("Object end } expected. Found: " + tokenizer.toString());
        }
    }

    protected void parseEntity(JTLEntity e) throws Exception {

        if ((tokenizer.ttype == StreamTokenizer.TT_EOF))
            return;

        if ((tokenizer.ttype != StreamTokenizer.TT_WORD) && (tokenizer.ttype != QUOTE_CHAR)) {
            throw new Exception("Identifier expected. Found: " + tokenizer.toString());
        }
        e.name = tokenizer.sval;
        tokenizer.nextToken();
        //JTLOut.out.println("1: " + tokenizer.ttype);
        if (tokenizer.ttype == PARAM_SEPAR) {
            //JTLOut.out.println("2: ");
            tokenizer.nextToken(); // accept comma as separator for entity
            return;
        }

        if (tokenizer.ttype == PARAM_BEGIN) {
           // JTLOut.out.println("3: ");
            parseParams(e);
           // JTLOut.out.println("4: ");

        }

        if (tokenizer.ttype == PARAM_SEPAR) {
           // JTLOut.out.println("5: ");

            tokenizer.nextToken();// accept comma as separator for entity
            return;
        }

        //JTLOut.out.println("6: ");
        //while (tokenizer.ttype == tokenizer.TT_EOL) tokenizer.nextToken();
        if (tokenizer.ttype == BLOCK_BEGIN) {
            parseBlock(e);
        }

       // JTLOut.out.println("7: ");
    }

    public void parseDefStart() throws java.io.IOException, Exception {
        tokenizer.nextToken();
        parseEntity(root);
    }

    public void parseJsonStart() throws java.io.IOException, Exception {
        root.name = "json";
        tokenizer.nextToken();
        if (tokenizer.ttype != BLOCK_BEGIN) {
            throw new Exception("JSON format must start with a block {}");
        }
        parseJsonObject(root);
    }

    public void parseCsvStart() throws java.io.IOException, Exception {
        root.name = "csv";
        tokenizer.nextToken();
        while (tokenizer.ttype != StreamTokenizer.TT_EOF)
        {
            JTLEntity e = new JTLEntity();
            e.name = CSV_ELEM_NAME;
            while (tokenizer.ttype != StreamTokenizer.TT_EOL)
            {
                e.addParam(tokenizer.sval);
                tokenizer.nextToken();
                if (tokenizer.ttype != CSV_DELIM && tokenizer.ttype != StreamTokenizer.TT_EOL)
                    throw new Exception("CSV elements must be delimited by "+CSV_DELIM);
                if (tokenizer.ttype == CSV_DELIM)
                    tokenizer.nextToken(); // eat ;
            }
            tokenizer.nextToken(); // eat EOL
            if (e.params.size()>0)
                root.addChild(e);
        }
    }
    
    public void parseDefFormat() throws java.io.IOException, Exception {
        tokenizer.commentChar('/');
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('_', '_'); // accept underscore as part of identifier
		tokenizer.wordChars('-', '-'); // accept - as part of identifier
        tokenizer.wordChars(':', ':'); // accept : as part of identifier
		tokenizer.wordChars('.', '.'); // accept . as part of identifier
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.eolIsSignificant(false);

        defMode = true;
        parseDefStart();
    }

    public void parseJsonFormat() throws java.io.IOException, Exception {
        tokenizer.commentChar('/');
        tokenizer.slashSlashComments(true);
        tokenizer.slashStarComments(true);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('_', '_'); // accept underscore as part of identifier
		tokenizer.wordChars('-', '-'); // accept - as part of identifier
		tokenizer.wordChars('.', '.'); // accept . as part of identifier
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.eolIsSignificant(false);

        jsonMode = true;
        parseJsonStart();
    }

    public void parseCsvFormat() throws java.io.IOException, Exception {
        tokenizer.commentChar('/');
        tokenizer.slashSlashComments(false);
        tokenizer.slashStarComments(false);
        tokenizer.whitespaceChars(0, ' ');
        tokenizer.wordChars('A', 'Z');
        tokenizer.wordChars('a', 'z');
        tokenizer.wordChars('0', '9');
        tokenizer.wordChars('_', '_'); // accept underscore as part of identifier
		tokenizer.wordChars('-', '-'); // accept - as part of identifier
		tokenizer.wordChars('.', '.'); // accept . as part of identifier
        tokenizer.quoteChar(QUOTE_CHAR);
        tokenizer.eolIsSignificant(true);
        
        csvMode = true;
        parseCsvStart();
    }    
    
    public JTLDefinitionParser(String filename) throws java.io.IOException, Exception {
        root = new JTLEntity();
        fis = new FileInputStream(filename);
        isr = new InputStreamReader(fis, "UTF8");
        tokenizer = new StreamTokenizer(isr);
        tokenizer.resetSyntax();
		JTLOut.out.print("Definition file format: ");
        if (filename.endsWith(".def")||filename.endsWith(".jtlp")) {
			JTLOut.out.println(" DEF");
            parseDefFormat();
        } else if (filename.endsWith(".json")) {
			JTLOut.out.println(" JSON");
            parseJsonFormat();
        } else if (filename.endsWith(".csv")){
			JTLOut.out.println(" CSV");
            parseCsvFormat();
        }
		else
		{
			 throw new Exception("Definition format not identified");
		}
    }

    public static void main(String[] args) throws Exception {

        if (args.length == 0) {
            JTLOut.out.println("JTLDefinitionParser reads a definition and prints the parsed structure.");
            JTLOut.out.println("Usage: JTLDefinitionParser <filename>");
            JTLOut.out.println("Supported formats by file extension: def,json,csv");
            return;
        }
		JTLOut.out.println("JTLDefinitionParser main called with "+args[0]);
		
        JTLDefinitionParser parser = new JTLDefinitionParser(args[0]);

        JTLOut.out.println("Model in Def Format:");
        parser.root.dump(0);
        JTLOut.out.println("Model in XML Format:");
        parser.root.dumpXML(0);
    }
}
