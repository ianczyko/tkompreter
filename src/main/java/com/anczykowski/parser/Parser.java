package com.anczykowski.parser;

import java.util.ArrayList;
import java.util.HashMap;

import com.anczykowski.errormodule.ErrorElement;
import com.anczykowski.errormodule.ErrorModule;
import com.anczykowski.errormodule.ErrorType;
import com.anczykowski.lexer.Lexer;
import com.anczykowski.lexer.StringToken;
import com.anczykowski.lexer.TokenType;
import com.anczykowski.parser.structures.ClassBody;
import com.anczykowski.parser.structures.ClassDef;
import com.anczykowski.parser.structures.CodeBLock;
import com.anczykowski.parser.structures.FuncDef;
import com.anczykowski.parser.structures.Parameter;
import com.anczykowski.parser.structures.Program;
import com.anczykowski.parser.structures.Statement;
import com.anczykowski.parser.structures.VarStmt;
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

        while (parseFunDef(methods) || parseVarStmt(attributes)) {
        }

        return new ClassBody(methods, attributes);
    }

    // var_stmt = "var", identifier, ["=", expr], ";";
    protected boolean parseVarStmt(HashMap<String, VarStmt> attributes) {
        if (!lexer.getCurrentToken().getType().equals(TokenType.VAR_KEYWORD)) {
            return false;
        }

        lexer.getNextToken();

        if (!lexer.getCurrentToken().getType().equals(TokenType.IDENTIFIER)) {
            reportUnexpectedToken("var", "var keyword must be followed by identifier");
            return false;
        }

        var varIdentifier = ((StringToken) lexer.getCurrentToken()).getValue();

        if (attributes.containsKey(varIdentifier)) {
            reportAlreadyDeclared(varIdentifier);
            return false;
        }

        attributes.put(varIdentifier, new VarStmt(varIdentifier));

        lexer.getNextToken();

        // TODO: ["=", expr] part

        if (!lexer.getCurrentToken().getType().equals(TokenType.SEMICOLON)) {
            errorModule.addError(
                ErrorElement.builder()
                    .errorType(ErrorType.MISSING_SEMICOLON)
                    .location(lexer.getCurrentLocation())
                    .codeLineBuffer(lexer.getCharacterBuffer())
                    .underlineFragment(varIdentifier)
                    .build()
            );
            return false;
        }
        lexer.getNextToken();

        return true;
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
        // TODO: CodeBLock parsing

        if (!lexer.getCurrentToken().getType().equals(TokenType.LBRACE)) {
            // TODO: Underlining for missing LBRACE?
            reportUnexpectedToken();
        }
        lexer.getNextToken();

        // TODO: { non_ret_stmt | ["return"], expr, ["=", expr], ";" }

        ArrayList<Statement> statements = new ArrayList<>();

        while (parseNonRetStmt(statements) || parseExprInsideCodeBlock(statements)) {
        }


        if (!lexer.getCurrentToken().getType().equals(TokenType.RBRACE)) {
            // TODO: Underlining for missing RBRACE?
            reportUnexpectedToken();
        }
        lexer.getNextToken();

        return new CodeBLock();
    }

    protected boolean parseExprInsideCodeBlock(ArrayList<Statement> statements) {
        // TODO: parseExprInsideCodeBlock
        return false;
    }

    protected boolean parseNonRetStmt(ArrayList<Statement> statements) {
        // TODO: parseNonRetStmt
        return false;
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
        errorModule.addError(
            ErrorElement.builder()
                .errorType(ErrorType.ALREADY_DECLARED)
                .location(lexer.getCurrentLocation())
                .codeLineBuffer(lexer.getCharacterBuffer())
                .underlineFragment(identifier)
                .build()
        );
    }

    @SuppressWarnings("SameParameterValue")
    private void reportUnexpectedToken(String underline, String explanation) {
        errorModule.addError(
            ErrorElement.builder()
                .errorType(ErrorType.UNEXPECTED_TOKEN)
                .location(lexer.getCurrentLocation())
                .underlineFragment(underline)
                .explanation(explanation)
                .codeLineBuffer(lexer.getCharacterBuffer())
                .build()
        );
    }

    private void reportUnexpectedToken() {
        errorModule.addError(
            ErrorElement.builder()
                .errorType(ErrorType.UNEXPECTED_TOKEN)
                .location(lexer.getCurrentLocation())
                .codeLineBuffer(lexer.getCharacterBuffer())
                .build()
        );
    }
}
