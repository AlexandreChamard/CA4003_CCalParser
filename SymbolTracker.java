
import java.util.*;

class SymbolTracker {
    Hashtable<String, LinkedList<Symbol>> symTracker = new Hashtable<String, LinkedList<Symbol>>();
    Stack<String> undoStack = new Stack<String>();
    public Vector<Symbol> symbols = new Vector<Symbol>();

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
        symbols.add(s);
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

    public boolean verifyUsages() {
        boolean v = true;

        for (Symbol s : symbols) {
            if (s.isWrite == false) {
                System.out.println("WARNING: var "+s.name+":"+Typeable.typeToString(s.type)+" ("+s.tok.beginLine+":"+s.tok.beginColumn+") is never initialised.");
                v = false;
            }
            if (s.isRead == false) {
                if (s.complexType == null)
                    System.out.println("WARNING: var "+s.name+":"+Typeable.typeToString(s.type)+" ("+s.tok.beginLine+":"+s.tok.beginColumn+") is never used.");
                else
                    System.out.println("WARNING: function "+s.name+" ("+s.tok.beginLine+":"+s.tok.beginColumn+") is never called.");
                v = false;
            }
        }
        return v;
    }

}