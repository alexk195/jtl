import java.io.*;
import java.util.*;
import java.text.*;
public class example3 extends JTLTemplate
{
  static public void main(String[] args) {
      JTLTemplate._run(args,new example3(new JTLContext()),"example3");
  }
  public example3(JTLContext ctxIn) { ctx(ctxIn); }
  @Override
  protected void process(Object object)  throws Exception {
  JTLEntity entity=null;
  if ( object instanceof JTLEntity) entity = (JTLEntity)object;
  // Code from jtl file follows
println("Dumping contents of definition in def format:");                       _line(1);
entity.dump();                                                                  _line(2);
println("");                                                                    _line(3);
println("");                                                                    _line(4);
println("");                                                                    _line(5);
} // end of process
} // end of class

