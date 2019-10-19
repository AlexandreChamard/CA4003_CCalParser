
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

    public void addSymbol(String key, Symbol s) throws RuntimeException {
        if (inCurrentBlock(key) == true)
            throw new RuntimeException("var " + key + "already instanciated.");

        LinkedList<Symbol> l = symTracker.get(key);
        if (l == null) {
            l = new LinkedList<Symbol>();
            symTracker.put(key, l);
        }
        undoStack.push(key);
        l.push(s);
    }

    public Symbol getSymbol(String key) throws RuntimeException {
        LinkedList<Symbol> l = symTracker.get(key);

        if (l == null)
            throw new RuntimeException("Symbol "+key+" not found.");
        for (Symbol s : l) {
            if (s.name.equals(key)) {
                if (l.peek() != s) { // put the found symbol at the top
                    l.remove(s);
                    l.push(s);
                }
                return s;
            }
        }
        throw new RuntimeException("Symbol "+key+" not found.");
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
        st.addSymbol("a", new Symbol("a", "int"));
        st.addSymbol("b", new Symbol("b", "bool"));

        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.newBlock();

        st.addSymbol("a", new Symbol("a", "String"));

        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.deleteBlock();
        System.out.println("a: "+symbolToString(st.getSymbol("a")));
        System.out.println("b: "+symbolToString(st.getSymbol("b")));

        st.deleteBlock();
    }
}