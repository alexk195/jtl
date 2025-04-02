import java.io.*;
import java.util.*;
import java.text.*;
public class example1 extends JTLTemplate
{
  static public void main(String[] args) {
      JTLTemplate._run(args,new example1(new JTLContext()),"example1");
  }
  public example1(JTLContext ctxIn) { ctx(ctxIn); }
  @Override
  protected void process(Object object)  throws Exception {
  JTLEntity entity=null;
  if ( object instanceof JTLEntity) entity = (JTLEntity)object;
  // Code from jtl file follows
println("Example 1: Hello JTL World!!!");                                       _line(1);
for (JTLEntity e:entity.children) {
println("Hello "+e.param(0)+"");                                                _line(3);
}
} // end of process
} // end of class

