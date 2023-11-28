package io.github.gaming32.nullabilitysyntax;

import com.sun.tools.javac.parser.Tokens;
import net.lenni0451.reflect.Enums;

public class CustomTokens {
    public static final Tokens.TokenKind QUESTION_COLON;
    public static final Tokens.TokenKind QUESTION_DOT;

    static {
        int ordinal = Tokens.TokenKind.values().length;
        QUESTION_COLON = Enums.newInstance(
            Tokens.TokenKind.class,
            "QUESTION_COLON", ordinal++,
            new Class[] {String.class},
            new Object[] {"?:"}
        );
        QUESTION_DOT = Enums.newInstance(
            Tokens.TokenKind.class,
            "QUESTION_DOT", ordinal++,
            new Class[] {String.class},
            new Object[] {"?."}
        );

        Enums.addEnumInstance(Tokens.TokenKind.class, QUESTION_COLON);
        Enums.addEnumInstance(Tokens.TokenKind.class, QUESTION_DOT);
        Enums.clearEnumCache(Tokens.TokenKind.class);
    }

    static void init() {
    }
}
