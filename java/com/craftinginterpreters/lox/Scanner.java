package com.craftinginterpreters.lox;

import static com.craftinginterpreters.lox.TokenType.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Scanner {

  private final String source;
  //list to fill with tokens
  private final List<Token> tokens = new ArrayList<>();
  //start and current fields are offsets that index into the string
  //start points to the first character in the lexeme being scanned
  private int start = 0;
  //current points at the character currently being considered
  private int current = 0;
  //line tracks what source line current is on so we can produce tokens that known thier locations
  private int line = 1;

  //stores raw source code as a simple string
  Scanner(String source) {
    this.source = source;
  }

  List<Token> scanTokens() {
    while (!isAtEnd()) {
      //We are at the beginning of the next lexeme/
      start = current;
      scanToken();
    }

    tokens.add(new Token(EOF, "", null, line));
    return tokens;
  }

  private void scanToken() {
    //for all single length characters we consume and pick a token type for it
    char c = advance();
    switch (c) {
      //single character lexemes
      case '(':
        addToken(LEFT_PAREN);
        break;
      case ')':
        addToken(RIGHT_PAREN);
        break;
      case '{':
        addToken(RIGHT_BRACE);
        break;
      case '}':
        addToken(LEFT_BRACE);
        break;
      case ',':
        addToken(COMMA);
        break;
      case '.':
        addToken(DOT);
        break;
      case '-':
        addToken(MINUS);
        break;
      case '+':
        addToken(PLUS);
        break;
      case ';':
        addToken(SEMICOLON);
        break;
      case '*':
        addToken(STAR);
        break;
      //single character lexemes that can be combined to be dual character lexemes
      case '!':
        addToken(match('=') ? BANG_EQUAL : BANG);
        break;
      case '=':
        addToken(match('=') ? EQUAL_EQUAL : EQUAL);
        break;
      case '<':
        addToken(match('=') ? LESS_EQUAL : LESS);
        break;
      case '>':
        addToken(match('=') ? GREATER_EQUAL : GREATER);
        break;
      //special case division/comment
      case '/':
        if (match('/')) {
          //A comment goes until the end of the line.
          while (peek() != '\n' && !isAtEnd()) advance();
        } else {
          addToken(SLASH);
        }
        break;
      case ' ':
      case '\r':
      case '\t':
        //Ignore whitespace.
        break;

      case'\n':
        line++;
        break;
        //Strings
      case '"': string(); break;

      default:
        if(isDigit(c)){
          number();
        }
        Lox.error(line, "Unexpected character.");
        break;
    }
  }

  private void string() {
    while(peek() != '"' && !isAtEnd()) {
      if(peek() == '\n') line++;
      advance();
    }

    if(isAtEnd()){
      Lox.error(line, "Unterminated string.");
      return;
    }

    //The closing ".
    advance();

    //Trim the surrounding quotes.
    String value = source.substring(start + 1, current - 1);
    addToken(STRING, value);
  }


  //similar to advance, but only consumes character if it is what we're looking for
  private boolean match(char expected) {
    if (isAtEnd()) return false;
    if (source.charAt(current) != expected) return false;

    current++;
    return true;
  }

  //similar to advance but does not consume the character
  private char peek() {
    if (isAtEnd()) return '\0';
    return source.charAt(current);
  }

  private boolean isDigit(char c){
    return c >= '0' && c <= '9';
  }

  //helper function that tells us if we've consumed all the characters
  private boolean isAtEnd() {
    return current >= source.length();
  }

  //consumes the next character in the source file and returns it
  private char advance() {
    return source.charAt(current++);
  }

  //grabs the text of the current lexeme and creates a new token for it
  private void addToken(TokenType type) {
    addToken(type, null);
  }

  private void addToken(TokenType type, Object literal) {
    String text = source.substring(start, current);
    tokens.add(new Token(type, text, literal, line));
  }
}
