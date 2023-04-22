package com.anczykowski.parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.lexer.FloatToken;
import com.anczykowski.lexer.IntegerToken;
import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.structures.ClassBody;
import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.Parameter;
import com.anczykowski.parser.structures.Program;
import com.anczykowski.parser.structures.expressions.AdditionTerm;
import com.anczykowski.parser.structures.expressions.AssignmentExpression;
import com.anczykowski.parser.structures.expressions.DivisionFactor;
import com.anczykowski.parser.structures.expressions.Expression;
import com.anczykowski.parser.structures.expressions.FloatConstantExpr;
import com.anczykowski.parser.structures.expressions.IntegerConstantExpr;
import com.anczykowski.parser.structures.expressions.MultiplicationFactor;
import com.anczykowski.parser.structures.expressions.NegatedExpression;
import com.anczykowski.parser.structures.expressions.OrExpression;
import com.anczykowski.parser.structures.expressions.OrOpArg;
import com.anczykowski.parser.structures.expressions.SubtractionTerm;
import com.anczykowski.parser.structures.expressions.relops.EqRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.GtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LeRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.LtRelOpArg;
import com.anczykowski.parser.structures.expressions.relops.NeRelOpArg;
import com.anczykowski.parser.structures.statements.VarStmt;
import lombok.RequiredArgsConstructor;

@SuppressWarnings("StatementWithEmptyBody")
@RequiredArgsConstructor
public class Parser {
    final Lexer lexer;

    final ErrorModule errorModule;

    // program = { func_def | class_def };
    public Program parse() {
        HashMap<String, FuncDef> functions = new HashMap<>();
        HashMap<String, ClassDef> classes = new HashMap<>();

        lexer.getNextToken();

        while (parseFunDef(functions) || parseClassDef(classes)) {
        }

        return new Program(functions, classes);
    }

    // class_def = "class", class_id, class_body;
    protected boolean parseClassDef(HashMap<String, ClassDef> classes) {
        if (!lexer.getCurrentToken().getType().equals(TokenType.CLASS_KEYWORD)) {
            return false;
        }
        lexer.getNextToken();

        if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("class", "class keyword must be followed by class identifier");
            return false; // TODO: what to do in case of error (all returns)
        }

        var classIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (classes.containsKey(classIdentifier)) {
            reportAlreadyDeclared(classIdentifier);
            return false;
        }

        lexer.getNextToken();

        if (!lexer.getCurrentToken().getType().equals(TokenType.LBRACE)) {
            // TODO: Underlining for missing brace?
            reportUnexpectedToken();
            return false;
        }
        lexer.getNextToken();

        var classBody = parseClassBody();

        classes.put(classIdentifier, new ClassDef(classIdentifier, classBody));

        return true;
    }

    // class_body = "{", { func_def | var_stmt }, "}";
    protected ClassBody parseClassBody() {
        HashMap<String, FuncDef> methods = new HashMap<>();
        HashMap<String, VarStmt> attributes = new HashMap<>();

        while (parseFunDef(methods) || parseVarStmt(attributes) != null) {
        }

        return new ClassBody(methods, attributes);
    }

    // var_stmt = "var", identifier, ["=", expr], ";";
    protected VarStmt parseVarStmt(HashMap<String, VarStmt> variables) {
        if (!lexer.getCurrentToken().getType().equals(TokenType.VAR_KEYWORD)) {
            return null;
        }

        lexer.getNextToken();

        if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("var", "var keyword must be followed by identifier");
            return null;
        }

        var varIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (variables.containsKey(varIdentifier)) {
            reportAlreadyDeclared(varIdentifier);
            return null;
        }

        var varStmt = new VarStmt(varIdentifier);

        lexer.getNextToken();

        if (lexer.getCurrentToken().getType().equals(TokenType.ASSIGNMENT)) {
            lexer.getNextToken();
            var expr = parseExpr();
            if (expr == null) {
                reportUnexpectedToken("=", "= operator without expression");
            } else {
                varStmt = new VarStmt(varIdentifier, expr);
            }
        }

        variables.put(varIdentifier, varStmt);

        if (!lexer.getCurrentToken().getType().equals(TokenType.SEMICOLON)) {
            errorModule.addError(ErrorElement.builder()
                                     .errorType(ErrorType.MISSING_SEMICOLON)
                                     .location(lexer.getCurrentLocation())
                                     .codeLineBuffer(lexer.getCharacterBuffer())
                                     .underlineFragment(varIdentifier)
                                     .build());
            return null;
        }
        lexer.getNextToken();

        return varStmt;
    }

    // func_def = identifier, "(", [parameters], ")", code_block;
    protected boolean parseFunDef(HashMap<String, FuncDef> functions) {
        if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
            return false;
        }

        var funIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (functions.containsKey(funIdentifier)) {
            reportAlreadyDeclared(funIdentifier);
            return false;
        }

        lexer.getNextToken();

        if (!lexer.getCurrentToken().getType().equals(TokenType.LPAREN)) {
            // TODO: Underlining for missing paren?
            reportUnexpectedToken();
            return false;
        }
        lexer.getNextToken();

        ArrayList<Parameter> params = parseParams();

        if (!lexer.getCurrentToken().getType().equals(TokenType.RPAREN)) {
            // TODO: Underlining for missing paren?
            reportUnexpectedToken();
            return false;
        }
        lexer.getNextToken();

        var codeBlock = parseCodeBlock();

        functions.put(funIdentifier, new FuncDef(funIdentifier, params, codeBlock));

        return true;
    }

    // code_block = "{", { non_ret_stmt | ["return"], expr, ["=", expr], ";" }, "}";
    protected CodeBLock parseCodeBlock() {
        if (!lexer.getCurrentToken().getType().equals(TokenType.LBRACE)) {
            // TODO: Underlining for missing LBRACE?
            reportUnexpectedToken();
        }
        lexer.getNextToken();

        ArrayList<Expression> statementsAndExpressions = new ArrayList<>();

        HashMap<String, VarStmt> variables = new HashMap<>();

        while (parseNonRetStmt(variables, statementsAndExpressions) || parseExprInsideCodeBlock(
            statementsAndExpressions)) {
        }


        if (!lexer.getCurrentToken().getType().equals(TokenType.RBRACE)) {
            // TODO: Underlining for missing RBRACE?
            reportUnexpectedToken();
        }
        lexer.getNextToken();

        return new CodeBLock(statementsAndExpressions);
    }

    // ["return"], expr, ["=", expr], ";"
    protected boolean parseExprInsideCodeBlock(ArrayList<Expression> statementsAndExpressions) {
        var isReturn = false;

        if (lexer.getCurrentToken().getType().equals(TokenType.RETURN_KEYWORD)) {
            isReturn = true;
            lexer.getNextToken();
        }

        var expression = parseExpr();
        if (expression == null) {
            return false;
        }

        if (lexer.getCurrentToken().getType().equals(TokenType.ASSIGNMENT)) {
            lexer.getNextToken();
            var assignExpr = parseExpr();
            if (assignExpr == null) {
                reportUnexpectedToken();
            } else {
                expression = new AssignmentExpression(expression, assignExpr);
            }
        }

        expression.setReturn(isReturn);

        statementsAndExpressions.add(expression);

        if (!lexer.getCurrentToken().getType().equals(TokenType.SEMICOLON)) {
            reportUnexpectedToken();
        }
        lexer.getNextToken();

        return true;
    }

    // expr = or_op_arg, { "or", or_op_arg };
    protected Expression parseExpr() {
        var left = parseOrOpArg();
        if (left == null) return null;

        while (lexer.getCurrentToken().getType().equals(TokenType.OR_KEYWORD)) {
            lexer.getNextToken();
            var right = parseOrOpArg();
            if (right == null) {
                reportUnexpectedToken();
                continue;
            }
            left = new OrExpression(left, right);
        }
        return left;
    }

    // or_op_arg = and_op_arg, { "and", and_op_arg };
    protected Expression parseOrOpArg() {
        var left = parseAndOpArg();
        if (left == null) return null;

        while (lexer.getCurrentToken().getType().equals(TokenType.AND_KEYWORD)) {
            lexer.getNextToken();
            var right = parseAndOpArg();
            if (right == null) {
                reportUnexpectedToken();
                continue;
            }
            left = new OrOpArg(left, right);
        }
        return left;
    }

    private static final Set<TokenType> relOp = new HashSet<>(
        Arrays.asList(TokenType.EQ, TokenType.NE, TokenType.LT, TokenType.LE, TokenType.GT, TokenType.GE));

    // and_op_arg = rel_op_arg, [rel_operator, rel_op_arg];
    protected Expression parseAndOpArg() {
        var left = parseRelOpArg();
        if (left == null) return null;

        if (relOp.contains(lexer.getCurrentToken().getType())) {
            var operatorToken = lexer.getCurrentToken().getType();
            lexer.getNextToken();
            var right = parseRelOpArg();
            if (right == null) {
                reportUnexpectedToken();
                return left;
            }
            left = switch (operatorToken) {
                case EQ -> new EqRelOpArg(left, right);
                case NE -> new NeRelOpArg(left, right);
                case LT -> new LtRelOpArg(left, right);
                case LE -> new LeRelOpArg(left, right);
                case GT -> new GtRelOpArg(left, right);
                case GE -> new GeRelOpArg(left, right);
                default -> throw new IllegalStateException(operatorToken.toString());
            };
        }

        if (relOp.contains(lexer.getCurrentToken().getType())) {
            reportUnsupportedChaining();
            // consume all unsupported chains (e.g.: a > b > c)
            while (relOp.contains(lexer.getCurrentToken().getType()) || parseRelOpArg() != null) {
                if (relOp.contains(lexer.getCurrentToken().getType())) {
                    lexer.getNextToken();
                }
            }
            return left;
        }

        return left;
    }

    private static final Set<TokenType> addOp = new HashSet<>(Arrays.asList(TokenType.PLUS, TokenType.MINUS));

    // rel_op_arg = term, { add_op, term };
    protected Expression parseRelOpArg() {
        var left = parseTerm();
        if (left == null) return null;

        while (addOp.contains(lexer.getCurrentToken().getType())) {
            var operatorToken = lexer.getCurrentToken().getType();
            lexer.getNextToken();
            var right = parseTerm();
            if (right == null) {
                reportUnexpectedToken();
                continue;
            }
            left = switch (operatorToken) {
                case PLUS -> new AdditionTerm(left, right);
                case MINUS -> new SubtractionTerm(left, right);
                default -> throw new IllegalStateException(operatorToken.toString());
            };
        }
        return left;
    }

    private static final Set<TokenType> multOp = new HashSet<>(Arrays.asList(TokenType.ASTERISK, TokenType.SLASH));

    // term = factor, { mult_op, factor };
    protected Expression parseTerm() {
        var left = parseFactor();
        if (left == null) return null;

        while (multOp.contains(lexer.getCurrentToken().getType())) {
            var operatorToken = lexer.getCurrentToken().getType();
            lexer.getNextToken();
            var right = parseFactor();
            if (right == null) {
                reportUnexpectedToken();
                continue;
            }
            left = switch (operatorToken) {
                case ASTERISK -> new MultiplicationFactor(left, right);
                case SLASH -> new DivisionFactor(left, right);
                default -> throw new IllegalStateException(operatorToken.toString());
            };
        }
        return left;
    }

    // factor = ["not" | "-"], (factor_inner | "(", expr, ")"), ["as", (type | class_id)];
    protected Expression parseFactor() {
        boolean isNegated = false;
        if (lexer.getCurrentToken().getType().equals(TokenType.MINUS) ||
            lexer.getCurrentToken().getType().equals(TokenType.NOT_KEYWORD)) {
            isNegated = true;
            lexer.getNextToken();
        }

        var factor = parseFactorInner();
        if (factor == null) {
            factor = parseExprParenthesized();
        }

        // TODO: ["as", (type | class_id)]

        if (isNegated) {
            factor = new NegatedExpression(factor);
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
        if (lexer.getCurrentToken().getType().equals(TokenType.FLOAT_NUMBER)) {
            var floatToken = (FloatToken) lexer.getCurrentToken();
            lexer.getNextToken();
            return new FloatConstantExpr(floatToken.getValue());
        }
        return null;
    }

    protected IntegerConstantExpr parseIntegerConstant() {
        if (lexer.getCurrentToken().getType().equals(TokenType.INTEGER_NUMBER)) {
            var integerToken = (IntegerToken) lexer.getCurrentToken();
            lexer.getNextToken();
            return new IntegerConstantExpr(integerToken.getValue());
        }
        return null;
    }


    protected Expression parseObjAccess() {
        // TODO: parseObjAccess
        return null;
    }

    protected Expression parseString() {
        // TODO: parseString
        return null;
    }

    protected Expression parseClassInit() {
        // TODO: parseClassInit
        return null;
    }

    // "(", expr, ")"
    protected Expression parseExprParenthesized() {
        if (!lexer.getCurrentToken().getType().equals(TokenType.LBRACE)) {
            return null;
        }
        lexer.getNextToken();
        var expr = parseExpr();
        if (expr == null) {
            reportUnexpectedToken();
            return null;
        }
        lexer.getNextToken();
        if (!lexer.getCurrentToken().getType().equals(TokenType.RBRACE)) {
            reportUnexpectedToken();
        }
        return expr;
    }

    // non_ret_stmt = var_stmt | cond_stmt | while_stmt | for_stmt | switch_stmt;
    protected boolean parseNonRetStmt(
        HashMap<String, VarStmt> variables, ArrayList<Expression> statementsAndExpressions
    ) {
        Expression nonRetStmt = parseVarStmt(variables);
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
    protected Expression parseConditionalStmt(){
        // TODO: parseConditionalStmt
        return null;
    }

    // while_stmt = "while", "(", expr, ")", code_block;
    protected Expression parseWhileStmt(){
        // TODO: parseWhileStmt
        return null;
    }

    // for_stmt = "for", "(", identifier, "in", expr, ")", code_block;
    protected Expression parseForStmt(){
        // TODO: parseForStmt
        return null;
    }

    // switch_stmt = "switch", "(", (expr), ")", "{", { (type | class_id | "default"), "->", code_block } ,"}";
    protected Expression parseSwitchStmt(){
        // TODO: parseSwitchStmt
        return null;
    }


    // parameters = identifier, { ",", identifier };
    protected ArrayList<Parameter> parseParams() {
        if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
            return new ArrayList<>();
        }
        ArrayList<Parameter> params = new ArrayList<>();
        params.add(new Parameter(((StringToken) lexer.getCurrentToken()).getValue()));
        lexer.getNextToken();
        while (lexer.getCurrentToken().getType().equals(TokenType.COMMA)) {
            lexer.getNextToken();
            if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
                reportUnexpectedToken();
                continue;
            }
            params.add(new Parameter(((StringToken) lexer.getCurrentToken()).getValue()));
            lexer.getNextToken();
        }
        return params;
    }

    private void reportAlreadyDeclared(String identifier) {
        errorModule.addError(ErrorElement.builder()
                                 .errorType(ErrorType.ALREADY_DECLARED)
                                 .location(lexer.getCurrentLocation())
                                 .codeLineBuffer(lexer.getCharacterBuffer())
                                 .underlineFragment(identifier)
                                 .build());
    }

    @SuppressWarnings("SameParameterValue")
    private void reportUnexpectedToken(String underline, String explanation) {
        errorModule.addError(ErrorElement.builder()
                                 .errorType(ErrorType.UNEXPECTED_TOKEN)
                                 .location(lexer.getCurrentLocation())
                                 .underlineFragment(underline)
                                 .explanation(explanation)
                                 .codeLineBuffer(lexer.getCharacterBuffer())
                                 .build());
    }

    @SuppressWarnings({"SameParameterValue", "unused"})
    private void reportUnexpectedToken(String underline) {
        errorModule.addError(ErrorElement.builder()
                                 .errorType(ErrorType.UNEXPECTED_TOKEN)
                                 .location(lexer.getCurrentLocation())
                                 .underlineFragment(underline)
                                 .codeLineBuffer(lexer.getCharacterBuffer())
                                 .build());
    }

    private void reportUnexpectedToken() {
        errorModule.addError(ErrorElement.builder()
                                 .errorType(ErrorType.UNEXPECTED_TOKEN)
                                 .location(lexer.getCurrentLocation())
                                 .codeLineBuffer(lexer.getCharacterBuffer())
                                 .build());
    }

    private void reportUnsupportedChaining() {
        errorModule.addError(ErrorElement.builder()
                                 .errorType(ErrorType.UNSUPPORTED_CHAINING)
                                 .location(lexer.getCurrentLocation())
                                 .codeLineBuffer(lexer.getCharacterBuffer())
                                 .build());
    }
}
