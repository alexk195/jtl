
import java.io.*;
import java.util.*;
import java.text.*;
/**
 * Writes result generated by template into file. Text between manual sections 
 * is preserved.
 * 
 */
public class JTLResultWriter
        extends Writer {

    private final String definitionFileName;
    private final String templateFileName;
    private final Vector<String> Lines; ///< contains new processed file content
    private final String filename;
    private boolean oldFileExist = false;
	private boolean createBackup = true;
    private File oldFile;
	private String manualSectionBeginPattern = ""; ///< use @id@ for key 
	private String manualSectionEndPattern = ""; ///< use @id@ for key

    public Vector<String> filebuffer; ///< contains old file content

    private void loadOldFile(String fname) throws Exception {
        oldFile = new File(fname);

        oldFileExist = oldFile.exists();
        if (oldFileExist) {
            filebuffer.clear();

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
        }
    }

    private void twrite(String s) throws IOException {
        Lines.add(s);
    }

	private String combineAll(Vector<String> v,String endl)
	{
		String res = "";
		for (int i = 0; i < v.size(); i++) {
			if (v.elementAt(i) != null)
				res+= (String)v.elementAt(i) + endl;
		}
		return res;
	}
	
	private void displayDiff(String s1, String s2)
	{
			JTLOut.out.println("Size:" + s1.length() +" <> "+s2.length());
			JTLOut.out.println("s1[0]:" + s1.codePointAt(0) +" .. s1[last-3]"+s1.codePointAt(s1.length()-3));
			JTLOut.out.println("s2[0]:" + s2.codePointAt(0) +" .. s2[last-3]"+s2.codePointAt(s2.length()-3));
			int changes=0;
			int len=s1.length();
			if (s2.length()<len) len=s2.length();
			
			for (int i=0;i<len;i++)
			{
				int c1 = s1.codePointAt(i);
				int c2 = s2.codePointAt(i); 
				if (c1 != c2)
				{
					JTLOut.out.println("Diff at "+i+":"+c1+" <> "+c2);
					changes++;
				}
				if (changes>100)
					break;
			}
			
			//JTLOut.out.println("================ old file ===============");
			//JTLOut.out.println(s1);
			//JTLOut.out.println("================ new file ===============");
			//JTLOut.out.println(s2);
			//JTLOut.out.println("================ end of new file ===============");
	}
	
    private boolean identicalFiles() {
		
		String s1 = combineAll(filebuffer,"\r\n");
		String s2 = combineAll(Lines,"\r\n");
		boolean equal =  s1.equals(s2);
		if (!equal)
		{
			//displayDiff(s1,s2);
		}
		return equal;
    }

    // ------------------------------- PUBLIC --------------------------------
	
	public void setCreateBackup(boolean v)
	{
		createBackup = v;
	}
	
	public boolean getCreateBackup()
	{
		return createBackup;
	}
	
	public void setManualSectionBeginPattern(String p)
	{
		manualSectionBeginPattern = p;
	}

	public void setManualSectionEndPattern(String p)
	{
		manualSectionEndPattern = p;
	}
	
    public String getManualSectionID_Begin(String usersecID) {
        return manualSectionBeginPattern.replace("@id@",usersecID);
    }

    public String getManualSectionID_End(String usersecID) {
		return manualSectionEndPattern.replace("@id@",usersecID);
    }

    public boolean copyManualSection(String usersecID, Writer stream) throws
            Exception {
		
		// Search for beginning of user section
        String mstart = getManualSectionID_Begin(usersecID);
        boolean found = false;
        int i = 0;
        while ((i < filebuffer.size()) && (!found)) {
            found = ((String)filebuffer.elementAt(i)).trim().equals(mstart);
            i++;
        }

        if (!found) {
            return false;
        }

        // Start found, copy starting with the next line
		// is pointing to next line already
        String mend = getManualSectionID_End(usersecID);
        stream.write(mstart);
		boolean mendFound = false;
		do
		{
			mendFound = ((String) filebuffer.elementAt(i)).trim().equals(mend);
			if (!mendFound) stream.write((String) filebuffer.elementAt(i));
			i++;
		} while(!mendFound && i<filebuffer.size());
		
		if (!mendFound)
		{
			throw new IOException("Error: Was not able to find end of user section "+mstart);
		}

        stream.write(mend);
        return true;
    }

    @Override
    public void write(String s) throws IOException {
        twrite(s);
    }

    @Override
    public void write(char[] cbuf,
            int off,
            int len) throws IOException {
        String s = new String(cbuf, off, len);
        twrite(s);
    }

    @Override
    public void flush() {
        //
    }

    private String extendString(String in, int length) {
        String res = in;
        for (int i = in.length(); i < length; i++) {
            res += " ";
        }
        return res;
    }

    @Override
    public void close() throws IOException {
        if (Lines.isEmpty()) {
            return;
        }

        boolean canWriteNewFile = false;
        boolean backupCreated = false;
        boolean identicalFiles = false;
        File backupFile = null;

        if (!oldFileExist || !createBackup) {
            canWriteNewFile = true;
        } else {
            identicalFiles = identicalFiles();
            if (!identicalFiles) {
                SimpleDateFormat formatter = new SimpleDateFormat(
                        " yyyy-MM-dd hh-mm-ss-sss");
                Date currentTime_1 = new Date();
                String dateString = formatter.format(currentTime_1);

                String backupFilename = filename + dateString + ".bak";
                backupFile = new File(backupFilename);

                if (oldFile.renameTo(backupFile)) {
                    canWriteNewFile = true;
                    backupCreated = true;
                } else {
                    throw new IOException(
                            "Error: file could not be backuped to  "+
                             backupFile.getAbsolutePath()+ 
                            ". Please check if destination file not alread exists.");
                }
            }

        }

        if (canWriteNewFile) {
            PrintStream out = new PrintStream(new FileOutputStream(filename), true, "UTF8");
            for (int i = 0; i < Lines.size(); i++) {
                out.println((String) Lines.elementAt(i));
            }
            out.close();
        }
        Lines.clear();
        filebuffer.clear();

        String res;
        if (identicalFiles) {
            res = "Unmodified : ";
        } else {
            res = "Modified   : ";
        }

        res += filename;

        JTLOut.out.println(res);

        res = "             ";
        res += "Definition File: " + definitionFileName + ", Class: " + templateFileName;

        JTLOut.out.println(res);

        if (backupCreated && backupFile != null) {
            res = "             ";
            res += "Backup: " + backupFile.getAbsolutePath();
            JTLOut.out.println(res);
        }

    }



    public JTLResultWriter(String fname, String definitionFileNameIn, String templateFileNameIn) throws Exception {
        definitionFileName = definitionFileNameIn;
        templateFileName = templateFileNameIn;
        Lines = new Vector<String>();
        filebuffer = new Vector<String>();
        filename = fname;
        loadOldFile(fname);
    }
}
