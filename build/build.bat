del /Q *.class
del /Q *.jar
REM javac --release 8 -g:none -d . ../src/*.java 
javac --release 8 -g -d . ../src/*.java 
jar cvfm JTL.jar MANIFEST.MF *.class
