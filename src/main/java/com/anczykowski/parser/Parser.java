package com.anczykowski.parser;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.errormodule.exceptions.ParserException;
import com.anczykowski.lexer.*;
import com.anczykowski.parser.structures.*;
import com.anczykowski.parser.structures.expressions.*;
import com.anczykowski.parser.structures.expressions.relops.*;
import com.anczykowski.parser.structures.statements.*;
import lombok.RequiredArgsConstructor;

import java.util.*;
import java.util.function.BiFunction;

@SuppressWarnings("StatementWithEmptyBody")
@RequiredArgsConstructor
public class Parser {
    final Lexer lexer;

    final ErrorModule errorModule;

    // program = { func_def | class_def };
    public Program parse() throws ParserException {
        HashMap<String, FuncDef> functions = new HashMap<>();
        HashMap<String, ClassDef> classes = new HashMap<>();

        lexer.getNextToken();

        while (parseFunDef(functions) || parseClassDef(classes)) {
        }

        return new Program(functions, classes);
    }

    // class_def = "class", class_id, class_body;
    protected boolean parseClassDef(HashMap<String, ClassDef> classes) throws ParserException {
        if (!consumeIf(TokenType.CLASS_KEYWORD)) {
            return false;
        }

        if (!peekIf(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("class", "class keyword must be followed by class identifier");
            return false;
        }

        var classIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (classes.containsKey(classIdentifier)) {
            reportAlreadyDeclared(classIdentifier);
        }

        lexer.getNextToken();

        var classBody = parseClassBody();

        if (classBody == null) {
            reportUnexpectedToken(classIdentifier, "expected class body after class identifier");
        }

        classes.put(classIdentifier, new ClassDef(classIdentifier, classBody));

        return true;
    }

    // class_body = "{", { func_def | var_stmt }, "}";
    protected ClassBody parseClassBody() throws ParserException {
        if (!consumeIf(TokenType.LBRACE)) {
            return null;
        }

        HashMap<String, FuncDef> methods = new HashMap<>();
        HashMap<String, VarStmt> attributes = new HashMap<>();

        while (parseFunDef(methods) || parseVarStmt(attributes) != null) {
        }

        if (!consumeIf(TokenType.RBRACE)) {
            reportUnexpectedTokenWithExplanation("'}' at the end of class body");
        }

        methods.values().forEach(method -> method.setIsMethod(true));

        return new ClassBody(methods, attributes);
    }

    // var_stmt = "var", identifier, "=", expr, ";";
    protected VarStmt parseVarStmt(HashMap<String, VarStmt> variables) throws ParserException {
        if (!consumeIf(TokenType.VAR_KEYWORD)) {
            return null;
        }


        if (!peekIf(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("var", "var keyword must be followed by identifier");
            return null;
        }

        var varIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (variables.containsKey(varIdentifier)) {
            reportAlreadyDeclared(varIdentifier);
            return null;
        }

        lexer.getNextToken();

        if (!consumeIf(TokenType.ASSIGNMENT)) {
            reportUnexpectedToken(varIdentifier, "expected '=' after identifier in var statement");
        }

        var expr = parseExpr();
        VarStmt varStmt;
        if (expr == null) {
            reportUnexpectedToken("=", "= operator without expression");
            throw new ParserException();
        }

        varStmt = new VarStmt(varIdentifier, expr);

        variables.put(varIdentifier, varStmt);

        if (!consumeIf(TokenType.SEMICOLON)) {
            reportUnexpectedTokenWithExplanation("';' expected");
            return null;
        }

        return varStmt;
    }

    // func_def = identifier, "(", [parameters], ")", code_block;
    protected boolean parseFunDef(HashMap<String, FuncDef> functions) throws ParserException {
        if (!peekIf(TokenType.IDENTIFIER)) {
            return false;
        }

        var funIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        lexer.getNextToken();

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken(funIdentifier, "'(' expected after identifier in function definition");
        }

        ArrayList<Parameter> params = parseParams();

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedToken(funIdentifier, "unmatched ')' with '('");
        }

        var codeBlock = parseCodeBlock();

        if (codeBlock == null) {
            reportUnexpectedToken(")", "code block expected after ')' in function definition");
            throw new ParserException();
        }

        if (functions.containsKey(funIdentifier)) {
            reportAlreadyDeclared(funIdentifier);
            return false;
        }
        functions.put(funIdentifier, new FuncDef(funIdentifier, params, codeBlock));
        return true;
    }

    // code_block  = "{", { non_ret_stmt | ret_stmt | obj_access, ["=", expr], ";" }, "}";
    protected CodeBLock parseCodeBlock() throws ParserException {

        if (!consumeIf(TokenType.LBRACE)) {
            return null;
        }

        ArrayList<Statement> statements = new ArrayList<>();

        HashMap<String, VarStmt> variables = new HashMap<>();

        while (parseNonRetStmt(variables, statements)
                || parseRetStmt(statements)
                || parseExprInsideCodeBlock(statements)
        ) {
        }


        if (!consumeIf(TokenType.RBRACE)) {
            reportUnexpectedTokenWithExplanation("Expected '{' to close code block");
        }

        return new CodeBLock(statements);
    }

    // ret_stmt = "return", [expr], ";"
    protected boolean parseRetStmt(ArrayList<Statement> statementsAndExpressions) {
        if(!consumeIf(TokenType.RETURN_KEYWORD)){
            return false;
        }

        var expression = parseExpr();
        var returnExpression = new ReturnStatement(expression);
        statementsAndExpressions.add(returnExpression);

        if (!consumeIf(TokenType.SEMICOLON)) {
            reportUnexpectedTokenWithExplanation("';' expected");
        }

        return true;
    }

    // obj_access, ["=", expr], ";"
    protected boolean parseExprInsideCodeBlock(ArrayList<Statement> statementsAndExpressions) {
        var expression = parseObjAccess();
        if (expression == null) {
            return false;
        }

        Statement expressionStatement = null;
        if (consumeIf(TokenType.ASSIGNMENT)) {
            var assignExpr = parseExpr();
            if (assignExpr == null) {
                reportUnexpectedToken("=", "expected expression after '='");
            } else {
                expressionStatement = new AssignmentStatement(expression, assignExpr);
            }
        }

        if (expressionStatement == null) {
            expressionStatement = new ExpressionStatement(expression);
        }

        statementsAndExpressions.add(expressionStatement);

        if (!consumeIf(TokenType.SEMICOLON)) {
            reportUnexpectedTokenWithExplanation("';' expected");
        }

        return true;
    }

    // expr = and_expr, { "or", and_expr };
    protected Expression parseExpr() {
        var left = parseAndExpr();
        if (left == null) return null;

        while (consumeIf(TokenType.OR_KEYWORD)) {
            var right = parseAndExpr();
            if (right == null) {
                reportUnexpectedToken("or", "expected expression after 'or' keyword");
                continue;
            }
            left = new OrExpression(left, right);
        }
        return left;
    }

    // and_expr = rel_expr, { "and", rel_expr };
    protected Expression parseAndExpr() {
        var left = parseRelExpr();
        if (left == null) return null;

        while (consumeIf(TokenType.AND_KEYWORD)) {
            var right = parseRelExpr();
            if (right == null) {
                reportUnexpectedToken("and", "expected expression after 'and' keyword");
                continue;
            }
            left = new AndExpr(left, right);
        }
        return left;
    }

    private static final Map<TokenType, BiFunction<Expression, Expression, Expression>> relOps = Map.of(
        TokenType.EQ, EqRelExpr::new,
        TokenType.NE, NeRelExpr::new,
        TokenType.LT, LtRelExpr::new,
        TokenType.LE, LeRelExpr::new,
        TokenType.GT, GtRelExpr::new,
        TokenType.GE, GeRelExpr::new
    );

    // rel_expr = add_expr, [rel_operator, add_expr];
    protected Expression parseRelExpr() {
        var left = parseAddExpr();
        if (left == null) return null;



        BiFunction<Expression, Expression, Expression> relOpConstructor;
        if ((relOpConstructor = relOps.get(lexer.getCurrentToken().getType())) != null) {
            lexer.getNextToken();
            var right = parseAddExpr();
            if (right == null) {
                reportUnexpectedTokenWithExplanation("expected expression after relation operator");
                return left;
            }
            left = relOpConstructor.apply(left, right);
        }

        if (relOps.containsKey(lexer.getCurrentToken().getType())) {
            reportUnsupportedChaining();
            // consume all unsupported chains (e.g.: a > b > c)
            while (relOps.containsKey(lexer.getCurrentToken().getType()) || parseAddExpr() != null) {
                if (relOps.containsKey(lexer.getCurrentToken().getType())) {
                    lexer.getNextToken();
                }
            }
            return left;
        }

        return left;
    }

    private static final Map<TokenType, BiFunction<Expression, Expression, Expression>> addOps = Map.of(
            TokenType.PLUS, AdditionTerm::new,
            TokenType.MINUS, SubtractionTerm::new
    );


    // add_expr = term, { add_op, term };
    protected Expression parseAddExpr() {
        var left = parseTerm();
        if (left == null) return null;

        BiFunction<Expression, Expression, Expression> addOpConstructor;
        while ((addOpConstructor = addOps.get(lexer.getCurrentToken().getType())) != null) {
            lexer.getNextToken();
            var right = parseTerm();
            if (right == null) {
                reportUnexpectedTokenWithExplanation("expected expression after additive operator");
                continue;
            }
            left = addOpConstructor.apply(left, right);
        }
        return left;
    }

    private static final Map<TokenType, BiFunction<Expression, Expression, Expression>> multOps = Map.of(
            TokenType.ASTERISK, MultiplicationFactor::new,
            TokenType.SLASH, DivisionFactor::new
    );

    // term = factor, { mult_op, factor };
    protected Expression parseTerm() {
        var left = parseFactor();
        if (left == null) return null;

        BiFunction<Expression, Expression, Expression> multOpConstructor;
        while ((multOpConstructor = multOps.get(lexer.getCurrentToken().getType())) != null) {
            lexer.getNextToken();
            var right = parseFactor();
            if (right == null) {
                reportUnexpectedTokenWithExplanation("expected expression after multiplicative operator");
                continue;
            }
            left = multOpConstructor.apply(left, right);
        }
        return left;
    }

    // factor = ["not" | "-"], (factor_inner | "(", expr, ")"), ["as", (type | class_id)];
    protected Expression parseFactor() {
        boolean isNegated = consumeIf(TokenType.MINUS) || consumeIf(TokenType.NOT_KEYWORD);

        var factor = parseFactorInner();
        if (factor == null) {
            factor = parseExprParenthesized();
        }

        if (factor == null && !isNegated) {
            return null;
        }

        if (factor == null) {
            reportUnexpectedTokenWithExplanation("negation must be followed by inner factor or parenthesized expression");
            return null;
        }

        if (isNegated) {
            factor = new NegatedExpression(factor);
        }

        if (consumeIf(TokenType.AS_KEYWORD)) {
            if (!peekIf(TokenType.IDENTIFIER)) {
                reportUnexpectedToken("as", "expected identifier after 'as' keyboard");
                return factor;
            }
            var identifier = ((StringToken) lexer.getCurrentToken()).getValue();
            lexer.getNextToken();
            factor = new CastExpression(factor, identifier);
        }

        return factor;
    }

    // factor_inner = constant | obj_access | string | class_init;
    protected Expression parseFactorInner() {
        var inner = parseConstant();
        if (inner == null) {
            inner = parseObjAccess();
        }
        if (inner == null) {
            inner = parseString();
        }
        if (inner == null) {
            inner = parseClassInit();
        }
        return inner;
    }

    protected Expression parseConstant() {
        Expression constant = parseFloatConstant();
        if (constant == null) {
            constant = parseIntegerConstant();
        }
        return constant;
    }

    protected FloatConstantExpr parseFloatConstant() {
        if (peekIf(TokenType.FLOAT_NUMBER)) {
            var floatToken = (FloatToken) lexer.getCurrentToken();
            lexer.getNextToken();
            return new FloatConstantExpr(floatToken.getValue());
        }
        return null;
    }

    protected IntegerConstantExpr parseIntegerConstant() {
        if (peekIf(TokenType.INTEGER_NUMBER)) {
            var integerToken = (IntegerToken) lexer.getCurrentToken();
            lexer.getNextToken();
            return new IntegerConstantExpr(integerToken.getValue());
        }
        return null;
    }


    // obj_access = ident_or_fun_call, { ".",  ident_or_fun_call };
    protected Expression parseObjAccess() {

        var objAccess = parseIdentOrFunCall();

        if (objAccess == null) {
            return null;
        }

        var accessChildren = new ArrayDeque<Expression>();
        accessChildren.add(objAccess);
        while (consumeIf(TokenType.PERIOD)) {
            var child = parseIdentOrFunCall();
            if (child == null) {
                reportUnexpectedToken(".", "expected identifier or function call after '.'");
            } else {
                accessChildren.add(child);
            }
        }

        var lastChild = accessChildren.remove();
        for (var accessChild : accessChildren) {
            lastChild = new ObjectAccessExpression(accessChild, lastChild);
        }

        return lastChild;
    }

    // ident_or_fun_call  = identifier, ["(", [args], ")"];
    protected Expression parseIdentOrFunCall() {
        if (!peekIf(TokenType.IDENTIFIER)) {
            return null;
        }
        var identifier = ((StringToken) lexer.getCurrentToken()).getValue();
        lexer.getNextToken();
        if (consumeIf(TokenType.LPAREN)) {
            var args = parseArgs();
            if (!consumeIf(TokenType.RPAREN)) {
                reportUnexpectedToken("(", "unmatched ')' in function call");
            }
            return new FunctionCallExpression(identifier, args);
        }
        return new IdentifierExpression(identifier);
    }

    // args = arg, {",", arg }
    protected ArrayList<Arg> parseArgs() {
        var args = new ArrayList<Arg>();
        var firstArg = parseArg();
        if (firstArg == null) return args;
        args.add(firstArg);
        while (consumeIf(TokenType.COMMA)) {
            var arg = parseArg();
            if (arg == null) {
                reportUnexpectedToken(",", "expected another argument after ','");
                continue;
            }
            args.add(arg);
        }

        return args;
    }

    // arg = ["ref"] expr;
    protected Arg parseArg() {
        var isByRef = consumeIf(TokenType.REF_KEYWORD);
        var expr = parseExpr();
        if (expr == null) {
            if (isByRef) {
                reportUnexpectedToken("ref", "expected expression after ref keyword (argument)");
            }
            return null;
        }
        return new Arg(expr, isByRef);
    }

    protected Expression parseString() {
        if (!peekIf(TokenType.STRING)) {
            return null;
        }
        var stringToken = (StringToken) lexer.getCurrentToken();
        lexer.getNextToken();
        return new StringExpression(stringToken.getValue());
    }

    // class_init = "new", class_id, "(", [args], ")";
    protected Expression parseClassInit() {
        if (!consumeIf(TokenType.NEW_KEYWORD)) {
            return null;
        }
        if (!peekIf(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("new", "expected class identifier after new keyword");
            return null;
        }

        var identifier = ((StringToken) lexer.getCurrentToken()).getValue();
        lexer.getNextToken();

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken(identifier, "'(' expected after identifier in class initialization");
        }

        var args = parseArgs();

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedToken(identifier, "unmatched ')' in class initialization");
        }
        return new ClassInitExpression(identifier, args);
    }

    // "(", expr, ")"
    protected Expression parseExprParenthesized() {
        if (!consumeIf(TokenType.LPAREN)) {
            return null;
        }
        var expr = parseExpr();
        if (expr == null) {
            reportUnexpectedToken("(", "expected expression after '('");
            return null;
        }
        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedTokenWithExplanation("unmatched ')'");
        }
        return expr;
    }

    // non_ret_stmt = var_stmt | cond_stmt | while_stmt | for_stmt | switch_stmt;
    protected boolean parseNonRetStmt(
            HashMap<String, VarStmt> variables, ArrayList<Statement> statementsAndExpressions
    ) throws ParserException {
        Statement nonRetStmt = parseVarStmt(variables);
        if (nonRetStmt == null) {
            nonRetStmt = parseConditionalStmt();
        }
        if (nonRetStmt == null) {
            nonRetStmt = parseWhileStmt();
        }
        if (nonRetStmt == null) {
            nonRetStmt = parseForStmt();
        }
        if (nonRetStmt == null) {
            nonRetStmt = parseSwitchStmt();
        }
        if (nonRetStmt == null) {
            return false;
        }

        statementsAndExpressions.add(nonRetStmt);
        return true;
    }

    // cond_stmt = "if", "(", expr, ")", code_block, ["else", code_block];
    protected Statement parseConditionalStmt() throws ParserException {
        if (!consumeIf(TokenType.IF_KEYWORD)) {
            return null;
        }

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken("if", "expected '(' after if keyword");
        }

        var condition = parseExpr();

        if (condition == null) {
            reportUnexpectedToken("(", "expression expected after '(' in if statement");
            throw new ParserException();
        }

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedTokenWithExplanation("unmatched ')'");
        }

        var codeBlock = parseCodeBlock();
        if (codeBlock == null) {
            reportUnexpectedToken(")", "code block expected after ')' in if statement");
            return null;
        }

        CodeBLock elseCodeBlock = null;

        if (consumeIf(TokenType.ELSE_KEYWORD)) {
            elseCodeBlock = parseCodeBlock();
            if (elseCodeBlock == null) {
                reportUnexpectedToken("else", "code block expected after else in if statement");
                return null;
            }
        }

        return new CondStmt(condition, codeBlock, elseCodeBlock);
    }

    // while_stmt = "while", "(", expr, ")", code_block;
    protected Statement parseWhileStmt() throws ParserException {
        if (!consumeIf(TokenType.WHILE_KEYWORD)) {
            return null;
        }

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken("while", "expected '(' after while keyword");
        }

        var condition = parseExpr();

        if (condition == null) {
            reportUnexpectedToken("(", "expression expected after '(' in while statement");
            throw new ParserException();
        }

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedTokenWithExplanation("unmatched ')'");
        }

        var codeBlock = parseCodeBlock();
        if (codeBlock == null) {
            reportUnexpectedToken(")", "code block expected after ')' in while statement");
            return null;
        }

        return new WhileStmt(condition, codeBlock);
    }

    // for_stmt = "for", "(", identifier, "in", expr, ")", code_block;
    protected Statement parseForStmt() throws ParserException {
        if (!consumeIf(TokenType.FOR_KEYWORD)) {
            return null;
        }

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken("for", "expected '(' after for keyword");
        }

        if (!peekIf(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("(", "expected identifier after '(' in for statement");
            return null;
        }

        var iteratorIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        lexer.getNextToken();

        if (!consumeIf(TokenType.IN_KEYWORD)) {
            reportUnexpectedTokenWithExplanation("expected in keyword after identifier in for statement");
        }

        var iterable = parseExpr();

        if (iterable == null) {
            reportUnexpectedToken("in", "expression expected after 'in' in for statement");
            throw new ParserException();
        }

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedTokenWithExplanation("unmatched ')'");
        }

        var codeBlock = parseCodeBlock();
        if (codeBlock == null) {
            reportUnexpectedToken(")", "code block expected after ')' in for statement");
            return null;
        }

        return new ForStmt(iteratorIdentifier, iterable, codeBlock);
    }

    // switch_stmt = "switch", "(", (expr), ")", "{", { (type | class_id | "default"), "->", code_block } ,"}";
    protected Statement parseSwitchStmt() throws ParserException {
        if (!consumeIf(TokenType.SWITCH_KEYWORD)) {
            return null;
        }

        if (!consumeIf(TokenType.LPAREN)) {
            reportUnexpectedToken("switch", "expected '(' after switch keyword");
        }

        var expr = parseExpr();

        if (expr == null) {
            reportUnexpectedToken("(", "expression expected after '(' in switch statement");
            throw new ParserException();
        }

        if (!consumeIf(TokenType.RPAREN)) {
            reportUnexpectedTokenWithExplanation("unmatched ')'");
        }

        if (!consumeIf(TokenType.LBRACE)) {
            reportUnexpectedToken(")", "expected '{' after ')' in switch statement");
        }

        Map<SwitchLabel, CodeBLock> switchElements = new HashMap<>();

        while (peekIf(TokenType.IDENTIFIER) || peekIf(TokenType.DEFAULT_KEYWORD)) {
            var switchLabel = peekIf(TokenType.IDENTIFIER)
                    ? new SwitchLabel(((StringToken) lexer.getCurrentToken()).getValue())
                    : new SwitchLabel("default");

            lexer.getNextToken();

            if (!consumeIf(TokenType.ARROW)) {
                reportUnexpectedTokenWithExplanation("expected '->' after type in switch statement element");
            }

            var codeBlock = parseCodeBlock();
            if (codeBlock == null) {
                reportUnexpectedToken("->", "code block expected after '->' in switch statement");
                continue;
            }

            if(switchElements.containsKey(switchLabel)){
                reportDuplicateLabel(switchLabel.getLabel());
                continue;
            }
            switchElements.put(switchLabel, codeBlock);
        }

        if (!consumeIf(TokenType.RBRACE)) {
            reportUnexpectedTokenWithExplanation("unmatched '}'");
        }

        return new SwitchStmt(expr, switchElements);
    }


    // parameters = identifier, { ",", identifier };
    protected ArrayList<Parameter> parseParams() {
        if (!peekIf(TokenType.IDENTIFIER)) {
            return new ArrayList<>();
        }
        ArrayList<Parameter> params = new ArrayList<>();
        params.add(new Parameter(((StringToken) lexer.getCurrentToken()).getValue()));
        lexer.getNextToken();
        while (consumeIf(TokenType.COMMA)) {
            if (!peekIf(TokenType.IDENTIFIER)) {
                reportUnexpectedToken(",", "expected token after ',' in parameters");
                continue;
            }
            params.add(new Parameter(((StringToken) lexer.getCurrentToken()).getValue()));
            lexer.getNextToken();
        }
        return params;
    }

    private boolean consumeIf(TokenType tokenType) {
        if (!peekIf(tokenType)) {
            return false;
        }
        lexer.getNextToken();
        return true;
    }

    private boolean peekIf(TokenType tokenType) {
        return lexer.getCurrentToken().getType().equals(tokenType);
    }

    private void reportAlreadyDeclared(String identifier) {
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.ALREADY_DECLARED)
                .location(lexer.getCurrentLocation().clone())
                .codeLineBuffer(lexer.getCharacterBuffer())
                .underlineFragment(identifier)
                .build());
    }

    private void reportDuplicateLabel(String identifier) {
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.DUPLICATE_LABEL)
                .location(lexer.getCurrentLocation().clone())
                .codeLineBuffer(lexer.getCharacterBuffer())
                .underlineFragment(identifier)
                .build());
    }

    private void reportUnexpectedToken(String underline, String explanation) {
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.UNEXPECTED_TOKEN)
                .location(lexer.getCurrentLocation().clone())
                .underlineFragment(underline)
                .explanation(explanation)
                .codeLineBuffer(lexer.getCharacterBuffer())
                .build());
    }

    private void reportUnexpectedTokenWithExplanation(String explanation) {
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.UNEXPECTED_TOKEN)
                .location(lexer.getCurrentLocation().clone())
                .explanation(explanation)
                .codeLineBuffer(lexer.getCharacterBuffer())
                .build());
    }

    private void reportUnsupportedChaining() {
        errorModule.addError(ErrorElement.builder()
                .errorType(ErrorType.UNSUPPORTED_CHAINING)
                .location(lexer.getCurrentLocation().clone())
                .codeLineBuffer(lexer.getCharacterBuffer())
                .build());
    }
}
