
import java.util.*;

/**
 * One entity of model tree. The entity has further entities as children and
 * parameters (Collection of strings).
 *
 */
public class JTLEntity extends Object {

    ///@ children name
    public String name;

    ///@ parameters of the entity
    public Vector<String> params;

    ///@ children of the entity
    public Vector<JTLEntity> children;

    ///@ parent entity
    public JTLEntity parent;

    public JTLEntity() {
        parent = null;
        name = "";
        params = new Vector<String>();
        children = new Vector<JTLEntity>();
    }

    ///@ returns child by given name
    public JTLEntity child(String sname) {
        if (sname.equals("..")) {
            return parent;
        }

        for (int i = 0; i < children.size(); i++) {
            JTLEntity e = (JTLEntity) children.elementAt(i);
            if (e.name.equals(sname)) {
                return e;
            }
        }
        //JTLOut.out.println("JTLEntity.child(String) method didn't find child named:"+sname);
        //JTLOut.out.println("Entity "+fullpath());
        //dump(5);
        return null;
    }

    ///@ returns true if child exists
    public boolean hasChild(String sname) {
        return child(sname) != null;
    }

    ///@ returns full path of entity with "/" as delimiter
    public String fullpath() {
        if (parent == null) {
            return name;
        } else {
            return parent.fullpath() + '/' + name;
        }
    }

    ///@ returns child by index
    public JTLEntity child(int nr) throws Exception {
        if (nr < children.size()) {
            return (JTLEntity) children.elementAt(nr);
        } else {
            JTLOut.out.println("JTLEntity.child(int) method didn't find child with index:" + nr);
            JTLOut.out.println("Entity " + fullpath());
            dump(5);
            throw new Exception("Wrong index of child in method child");
        }
    }

    ///@ returns param by index
    public String param(int nr) throws Exception {
        if (nr < params.size()) {
            return (String) params.elementAt(nr);
        } else {
            JTLOut.out.println("JTLEntity.param(int) method didn't find param with index:" + nr);
            JTLOut.out.println("Entity " + fullpath());
            dump(5);
            throw new Exception("Wrong index of parameter in method param");
        }
    }

    ///@ returns true if this child is last one
    public boolean isLast() {
        return parent.children.elementAt(parent.children.size() - 1) == this;
    }

    ///@ returns true if this child is first one
    public boolean isFirst() {
        return parent.children.elementAt(0) == this;
    }

    /// @ returns c_then character if entity is last child, c_else otherwise
    public Character ifLast(Character c_then, Character c_else) {
        if (isLast()) {
            return c_then;
        } else {
            return c_else;
        }
    }

    /// @ returns c_then String if entity is last child, c_else otherwise
    public String ifLast(String c_then, String c_else) {
        if (isLast()) {
            return c_then;
        } else {
            return c_else;
        }
    }

    /// @ returns c_then character if entity is first child, c_else otherwise
    public Character ifFirst(Character c_then, Character c_else) {
        if (isFirst()) {
            return c_then;
        } else {
            return c_else;
        }
    }

    /// @ returns c_then String if entity is first child, c_else otherwise
    public String ifFirst(String c_then, String c_else) {
        if (isFirst()) {
            return c_then;
        } else {
            return c_else;
        }
    }

    @Override
    public String toString() {
        return name;
    }

    ///@ adds child to entity
    public JTLEntity addChild(JTLEntity e) {
        children.addElement(e);
        e.parent = this;
        return this;
    }

    ///@ adds parameter to entity
    public JTLEntity addParam(String p) {
        params.addElement(p);
        return this;
    }

    ///@ sets parameter by index
    public JTLEntity setParam(int i, String p) throws Exception {

        if (i < params.size()) {
            params.set(i, p);
        } else {
            JTLOut.out.println("JTLEntity.setParam(int,String) method didn't find param with index:" + i);
            JTLOut.out.println("Entity " + fullpath());
            dump(5);
            throw new Exception("Wrong index of parameter in method setParam");
        }
        return this;
    }

    /// dump recursive for debugging with provided depth (=indentation)
    public void dump(int depth) {

        String sparams = "";
        for (int i = 0; i < params.size(); i++) {
            if (i == 0) {
                sparams = '"' + (String) params.elementAt(0) + '"';
            } else {
                sparams = sparams + ",\"" + (String) params.elementAt(i) + "\"";
            }
        }

        printshifted(name + "(" + sparams + ")", depth);

        if (children.size() > 0) {
            printshifted("{", depth);
            for (int i = 0; i < children.size(); i++) {
                JTLEntity e = (JTLEntity) children.elementAt(i);
                e.dump(depth + 1);
            }
            printshifted("}", depth);
        }

    }

    /// dumps definition in def format
    public void dump()
    {
        dump(0);
    }

    /// dumps in xml format with provided depth (=indentation)
    public void dumpXML(int depth) {
        String sparams = "";
        for (int i = 0; i < params.size(); i++) {
            if (i != 0) {
                sparams = sparams + ' ';
            }
            sparams = sparams + "PARAM" + i + "=\"" + (String) params.elementAt(i) + '"';
        }

        if (children.size() > 0) {
            if (sparams.length()==0)
                printshifted("<" + name + ">", depth);
            else
                printshifted("<" + name + " " + sparams + ">", depth);
            
            for (int i = 0; i < children.size(); i++) {
                JTLEntity e = (JTLEntity) children.elementAt(i);
                e.dumpXML(depth + 1);
            }
            printshifted("</" + name + ">", depth);
        } else {
            printshifted("<" + name + " " + sparams + "/>", depth);
        }

    }

    /// dumps definitionin XML format
    public void dumpXML()
    {
        dumpXML(0);
    }

    /// prints something shifted
    private void printshifted(String what, int depth) {
        for (int i = 0; i < depth * 2; i++) {
            JTLOut.out.print(" ");
        }
        JTLOut.out.println(what);
    }

}
