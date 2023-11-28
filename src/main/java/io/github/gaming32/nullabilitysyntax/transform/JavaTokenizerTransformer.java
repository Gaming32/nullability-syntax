package io.github.gaming32.nullabilitysyntax.transform;

import com.sun.tools.javac.parser.JavaTokenizer;
import net.lenni0451.classtransform.annotations.CInline;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(JavaTokenizer.class)
public abstract class JavaTokenizerTransformer {
    @Shadow
    StringBuilder sb;

    @Shadow
    abstract boolean isSpecial(char ch);

    @Redirect(
        method = "scanOperator",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/parser/JavaTokenizer;isSpecial(C)Z"
        )
    )
    @CInline
    private boolean questionDotIsSpecial(JavaTokenizer instance, char ch) {
        return isSpecial(ch) || (ch == '.' && sb.toString().equals("?"));
    }
}
