
class JsonShowHelper {
    public String spaces = new String();
    public int n;

    public String increase() {
        n = n + 1;
        spaces = spaces.concat("  ");
        return spaces;
    }

    public String decrease() {
        if (n != 0) {
            n = n - 1;
            spaces = spaces.substring(0, spaces.length() - 2);
        }
        return spaces;
    }
}

public class JsonVisitor implements ASTVisitor {
    JsonShowHelper jsh = new JsonShowHelper();

    public Object accept(Program node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Program\",\n"
            + jsh.spaces + "\"body\": " + node.statements.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(FunctionDeclaration node, Object data) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"FunctionDeclaration\",\n"
            + jsh.spaces + "\"id\": " + node.id.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"return_type\": \"" + node.id.s.getType() + "\",\n";

        str += jsh.spaces + "\"params\": [\n";
        jsh.increase();
        for (VariableDeclaration p : node.params) {
            str += jsh.spaces + p.accept(this, jsh);
            if (p != node.params.get(node.params.size() - 1))
                str += ",";
            str += "\n";
        }
        str += jsh.decrease() + "],\n";

        str += jsh.spaces + "\"body\": " + node.statements.accept(this, jsh) + "\n";
        return str + jsh.decrease() + "}";
    }

    public Object accept(MainFunction node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"FunctionDeclaration\",\n"
            + jsh.spaces + "\"name\": \"main\",\n" // should be an identifier
            + jsh.spaces + "\"body\": " + node.statements.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(StatementBlock node, Object data) {
        String str = "[\n";

        jsh.increase();
        for (Statement s : node.statements) {
            str += jsh.spaces + s.accept(this, jsh);
            if (s != node.statements.get(node.statements.size() - 1))
                str += ",";
            str += "\n";
        }
        return str + jsh.decrease() + "]";
    }

    public Object accept(VariableDeclaration node, Object data) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"VariableDeclaration\",\n"
            + jsh.spaces + "\"id\": " + node.id.accept(this, jsh) + ",\n";
        if (node.e != null) {
            str += jsh.spaces + "\"init\": " + node.e.accept(this, jsh) + ",\n";
        }
        str += jsh.spaces + "\"var_type\": \"" + node.id.s.getType() + "\",\n"
            + jsh.spaces + "\"kind\": \"" + node.id.s.kind + "\"\n"
            + jsh.decrease() + "}";
        return str;
    }

    public Object accept(IfStatement node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"IfStatement\",\n"
            + jsh.spaces + "\"test\": " + node.test.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"consequent\": " + node.consequent.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"alternate\": " + node.alternate.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(WhileStatement node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"WhileStatement\",\n"
            + jsh.spaces + "\"test\": " + node.test.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"body\": " + node.body.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(SkipStatement node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"SkipStatement\"\n"
            + jsh.decrease() + "}";
    }

    public Object accept(ReturnStatement node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"ReturnStatement\",\n"
            + jsh.spaces + "\"argument\": " + node.argument.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(ExpressionStatement node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"ExpressionStatement\",\n"
            + jsh.spaces + "\"expression\": " + node.e.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(AssignmentExpression node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"AssignmentExpression\",\n"
            + jsh.spaces + "\"operator\": \"=\",\n"
            + jsh.spaces + "\"left\": " + node.id.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"right\": " + node.e.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(LogicalExpression node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"LogicalExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + node.operator + "\",\n"
            + jsh.spaces + "\"left\": " + node.e1.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"right\": " + node.e2.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(BinaryExpression node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"BinaryExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + node.operator + "\",\n"
            + jsh.spaces + "\"left\": " + node.e1.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"right\": " + node.e2.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(UnaryExpression node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"UnaryExpression\",\n"
            + jsh.spaces + "\"operator\": \"" + node.operator + "\",\n"
            + jsh.spaces + "\"argument\": " + node.e.accept(this, jsh) + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(CallExpression node, Object data) {
        String str = "{\n"
            + jsh.increase() + "\"type\": \"CallExpression\",\n"
            + jsh.spaces + "\"calee\": " + node.calee.accept(this, jsh) + ",\n"
            + jsh.spaces + "\"arguments\": [\n";

        jsh.increase();
        for (Identifier arg : node.arguments) {
            str += jsh.spaces + arg.accept(this, jsh);
            if (arg != node.arguments.get(node.arguments.size() - 1))
                str += ",";
            str += "\n";
        }
        return str
            + jsh.decrease() + "]\n"
            + jsh.decrease() + "}";
    }

    public Object accept(Identifier node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Identifier\",\n"
            + jsh.spaces + "\"name\": \"" + node.id + "\"\n"
            + jsh.decrease() + "}";
    }

    public Object accept(Number node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Literal\",\n"
            + jsh.spaces + "\"value\": " + node.value + "\n"
            + jsh.decrease() + "}";
    }

    public Object accept(Bool node, Object data) {
        return "{\n"
            + jsh.increase() + "\"type\": \"Literal\",\n"
            + jsh.spaces + "\"value\": " + node.value + "\n"
            + jsh.decrease() + "}";
    }

}