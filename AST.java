
import java.util.ArrayList;

interface Showable {
    String toString(JsonShowHelper jsh);
}


enum Type {
    INVALID,
    VOID,
    INTEGER,
    BOOLEAN
}

interface Typeable {
    Type getType() throws ParseException;
    ArrayList<Type> getComplexType() throws ParseException;
    boolean isLiteral();
    boolean isLogical();

    static String typeToString(Type t) {
        switch (t) {
            case BOOLEAN:
                return "boolean";
            case INTEGER:
                return "integer";
            case VOID:
                return "void";
            default:
                return "invalid";
        }
    }

    static Type stringToType(String s) {
        switch (s) {
            case "boolean":
                return Type.BOOLEAN;
            case "integer":
                return Type.INTEGER;
            case "void":
                return Type.VOID;
            default:
                return Type.INVALID;
        }
    }
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


//////////////////
///   Program  ///
//////////////////
class Program implements Showable {
    private StatementBlock statements;

    public Program(StatementBlock _statements) {
        statements = _statements;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Program\",\n"
            + jsh.spaces + "\"body\": " + statements.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}



//////////////////
///  Statement ///
//////////////////
abstract class Statement implements Showable {
}

////////////////////
/// FunctionDecl ///
////////////////////
class FunctionDeclaration extends Statement {
    Identifier id;
    ArrayList<VariableDeclaration> params;
    StatementBlock statements;

    public FunctionDeclaration(Token _id, Symbol s, ArrayList<VariableDeclaration> _params, StatementBlock _statements) throws ParseException {
        id = new Identifier(_id);
        params = _params;
        statements = _statements;

        id.updateSymbol(s);
        SymbolTracker.getInstance().addSymbol(id.s);
    }

    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"FunctionDeclaration\",\n"
            + jsh.spaces + "\"id\": " + id.toString(jsh) + ",\n"
            + jsh.spaces + "\"return_type\": \"" + id.s.getType() + "\",\n";

        str += jsh.spaces + "\"params\": [\n";
        jsh.increase();
        for (VariableDeclaration p : params) {
            str += jsh.spaces + p.toString(jsh);
            if (p != params.get(params.size() - 1))
                str += ",";
            str += "\n";
        }
        str += jsh.decrease() + "],\n";

        str += jsh.spaces + "\"body\": " + statements.toString(jsh) + "\n";
        return str + jsh.decrease() + "}";
    }
}

//////////////////
///  MainFunc  ///
//////////////////
class MainFunction extends Statement {
    StatementBlock statements;

    public MainFunction(StatementBlock _statements) {
        statements = _statements;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"FunctionDeclaration\",\n"
            + jsh.spaces + "\"name\": \"main\",\n" // should be an identifier
            + jsh.spaces + "\"body\": " + statements.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}


//////////////////
/// StateBlock ///
//////////////////
class StatementBlock extends Statement {
    ArrayList<Statement> statements;

    public StatementBlock() {
        statements = new ArrayList<Statement>();
    }

    public StatementBlock(ArrayList<Statement> _statements) {
        statements = _statements;
    }

    public void addFront(StatementBlock sb) {
        statements.addAll(0, sb.statements);
    }

    public void addFront(Statement statement) {
        statements.add(0, statement);
    }

    public void addBack(StatementBlock sb) {
        statements.addAll(sb.statements);
    }
    public void addBack(Statement statement) {
        statements.add(statement);
    }

    public String toString(JsonShowHelper jsh) {
        String str = "[\n";

        jsh.increase();
        for (Statement s : statements) {
            str += jsh.spaces + s.toString(jsh);
            if (s != statements.get(statements.size() - 1))
                str += ",";
            str += "\n";
        }
        return str + jsh.decrease() + "]";
    }
}

//////////////////
///   VarDecl  ///
//////////////////
class VariableDeclaration extends Statement {
    Identifier id;
    Expression e;

    public VariableDeclaration(Token _id, Token type, Expression _e) {
        Symbol s = initSymbol(_id.toString(), type.toString(), "const");
        id = new Identifier(_id, s);
        e = _e;
    }

    public VariableDeclaration(Token _id, Token type) {
        Symbol s = initSymbol(_id.toString(), type.toString(), "var");
        id = new Identifier(_id, s);
    }

    Symbol initSymbol(String name, String type, String kind) {
        Symbol s = new Symbol(name, type, kind);
        SymbolTracker.getInstance().addSymbol(s);

        return s;
    }

    public Type getType() throws ParseException {
        return id.getType();
    }

    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"VariableDeclaration\",\n"
            + jsh.spaces + "\"id\": " + id.toString(jsh) + ",\n";
        if (e != null) {
            str += jsh.spaces + "\"init\": " + e.toString(jsh) + ",\n";
        }
        str += jsh.spaces + "\"var_type\": \"" + id.s.getType() + "\",\n"
            + jsh.spaces + "\"kind\": \"" + id.s.kind + "\"\n"
            + jsh.decrease() + "}";
        return str;
    }
}

//////////////////
///   IfState  ///
//////////////////
class IfStatement extends Statement {
    Expression test;
    StatementBlock consequent;
    StatementBlock alternate;

    public IfStatement(Expression _test, StatementBlock _consequent, StatementBlock _alternate) {
        test = _test;
        consequent = _consequent;
        alternate = _alternate;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"IfStatement\",\n"
            + jsh.spaces + "\"test\": " + test.toString(jsh) + ",\n"
            + jsh.spaces + "\"consequent\": " + consequent.toString(jsh) + ",\n"
            + jsh.spaces + "\"alternate\": " + alternate.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
/// WhileState ///
//////////////////
class WhileStatement extends Statement {
    Expression test;
    StatementBlock body;

    public WhileStatement(Expression _test, StatementBlock _body) {
        test = _test;
        body = _body;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"WhileStatement\",\n"
            + jsh.spaces + "\"test\": " + test.toString(jsh) + ",\n"
            + jsh.spaces + "\"body\": " + body.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
///  SkipState ///
//////////////////
class SkipStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"SkipStatement\"\n"
            + jsh.decrease() + "}";
    }
}

///////////////////
/// ReturnState ///
///////////////////
class ReturnStatement extends Statement {
    Expression argument;

    public ReturnStatement() {
    }

    public ReturnStatement(Expression _argument) {
        argument = _argument;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"ReturnStatement\",\n"
            + jsh.spaces + "\"argument\": " + argument.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
///  ExprState ///
//////////////////
class ExpressionStatement extends Statement {
    Expression e;

    public ExpressionStatement(Expression _e) {
        e = _e;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"ExpressionStatement\",\n"
            + jsh.spaces + "\"expression\": " + e.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}



//////////////////
///    Expr    ///
//////////////////
abstract class Expression implements Showable, Typeable {
    public ArrayList<Type> getComplexType() throws ParseException {
        return new ArrayList<Type>();
    }

    public boolean isLiteral() { return false; }
    public boolean isLogical() { return false; }

    public void checkValidity() throws ParseException {
        if (getType() == Type.INVALID)
            throw new ParseException("some types are invalid.");
    }
}

//////////////////////
/// AssignmentExpr ///
//////////////////////
class AssignmentExpression extends Expression {
    private Identifier id;
    private Expression e;

    AssignmentExpression(Identifier _id, Expression _e) throws ParseException {
        id = _id;
        e = _e;
        checkValidity();
    }

    public Type getType() throws ParseException {
        Type t1 = id.getType(), t2 = e.getType();

        if (t1 != Type.INVALID && t1 != Type.VOID && t1 == t2)
            return t1;
        return Type.INVALID;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"AssignmentExpression\",\n"
            + jsh.spaces + "\"operator\": \"=\",\n"
            + jsh.spaces + "\"left\": " + id.toString(jsh) + ",\n"
            + jsh.spaces + "\"right\": " + e.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

///////////////////
/// LogicalExpr ///
///////////////////
class LogicalExpression extends Expression {
    private String operator;
    private Expression e1, e2;

    LogicalExpression(String _operator, Expression _e1, Expression _e2) throws ParseException {
        operator = _operator;
        e1 = _e1;
        e2 = _e2;
        checkValidity();
    }

    public boolean isLogical() { return true; }

    public Type getType() throws ParseException {
        Type t1 = e1.getType(), t2 = e2.getType();

        if (t1 == Type.BOOLEAN && t2 == Type.BOOLEAN) {
            if (e1.isLiteral() == true || e2.isLiteral() == true)
                throw new ParseException("Invalid Logical Expression on Literal."); // because ccal
            return Type.BOOLEAN;
        }
        return Type.INVALID;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"LogicalExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            + jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            + jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
/// BinaryExpr ///
//////////////////
class BinaryExpression extends Expression {
    private String operator;
    private Expression e1, e2;

    BinaryExpression(String _operator, Expression _e1, Expression _e2) throws ParseException {
        operator = _operator;
        e1 = _e1;
        e2 = _e2;
        checkValidity();
    }

    public Type getType() throws ParseException {
        if (e1.isLogical() == true || e2.isLogical() == true)
            return Type.INVALID;

        Type t1 = e1.getType(), t2 = e2.getType();

        switch (operator) {
            case "+": case "-":
                if (t1 == Type.INTEGER && t2 == Type.INTEGER)
                    return Type.INTEGER;
                return Type.INVALID;
            case "<": case "<=": case ">": case ">=":
                if (t1 == Type.INTEGER && t2 == Type.INTEGER)
                    return Type.BOOLEAN;
                return Type.INVALID;
            case "==": case "!=":
                if (t1 != Type.INVALID && t1 != Type.VOID && t1 == t2)
                    return Type.BOOLEAN;
                return Type.INVALID;
            default:
                return Type.INVALID; // normally unreachable
        }
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"BinaryExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            + jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            + jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
///  UnaryExpr ///
//////////////////
class UnaryExpression extends Expression {
    private String operator;
    private Expression e;

    UnaryExpression(String _operator, Expression _e) throws ParseException {
        operator = _operator;
        e = _e;
        checkValidity();
    }

    public Type getType() throws ParseException {
        switch (operator) {
            case "-":
                if (e.getType() == Type.INTEGER)
                    return  Type.INTEGER;
                return Type.INVALID;
            case "~":
                if (e.getType() == Type.BOOLEAN) {
                    if (e.isLiteral() == true)
                        throw new ParseException("Invalid Unary '~` on Literal."); // because ccal
                    return  Type.BOOLEAN;
                }
            default:
                return Type.INVALID;
        }
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"UnaryExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            + jsh.spaces + "\"argument\": " + e.toString(jsh) + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
///  CallExpr  ///
//////////////////
class CallExpression extends Expression {
    Identifier calee;
    ArrayList<Identifier> arguments;

    public CallExpression(Identifier _calee, ArrayList<Identifier> _arguments) throws ParseException {
        calee = _calee;
        arguments = _arguments;
        ArrayList<Type> complexType = new ArrayList<Type>();

        for (Typeable t : arguments) {
            complexType.add(t.getType());
        }
        if (calee.s.isComplex() == false) {
            throw new ParseException(calee.id+" is not a function.");
        }
        if (complexType.size() != calee.s.complexType.size())
            throw new ParseException("in "+calee.id+" call arguments number does not match with the definition.");
        if (complexType.equals(calee.s.complexType) == false)
            throw new ParseException("in "+calee.id+" call , arguments type does not match with the definition.");
    }

    public Type getType() throws ParseException { return calee.s.type; }

    public ArrayList<Type> getComplexType() throws ParseException { return calee.s.complexType; }
    
    public boolean isLiteral() {
        return true; // does not make any sens but use to onvalidated LogicalExpression
    }

    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"CallExpression\",\n"
            + jsh.spaces + "\"calee\": " + calee.toString(jsh) + ",\n"
            + jsh.spaces + "\"arguments\": [\n";

        jsh.increase();
        for (Identifier arg : arguments) {
            str += jsh.spaces + arg.toString(jsh);
            if (arg != arguments.get(arguments.size() - 1))
                str += ",";
            str += "\n";
        }
        return str
            + jsh.decrease() + "]\n"
            + jsh.decrease() + "}";
    }
}



//////////////////
/// Identifier ///
//////////////////
class Identifier extends Expression {
    public Token id;
    public Symbol s;

    Identifier(Token _id) {
        id = _id;
        updateSymbol();
    }

    Identifier(Token _id, Symbol _s) {
        id = _id;
        s = _s;
        updateSymbol();
    }

    public void updateSymbol() {
        s = SymbolTracker.getInstance().getSymbol(id.toString());
    }

    public void updateSymbol(Symbol _s) {
        s = _s;
    }

    public boolean isFunction() {
        return s.kind.equals("function");
    }

    public boolean isConst() {
        return s.kind.equals("const") || isFunction();
    }

    public Type getType() throws ParseException {
        if (s == null)
            throw new ParseException("identifier "+id+" is not bind to anything.");
        return s.type;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Identifier\",\n"
            + jsh.spaces + "\"name\": \"" + id + "\"\n"
            + jsh.decrease() + "}";
    }
}



//////////////////
///   Literal  ///
//////////////////
abstract class Literal extends Expression {
    public boolean isLiteral() { return true; }
}

//////////////////
///   Number   ///
//////////////////
class Number extends Literal {
    public String value;

    public Number(String _value) {
        value = _value;
    }

    public Type getType() throws ParseException {
        return Type.INTEGER;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Literal\",\n"
            + jsh.spaces + "\"value\": " + value + "\n"
            + jsh.decrease() + "}";
    }
}

//////////////////
///    Bool    ///
//////////////////
class Bool extends Literal {
    public boolean value;

    Bool(boolean _value) {
        value = _value;
    }

    public Type getType() throws ParseException {
        return Type.BOOLEAN;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Literal\",\n"
            + jsh.spaces + "\"value\": " + value + "\n"
            + jsh.decrease() + "}";
    }
}
