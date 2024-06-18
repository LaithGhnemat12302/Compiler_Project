// Lexical Analyzer(Scanner).
// Made by Laith Ghnemat 1200610.

/* This class is the Lexical Analyzer class(Scanner), which reads the file written in MODULA-2, and return
   the tokens token by token. This class will throw an illegal character exception if there is something
   that is not defined is MODULA-2 language. For example, if the file contains the symbol '#' which is not
   defined in this language, the Scanner will throw an illegal character exception at the line which
   contains this symbol. If the tokens are right, they will be sent to the Syntax Analyzer(Parser).
 */

package com.example.laith_1200610_compiler;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class LexicalAnalyzer {
    // This scanner will read the file line by line.
    private final Scanner scanner;

    /* This variable represents the line number in the read file. If the file has errors(whether lexical or
       syntax errors) this variable will be printed in the console to represent the number of the line which
       contains this error.
     */
    private int lineNumber;

    // This String variable represents each line that will be read from the read file.
    private String line = "";

    /* This index variable represents the index of each token in each line. For example, if the line was
       "module program;", index = 0 --> module, index = 1 --> program, index = 2 --> ;
     */
    private int tokenIndexInLine = -1;
    // ______________________________________________________________________________________________________
    /* MODULA-2 file will be passed to this constructor. The Lexical Analyzer(scanner) algorithm will read
       this file and return its tokens token by token.
     */
    public LexicalAnalyzer(File file) throws FileNotFoundException{
        scanner = new Scanner(file);
    }
    // ______________________________________________________________________________________________________
    // This method will return the tokens, token by token in order.
    public String getNextToken(){
        // If we had reached the end of the line(if we had reached the last token in this line).
        if(tokenIndexInLine == line.length() - 1){
            /* If we had reached the end of the file, then close the scanner &
               return null(no tokens are available).
             */
            if(!scanner.hasNextLine()){
                scanner.close();
                return null;    // No tokens are available.
            }

            /* Get the next line as long as it's not empty & not blank OR until we reached the end of the
               file, reset the tokenIndexInLine to -1 and increment the line number.
             */
            do{
                lineNumber++;       // Increment the line number.
                tokenIndexInLine = -1;         // reset tokenIndexInLine to -1 because we got a new line.
                line = scanner.nextLine();  // Scan the new line.
            } while (scanner.hasNextLine() && (line.isEmpty() || line.isBlank()));

            /* If the line is empty(if we had reached the end of the file), then close the scanner and return
               null.
             */
            if(line.isEmpty() || line.isBlank()){
                scanner.close();
                return null;    // No tokens are available.
            }
        }

        getNextChar();      // Get the next char.
        return getNewToken();     // Return the next new token.
    }
    // ______________________________________________________________________________________________________
    /* This method will increment the tokenIndexInLine to return the char at this index(return the next char)
       or return null if we had reached the end of a line.
     */
    private Character getNextChar(){
        if(tokenIndexInLine != line.length() - 1) {   // If we had not reached the end of the line.
            tokenIndexInLine++;     // Increment the tokenIndexInLine.
            return line.charAt(tokenIndexInLine);   // Return the char at this index(return the next char).
        }
        else    // If we had reached the end of the line then return null.
            return null;
    }
    // ______________________________________________________________________________________________________
    /* This method will go back to the previous char. For example, if the char was '=', we will return to the
       previous char, and it may be '|' or ':', and so that the token will be "|=" or ":=" (the token
       consists of more than one char). The same thing with the tokens: "<=", ">=".
     */
    private void getPreviousChar(){
        tokenIndexInLine--;     // Decrement tokenIndexInLine which will be the index of the previous char.
    }
    // ______________________________________________________________________________________________________
    /* This method will return the correct token. If the token consists of only one char then return it(for
       example, the tokens: '<', '>' and '='). And if it consists of more than one char(for example the
       tokens: "<=", ">=" and "|="), than return a function that has written specially for these tokens.
    */
    private String getNewToken() throws IllegalArgumentException {
        switch (line.charAt(tokenIndexInLine)) {    // The char at this index in this line.
            case ' ':
                return getNextToken();

            case ',':           // Return "," as a single token.
                return ",";

            case ';':           // Return ";" as a single token.
                return ";";

            case '+':           // Return "+" as a single token.
                return "+";

            case '-':           // Return "-" as a single token.
                return "-";

            case '*':           // Return "*" as a single token.
                return "*";

            case '/':           // Return "/" as a single token.
                return "/";

            case '(':           // Return "(" as a single token.
                return "(";

            case ')':           // Return ")" as a single token.
                return ")";

            case '=':           // Return "=" as a single token.
                return "=";

            case '.':           // Return "." as a single token.
                return ".";

            case '\t':      // Tap --> continue until reach the first token in this line.
                return getNextToken();

            /* If the char was ':', then check it's next char, if it was '=', then return ":=" as a single
               token. Otherwise, return ':' as a single token.
             */
            case ':':
                return colonSymbol();

            /* If the char was '>', then check it's next char, if it was '=', then return ">=" as a single
               token. Otherwise, return '>' as a single token.
             */
            case '>':
                return greaterThanSymbol();

            /* If the char was '<', then check it's next char, if it was '=', then return "<=" as a single
               token. Otherwise, return '<' as a single token.
             */
            case '<':
                return lessThanSymbol();

            /* If the char was '|', then check it's next char, if it was '=', then return "|=" as a single
               token. Otherwise, throw an Illegal Argument Exception(The char '|' must always be followed by
               '=' in MODULA-2 language).
             */
            case '|':
                return notEqualSymbol();

            default:    // Letter or digit for names and values.
                // If the char was letter, then return string name as a single token.
                if(Character.isLetter(line.charAt(tokenIndexInLine)))
                    return tokenName();
                    // If the char was digit, then return(integer or real) as a string as a single token.
                else if(Character.isDigit(line.charAt(tokenIndexInLine)))
                    return tokenNumber();
                    // If neither letter nor digit, then close the Scanner and throw Illegal Argument Exception.
                else {
                    scanner.close();    // Close the Scanner.
                    // Throw an Illegal Argument Exception at this char index, with the line number.
                    throw new IllegalArgumentException("Illegal Character Error: line " + lineNumber +
                            " has Illegal character '" + line.charAt(tokenIndexInLine) + "'");
                }
        }
    }
    // ______________________________________________________________________________________________________
    // This method will return a string name as a single token.
    private String tokenName(){
        // Get the first char in the name(which is a letter).
        Character currentCharacter = line.charAt(tokenIndexInLine);
        StringBuilder stringBuilder = new StringBuilder();  // Will be used to build the string name in it.

        /* Since the first character is letter, so we won't check it ==> we will use(do ... while) loop. This
           loop stops when the current char equals null(the end of the word), or it's neither letter nor
           digit(special symbol).
         */
        do{
            // Append to get the whole string name using the string builder.
            stringBuilder.append(currentCharacter);
            // After we had finished with the current char, now go to the next char.
            currentCharacter = getNextChar();
        } while (currentCharacter != null && Character.isLetterOrDigit(currentCharacter));

        /* If the current char was neither letter nor digit(if it was a special symbol like '.', ',', ';',
           .etc), then we will return the previous whole string name as a single token, and the special
           symbol will be taken in another token. So that, we will decrement the tokenIndexInLine to come
           back to the previous char. For example, if the string was "laith.", then we will return laith as
           a single token, but since we passed the symbol '.', so that we should decrement the
           tokenIndexInLine to come back to the previous char.
         */
        if(currentCharacter != null)
            getPreviousChar();

        return stringBuilder.toString();    // Return the whole string name using the string builder.
    }
    // ______________________________________________________________________________________________________
    // This method will return a string number(either integer or real) as a single token.
    private String tokenNumber(){
        // Get the first char in the number(which is a digit).
        Character currentCharacter = line.charAt(tokenIndexInLine);
        StringBuilder stringBuilder = new StringBuilder();  // Will be used to build the string name in it.
        boolean isRealFlag = false;     // This boolean flag will be used for real numbers.

        /* Since the first character is digit, so we won't check it ==> we will use(do ... while) loop. This
           loop stops when the current char equals null(the end of the number), or it's neither digit nor
           '.', or when the number has more than one decimal point.
         */
        do{
            if(currentCharacter == '.')      // If the current char was '.', then set the flag to be true.
                isRealFlag = true;
            // Append to get the whole string number using the string builder.
            stringBuilder.append(currentCharacter);
            // After we had finished with the current char, now go to the next char.
            currentCharacter = getNextChar();
        } while (currentCharacter != null &&
                (Character.isDigit(currentCharacter) || (!isRealFlag && currentCharacter == '.')));

        /* If the last char in the string builder(in the number) is not a digit(illegal char), then close
           the Scanner and throw an Illegal Argument Exception at this char index, with the line number.
         */
        if(!Character.isDigit(stringBuilder.charAt(stringBuilder.length() - 1))){
            scanner.close();
            throw new IllegalArgumentException("Illegal Character Error: line " + lineNumber +
                    " has Illegal token '" + stringBuilder + "'");
        }

        /* If the current char was not digit(if it was a letter or a special symbol like '=', '>', '<',
           .etc), then we will return the previous whole string number as a single token, and the current
           char will be taken in another token. So that, we will decrement the tokenIndexInLine to come back
           to the previous char. For example, if the string was "98.82=", then we will return "98.82" as
           a single token, but since we passed the symbol '=', so that we should decrement the
           tokenIndexInLine to come back to the previous char.
         */
        if(currentCharacter != null)
            getPreviousChar();

        return stringBuilder.toString();    // Return the whole string number using the string builder.
    }
    // ______________________________________________________________________________________________________
    /* If the char was ':', and the next char was '=', then this method will return ":=" as a single token,
       otherwise come back to the previous char and return ':' as a single token.
     */
    private String colonSymbol(){
        Character nextCharacter = getNextChar();     // Get the next char to check if it was '=' or not.
        if(nextCharacter != null && nextCharacter == '=')   // If the next char was '='.
            return ":=";    // Return ":=" as a single token.
        else{   // If the next char wasn't '='.
            getPreviousChar();  // Come back to the previous char(decrement tokenIndexInLine).
            return ":";     // Return ":" as a single token.
        }
    }
    // ______________________________________________________________________________________________________
    /* If the char was '|', and the next char was '=', then this method will return "|=" as a single token,
       otherwise close the scanner and throw an Illegal Argument Exception at this char index, with the line
       number.
     */
    private String notEqualSymbol() throws IllegalArgumentException{
        Character nextCharacter = getNextChar();     // Get the next char to check if it was '=' or not.
        if(nextCharacter != null && nextCharacter == '=')   // If the next char was '='.
            return "|=";    // Return "|=" as a single token.
        /* If the next char wasn't '=', then close the scanner and throw an Illegal Argument Exception at
           this char index, with the line number.
         */
        else{
            scanner.close();
            throw new IllegalArgumentException("Illegal Character Error: line " + lineNumber +
                    " has Illegal character '" + line.charAt(tokenIndexInLine) + "'");
        }
    }
    // ______________________________________________________________________________________________________
    /* If the char was '<', and the next char was '=', then this method will return "<=" as a single token,
       otherwise come back to the previous char and return '<' as a single token.
     */
    private String lessThanSymbol(){
        Character nextCharacter = getNextChar();    // Get the next char to check if it was '=' or not.
        if(nextCharacter != null && nextCharacter == '=')   // If the next char was '='.
            return "<=";    // Return "<=" as a single token.
        else{   // If the next char wasn't '='.
            getPreviousChar();  // Come back to the previous char(decrement tokenIndexInLine).
            return "<";     // Return "<" as a single token.
        }
    }
    // ______________________________________________________________________________________________________
    /* If the char was '>', and the next char was '=', then this method will return ">=" as a single token,
       otherwise come back to the previous char and return '>' as a single token.
     */
    private String greaterThanSymbol(){
        Character nextCharacter = getNextChar();    // Get the next char to check if it was '=' or not.
        if(nextCharacter != null && nextCharacter == '=')   // If the next char was '='.
            return ">=";    // Return ">=" as a single token.
        else{   // If the next char wasn't '='.
            getPreviousChar();  // Come back to the previous char(decrement tokenIndexInLine).
            return ">";     // Return ">" as a single token.
        }
    }
    // ______________________________________________________________________________________________________
    // This method will be used to return the number of the line that has an error.
    public int getNumOfErrorLine(){
        return lineNumber;  // Return the number of the line that has an error.
    }
}