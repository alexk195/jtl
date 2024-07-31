import java.io.*;
import java.util.*;
import java.text.*;
public class example2 extends JTLTemplate
{
  static public void main(String[] args) {
      JTLTemplate._run(args,new example2(new JTLContext()),"example2");
  }
  public example2(JTLContext ctxIn) { ctx(ctxIn); }
  @Override
  protected void process(Object object)  throws Exception {
  JTLEntity entity=null;
  if ( object instanceof JTLEntity) entity = (JTLEntity)object;
  // Code from jtl file follows

JTLEntity prompts = entity.child("example2").child("prompts");                  _line(2);
prompts.dump(); // just dump the contents
file("main.cpp"); // create a file for output
println("// compile with \"g++ main.cpp -o example2\"");                        _line(6);
println("#include <iostream>");                                                 _line(7);
println("int main(int argc, char**argv) {");                                    _line(8);
println("	// define variables");                                                _line(9);
for (JTLEntity e:prompts.children) {
println("	"+e.param(0)+" "+e.name+";");                                         _line(11);
}
println("	// set from std::cin");                                               _line(13);
for (JTLEntity e:prompts.children) {
println("	std::cout << \"Type in "+e.name+" :\";");                             _line(15);
println("	std::cin  >> "+e.name+";");                                           _line(16);
}
println("	// print them");                                                      _line(18);
println("	std::cout << \"Your data:\" << std::endl;");                          _line(19);
println("");                                                                    _line(20);
for (JTLEntity e:prompts.children) {
println("	std::cout << \""+e.name+":\" << "+e.name+" << std::endl;");           _line(22);
}
println("	return 0;");                                                          _line(24);
println("}");                                                                   _line(25);
close();                                                                        _line(26);
} // end of process
} // end of class

