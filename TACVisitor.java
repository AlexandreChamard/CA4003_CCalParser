
/**
how to declare a var in 3 addr code ?
var i:int; ??
 */

class TACNode {
    public String code;
}

class TACStm {
    public String code;
    public String next;
}

class TACCond {
    public String code;
    public String _true;
    public String _false;
}

class TACExpr {
    public String code;
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
        return String.join(" ", words) + "\n";
    }

    public Object accept(Program node, Object data) {
        TACStm s = new TACStm();

        s.next = newLabel();
        node.statements.accept(this, s);

        TACNode p = new TACNode();
        p.code = s.code + toLabel(s.next);
        return p;
    }

    public Object accept(FunctionDeclaration node, Object data) {
        TACStm s = (TACStm)data;
        TACStm s1 = new TACStm();

        s1.next = newLabel();
        node.statements.accept(this, s1);
        s.code = toLabel(node.id.id.image)
            + s1.code
            + toLabel(s1.next);
        return s;
    }

    public Object accept(MainFunction node, Object data) {
        TACStm s = (TACStm)data;
        TACStm s1 = new TACStm();

        s1.next = newLabel();
        node.statements.accept(this, s1);
        s.code = toLabel("main")
            + s1.code
            + toLabel(s1.next)
            + "return\n";
        return s;
    }

    public Object accept(StatementBlock node, Object data) {
        TACStm s = (TACStm)data;

        for (Statement statement : node.statements) {
            TACStm s1 = new TACStm();

            if (statement != node.statements.get(node.statements.size()-1))
                s1.next = newLabel();
            else
                s1.next = s.next;
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
            s.code = e.addr + gen(node.id.id.image, "=", e.addr);
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
            s.code = b.code + toLabel(b._true) + s1.code + gen("goto", s.next) + toLabel(b._false) + s2.code;
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
        e.code = e1.code + gen(node.id.id.image, "=", e1.addr);
        e.addr = node.id.id.image;
        return e;
    }

    public Object accept(LogicalExpression node, Object data) {
        return null;
    }

    public Object accept(BinaryExpression node, Object data) {
        return null;
    }

    public Object accept(UnaryExpression node, Object data) {
        return null;
    }

    public Object accept(CallExpression node, Object data) {
        return null;
    }

    public Object accept(Identifier node, Object data) {
        return null;
    }

    public Object accept(Number node, Object data) {
        return null;
    }

    public Object accept(Bool node, Object data) {
        return null;
    }

}