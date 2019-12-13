
import java.util.ArrayList;


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
    public Token tok;
    public String name;
    public Type type;
    public ArrayList<Type> complexType;
    public String kind;
    public boolean isRead = false;
    public boolean isWrite = false;

    public Symbol(Token _tok, Type _type, String _kind) {
        tok = _tok;
        name = tok.image;
        type = _type;
        kind = _kind;
    }

    public Symbol(Token _tok, String _type, String _kind) {
        tok = _tok;
        name = tok.image;
        type = Typeable.stringToType(_type);
        kind = _kind;
    }

    public Symbol(Token _tok, String _type, ArrayList<VariableDeclaration> vars, String _kind) throws ParseException {
        tok = _tok;
        name = tok.image;
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
}

interface Visitable {
    public Object accept(ASTVisitor v, Object data);
}

abstract class ASTNode implements Visitable {
}

//////////////////
///   Program  ///
//////////////////
class Program extends ASTNode {
    public StatementBlock statements;

    public Program(StatementBlock _statements) {
        statements = _statements;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}



//////////////////
///  Statement ///
//////////////////
abstract class Statement extends ASTNode {
}

////////////////////
/// FunctionDecl ///
////////////////////
class FunctionDeclaration extends Statement {
    public Identifier id;
    public ArrayList<VariableDeclaration> params;
    public StatementBlock statements;

    public FunctionDeclaration(Token _id, Symbol s, ArrayList<VariableDeclaration> _params, StatementBlock _statements) throws ParseException {
        s.isWrite = true;
        id = new Identifier(_id);
        params = _params;
        statements = _statements;

        id.updateSymbol(s);
        SymbolTracker.getInstance().addSymbol(id.s);
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///  MainFunc  ///
//////////////////
class MainFunction extends Statement {
    public StatementBlock statements;

    public MainFunction(StatementBlock _statements) {
        statements = _statements;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}


//////////////////
/// StateBlock ///
//////////////////
class StatementBlock extends Statement {
    public ArrayList<Statement> statements;

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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///   VarDecl  ///
//////////////////
class VariableDeclaration extends Statement {
    public Identifier id;
    public Expression e;

    public VariableDeclaration(Token _id, Token type, Expression _e) {
        Symbol s = initSymbol(_id, type.image, "const");
        id = new Identifier(_id, s);
        s.isWrite = true;
        e = _e;
    }

    public VariableDeclaration(Token _id, Token type) {
        Symbol s = initSymbol(_id, type.image, "var");
        id = new Identifier(_id, s);
    }

    public VariableDeclaration(Token _id, Token type, boolean isRead, boolean isWrite) {
        Symbol s = initSymbol(_id, type.image, "var");
        s.isRead = isRead;
        s.isWrite = isWrite;
        id = new Identifier(_id, s);
    }

    Symbol initSymbol(Token tok, String type, String kind) {
        Symbol s = new Symbol(tok, type, kind);
        SymbolTracker.getInstance().addSymbol(s);

        return s;
    }

    public Type getType() throws ParseException {
        return id.getType();
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///   IfState  ///
//////////////////
class IfStatement extends Statement {
    public Expression test;
    public StatementBlock consequent;
    public StatementBlock alternate;

    public IfStatement(Expression _test, StatementBlock _consequent, StatementBlock _alternate) {
        test = _test;
        consequent = _consequent;
        alternate = _alternate;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
/// WhileState ///
//////////////////
class WhileStatement extends Statement {
    public Expression test;
    public StatementBlock body;

    public WhileStatement(Expression _test, StatementBlock _body) {
        test = _test;
        body = _body;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///  SkipState ///
//////////////////
class SkipStatement extends Statement {
    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

///////////////////
/// ReturnState ///
///////////////////
class ReturnStatement extends Statement {
    public Expression argument;

    public ReturnStatement() {
    }

    public ReturnStatement(Expression _argument) {
        argument = _argument;
        argument.setRead();
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///  ExprState ///
//////////////////
class ExpressionStatement extends Statement {
    public Expression e;

    public ExpressionStatement(Expression _e) {
        e = _e;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}



//////////////////
///    Expr    ///
//////////////////
abstract class Expression extends ASTNode implements Typeable {
    public ArrayList<Type> getComplexType() throws ParseException {
        return new ArrayList<Type>();
    }

    public boolean isLiteral() { return false; }
    public boolean isLogical() { return false; }
    public void setRead() {}

    public void checkValidity() throws ParseException {
        if (getType() == Type.INVALID)
            throw new ParseException("some types are invalid.");
    }
}

//////////////////////
/// AssignmentExpr ///
//////////////////////
class AssignmentExpression extends Expression {
    public Identifier id;
    public Expression e;

    AssignmentExpression(Identifier _id, Expression _e) throws ParseException {
        id = _id;
        id.s.isWrite = true;
        e = _e;
        e.setRead();
        checkValidity();
    }

    public Type getType() throws ParseException {
        Type t1 = id.getType(), t2 = e.getType();

        if (t1 != Type.INVALID && t1 != Type.VOID && t1 == t2)
            return t1;
        return Type.INVALID;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

///////////////////
/// LogicalExpr ///
///////////////////
class LogicalExpression extends Expression {
    public String operator;
    public Expression e1, e2;

    LogicalExpression(String _operator, Expression _e1, Expression _e2) throws ParseException {
        operator = _operator;
        e1 = _e1;
        e2 = _e2;
        e1.setRead();
        e1.setRead();
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
/// BinaryExpr ///
//////////////////
class BinaryExpression extends Expression {
    public String operator;
    public Expression e1, e2;

    BinaryExpression(String _operator, Expression _e1, Expression _e2) throws ParseException {
        operator = _operator;
        e1 = _e1;
        e2 = _e2;
        e1.setRead();
        e2.setRead();
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///  UnaryExpr ///
//////////////////
class UnaryExpression extends Expression {
    public String operator;
    public Expression e;

    UnaryExpression(String _operator, Expression _e) throws ParseException {
        operator = _operator;
        e = _e;
        e.setRead();
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}

//////////////////
///  CallExpr  ///
//////////////////
class CallExpression extends Expression {
    public Identifier calee;
    public ArrayList<Identifier> arguments;

    public CallExpression(Identifier _calee, ArrayList<Identifier> _arguments) throws ParseException {
        calee = _calee;
        calee.setRead();

        arguments = _arguments;
        ArrayList<Type> complexType = new ArrayList<Type>();
        for (Identifier argument : arguments) {
            complexType.add(argument.getType());
            argument.setRead();
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
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
        s = SymbolTracker.getInstance().getSymbol(id.image);
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

    public void setRead() {
        s.isRead = true;
    }

    public Type getType() throws ParseException {
        if (s == null)
            throw new ParseException("identifier "+id+" is not bind to anything.");
        return s.type;
    }

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
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

    public Object accept(ASTVisitor v, Object data) {
        return v.accept(this, data);
    }
}
