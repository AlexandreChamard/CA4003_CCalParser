
interface ASTVisitor {
    public Object accept(Program node, Object data);
    public Object accept(FunctionDeclaration node, Object data);
    public Object accept(MainFunction node, Object data);
    public Object accept(StatementBlock node, Object data);
    public Object accept(VariableDeclaration node, Object data);
    public Object accept(IfStatement node, Object data);
    public Object accept(WhileStatement node, Object data);
    public Object accept(SkipStatement node, Object data);
    public Object accept(ReturnStatement node, Object data);
    public Object accept(ExpressionStatement node, Object data);
    public Object accept(AssignmentExpression node, Object data);
    public Object accept(LogicalExpression node, Object data);
    public Object accept(BinaryExpression node, Object data);
    public Object accept(UnaryExpression node, Object data);
    public Object accept(CallExpression node, Object data);
    public Object accept(Identifier node, Object data);
    public Object accept(Number node, Object data);
    public Object accept(Bool node, Object data);
}