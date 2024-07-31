import java.io.*;
import java.util.*;
import java.text.*;
// Put additional imports here
import java.net.URL;
// End of extra imports
public class example4 extends JTLTemplate
{
  static public void main(String[] args) {
      JTLTemplate._run(args,new example4(new JTLContext()),"example4");
  }
  public example4(JTLContext ctxIn) { ctx(ctxIn); }
  @Override
  protected void process(Object object)  throws Exception {
  JTLEntity entity=null;
  if ( object instanceof JTLEntity) entity = (JTLEntity)object;
  // Code from jtl file follows
 file("example4_out.txt");                                                      _line(1);
println("Example 4:");                                                          _line(2);
 manual_begin("edit-by-hand");                                                  _line(3);
 manual_end();                                                                  _line(4);
 manual_begin(entity);                                                          _line(5);
 manual_end();                                                                  _line(6);
 String tmp = manual_prefix();                                                  _line(7);
 manual_prefix("//--myprefix--");                                               _line(8);
 manual_begin("edit-by-hand2");                                                 _line(9);
 manual_end();                                                                  _line(10);
 manual_prefix(tmp);                                                            _line(11);
 manual_begin("edit-by-hand3");                                                 _line(12);
 manual_end();                                                                  _line(13);
 manual_patterns("// MANUAL START @id@","// MANUAL END @id@");                  _line(14);
 manual_begin("myid");                                                          _line(15);
 manual_end();                                                                  _line(16);
 manual_patterns_default();                                                     _line(17);
 manual_begin("myid2");                                                         _line(18);
 manual_end();                                                                  _line(19);
println("Here is some text with newline\\n in between");                        _line(20);
println("Here is some text without newline in between");                        _line(21);
println("End of file");                                                         _line(22);
 close();                                                                       _line(23);
println("");                                                                    _line(24);
} // end of process
} // end of class

