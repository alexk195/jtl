rm -f *.class
rm -f *.jar
javac --release 8 -g:none -d $PWD ../src/*.java 
jar cvfm JTL.jar MANIFEST.MF *.class
