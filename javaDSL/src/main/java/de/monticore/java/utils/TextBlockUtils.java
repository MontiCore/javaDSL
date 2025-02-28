/* (c) https://github.com/MontiCore/monticore */
package de.monticore.java.utils;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/** Utilities for text blocks. */
public class TextBlockUtils {

  /*
   * These methods are implemented according to JEP378. In Java 15+, there are
   * built-in methods that provide the same functionality. Namely:
   * - String#stripIndent(), and
   * - String#translateEscapes().
   *
   * The code below can be considered a port of these to Java 11 and can be
   * replaced once we make the jump past 15.
   *
   * See also: https://openjdk.org/jeps/378
   */

  /**
   * Reformats the given {@code String} by performing the same processing steps
   * as the compiler does for text blocks.
   *
   * @param s the input string
   * @return  the processed text block
   */
  public static String preprocessTextBlock(String s) {
    return translateEscapes(stripIndent(s));
  }

  private static String stripIndent(String string) {
    int length = string.length();
    if (length == 0) return "";

    char lastChar = string.charAt(length - 1);
    boolean optOut = lastChar == '\n' || lastChar == '\r';

    /*
     * Note: Additionally make sure to skip the first line.
     * This is happening in the compiler but not in String#stripIndent().
     */
    List<String> lines = string.lines().skip(1).collect(Collectors.toUnmodifiableList());

    final int outdent = optOut ? 0 : outdent(lines);
    return lines.stream()
        .map(line -> {
          int firstNonWhitespace = indexOfNonWhitespace(line);
          int lastNonWhitespace = lastIndexOfNonWhitespace(line);
          int incidentalWhitespace = Math.min(outdent, firstNonWhitespace);
          return firstNonWhitespace > lastNonWhitespace
              ? "" : line.substring(incidentalWhitespace, lastNonWhitespace);
        })
        .collect(Collectors.joining("\n", "", optOut ? "\n" : ""));
  }

  private static int outdent(List<String> lines) {
    System.out.println(lines);

    // Note: outdent is guaranteed to be zero or positive number.
    // If there isn't a non-blank line then the last must be blank
    int outdent = Integer.MAX_VALUE;

    for (String line : lines) {
      int leadingWhitespace = indexOfNonWhitespace(line);

      if (leadingWhitespace != line.length()) {
        outdent = Integer.min(outdent, leadingWhitespace);
      }
    }

    String lastLine = lines.get(lines.size() - 1);

    if (lastLine.isBlank()) {
      outdent = Integer.min(outdent, lastLine.length());
    }

    return outdent;
  }

  private static int indexOfNonWhitespace(String string) {
    return string.codePoints()
        .takeWhile(cp -> cp == ' ' || cp == '\t' || Character.isWhitespace(cp))
        .map(Character::charCount)
        .sum();
  }

  private static int lastIndexOfNonWhitespace(String string) {
    List<Integer> cps = string.codePoints().boxed().collect(Collectors.toUnmodifiableList());

    return string.length() - IntStream.range(0, cps.size())
        .map(i -> cps.get(cps.size() - i - 1))
        .takeWhile(cp -> cp == ' ' || cp == '\t' || Character.isWhitespace(cp))
        .map(Character::charCount)
        .sum();
  }

  private static String translateEscapes(String string) {
    if (string.isEmpty()) return "";

    char[] chars = string.toCharArray();
    int length = chars.length;
    int from = 0;
    int to = 0;

    while (from < length) {
      char ch = chars[from++];

      if (ch == '\\') {
        ch = from < length ? chars[from++] : '\0';

        switch (ch) {
          case 'b':
            ch = '\b';
            break;
          case 'f':
            ch = '\f';
            break;
          case 'n':
            ch = '\n';
            break;
          case 'r':
            ch = '\r';
            break;
          case 's':
            ch = ' ';
            break;
          case 't':
            ch = '\t';
            break;
          case '\'':
          case '\"':
          case '\\':
            // as is
            break;
          case '0': case '1': case '2': case '3':
          case '4': case '5': case '6': case '7':
            int limit = Integer.min(from + (ch <= '3' ? 2 : 1), length);
            int code = ch - '0';
            while (from < limit) {
              ch = chars[from];
              if (ch < '0' || '7' < ch) break;

              from++;
              code = (code << 3) | (ch - '0');
            }
            ch = (char) code;
            break;
          case '\n':
            continue;
          case '\r':
            if (from < length && chars[from] == '\n') from++;
            continue;
          default: {
            String msg = String.format("Invalid escape sequence: \\%c \\\\u%04X", ch, (int) ch);
            throw new IllegalArgumentException(msg);
          }
        }
      }

      chars[to++] = ch;
    }

    return new String(chars, 0, to);
  }

}
