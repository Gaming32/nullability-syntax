package io.github.gaming32.nullabilitysyntax.transform;

import com.sun.tools.javac.parser.JavacParser;
import com.sun.tools.javac.parser.Lexer;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Name;
import io.github.gaming32.nullabilitysyntax.CommonFields;
import io.github.gaming32.nullabilitysyntax.CustomTokens;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(JavacParser.class)
public abstract class JavacParserTransformer {
    @Shadow
    Lexer S;
//
//    @Inject(method = "nextToken", at = @At("TAIL"))
//    @CInline
//    private void printTokens() {
//        System.out.println(S.token().kind);
//    }

    @Shadow
    Tokens.Token token;

    @Shadow
    abstract JCTree.JCExpression innerCreator(int newpos, List<JCTree.JCExpression> typeArgs, JCTree.JCExpression encl);

    private boolean isNullableAccess;

    @Redirect(
        method = "qualident",
        at = @At(
            value = "FIELD",
            target = "Lcom/sun/tools/javac/parser/Tokens$Token;kind:Lcom/sun/tools/javac/parser/Tokens$TokenKind;"
        )
    )
    private Tokens.TokenKind qualidentQuestionDotToDot(Tokens.Token instance) {
        return instance.kind == CustomTokens.QUESTION_DOT ? Tokens.TokenKind.DOT : instance.kind;
    }

    @Redirect(
        method = "qualident",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/TreeMaker;Select(Lcom/sun/tools/javac/tree/JCTree$JCExpression;Lcom/sun/tools/javac/util/Name;)Lcom/sun/tools/javac/tree/JCTree$JCFieldAccess;"
        )
    )
    private JCTree.JCFieldAccess qualidentMarkAsNullable(TreeMaker instance, JCTree.JCExpression selected, Name selector) {
        final JCTree.JCFieldAccess access = instance.Select(selected, selector);
        if (token.kind == CustomTokens.QUESTION_DOT) {
            CommonFields.NULLABLE_ACCESS.add(access);
        }
        return access;
    }

    @Redirect(
        method = "term3",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;ELLIPSIS:Lcom/sun/tools/javac/parser/Tokens$TokenKind;"
            )
        ),
        at = @At(
            value = "FIELD",
            target = "Lcom/sun/tools/javac/parser/Tokens$Token;kind:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
            ordinal = 0
        )
    )
    private Tokens.TokenKind term3QuestionDotToDot(Tokens.Token instance) {
        return instance.kind == CustomTokens.QUESTION_DOT ? Tokens.TokenKind.DOT : instance.kind;
    }

    @Inject(
        method = "term3",
        at = @At(
            value = "FIELD",
            target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;IDENTIFIER:Lcom/sun/tools/javac/parser/Tokens$TokenKind;"
        )
    )
    private void term3StoreIfNullable() {
        isNullableAccess = S.prevToken().kind == CustomTokens.QUESTION_DOT;
    }

    @Redirect(
        method = "term3",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/parser/JavacParser;innerCreator(ILcom/sun/tools/javac/util/List;Lcom/sun/tools/javac/tree/JCTree$JCExpression;)Lcom/sun/tools/javac/tree/JCTree$JCExpression;"
        )
    )
    private JCTree.JCExpression term3StoreInnerCreator(JavacParser instance, int newpos, List<JCTree.JCExpression> typeArgs, JCTree.JCExpression encl) {
        final JCTree.JCExpression result = innerCreator(newpos, typeArgs, encl);
        if (isNullableAccess) {
            CommonFields.NULLABLE_ACCESS.add(result);
        }
        return result;
    }

    @Redirect(
        method = "term3",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;MONKEYS_AT:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
                ordinal = 0
            ),
            to = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;MONKEYS_AT:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
                ordinal = 1
            )
        ),
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/TreeMaker;Select(Lcom/sun/tools/javac/tree/JCTree$JCExpression;Lcom/sun/tools/javac/util/Name;)Lcom/sun/tools/javac/tree/JCTree$JCFieldAccess;"
        )
    )
    private JCTree.JCFieldAccess term3StoreSelect(TreeMaker instance, JCTree.JCExpression selected, Name selector) {
        final JCTree.JCFieldAccess result = instance.Select(selected, selector);
        if (isNullableAccess) {
            CommonFields.NULLABLE_ACCESS.add(result);
        }
        return result;
    }

    @Redirect(
        method = "term3Rest",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;RBRACKET:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
                ordinal = 1
            ),
            to = @At(
                value = "INVOKE",
                target = "Lcom/sun/tools/javac/parser/JavacParser;typeArgumentsOpt(I)Lcom/sun/tools/javac/util/List;",
                ordinal = 0
            )
        ),
        at = @At(
            value = "FIELD",
            target = "Lcom/sun/tools/javac/parser/Tokens$Token;kind:Lcom/sun/tools/javac/parser/Tokens$TokenKind;"
        )
    )
    private Tokens.TokenKind term3RestQuestionDotToDot(Tokens.Token instance) {
        return instance.kind == CustomTokens.QUESTION_DOT ? Tokens.TokenKind.DOT : instance.kind;
    }

    @Redirect(
        method = "term3Rest",
        slice = @Slice(
            from = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;MONKEYS_AT:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
                ordinal = 0
            ),
            to = @At(
                value = "FIELD",
                target = "Lcom/sun/tools/javac/parser/Tokens$TokenKind;MONKEYS_AT:Lcom/sun/tools/javac/parser/Tokens$TokenKind;",
                ordinal = 1
            )
        ),
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/TreeMaker;Select(Lcom/sun/tools/javac/tree/JCTree$JCExpression;Lcom/sun/tools/javac/util/Name;)Lcom/sun/tools/javac/tree/JCTree$JCFieldAccess;"
        )
    )
    private JCTree.JCFieldAccess term3RestStoreSelect(TreeMaker instance, JCTree.JCExpression selected, Name selector) {
        final JCTree.JCFieldAccess result = instance.Select(selected, selector);
        if (S.prevToken().kind == CustomTokens.QUESTION_DOT) {
            CommonFields.NULLABLE_ACCESS.add(result);
        }
        return result;
    }

    @Redirect(
        method = "isUnboundMemberRef",
        at = @At(
            value = "FIELD",
            target = "Lcom/sun/tools/javac/parser/Tokens$Token;kind:Lcom/sun/tools/javac/parser/Tokens$TokenKind;"
        )
    )
    private Tokens.TokenKind isUnboundMemberRefQuestionDotToDot(Tokens.Token token) {
        return token.kind == CustomTokens.QUESTION_DOT ? Tokens.TokenKind.DOT : token.kind;
    }
}
