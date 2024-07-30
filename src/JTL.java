import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import javax.tools.StandardJavaFileManager;
import javax.tools.JavaFileObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Main class for executing JTL projects.
 * Basic structure of project file (def file format):
 * myproject
 * {
 *     template_rekursive
 *     {
 *         folder("src/example")
 *         template("rekursive.jtl")
 *     }
 *
 *     template_1
 *     {
 *         folder("src/example")
 *         template("example1.jtl")
 *         defs
 *         {
 *             def("example1.def")
 *         }
 *     }
 * }
 *
 */
public class JTL {
    private JTLDefinitionParser parser;
    String osName;
    String classPathParam;
    String classPathDelim;
    String classPathKey;
    Path jtlJar;
    Path projectFilePath;
    Path projectFolderPath;
    private boolean verbose = false;
    private boolean onWindows = false;
    void log(String s)
    {
        if (verbose)
            JTLOut.out.println(s);
    }

    void err(String s)
    {
        JTLOut.err.println(s);
    }

    public JTL(boolean verbose) throws Exception
    {
        this.verbose = verbose;
    }

    public void compileTemplate(Path folder, String javaFileName)
    {
        String folderStr = folder.toAbsolutePath().toString();
        log("Working folder: "+folderStr);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            System.err.println("Java Compiler not available. Ensure that you're using a JDK, not a JRE.");
            return;
        }
        // Set up a file manager
        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);

        // Get a Java file object from the file manager
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(
                Arrays.asList(new File(javaFileName))
        );

        // Compilation options
        List<String> options = Arrays.asList(
                classPathKey, classPathParam+folderStr,
                "-d", folderStr          // Output directory for compiled classes
        );

        // Create a compilation task
        JavaCompiler.CompilationTask task = compiler.getTask(
                null, // Writer, null means use the default System.err
                fileManager, // FileManager, null means use the default
                null, // DiagnosticListener, null means use the default
                options, // Compilation options, such as "-d bin"
                null, // Class names (for annotation processing), null means none
                compilationUnits // The Java file objects to compile
        );

        // Perform the compilation task
        boolean success = task.call();

        // Close the file manager
        try {
            fileManager.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Output the result
        if (success) {
            log("Compilation successful");
        } else {
            err("Compilation failed");
        }
    }

    public void executeTemplate(Path folder, String className, Object[] definitionFiles) throws Exception
    {
        Path classNamePath = Paths.get(className);
        String classFileName = classNamePath.getFileName().toString();

        URL classUrl = classNamePath.getParent().toAbsolutePath().toUri().toURL();
        URL jtlUrl = jtlJar.toAbsolutePath().toUri().toURL();

        URL[] classUrls = { classUrl, jtlUrl };
        log("Loading class with URLS:"+classUrl.toString()+","+jtlUrl.toString());

        // Create a class loader
        URLClassLoader classLoader = new URLClassLoader(classUrls);

        // Load the class by name
        Class<?> loadedClass = classLoader.loadClass(classFileName);

        Method mainMethod = loadedClass.getMethod("main", String[].class);

        Object[] methodArgs = new Object[] { definitionFiles };

        // Invoke a method with arguments
        mainMethod.invoke(null, methodArgs);
    }


    public void run(String prj_fname) throws Exception
    {
        projectFilePath = Paths.get(prj_fname);

        if (!projectFilePath.toFile().exists())
        {
            err("Project file not found:"+prj_fname);
            throw new FileNotFoundException(prj_fname);
        }
        else {
            log("Project file: "+projectFilePath.toAbsolutePath());
        }
        projectFolderPath = projectFilePath.toAbsolutePath().getParent();

        log("Project folder: "+projectFolderPath.toAbsolutePath());

        parser = new JTLDefinitionParser(prj_fname);
        parser.parseDefFormat();

        // detect platform
        osName = System.getProperty("os.name").toLowerCase();
        if (osName.contains("win")) {
            classPathDelim = ";";
            onWindows = true;
            classPathKey = "-classpath";
        } else if (osName.contains("nix") || osName.contains("nux") || osName.contains("aix")) {
            classPathDelim = ":";
            classPathKey = "-classpath";
        } else if (osName.contains("mac")) {
            classPathDelim = ":";
            classPathKey = "-classpath";
        } else {
            err("Unknown operating system: " + osName);
        }

        // look for JTL.jar in common locations folder
        jtlJar = Paths.get("JTL.jar");
        if (!jtlJar.toFile().exists())
        {
            jtlJar = projectFolderPath.resolve("JTL.jar");
            if (!jtlJar.toFile().exists())
            {
                String jarPath = JTL.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
                if (onWindows)
                    jarPath = jarPath.substring(1);
                log("Expecting JTL.jar in: "+jarPath);
                jtlJar = Paths.get(jarPath);
                if (!jtlJar.toFile().exists())
                {
                    throw new Exception("JTL: JTL.jar not found");
                }
            }
        }


        log("Using JTL.jar: "+jtlJar.toAbsolutePath().toString());
        String jtlJarStr = jtlJar.toAbsolutePath().toString();

        classPathParam = jtlJarStr+classPathDelim;

        for (JTLEntity template: parser.root.children) {
            log("Template:"+template.name);
            String folder = template.child("folder").param(0);
            String template_name = template.child("template").param(0);
            // Compile JTL
            Path templ_path = projectFolderPath.resolve(Paths.get(folder, template_name));

            Path normalized_templ_path = templ_path.normalize().toAbsolutePath();
            String abs_templ_path = normalized_templ_path.toString();
            log("Template full path:" + abs_templ_path);
            JTLC jtlc = new JTLC(abs_templ_path);
            jtlc.run();
            log("Template Java file:" + jtlc.getJavaTemplateFile());

            Path localFolder = projectFolderPath.resolve(folder);
            compileTemplate(localFolder, jtlc.getJavaTemplateFile());

            if (template.hasChild("defs")) {
                ArrayList<String> definitionFiles = new ArrayList<>();

                for (JTLEntity def_file : template.child("defs").children) {
                    Path defFilePath = localFolder.resolve(def_file.param(0));
                    definitionFiles.add(defFilePath.toAbsolutePath().toString());
                }

                Path templClassPath = templ_path.resolve(jtlc.getJavaTemplateFile());
                String className = templClassPath.toAbsolutePath().toString().replace(".java", "");
                executeTemplate(localFolder, className, definitionFiles.toArray(new String[0]));
            }
        }
    }

    public static void main(String[] args) throws Exception {
        boolean verbose = false;
        ArrayList<String> fileparams = new ArrayList<>();
        if (args.length == 0) {
            JTLOut.out.println("JTL Version "+JTLContext.majorVersion+"."+JTLContext.minorVersion);
            JTLOut.out.println("Usage: JTL [--verbose] <project file 1> <project file 2>");
        } else {

            for (String arg : args) {
                if (arg.equals("--verbose")) {
                    JTLOut.out.println("Verbose mode on");
                    verbose = true;
                }
                else {
                    fileparams.add(arg);
                }
            }

            JTL jtl = new JTL(verbose);
            for (String projectFile: fileparams)
            {
                jtl.run(projectFile);
            }
        }
    }
}

