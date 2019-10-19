
import java.util.ArrayList;

interface Show {
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
}

class Symbol {
    String name;
    String type;

    public Symbol(String _name, String _type) {
        name = _name;
        type = _type;
    }

    public String toString() { // debug
        return "<"+name+"::"+type+">";
    }
}


//////////////////
///   Program  ///
//////////////////
class Program implements Show {
    private ArrayList<Declaration> declarations;
    private ArrayList<Function> functions;
    private MainFunction mainFunction;

    public Program(ArrayList<Declaration> _declarations, ArrayList<Function> _functions, MainFunction _mainFunction) {
        declarations = _declarations;
        functions = _functions;
        mainFunction = _mainFunction;
    }

    public String toString(JsonShowHelper jsh) {
        String str = "\"Program\": {\n"
            + jsh.increase() + "\"type\": \"Program\",\n"
            + jsh.spaces + "\"body\": [\n";

        jsh.increase();
        for (Declaration d : declarations) {
            str += jsh.spaces + d.toString(jsh) + ",\n";
        }
        for (Function f : functions) {
            str += jsh.spaces + f.toString(jsh) + ",\n";
        }
        str += jsh.spaces + mainFunction.toString(jsh) + "\n"
            +  jsh.decrease() + "]\n";
        return str + jsh.decrease() + "}";
    }
}



////////////////////
/// FunctionDecl ///
////////////////////
class FunctionDeclaration implements Show {
    Identifier id;
    ArrayList<Identifier> params;
    BlockStatement statements;

    public FunctionDeclaration(Identifier _id, ArrayList<Identifier> _params, BlockStatement _statements) {
        id = _id;
        params = _params;
        statements = _statements;
    }

    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"FunctionDeclaration\",\n"
            + jsh.spaces + "\"id\": \"" + id + "\",\n";

        str += jsh.spaces + "\"params\": [\n";
        jsh.increase();
        for (Identifier p : params) {
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
class MainFunction implements Show {
    BlockStatement statements;

    public MainFunction(BlockStatement _statements) {
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
///  Statement ///
//////////////////
abstract class Statement implements Show {
}

class BlockStatement implements Show {
    ArrayList<Statement> statements;

    public BlockStatement(ArrayList<Statement> _statements) {
        statements = _statements;
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
        str += jsh.decrease() + "]\n";
        return str;
    }
}

//////////////////
///   VarDecl  ///
//////////////////
class VariableDeclaration extends Statement {
    Identifier id;
    Expression e;
    Symbol s;

    public VariableDeclaration(Identifier _id, Expression _e) {
        id = _id;
        e = _e;
        initSymbol(id.id.toString(), "const");
    }

    public VariableDeclaration(Identifier _id) {
        id = _id;
        initSymbol(id.id.toString(), "var");
    }

    void initSymbol(String name, String type) {
        s = new Symbol(name, type);
        SymbolTracker.getInstance().addSymbol(s);
    }

    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"VariableDeclaration\",\n"
            + jsh.spaces + "\"id\": " + id.toString(jsh) + ",\n";
        if (e != null) {
            str += jsh.spaces + "\"init\": " + e.toString(jsh) + ",\n";
        }
        str += jsh.spaces + "\"kind\": " + s.type + "\n"
            + jsh.decrease() + "}";
        return str;
    }
}

//////////////////
///   IfState  ///
//////////////////
class IfStatement extends Statement {
    Expression test;
    BlockStatement consequent;
    BlockStatement alternate;

    public IfStatement(Expression _test, BlockStatement _consequent, BlockStatement _alternate) {
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
    BlockStatement body;

    public WhileStatement(Expression _test, BlockStatement _body) {
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
            + jsh.increase() + "\"type\": \"SkipeStatement\"\n"
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
abstract class Expression implements Show, Typeable {
    public ArrayList<Type> getComplexType() throws ParseException {
        return new ArrayList();
    }

    public boolean isLiteral() { return false; }

    public void checkValidity() throws ParseException {
        if (getType() == Type.INVALID)
            throw new ParseException("some types are invalid.");
    }
}

//////////////////////
/// AssignmentExpr ///
//////////////////////
class AssignmentExpression extends Expression {
    private Expression e1, e2;

    AssignmentExpression(Expression _e1, Expression _e2) throws ParseException {
        e1 = _e1;
        e2 = _e2;
        checkValidity();
    }

    public Type getType() throws ParseException {
        Type t1 = e1.getType(), t2 = e2.getType();

        if (t1 != Type.INVALID && t1 != Type.VOID && t1 == t2)
            return t1;
        return Type.INVALID;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            + jsh.increase() + "\"type\": \"AssignmentExpression\",\n"
            + jsh.spaces + "\"operator\": \"=\",\n"
            + jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            + jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
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

    public Type getType() throws ParseException { return Type.VOID; }

    public ArrayList<Type> getComplexType() throws ParseException { return new ArrayList<Type>(); }
    
    public String toString(JsonShowHelper jsh) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"CallExpression\",\n"
            + jsh.spaces + "\"calee\": \"" + calee.toString(jsh) + "\",\n";
        
        str += jsh.spaces + "\"arguments\": [\n";
        jsh.increase();
        for (Identifier arg : arguments) {
            str += jsh.spaces + arg.toString(jsh);
            if (arg != arguments.get(arguments.size() - 1))
                str += ",";
            str += "\n";
        }
        str += jsh.decrease() + "],\n";
        return str + jsh.decrease() + "}";
    }
}



//////////////////
/// Identifier ///
//////////////////
class Identifier extends Expression {
    public Token id;

    Identifier(Token _id) {
        id = _id;
    }

    public Type getType() throws ParseException {
        /** check in identifier hashtable */
        return Type.VOID;
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
