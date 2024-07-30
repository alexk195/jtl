
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Vector;

/**
 * JTLTemplate. All templates are subclasses of this.
 */
public class JTLTemplate {

    private JTLContext ctx = null; ///< this context is shared during processing of one root template 

    public JTLTemplate() {
    }

    /// methods visible by templates. They are not allowed to start with _
	
    /// prints a line to output. This is actually the same as writing a line of text in non-control-code section. 
    public void println(CharSequence c) throws IOException {
        ctx.println(c);
    }

    /// prints vector of strings to output. This is actually the same as writing a line of text in non-control-code section. 
	public void println(Vector<String> vs) throws IOException {
		for (String s:vs)
		{
			ctx.println(s);
		}
	}
	
    /// starts a manual code section with given entity (JTLEntity.fullpath() will be used as key)
    public void manual_begin(JTLEntity e) throws Exception {
        ctx.manual_begin(e);
    }

    /// starts a manual code section with id used as section key
    public void manual_begin(String id) throws Exception {
        ctx.manual_begin(id);
    }

    /// starts a manual code section with given entity (JTLEntity.fullpath() will be used as key)
    public void manual_start(JTLEntity e) throws Exception {
        warning("manual_start is Deprecated. Use manual_begin");
        ctx.manual_begin(e);
    }

    /// starts a manual code section with id used as section key
    public void manual_start(String id) throws Exception {
        warning("manual_start is Deprecated. Use manual_begin");
        ctx.manual_begin(id);
    }

    /// ends a manual code section
    public void manual_end() throws Exception {
        ctx.manual_end();
    }

	/// From Version 2.4. Sets manual section patterns. The String @id@ will be replaced by actual id.
	public void manual_patterns(String pbegin, String pend)
	{
		ctx.ManualSectionStartPattern = pbegin;
		ctx.ManualSectionEndPattern = pend;
		ctx.updateManualPatternsInWriter();
	}
	
	/// From Version 2.4. Sets manual section patterns to default
	public void manual_patterns_default()
	{
		ctx.ManualCodePrefix = ctx.DefaultManualCodePrefix;
		ctx.ManualCodePostfix = ctx.DefaultManualCodePostfix;
		ctx.updateManualPatterns();
	}
	
    /// start writing to new file with given filename 
    public void file(String filename) throws Exception {
        close();
        ctx.twriter = new JTLResultWriter(filename, ctx.definitionFileName, ctx.templateFileName);
		ctx.updateManualPatternsInWriter();
    }

	/// creates a folder if not existent (from version 2.2)
	public void folder(String foldername) throws Exception {
		close();
		try
		{
			Path p = Paths.get(foldername);
			if (!Files.exists(p)) {
				Files.createDirectory(p);
			} else {
			}
		}catch (Exception e)
		{
		}			
	}
	
	/// reads a file and gives returns its contents as vector of strings
	public Vector<String> load_file(String fname) throws Exception
	{
		return ctx.load_file(fname);
	}
	
    /// returns template file name
    public String template() {
        return ctx.templateFileName;
    }

    /// returns definition file name
    public String definition() {
        return ctx.definitionFileName;
    }

    /// sets prefix string for manual code sections. Used to identify manual code sections
    public void manual_prefix(String s) {
        ctx.ManualCodePrefix = s;
		ctx.updateManualPatterns();
    }

    /// returns prefix string for manual code sections. Used to identify manual code sections
    public String manual_prefix() {
        return ctx.ManualCodePrefix;
    }
	
    /// sets postfix string for manual code sections. Used to identify manual code sections
    public void manual_postfix(String s) {
        ctx.ManualCodePostfix = s;
		ctx.updateManualPatterns();
    }

    /// returns postfix string for manual code sections. Used to identify manual code sections
    public String manual_postfix() {
        return ctx.ManualCodePostfix;
    }

	/// String representation of version
	public String version()
	{
		String r=""+ctx.majorVersion+"."+ctx.minorVersion;
		return r;
	}
	
    /// returns context. Context must be passed to underlying templates
    public final JTLContext ctx() {
        return ctx;
    }

    /// sets context
    public final void ctx(JTLContext _ctx) {
        ctx = _ctx;
    }

    /// print error message from inside template
    public void warning(String what) {
        JTLOut.err.println(what);
    }

    /// Throws exception with error message. This also aborts generation. internal throw
    protected void error(String what) throws Exception {
        throw new Exception(what);
    }

	/// disable backup file creation for currently opened file
	public void disable_backup()
	{
		if (ctx.twriter != null)
			ctx.twriter.setCreateBackup(false);
	}
	
	/// enables backup file creation for currently opened file
	public void enable_backup()
	{
		if (ctx.twriter != null)
			ctx.twriter.setCreateBackup(true);
	}
	
	
    /// entry method for template generation, overriden in all templates
    protected void process(Object object) throws Exception {
    }

    /// closes current output file
    protected void close() throws IOException {
        if (ctx.twriter != null) {
            ctx.twriter.close();
			JTLOut.out.print("             ");
            JTLOut.out.print("Skipped Lines in manual code:");
            JTLOut.out.println(ctx.skippedLines);
            ctx.twriter = null;
        }
    }

    /// stores line of template for error messages
    protected final void _line(int n) {
        ctx.tline = n;
    }

    /// will be called by subclass templates main method
    public static void _run(String args[], JTLTemplate t, String templateName) {
		JTLOut.err.println("Run template "+templateName);
        if (args.length >= 1) {
            for (String defname : args) {
				JTLOut.err.println("Definition file "+defname);
                JTLDefinitionParser defparser;
                JTLContext ctx = t.ctx();
                try {
                    defparser = new JTLDefinitionParser(defname);
                    ctx.root = defparser.root;
                    ctx.definitionFileName = defname;
                    ctx.templateFileName = templateName;
                    t.process(defparser.root);
                    t.close();
                    t.ctx(new JTLContext());
                } catch (Exception e) {
                    JTLOut.err.println(e);
                    JTLOut.err.print("Near template line ");
                    JTLOut.err.println(ctx.tline);
                    e.printStackTrace(JTLOut.err);
                    JTLOut.err.println("Template processing will be finished now");

                    try {
                        t.close();
                    } catch (IOException e2) {
                        JTLOut.err.println(e2);
                        JTLOut.err.print("Near template line ");
                        JTLOut.err.println(ctx.tline);
                        e2.printStackTrace(JTLOut.err);
                        JTLOut.err.println("Template processing could not be finished");
                    }

                }
            }
        } else {
            JTLOut.err.println("Expected definition file(s) as argument");
        }

    }
}
