import java.io.*;
import java.util.*;
import java.text.*;

/**
 * Code generation context. This will be shared during processing of one
 * template by main template and potential inner classes
 *
 */
public class JTLContext {

	/// Tool version major number
	public static final int majorVersion = 3;
	
	/// Tool version minor number
	public static final int minorVersion = 3;
	
    /// Writer object which will be used for output. If its null the standard
    /// output will be used
    public JTLResultWriter twriter; 

    public boolean inManualCode;
    public String definitionFileName;
    public String templateFileName;

    /// if true we are currently in manual section and skipping the generated output
    public boolean skipUntilManualSectionEnd;
    
    /// current user section key
    public String manualCodeKey;
    
    /// skipped lines if processing user sections
    public int skippedLines; 
    
    /// current template line
    public int tline; 
    
	/// default manual pattern for start
	public static final String DefaultManualStartPattern = "//--jtl--@id@--begin--*";

	/// default manual pattern for end
	public static final String DefaultManualEndPattern = "//--jtl--@id@--end--*";
	
	/// current manual pattern for start
    public String ManualSectionStartPattern = DefaultManualStartPattern;
	
	/// current manual pattern for end
    public String ManualSectionEndPattern = DefaultManualEndPattern;

	/// Default prefix
	public String DefaultManualCodePrefix = "//--jtl--";
	
	/// Default postfix
	public String DefaultManualCodePostfix = "*";
	
	/// Current prefix
	public String ManualCodePrefix = "//--jtl--";
	
	/// Current postfix
	public String ManualCodePostfix = "*";
	
	public void updateManualPatternsInWriter()
	{
		if (null != twriter)
		{
			twriter.setManualSectionBeginPattern(ManualSectionStartPattern);
			twriter.setManualSectionEndPattern(ManualSectionEndPattern);
		}		
	}
	public void updateManualPatterns()
	{
		ManualSectionStartPattern = ManualCodePrefix+"@id@--begin--"+ManualCodePostfix;
		ManualSectionEndPattern = ManualCodePrefix+"@id@--end--"+ManualCodePostfix;
		updateManualPatternsInWriter();
	}
		
    /// reference to the root entity
    public JTLEntity root;

    public JTLContext() {
        twriter = null;
        inManualCode = false;
        skipUntilManualSectionEnd = false;
        skippedLines = 0;
        manualCodeKey = null;
        root = null;
        tline = 0;
    }

   
    private String getManualSectionID(String s) {
        return s;
    }
   
    public void println(CharSequence c) throws IOException {
        if (skipUntilManualSectionEnd) {
            skippedLines++;
        } else {
            if (twriter == null) {
                JTLOut.out.println(c);
            } else {
                twriter.append(c);
            }
        }
    }

    public void manual_begin(JTLEntity e) throws Exception {
        manual_begin(e.fullpath());
    }
    
    
    public void manual_begin(String s) throws Exception {
        if (inManualCode) {
            _throw("Nested manual code sections are not allowed");
        } else {
            inManualCode = true;
        }

        manualCodeKey = s;

        String e_uuid = getManualSectionID(s);
        
        if (twriter==null)
            _throw("Manual code section can only be used after a file() commando was issued. Section ID="+s);
        
        if (twriter.copyManualSection(e_uuid, twriter)) {
            skipUntilManualSectionEnd = true;
        } else {
            twriter.write(twriter.getManualSectionID_Begin(e_uuid));
        }
    }
    public void manual_end() throws Exception {
        if (!inManualCode) {
            _throw("End of manual code without start");
        } else {
            inManualCode = false;
        }

        if (skipUntilManualSectionEnd) {
            skipUntilManualSectionEnd = false;
        } else {
            twriter.write(twriter.getManualSectionID_End(getManualSectionID(manualCodeKey)));
        }
    }

	/// reads a file and gives returns it's contents as vector of strings
	public Vector<String> load_file(String fname) throws Exception
	{
			Vector<String> filebuffer = new Vector<String>();
			FileInputStream fis = new FileInputStream(fname);
            InputStreamReader isr = new InputStreamReader(fis, "UTF8");
            BufferedReader in = new BufferedReader(isr);

            String line = in.readLine();
            while (line != null) {
                filebuffer.add(line);
                line = in.readLine();
            }
            in.close();
            fis.close();
			
			return filebuffer;
	}
	
    protected void _throw(String what) throws Exception {
        throw new Exception(what);
    }
}
