
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
    ArrayList<Type> getComplexType();
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



class Program implements Show {
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}

class Declaration implements Show {
    public String toString(JsonShowHelper jsh) {
        return "";
    }

}

class Function implements Show {
    public String toString(JsonShowHelper jsh) {
        return "";
    }

}

class Main implements Show {
    public String toString(JsonShowHelper jsh) {
        return "";
    }

}

abstract class Statement implements Show {

}

class VariableDeclaration extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }

}

class IfStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }

}

class WhileStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}

class SkipStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}

class ReturnStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}

class ExpressionStatement extends Statement {
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}


abstract class Expression implements Show, Typeable {
    public ArrayList<Type> getComplexType() { return new ArrayList(); }
    public boolean isLiteral() { return false; }

    public void checkValidity() throws ParseException {
        if (getType() == Type.INVALID)
            throw new ParseException("some types are invalid.");
    }
}

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
            +jsh.increase() + "\"type\": \"AssignmentExpression\",\n"
            +jsh.spaces + "\"operator\": \"=\",\n"
            +jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            +jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
            +jsh.decrease() + "}";
    }
}

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
            +jsh.increase() + "\"type\": \"LogicalExpression\",\n"
            +jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            +jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            +jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
            +jsh.decrease() + "}";
    }
}

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
                return Type.INVALID; // normaly unreachable
        }
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            +jsh.increase() + "\"type\": \"BinaryExpression\",\n"
            +jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            +jsh.spaces + "\"left\": " + e1.toString(jsh) + ",\n"
            +jsh.spaces + "\"right\": " + e2.toString(jsh) + "\n"
            +jsh.decrease() + "}";
    }
}

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
            +jsh.increase() + "\"type\": \"UnaryExpression\",\n"
            +jsh.spaces + "\"operator\": \"" + operator + "\",\n"
            +jsh.spaces + "\"argument\": " + e.toString(jsh) + "\n"
            +jsh.decrease() + "}";
    }
}

class CallExpression extends Expression {
    public Type getType() throws ParseException { return Type.VOID; }
    
    public String toString(JsonShowHelper jsh) {
        return "";
    }
}

class Identifier extends Expression {
    private Token id;

    Identifier(Token _id) {
        id = _id;
    }

    public Type getType() throws ParseException {
        /** check in identifier hashtable */
        return Type.VOID;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            +jsh.increase() + "\"type\": \"Identifier\",\n"
            +jsh.spaces + "\"name\": \"" + id + "\"\n"
            +jsh.decrease() + "}";
    }
}

abstract class Literal extends Expression {
    public boolean isLiteral() { return true; }
}

class Number extends Literal {
    private String value;

    public Number(String _value) {
        value = _value;
    }

    public Type getType() throws ParseException {
        return Type.INTEGER;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            +jsh.increase() + "\"type\": \"Literal\",\n"
            +jsh.spaces + "\"value\": " + value + "\n"
            +jsh.decrease() + "}";
    }
}

class Bool extends Literal {
    private boolean value;

    Bool(boolean _value) {
        value = _value;
    }

    public Type getType() throws ParseException {
        return Type.BOOLEAN;
    }

    public String toString(JsonShowHelper jsh) {
        return "{\n"
            +jsh.increase() + "\"type\": \"Literal\",\n"
            +jsh.spaces + "\"value\": " + Boolean.toString(value) + "\n"
            +jsh.decrease() + "}";
    }
}
