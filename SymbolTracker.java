
import java.util.*;

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

    public Symbol getSymbol(String key) {
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