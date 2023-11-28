package io.github.gaming32.nullabilitysyntax.transform;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;
import io.github.gaming32.nullabilitysyntax.CommonFields;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.io.IOException;

@Mixin(Pretty.class)
public class PrettyTransformer {
    @Redirect(
        method = "visitSelect",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/Pretty;print(C)V"
        )
    )
    private void printSelect(Pretty instance, char c, @CLocalVariable JCTree.JCFieldAccess tree) throws IOException {
        if (c == '.' && CommonFields.NULLABLE_ACCESS.contains(tree)) {
            instance.print("?.");
            return;
        }
        instance.print(c);
    }

    @Redirect(
        method = "visitNewClass",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/Pretty;print(C)V"
        )
    )
    private void printNewClass(Pretty instance, char c, @CLocalVariable JCTree.JCNewClass tree) throws IOException {
        if (c == '.' && CommonFields.NULLABLE_ACCESS.contains(tree)) {
            instance.print("?.");
            return;
        }
        instance.print(c);
    }

    @Redirect(
        method = "visitApply",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/tree/Pretty;print(Ljava/lang/Object;)V"
        )
    )
    private void printApply(Pretty instance, Object s, @CLocalVariable JCTree.JCMethodInvocation tree) throws IOException {
        if (s.equals(".<") && CommonFields.NULLABLE_ACCESS.contains(tree)) {
            instance.print("?.<");
            return;
        }
        instance.print(s);
    }
}
