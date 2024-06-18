package com.example.laith_1200610_compiler;// Syntax Analyzer(Parser).
// Made by Laith Ghnemat 1200610.

/* This is the Syntax Analyzer class(Parser), which takes the tokens from Lexical Analyzer(Scanner) and
   compare them with the terminals in the stack. First, we push the non-terminal "module-decl", which is the
   starting symbol, into the stack. If the stack.peek() was non-terminal, then we pop it from the stack, and
   compensate for its value by its production rule from the grammars. Otherwise, the stack.peek() will be
   terminal, so we will compare it with the token that came from the scanner. If the stack.peek() suits the
   token, then we will pop from the stack and get the next token from the scanner. Otherwise, there will be
   an error in the syntax(in the parser), and so that the program will print this error with the line number.
 */



import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import java.util.Collections;   // To add elements to the sets(terminals, non-terminals and reserved words).
import java.util.HashSet;   // Sets for terminals, non-terminals and reserved words.
import java.util.Stack; // Stack structure will be used to store terminals and non-terminals.

public class SyntaxAnalyzer extends Application{
    /* This two dimension array represents the parsing table which consists of 40 rows and 49 columns. The
       first row represents the terminals and the first columns represents the nonTerminals. Each entity in
       the parsing table will contain the number of the production rule for the non-terminal that is on the
       peek of the stack and the token from the scanner.
     */
    private static final String[][] tableLL1 = new String[40][49];

    /* This stack will be used to store terminals and non-terminals to compare them with the tokens from the
       scanner(Lexical Analyzer).
     */
    public static Stack<String> stack = new Stack<>();

    /* This array contains a subset of the productions rules for MODULA-2 language. These production rules
       will be compensated by taking their indicators from the parsing table, and push them into the stack
       from the last to the first. For example, if the index was 2, then the production rule will be
       "module name ;", and so we will pop the non-terminal which has been derived and push ";", "name" and
       "module" respectively into the stack and so on.
     */
    private static final String [] productionRules = {"",
            "module-heading declarations block name .", "module name ;",
            "begin stmt-list end", "const-decl var-decl procedure-decl",
            "const const-list", "",
            "name = value ; const-list", "",
            "var var-list", "",     // 10
            "var-item ; var-list", "",
            "name-list : data-type", "name more-names",
            ", name-list", "",
            "integer", "real",
            "char", "procedure-heading declarations block name ; procedure-decl", // 20
            "", "procedure name ;",
            "statement ; stmt-list", "",
            "ass-stmt", "read-stmt",
            "write-stmt", "if-stmt",
            "while-stmt", "loop-stmt",      // 30
            "exit-stmt", "call-stmt",
            "block", "",
            "name := exp", "term exp-prime",
            "add-oper term exp-prime", "",
            "factor term-prime", "mul-oper factor term-prime",  // 40
            "", "( exp )",
            "name-value", "+",
            "-", "*",
            "/", "mod",
            "div", "readint ( name-list )",     // 50
            "readreal ( name-list )", "readchar ( name-list )",
            "readln", "writeint ( write-list )",
            "writereal ( write-list )", "writechar ( write-list )",
            "writeln", "write-item more-write-value",
            ", write-list", "",     // 60
            "name", "value",
            "if condition then stmt-list else-part end", "else stmt-list",
            "", "while condition do stmt-list end",
            "loop stmt-list until condition", "exit",
            "call name", "name-value relational-oper name-value",       // 70
            "=", "|=",
            "<", "<=",
            ">", ">=",
            "name", "value",
            "integer-value", "real-value"       // 80
    };
    // ______________________________________________________________________________________________________
    /* This method will take values of type String as input and add them to a set using "Collections.addAll",
       and return this set. We have three sets: terminals, non-terminals and reserved words.
     */
    private static HashSet<String> createSet(String... values) {
        HashSet<String> set = new HashSet<>(values.length);
        Collections.addAll(set, values);
        return set;
    }
    // ______________________________________________________________________________________________________
    /* This is a set of terminals, that we can't derive them. If the peek of the stack was an element of this
       set, then we will compare it with the current token from the Lexical Analyzer(Scanner). If they are
       proportional to each other, then we will pop from the stack and go to the next token from the Lexical
       Analyzer(Scanner). Otherwise, we will print an error for this invalid token with the line number.
     */
    private static final HashSet<String> terminals = createSet("module", "begin", "end", "const",
            "var", "integer", "real", "char", "procedure", "mod", "div", "readint", "readreal", "readchar",
            "readln", "writeint", "writereal", "writechar", "writeln", "if", "then", "else", "while", "do",
            "loop", "until", "exit", "call", ".", ";", "=", ":", ",", ":=", "(", ")", "+", "-", "*", "/",
            "|=", "<", "<=", ">", ">=", "", "name", "integer-value", "real-value");
    // ______________________________________________________________________________________________________
    /* This is a set of non-terminals, that we can derive them. If the peek of the stack was an element of
       this set, then we will pop from the stack, and compensate for its value from the production rules and
       push the production rule into the stack from the last to the first, and so on.
     */
    private static final HashSet<String> nonTerminals = createSet("module-decl", "module-heading",
            "block", "declarations", "const-decl", "const-list", "var-decl", "var-list", "var-item",
            "name-list", "more-names", "data-type", "procedure-decl", "procedure-heading", "stmt-list",
            "statement", "ass-stmt", "exp", "exp-prime", "term", "term-prime", "factor", "add-oper",
            "mul-oper", "read-stmt", "write-stmt", "write-list", "more-write-value", "write-item",
            "if-stmt", "else-part", "while-stmt", "loop-stmt", "exit-stmt", "call-stmt", "condition",
            "relational-oper", "name-value", "value");
    // ______________________________________________________________________________________________________
    /* This is a set of the reserved words in MODULA-2 language, that we can't name a variable by any of
       these names.
     */
    private static final HashSet<String> reservedWords = createSet("module", "begin", "end", "const",
            "var", "integer", "real", "char", "procedure", "mod", "div", "readint", "readreal", "readchar",
            "readln", "writeint", "writereal", "writechar", "writeln", "if", "then", "else", "while", "do",
            "loop", "until", "exit", "call");
    // ______________________________________________________________________________________________________
    /* In this method, we will fill the parsing table. The first column(index --> 0) contains the
       non-terminals, the first row(index --> 0) contains the terminals, and other entries contain the
       numbers of the production rules.
     */
    private static void fillTableLL1() {
        for (int i = 1; i <= 39; i++)       // Fill initial values for all the entries with zeros.
            for(int j = 1; j <= 48; j++)
                tableLL1[i][j] = "0";

        // Fill the non-terminals in the first column.
        tableLL1[1][0] = "module-decl";
        tableLL1[2][0] = "module-heading";
        tableLL1[3][0] = "block";
        tableLL1[4][0] = "declarations";
        tableLL1[5][0] = "const-decl";
        tableLL1[6][0] = "const-list";
        tableLL1[7][0] = "var-decl";
        tableLL1[8][0] = "var-list";
        tableLL1[9][0] = "var-item";
        tableLL1[10][0] = "name-list";
        tableLL1[11][0] = "more-names";
        tableLL1[12][0] = "data-type";
        tableLL1[13][0] = "procedure-decl";
        tableLL1[14][0] = "procedure-heading";
        tableLL1[15][0] = "stmt-list";
        tableLL1[16][0] = "statement";
        tableLL1[17][0] = "ass-stmt";
        tableLL1[18][0] = "exp";
        tableLL1[19][0] = "exp-prime";
        tableLL1[20][0] = "term";
        tableLL1[21][0] = "term-prime";
        tableLL1[22][0] = "factor";
        tableLL1[23][0] = "add-oper";
        tableLL1[24][0] = "mul-oper";
        tableLL1[25][0] = "read-stmt";
        tableLL1[26][0] = "write-stmt";
        tableLL1[27][0] = "write-list";
        tableLL1[28][0] = "more-write-value";
        tableLL1[29][0] = "write-item";
        tableLL1[30][0] = "if-stmt";
        tableLL1[31][0] = "else-part";
        tableLL1[32][0] = "while-stmt";
        tableLL1[33][0] = "loop-stmt";
        tableLL1[34][0] = "exit-stmt";
        tableLL1[35][0] = "call-stmt";
        tableLL1[36][0] = "condition";
        tableLL1[37][0] = "relational-oper";
        tableLL1[38][0] = "name-value";
        tableLL1[39][0] = "value";
        // __________________________________________________________________________________________________
        // Fill the terminals in the first row.
        tableLL1[0][1] = "name";
        tableLL1[0][2] = "module";
        tableLL1[0][3] = "begin";
        tableLL1[0][4] = "end";
        tableLL1[0][5] = "const";
        tableLL1[0][6] = "var";
        tableLL1[0][7] = "integer";
        tableLL1[0][8] = "real";
        tableLL1[0][9] = "char";
        tableLL1[0][10] = "procedure";
        tableLL1[0][11] = "mod";
        tableLL1[0][12] = "div";
        tableLL1[0][13] = "readint";
        tableLL1[0][14] = "readreal";
        tableLL1[0][15] = "readchar";
        tableLL1[0][16] = "readln";
        tableLL1[0][17] = "writeint";
        tableLL1[0][18] = "writereal";
        tableLL1[0][19] = "writechar";
        tableLL1[0][20] = "writeln";
        tableLL1[0][21] = "if";
        tableLL1[0][22] = "then";
        tableLL1[0][23] = "else";
        tableLL1[0][24] = "while";
        tableLL1[0][25] = "do";
        tableLL1[0][26] = "loop";
        tableLL1[0][27] = "until";
        tableLL1[0][28] = "exit";
        tableLL1[0][29] = "call";
        tableLL1[0][30] = "integer-value";
        tableLL1[0][31] = "real-value";
        tableLL1[0][32] = ".";
        tableLL1[0][33] = ";";
        tableLL1[0][34] = "=";
        tableLL1[0][35] = ":";
        tableLL1[0][36] = ",";
        tableLL1[0][37] = ":=";
        tableLL1[0][38] = "(";
        tableLL1[0][39] = ")";
        tableLL1[0][40] = "+";
        tableLL1[0][41] = "-";
        tableLL1[0][42] = "*";
        tableLL1[0][43] = "/";
        tableLL1[0][44] = "|=";
        tableLL1[0][45] = "<";
        tableLL1[0][46] = "<=";
        tableLL1[0][47] = ">";
        tableLL1[0][48] = ">=";
        // __________________________________________________________________________________________________
        // module-decl
        tableLL1[1][2] = "1";           // module --> module-heading declarations block name .

        // module-heading
        tableLL1[2][2] = "2";           // module --> module name ;

        // block
        tableLL1[3][3] = "3";           // begin --> begin stmt-list end

        // declarations
        tableLL1[4][3] = "4";           // begin --> const-decl var-decl procedure-decl
        tableLL1[4][5] = "4";           // const --> const-decl var-decl procedure-decl
        tableLL1[4][6] = "4";           // var --> const-decl var-decl procedure-decl
        tableLL1[4][10] = "4";          // procedure --> const-decl var-decl procedure-decl

        // const-decl
        tableLL1[5][3] = "6";           // begin --> lambda
        tableLL1[5][5] = "5";           // const --> const const-list
        tableLL1[5][6] = "6";           // var --> lambda
        tableLL1[5][10] = "6";          // procedure --> lambda

        // const-list
        tableLL1[6][1] = "7";           // name --> name = value ; const-list
        tableLL1[6][3] = "8";           // begin --> lambda
        tableLL1[6][6] = "8";           // var --> lambda
        tableLL1[6][10] = "8";          // procedure --> lambda

        // var-decl
        tableLL1[7][3] = "10";          // begin --> lambda
        tableLL1[7][6] = "9";           // var --> var var-list
        tableLL1[7][10] = "10";         // procedure --> lambda

        // var-list
        tableLL1[8][1] = "11";          // name --> var-item ; var-list
        tableLL1[8][3] = "12";          // begin --> lambda
        tableLL1[8][10] = "12";         // procedure --> lambda

        // var-item
        tableLL1[9][1] = "13";          // name --> name-list : data-type

        // name-list
        tableLL1[10][1] = "14";         // name --> name more-names

        // more-names
        tableLL1[11][35] = "16";        // : --> lambda
        tableLL1[11][36] = "15";        // , --> , name-list
        tableLL1[11][39] = "16";        // ) --> lambda

        // data-type
        tableLL1[12][7] = "17";         // integer --> integer
        tableLL1[12][8] = "18";         // real --> real
        tableLL1[12][9] = "19";         // char --> char

        // procedure-decl
        tableLL1[13][3] = "21";         // begin --> lambda
        tableLL1[13][10] = "20";        // procedure --> procedure-heading declarations block name ; procedure-decl

        // procedure-heading
        tableLL1[14][10] = "22";        // procedure --> procedure name ;

        // stmt-list
        tableLL1[15][1] = "23";         // name --> statement ; stmt-list
        tableLL1[15][3] = "23";         // begin --> statement ; stmt-list
        tableLL1[15][4] = "24";         // end --> lambda
        tableLL1[15][13] = "23";        // readint --> statement ; stmt-list
        tableLL1[15][14] = "23";        // readreal --> statement ; stmt-list
        tableLL1[15][15] = "23";        // readchar --> statement ; stmt-list
        tableLL1[15][16] = "23";        // readln --> statement ; stmt-list
        tableLL1[15][17] = "23";        // writeint --> statement ; stmt-list
        tableLL1[15][18] = "23";        // writereal --> statement ; stmt-list
        tableLL1[15][19] = "23";        // writechar --> statement ; stmt-list
        tableLL1[15][20] = "23";        // writeln --> statement ; stmt-list
        tableLL1[15][21] = "23";        // if --> statement ; stmt-list
        tableLL1[15][23] = "24";        // else --> lambda
        tableLL1[15][24] = "23";        // while --> statement ; stmt-list
        tableLL1[15][26] = "23";        // loop --> statement ; stmt-list
        tableLL1[15][27] = "24";        // until --> lambda
        tableLL1[15][28] = "23";        // exit --> statement ; stmt-list
        tableLL1[15][29] = "23";        // call --> statement ; stmt-list
        tableLL1[15][33] = "23";        // ; --> statement ; stmt-list

        // statement
        tableLL1[16][1] = "25";         // name --> ass-stmt
        tableLL1[16][3] = "33";         // begin --> block
        tableLL1[16][13] = "26";        // readint --> read-stmt
        tableLL1[16][14] = "26";        // readreal --> read-stmt
        tableLL1[16][15] = "26";        // readchar --> read-stmt
        tableLL1[16][16] = "26";        // readln --> read-stmt
        tableLL1[16][17] = "27";        // writeint --> write-stmt
        tableLL1[16][18] = "27";        // writereal --> write-stmt
        tableLL1[16][19] = "27";        // writechar --> write-stmt
        tableLL1[16][20] = "27";        // writeln --> write-stmt
        tableLL1[16][21] = "28";        // if --> if-stmt
        tableLL1[16][24] = "29";        // while --> while-stmt
        tableLL1[16][26] = "30";        // loop --> loop-stmt
        tableLL1[16][28] = "31";        // exit --> exit-stmt
        tableLL1[16][29] = "32";        // call --> call-stmt
        tableLL1[16][33] = "34";        // ; --> lambda

        // ass-stmt
        tableLL1[17][1] = "35";         // name --> name := exp

        // exp
        tableLL1[18][1] = "36";         // name --> term exp-prime
        tableLL1[18][30] = "36";        // integer-value --> term exp-prime
        tableLL1[18][31] = "36";        // real-value --> term exp-prime
        tableLL1[18][38] = "36";        // ( --> term exp-prime

        // exp-prime
        tableLL1[19][33] = "38";        // ; --> lambda
        tableLL1[19][39] = "38";        // ) --> lambda
        tableLL1[19][40] = "37";        // + --> add-oper term exp-prime
        tableLL1[19][41] = "37";        // - --> add-oper term exp-prime

        // term
        tableLL1[20][1] = "39";         // name --> factor term-prime
        tableLL1[20][30] = "39";        // integer-value --> factor term-prime
        tableLL1[20][31] = "39";        // real-value --> factor term-prime
        tableLL1[20][38] = "39";        // ( --> factor term-prime

        // term-prime
        tableLL1[21][11] = "40";        // mod --> mul-oper factor term-prime
        tableLL1[21][12] = "40";        // div --> mul-oper factor term-prime
        tableLL1[21][33] = "41";        // ; --> lambda
        tableLL1[21][39] = "41";        // ) --> lambda
        tableLL1[21][40] = "41";        // + --> lambda
        tableLL1[21][41] = "41";        // - --> lambda
        tableLL1[21][42] = "40";        // * --> mul-oper factor term-prime
        tableLL1[21][43] = "40";        // / --> mul-oper factor term-prime

        // factor
        tableLL1[22][1] = "43";         // name --> name-value
        tableLL1[22][30] = "43";        // integer-value --> name-value
        tableLL1[22][31] = "43";        // real-value --> name-value
        tableLL1[22][38] = "42";        // ( --> ( exp )

        // add-oper
        tableLL1[23][40] = "44";        // + --> +
        tableLL1[23][41] = "45";        // - --> -

        // mul-oper
        tableLL1[24][11] = "48";        // mod --> mod
        tableLL1[24][12] = "49";        // div --> div
        tableLL1[24][42] = "46";        // * --> *
        tableLL1[24][43] = "47";        // / --> /

        // read-stmt
        tableLL1[25][13] = "50";        // readint --> readint ( name-list )
        tableLL1[25][14] = "51";        // readreal --> readreal ( name-list )
        tableLL1[25][15] = "52";        // readchar --> readchar ( name-list )
        tableLL1[25][16] = "53";        // readln --> readln

        // write-stmt
        tableLL1[26][17] = "54";        // writeint --> writeint ( write-list )
        tableLL1[26][18] = "55";        // writereal --> writereal ( write-list )
        tableLL1[26][19] = "56";        // writechar --> writechar ( write-list )
        tableLL1[26][20] = "57";        // writeln --> writeln

        // write-list
        tableLL1[27][1] = "58";         // name --> write-item more-write-value
        tableLL1[27][30] = "58";        // integer-value --> write-item more-write-value
        tableLL1[27][31] = "58";        // real-value --> write-item more-write-value

        // more-write-value
        tableLL1[28][36] = "59";        // , --> , write-list
        tableLL1[28][39] = "60";        // ) --> lambda

        // write-item
        tableLL1[29][1] = "61";         // name --> name
        tableLL1[29][30] = "62";        // integer-value --> value
        tableLL1[29][31] = "62";        // real-value --> value

        // if-stmt
        tableLL1[30][21] = "63";        // if --> if condition then stmt-list else-part end

        // else-part
        tableLL1[31][4] = "65";         // end --> lambda
        tableLL1[31][23] = "64";        // else --> else stmt-list

        // while-stmt
        tableLL1[32][24] = "66";        // while --> while condition do stmt-list end

        // loop-stmt
        tableLL1[33][26] = "67";        // loop --> loop stmt-list until condition

        // exit-stmt
        tableLL1[34][28] = "68";        // exit --> exit

        // call-stmt
        tableLL1[35][29] = "69";        // call --> call name

        // condition
        tableLL1[36][1] = "70";         // name --> name-value relational-oper name-value
        tableLL1[36][30] = "70";        // integer-value --> name-value relational-oper name-value
        tableLL1[36][31] = "70";        // real-value --> name-value relational-oper name-value

        // relational-oper
        tableLL1[37][34] = "71";        // = --> =
        tableLL1[37][44] = "72";        // |= --> |=
        tableLL1[37][45] = "73";        // < --> <
        tableLL1[37][46] = "74";        // <= --> <=
        tableLL1[37][47] = "75";        // > --> >
        tableLL1[37][48] = "76";        // >= --> >=

        // name-value
        tableLL1[38][1] = "77";         // name --> name
        tableLL1[38][30] = "78";        // integer-value --> value
        tableLL1[38][31] = "78";        // real-value --> value

        // value
        tableLL1[39][30] = "79";        // integer-value --> integer-value
        tableLL1[39][31] = "80";        // real-value --> real-value
    }
    // ______________________________________________________________________________________________________
    /* This method takes a LexicalAnalyzer(Scanner) object and a string token(the first token from the
       scanner) as inputs, and returns a string which is the parser result. If MODULA-2 file is correct
       according to the grammars, then the output is "No errors in this file". If there is an error in the
       file(if the tokens don't match with MODULA-2 grammars), then this method will give the error and the
       line number as outputs. This operation used a stack structure(push, pull and peek methods), and we
       push "module-decl" as a starting symbol. If the peek of the stack was non-terminal, then we pop from
       the stack and substitute the value of its derivative from the last to the first. Otherwise, the peek
       of the stack will be terminal. If the peek of the stack was "name", then we check if the token is a
       valid name or not, if the token is an invalid name, then we return an error message, and if the token
       is a valid name, then we pop from the stack and get the next token. If the peek of the stack was
       "integer-value", then we check if the token is a valid integer value or not, if the token is an
       invalid integer value, then we return an error message, and if the token is a valid integer value,
       then we pop from the stack and get the next token. If the peek of the stack was "real-value", then we
       check if the token is a valid real value or not, if the token is an invalid real value, then we return
       an error message, and if the token is a valid real value, then we pop from the stack and get the next
       token. If the token was the same as the peek of the stack, then we pop from the stack and get the next
       token. Otherwise, we return an error message.
     */

    private static String checkValidation(LexicalAnalyzer remainingInput, String token){
        stack.clear();
        stack.push("module-decl");  // Push module-decl(the starting symbol) into the stack.

        /* If the first token doesn't equal to "module", then return an error message. This is because
           MODULA-2 files should start with the reserved word "module".
         */
        if(!token.equals("module"))
            return "Missed required token \"module\" at the beginning of this file at line: " +
                    remainingInput.getNumOfErrorLine();

        /* The process will continue until the stack becomes empty, or when there is an error in the file(if
           the tokens from the scanner don't match with the production rules), then we will exit from this
           loop and return an error message.
         */
        while (!stack.isEmpty()) {      // The stack contains terminals and non-terminals.
            /* If the peek of the stack is terminal, then it will be one of these four options: either
               "name", "integer-value", "real-value", or equals to the token. If it was one of these: "name",
               "integer-value", or "real-value", then we will check the token validation(valid name, valid
               integer, or valid real), and if the token was valid, then we will pop from the stack and get
               the next token, and if it was invalid, then we will return an error message. Otherwise, we
               will check if the peek of the stack equals to the token, if they are equal, then we will pop
               from the stack and get the next token, and if they are not equal, then we will return an error
               message.
             */
            if (terminals.contains(stack.peek())) {
                // The first option: name value.
                if (stack.peek().equals("name")) {
                    // If the token was invalid name, then return an error message.
                    if (!isValidName(token))
                        return "Syntax Error, expected a valid name instead of \"" + token + "\" at line " +
                                remainingInput.getNumOfErrorLine();
                        // If the token was a valid name, then we will pop from the stack and get the next token.
                    else {
                        stack.pop();
                        token = remainingInput.getNextToken();
                    }
                }
                // The second option: integer value.
                else if (stack.peek().equals("integer-value")) {
                    // If the token was invalid integer value, then return an error message.
                    if (!isValidInteger(token))
                        return "Syntax Error, expected a valid integer value instead of \"" + token + "\" at"
                                + " line " + remainingInput.getNumOfErrorLine();
                    /* If the token was a valid integer, then we will pop from the stack and get the next
                       token.
                     */
                    else {
                        stack.pop();
                        token = remainingInput.getNextToken();
                    }
                }
                // The third option: real value.
                else if (stack.peek().equals("real-value")) {
                    // If the token was invalid real value, then return an error message.
                    if (!isValidReal(token))
                        return "Syntax Error, expected a valid real value instead of \"" + token + "\" at" +
                                remainingInput.getNumOfErrorLine();
                        // If the token was a valid real, then we will pop from the stack and get the next token.
                    else {
                        stack.pop();
                        token = remainingInput.getNextToken();
                    }
                }
                // The fourth option: the peek of the stack equals to the token.
                else{
                    // If they aren't equal, then return an error message.
                    if (!stack.peek().equalsIgnoreCase(token))
                        return "Syntax Error, expected \"" + stack.peek() + "\" instead of \"" + token +
                                "\" at line " + remainingInput.getNumOfErrorLine();
                        // If they are equal, then we will pop from the stack and get the next token.
                    else{
                        stack.pop();
                        token = remainingInput.getNextToken();
                    }
                }
            }
            /* ##############################################################################################
               ##############################################################################################
               ##############################################################################################
               ##############################################################################################
               ##############################################################################################
             */
            /* If the peek of the stack is non-terminal, then we will find its row in the parser table, and
               then we will search on the column(terminal) that matches with the token. If we find it, then
               we will pop from the stack, and get the production rule(intersection of row and column), and
               split it by the space, and push its splits into the stack from the last to the first(if the
               rule equals to lambda, then we don't push anything into the stack). If we don't find the
               column(terminal) that matches with the token, then the column is either name, integer or real,
               so we will pop from the stack and get the production rule(intersection of row and column), and
               split it by the space, and push its splits into the stack from the last to the first(if the
               rule equals to lambda, then we don't push anything into the stack).
             */

            else{       // If the peek of the stack is non-terminal.
                for (int i = 1; i <= 39; i++) {  // Iterate on rows(non-terminals) in the parser table.
                    if (stack.peek().equals(tableLL1[i][0])) {    // If we find the non-terminal.
                        for (int j = 1; j <= 48; j++) { // Iterate on columns(terminals) in the parser table.
                            /* If the terminal equals to this token and there is a production rule at this
                               entry.
                             */
                            if (tableLL1[0][j].equals(token)){
                                if(Integer.parseInt(tableLL1[i][j]) == 0)
                                    return "The peek of the stack is:\" " + stack.peek() +
                                            "\" and the token \"" + token + "\" doesn't match with it.";

                                /* We will pop the non-terminal from the stack and get the production rule
                                   (intersection of the row and the column) which will be split by the space
                                   and push its components into the stack separately from the last to the
                                   first(if the rule equals to lambda, then we don't push anything into the
                                   stack).
                                 */
                                pushRuleDerivation(i, j);
                                break;
                            }
                        }

                        // The token is either name value, integer value, or real value.
                        if (isValidName(token)){
                            /* We will pop the non-terminal from the stack and get the production rule
                               (intersection of the row and the column) which will be split by the space
                               and push its components into the stack separately from the last to the
                               first(if the rule equals to lambda, then we don't push anything into the
                               stack).
                             */
                            pushRuleDerivation(i, 1);
                            break;
                        }

                        else if (isValidInteger(token)){
                            /* We will pop the non-terminal from the stack and get the production rule
                               (intersection of the row and the column) which will be split by the space
                               and push its components into the stack separately from the last to the
                               first(if the rule equals to lambda, then we don't push anything into the
                               stack).
                             */
                            pushRuleDerivation(i, 30);
                            break;
                        }

                        else if(isValidReal(token)){
                            /* We will pop the non-terminal from the stack and get the production rule
                               (intersection of the row and the column) which will be split by the space
                               and push its components into the stack separately from the last to the
                               first(if the rule equals to lambda, then we don't push anything into the
                               stack).
                             */
                            pushRuleDerivation(i, 31);
                            break;
                        }
                    }
                }
            }
        }

        // The stack is empty, but the tokens from the scanner aren't(if there is an extra token).
        if(token != null)
            return "Syntax Error, unexpected token \"" + token + "\" at line " +
                    remainingInput.getNumOfErrorLine();

        return "Successful parsing for this file.";        // Successful parsing.
    }
    // ______________________________________________________________________________________________________
    /* This method takes an input of type string, and returns if this string is a valid name value or not.
       The name value to be valid should start with a letter, and other characters should be letters or
       digits, and shouldn't belong to the reserved words set. So, if the input string starts with a digit or
       contains any special symbol(for example: ';', '.', ':', '#', '$', .etc), or belongs to the reserved
       words set, then this string is an invalid name.
     */
    public static boolean isValidName(String name){
        if(reservedWords.contains(name))    // If the name is a reserved word, then it's an invalid name.
            return false;

        // If the name is empty, or if the first character isn't a letter, then it's an invalid name.
        if(name.isEmpty() || !Character.isLetter(name.charAt(0)))
            return false;

        // If any character in the name is neither letter nor digit, then the name is invalid.
        for (int i = 1; i < name.length(); i++){
            if (!Character.isLetter(name.charAt(i)) && !Character.isDigit(name.charAt(i)))
                return false;
        }
        return true;    // The input string is a valid name.
    }
    // ______________________________________________________________________________________________________
    /* This method takes an input of type string, and returns if this string is a valid integer value or not.
       The integer value to be valid should only have characters of type digits. Otherwise, the integer will
       be invalid(if it contains at least one character that is not a digit(it's a letter or a special
       symbol)).
     */
    private static boolean isValidInteger(String integerValue){
        if(integerValue.isEmpty())      // If the integer value is empty, then it's an invalid integer value.
            return false;
        /* If the integer value contains any character that is not a digit(it's a letter or a special
           symbol), then the integer value is invalid.
         */
        for (int i = 0; i < integerValue.length(); i++){
            if(!Character.isDigit(integerValue.charAt(i)))
                return false;
        }
        return true;        // The integer value is valid.
    }
    // ______________________________________________________________________________________________________
    /* This method takes an input of type string, and returns if this string is a valid real value or not.
       The real value to be valid should contain only digits, and exactly one decimal point, also the first
       character and the last character should be digits(the real value should start and end with a digit).
       Otherwise, the real value is invalid.
     */
    private static boolean isValidReal(String realValue){
        // This variable will be used to ensure that we have exactly one decimal point in the real value.
        int numberOfDecimalPoints = 0;

        /* If the real value is empty, or if the first character isn't a digit, or if the last character
           isn't a digit, then the real value is invalid.
         */
        if(realValue.isEmpty() || !Character.isDigit(realValue.charAt(0)) ||
                !Character.isDigit(realValue.charAt(realValue.length() - 1)))
            return false;

        /* If any character in the real value is neither digit nor decimal point, then the real value is
           invalid. Also, inside this loop and if the character is decimal point, then increment the number
           of decimal points by one, because if it doesn't equal one, then the real value is invalid.
         */
        for (int i = 1; i < realValue.length() - 1; i++){
            if(!Character.isDigit(realValue.charAt(i)) && realValue.charAt(i) != '.')
                return false;
            // If the character is decimal point, then increment the number of decimal points by one.
            if(realValue.charAt(i) == '.')
                numberOfDecimalPoints++;
        }
        // If the number of decimal points equals one, then the real value is valid. Otherwise, it's invalid.
        return numberOfDecimalPoints == 1;
    }
    // ______________________________________________________________________________________________________
    /* This method takes the row and the column in the parser table, we pop from the stack and get the
       production rule(intersection of the row and the column) which will split by the space and push its
       components separately from the last to the first into the stack.
     */
    private static void pushRuleDerivation(int row, int column){
        stack.pop();        // Remove the non-terminal from the stack.
        // Get the production rule from the parsing table.

        String rule = productionRules[Integer.parseInt(tableLL1[row][column])];

        String[] orders = rule.split(" ");  // Split the rule by spaces to push its components separately.

        if (!rule.isEmpty())
            for (int k = orders.length - 1; k >= 0; k--)
                stack.push(orders[k].trim());
    }
    // ______________________________________________________________________________________________________
    @Override
    public void start(Stage primaryStage) throws IOException {
        Label scannerLabel = new Label("Scanner Results(Tokens):");
        Label parserLabel = new Label("Parser Result:");
        Label laithLabel = new Label("Made by: Laith Ghnemat\n1200610");

        Font font = Font.font("Courier New", FontWeight.BOLD, 36);

        scannerLabel.setTextFill(Color.BLACK);
        scannerLabel.setFont(font);
        parserLabel.setTextFill(Color.BLACK);
        parserLabel.setFont(font);
        laithLabel.setTextFill(Color.BLACK);
        laithLabel.setFont(font);

        Button chooseFileButton = new Button("Choose File");
        chooseFileButton.setFont(font);
        chooseFileButton.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-radius: 5;");
        chooseFileButton.setTextFill(Color.RED);

        Button scannerButton = new Button("Show Scanner Result");
        scannerButton.setFont(font);
        scannerButton.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-radius: 5;");
        scannerButton.setTextFill(Color.RED);

        Button parserButton = new Button("Show Parser Result");
        parserButton.setFont(font);
        parserButton.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-radius: 5;");
        parserButton.setTextFill(Color.RED);

        TextArea scannerTextArea = new TextArea();
        scannerTextArea.setEditable(false);
        scannerTextArea.setPrefHeight(350);
        scannerTextArea.setPrefWidth(1000);
        scannerTextArea.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        TextArea parserTextArea = new TextArea();
        parserTextArea.setEditable(false);
        parserTextArea.setPrefHeight(350);
        parserTextArea.setPrefWidth(1000);
        parserTextArea.setFont(Font.font("Verdana", FontWeight.BOLD, 20));

        VBox buttonsVBox = new VBox();
        buttonsVBox.getChildren().addAll(chooseFileButton, scannerButton);
        buttonsVBox.setSpacing(15);
        buttonsVBox.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        HBox hBoxScanner = new HBox();
        hBoxScanner.getChildren().addAll(scannerTextArea, buttonsVBox);
        hBoxScanner.setSpacing(40);

        VBox vBoxParser = new VBox();
        vBoxParser.getChildren().addAll(parserButton, laithLabel);
        vBoxParser.setSpacing(15);
        vBoxParser.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        HBox hBoxParser = new HBox();
        hBoxParser.getChildren().addAll(parserTextArea, vBoxParser);
        hBoxParser.setSpacing(40);

        VBox vBox = new VBox();
        vBox.getChildren().addAll(scannerLabel, hBoxScanner, parserLabel, hBoxParser);
        vBox.setSpacing(5);
        vBox.setBackground(new Background(new BackgroundFill(Color.GREEN, CornerRadii.EMPTY, Insets.EMPTY)));

        Scene scene = new Scene(vBox, 800, 600);

        chooseFileButton.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Resource File");
            File file = fileChooser.showOpenDialog(primaryStage);

            scannerTextArea.setText("");
            parserTextArea.setText("");

            scannerButton.setOnAction(r -> {
                scannerTextArea.setText("");
                LexicalAnalyzer scanner = null;
                try {
                    scanner = new LexicalAnalyzer(file);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }

                String token = scanner.getNextToken();
                while (token != null && !token.isEmpty()){
                    scannerTextArea.appendText(token + "\n");
                    token = scanner.getNextToken();
                }
            });

            parserButton.setOnAction(s -> {
                parserTextArea.setText("");
                LexicalAnalyzer scanner = null;
                try {
                    scanner = new LexicalAnalyzer(file);
                } catch (FileNotFoundException ex) {
                    throw new RuntimeException(ex);
                }

                String token = scanner.getNextToken();
                fillTableLL1();

                parserTextArea.appendText(checkValidation(scanner, token));
            });

        });

        primaryStage.setTitle("Compiler Project LL1");
        primaryStage.setScene(scene);
        primaryStage.setFullScreen(true);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}