
import java.util.*;

enum Type {
    INVALID,
    VOID,
    INTEGER,
    BOOLEAN
}

class Symbol {
    String name;
    Type type;
    ArrayList<Type> complexType;
    String kind;

    public Symbol(String _name, Type _type, String _kind) {
        name = _name;
        type = _type;
        kind = _kind;
    }

    public Symbol(String _name, String _type, String _kind) {
        name = _name;
        type = Typeable.stringToType(_type);
        kind = _kind;
    }

    public Symbol(String _name, String _type, ArrayList<VariableDeclaration> vars, String _kind) throws ParseException {
        name = _name.toLowerCase();
        type = Typeable.stringToType(_type);
        complexType = new ArrayList<Type>();
        for (VariableDeclaration v : vars) {
            complexType.add(v.getType());
        }
        kind = _kind;
    }

    public boolean isComplex() {
        return complexType != null;
    }

    public String getType() {
        return Typeable.typeToString(type);
    }

    public String toString() { // debug
        return "<"+kind+" "+name+"::"+type+">";
    }
}

class SymbolTracker {
    Hashtable<String, LinkedList<Symbol>> symTracker = new Hashtable<String, LinkedList<Symbol>>();
    Stack<String> undoStack = new Stack<String>();

    private static SymbolTracker st;

    public static SymbolTracker getInstance() {
        if (st == null) {
            st = new SymbolTracker();
        }
        return st;
    }

    public void addSymbol(Symbol s) throws RuntimeException {
        if (inCurrentBlock(s.name) == true)
            throw new RuntimeException("var " + s.name + "already instanciated.");

        LinkedList<Symbol> l = symTracker.get(s.name);
        if (l == null) {
            l = new LinkedList<Symbol>();
            symTracker.put(s.name, l);
        }
        undoStack.push(s.name);
        l.push(s);
    }

    public Symbol getSymbol(String _key) {
        String key = _key.toLowerCase();
        LinkedList<Symbol> l = symTracker.get(key);

        if (l == null)
            return null;
        for (Symbol s : l) {
            if (s.name.equals(key)) {
                if (l.peek() != s) { // put the found symbol at the top
                    l.remove(s);
                    l.push(s);
                }
                return s;
            }
        }
        System.out.println("error: cannot find symbol "+_key);
        CcalParser.errorState = true;
        return null;
    }

    protected void remove(String key) throws RuntimeException {
        LinkedList<Symbol> l = symTracker.get(key);

        for (Symbol s : l) {
            if (s.name.equals(key)) {
                l.remove(s);
                return;
            }
        }
        throw new RuntimeException("var doesn't exist.");
    }

    public void newBlock() {
        undoStack.push(null);
    }

    public void deleteBlock() throws RuntimeException {
        if (undoStack.empty() == true)
            throw new RuntimeException("destroying an unexisting block.");

        while (undoStack.peek() != null) {
            remove(undoStack.peek());
            undoStack.pop();
        }
        undoStack.pop();
    }

    public boolean inCurrentBlock(String key) {
        for (String s : undoStack) {
            if (s == null)
                return false;
            if (s.equals(key))
                return true;
        }
        return false;
    }

    protected static String symbolToString(Symbol s) { // debug
        if (s == null)
            return "null";
        return s.toString();
    }

    public static void main(String[] args) { // SymbolTracker example
        SymbolTracker st = new SymbolTracker();

        st.newBlock();
        st.addSymbol(new Symbol("a", "int", "var"));
        st.addSymbol(new Symbol("b", "bool", "var"));

        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.newBlock();

        st.addSymbol(new Symbol("a", "String", "var"));

        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.deleteBlock();
        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.deleteBlock();
    }
}