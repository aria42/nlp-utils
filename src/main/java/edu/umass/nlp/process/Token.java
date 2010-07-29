package edu.umass.nlp.process;

import edu.umass.nlp.functional.Fn;
import edu.umass.nlp.io.IOUtils;
import edu.umass.nlp.utils.Span;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a token grounded in a document. Also
 * tracks the character span from the source in which
 * the token appears. Also, a field for a <code>label</code>
 * in case its needed.
 */
public class Token implements Comparable<Token> {

  public final String origWord;
  public int tokIndex;
  public int sentIndex;
  public Span charSpan;
  public String word ;
  public String label = NO_LABEL;

  public static String NO_LABEL = "O";

  public Token(String origWord, Span charSpan, int tokIndex, int sentIndex) {
    //assert origWord.length() == charSpan.getLength();
    this.origWord = origWord;
    this.charSpan = charSpan;
    this.tokIndex = tokIndex;
    this.sentIndex = sentIndex;
    this.word = origWord;
  }

  public String toLine() {
    return String.format("%s %s %d %d %d %d %s",word,origWord,charSpan.getStart(),charSpan.getStop(),tokIndex,sentIndex,label);
  }

  public String getOrigWord() {
    return origWord;
  }

  public int getTokIndex() {
    return tokIndex;
  }

  public int getSentIndex() {
    return sentIndex;
  }

  public int getStartChar() {
    return charSpan.getStart();
  }

  public int getStopChar() {
    return charSpan.getStop();
  }

  public String getWord() {
    return word;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
  }

  public void setWord(String word) {

    this.word = word;
  }

  public Span getCharSpan() {
    return charSpan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Token token = (Token) o;

    if (sentIndex != token.sentIndex) return false;
    if (tokIndex != token.tokIndex) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = tokIndex;
    result = 31 * result + sentIndex;
    return result;
  }

  public int compareTo(Token o) {
    if (o.sentIndex != this.sentIndex) {
      return this.sentIndex - o.sentIndex;
    }
    return this.tokIndex - o.tokIndex;
  }

  @Override
  public String toString() {
    return String.format("Token(%d,%d,%s,%s)",sentIndex,tokIndex,word,label);
  }


  /*
   *
   *  Static Factory Methods
   *
   */

  public static Token fromLine(String line) {
    String[] pieces = line.split("\\s+");
    if (pieces.length != 7) throw new RuntimeException("Token, bad line: " + line);
    Token tok = new Token(pieces[1],new Span(Integer.parseInt(pieces[2]),Integer.parseInt(pieces[3])),
      Integer.parseInt(pieces[4]),Integer.parseInt(pieces[5]));
    tok.label = pieces[6];
    tok.word = pieces[0];
    return tok;
  }

  public static Fn<String,Token> fromLineFn = new Fn<String, Token>() {
    public Token apply(String input) { return fromLine(input); }
  };

  public static List<List<Token>> fromInputStream(InputStream is) {
    List<String> lines = IOUtils.lines(is);
    List<List<Token>> res = new ArrayList<List<Token>>();
    for (int i = 0; i < lines.size(); ++i) {
      Token tok = fromLine(lines.get(i));
      if (tok.getSentIndex() >= res.size()) {
        assert tok.getSentIndex() == res.size();
        res.add(new ArrayList<Token>());
      }
      res.get(tok.getSentIndex()).add(tok);
    }
    for (List<Token> toks : res) {
      Collections.sort(toks);
    }
    return res;
  }
  

}