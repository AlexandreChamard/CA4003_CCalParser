
class TACStm {
    public String code = "";
    public String next;
}

class TACCond {
    public String code = "";
    public String _true;
    public String _false;
}

class TACExpr {
    public String code = "";
    public String addr;
}

/** return String representing the Three Adress Code of the AST */
public class TACVisitor implements ASTVisitor {
    private int iLabel = 0;
    private int iTmp = 0;

    public String newLabel() {
        ++iLabel;
        return "_L"+iLabel;
    }

    public String newTmp() {
        ++iTmp;
        return "_t"+iTmp;
    }

    public String toLabel(String label) {
        return label + ":\n";
    }

    private static String gen(String ... words) {
        return "\t" + String.join(" ", words) + "\n";
    }

    public Object accept(Program node, Object data) {
        TACStm s = new TACStm();

        s.next = newLabel();
        node.statements.accept(this, s);
        return s.code;
    }

    public Object accept(FunctionDeclaration node, Object data) {
        TACStm s = (TACStm)data;
        TACStm s1 = new TACStm();

        s1.next = newLabel();
        node.statements.accept(this, s1);
        s.code = toLabel(node.id.s.name) + s1.code;
        return s;
    }

    public Object accept(MainFunction node, Object data) {
        TACStm s = (TACStm)data;
        TACStm s1 = new TACStm();

        s1.next = newLabel();
        node.statements.accept(this, s1);
        s.code = toLabel("main") + s1.code + gen("return");
        return s;
    }

    public Object accept(StatementBlock node, Object data) {
        TACStm s = (TACStm)data;

        for (Statement statement : node.statements) {
            TACStm s1 = new TACStm();

            s1.next = newLabel();
            statement.accept(this, s1);
            s.code += s1.code + toLabel(s1.next);
        }
        return s;
    }

    public Object accept(VariableDeclaration node, Object data) {
        TACStm s = (TACStm)data;

        if (node.e != null) {
            TACExpr e = new TACExpr();

            node.e.accept(this, e);
            s.code = e.code + gen(node.id.s.name, "=", e.addr);
        }
        return s;
    }

    public Object accept(IfStatement node, Object data) {
        TACStm s = (TACStm)data;

        if (node.alternate == null) {
            TACCond b = new TACCond();
            TACStm s1 = new TACStm();

            b._true = newLabel();
            b._false = s1.next = s.next;
            node.test.accept(this, b);
            node.consequent.accept(this, s1);
            s.code = b.code + toLabel(b._true) + s1.code;
        } else {
            TACCond b = new TACCond();
            TACStm s1 = new TACStm();
            TACStm s2 = new TACStm();

            b._true = newLabel();
            b._false = newLabel();
            s1.next = s2.next = s.next;
            node.test.accept(this, b);
            node.consequent.accept(this, s1);
            node.alternate.accept(this, s2);
            s.code = b.code + toLabel(b._true) + s1.code + gen("GOTO", s.next) + toLabel(b._false) + s2.code;
        }
        return s;
    }

    public Object accept(WhileStatement node, Object data) {
        TACStm s = (TACStm)data;
        TACCond b = new TACCond();
        TACStm s1 = new TACStm();
        String begin = newLabel();

        b._true = newLabel();
        b._false = s.next;
        s1.next = begin;
        node.test.accept(this, b);
        node.body.accept(this, s1);
        s.code = toLabel(begin) + b.code + toLabel(b._true) + s1.code + gen("goto", begin);
        return s;
    }

    public Object accept(SkipStatement node, Object data) {
        return data;
    }

    public Object accept(ReturnStatement node, Object data) {
        TACStm s = (TACStm)data;
    
        if (node.argument == null) {
            s.code = gen("return");
        } else {
            TACExpr e = new TACExpr();

            node.argument.accept(this, e);
            s.code = e.code + gen("return", e.addr);
        }
        return s;
    }

    public Object accept(ExpressionStatement node, Object data) {
        TACStm s = (TACStm)data;
        TACExpr e = new TACExpr();

        node.e.accept(this, e);
        s.code = e.code;
        return s;
    }

    public Object accept(AssignmentExpression node, Object data) {
        TACExpr e = (TACExpr)data;
        TACExpr e1 = new TACExpr();

        node.e.accept(this, e1);
        e.code = e1.code + gen(node.id.s.name, "=", e1.addr);
        e.addr = node.id.s.name;
        return e;
    }

    public Object accept(LogicalExpression node, Object data) {
        TACCond b = (TACCond)data;

        switch (node.operator) {
            case "&&": {
                TACCond b1 = new TACCond();
                TACCond b2 = new TACCond();

                b1._true = newLabel();
                b2._true = b._true;
                b1._false = b2._false = b._false;
                node.e1.accept(this, b1);
                node.e2.accept(this, b2);
                b.code = b1.code + toLabel(b1._true) + b2.code;
                break;
            }

            case "||": {
                TACCond b1 = new TACCond();
                TACCond b2 = new TACCond();

                b1._true = b2._true = b._true;
                b1._false = b._false;
                b2._false = newLabel();
                node.e1.accept(this, b1);
                node.e2.accept(this, b2);
                b.code = b1.code + toLabel(b1._false) + b2.code;
                break;
            }
            default:
                break;
        }
        return b;
    }

    public Object accept(BinaryExpression node, Object data) {
        TACExpr e1 = new TACExpr();
        TACExpr e2 = new TACExpr();

        node.e1.accept(this, e1);
        node.e2.accept(this, e2);
        switch (node.operator) {
            case "<": case "<=": case ">": case ">=": case "==": case "!=":
                TACCond b = (TACCond)data;

                b.code = e1.code + e2.code + gen("if", e1.addr, node.operator, e2.addr, "goto", b._true) + gen("goto", b._false);
                return b;

            case "+": case "-":
                TACExpr e = (TACExpr)data;

                e.addr = newTmp();
                e.code = e1.code + e2.code + gen(e.addr, "=", e1.addr, node.operator, e2.addr);
                return e;

            default:
                return data;
        }
    }

    public Object accept(UnaryExpression node, Object data) {
        switch (node.operator) {
            case "~":
                TACCond b = (TACCond)data;
                TACCond b1 = new TACCond();

                b1._true = b._false;
                b1._false = b._true;
                node.e.accept(this, b1);
                b.code = b1.code;
                return b;

            case "-":
                TACExpr e = (TACExpr)data;
                TACExpr e1 = new TACExpr();

                node.e.accept(this, e1);
                e.addr = newTmp();
                e.code = e1.code + gen(e.addr, "=", node.operator, e1.addr);
                return e;

            default:
                return data;
        }
    }

    public Object accept(CallExpression node, Object data) {
        TACExpr e = (TACExpr)data;
        String stack = "";

        for (int i = node.arguments.size(); i-- > 0; ) {
            TACExpr e1 = new TACExpr();
            node.arguments.get(i).accept(this, e);
            e.code += e1.code;
            stack += gen("param", node.arguments.get(i).s.name);
        }
        e.addr = newTmp();
        e.code += stack + gen(e.addr, "=", "call", node.calee.s.name, ",", String.valueOf(node.arguments.size()));
        return e;
    }

    public Object accept(Identifier node, Object data) {
        TACExpr e = (TACExpr)data;

        e.addr = node.s.name;
        return e;
    }

    public Object accept(Number node, Object data) {
        TACExpr e = (TACExpr)data;

        e.addr = String.valueOf(node.value);
        return e;
    }

    public Object accept(Bool node, Object data) {
        TACExpr e = (TACExpr)data;

        e.addr = String.valueOf(node.value);
        return e;
    }

}