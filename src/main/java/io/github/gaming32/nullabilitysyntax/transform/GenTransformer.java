package io.github.gaming32.nullabilitysyntax.transform;

import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.jvm.ByteCodes;
import com.sun.tools.javac.jvm.Code;
import com.sun.tools.javac.jvm.Gen;
import com.sun.tools.javac.jvm.PoolWriter;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import io.github.gaming32.nullabilitysyntax.CommonFields;
import net.lenni0451.classtransform.annotations.CInline;
import net.lenni0451.classtransform.annotations.CLocalVariable;
import net.lenni0451.classtransform.annotations.injection.CASM;
import net.lenni0451.classtransform.utils.ASMUtils;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import java.util.function.ToIntBiFunction;

@Mixin(Gen.class)
public abstract class GenTransformer {
    @Shadow
    Code code;

    @Shadow
    Log log;

    @Shadow
    Types types;

    @Shadow
    abstract Type checkDimension(JCDiagnostic.DiagnosticPosition pos, Type t);

    @Inject(
        method = "visitApply",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/jvm/Gen;genExpr(Lcom/sun/tools/javac/tree/JCTree;Lcom/sun/tools/javac/code/Type;)Lcom/sun/tools/javac/jvm/Items$Item;",
            shift = At.Shift.AFTER
        )
    )
    @CInline
    private void startJump(JCTree.JCMethodInvocation tree) {
        if (CommonFields.NULLABLE_ACCESS.contains(tree.meth)) {
            code.emitop0(ByteCodes.dup);
            CommonFields.JUMP_CHAINS.add(code.branch(ByteCodes.if_acmp_null));
        }
    }

    @Inject(method = "visitApply", at = @At("TAIL"))
    @CInline
    private void endJump(JCTree.JCMethodInvocation tree) {
        if (CommonFields.NULLABLE_ACCESS.contains(tree.meth)) {
            code.resolve(CommonFields.JUMP_CHAINS.removeLast());
        }
    }

    @Inject(method = "visitNewClass", at = @At("HEAD"))
    @CInline
    private void dontKnowHow(JCTree.JCNewClass tree) {
        if (CommonFields.NULLABLE_ACCESS.contains(tree)) {
            log.error(tree.pos(), CommonFields.CANT_USE_QUESTION_DOT_HERE);
        }
    }

    @Inject(
        method = "genArgs",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/jvm/Items$Item;load()Lcom/sun/tools/javac/jvm/Items$Item;"
        )
    )
    @CInline
    private void startJump(@CLocalVariable(name = "l") List<JCTree.JCExpression> l) {
        if (l.head instanceof JCTree.JCFieldAccess field && CommonFields.NULLABLE_ACCESS.contains(field)) {
            code.emitop0(ByteCodes.dup);
            CommonFields.JUMP_CHAINS.add(code.branch(ByteCodes.if_acmp_null));
        }
    }

    @CASM("genArgs")
    private static void giveMyselfALambda(MethodNode node) {
        final int varIndex = ASMUtils.getFreeVarIndex(node);
        final LabelNode startNode = new LabelNode();
        final var it = node.instructions.iterator();
        it.add(startNode);
        it.add(new InvokeDynamicInsnNode(
            "applyAsInt",
            "()Ljava/util/function/ToIntBiFunction;",
            new Handle(
                Opcodes.H_INVOKESTATIC,
                "java/lang/invoke/LambdaMetafactory",
                "metafactory",
                "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                false
            ),
            org.objectweb.asm.Type.getMethodType("(Ljava/lang/Object;Ljava/lang/Object;)I"),
            new Handle(
                Opcodes.H_INVOKEVIRTUAL,
                "com/sun/tools/javac/jvm/PoolWriter",
                "putClass",
                "(Lcom/sun/tools/javac/code/Type;)I",
                false
            ),
            org.objectweb.asm.Type.getMethodType("(Lcom/sun/tools/javac/jvm/PoolWriter;Lcom/sun/tools/javac/code/Type;)I")
        ));
        it.add(new VarInsnNode(Opcodes.ASTORE, varIndex));
        node.localVariables.add(new LocalVariableNode(
            "putClass", "Ljava/util/function/ToIntBiFunction;", null,
            startNode, startNode, varIndex
        ));
    }

    @Inject(
        method = "genArgs",
        at = @At(
            value = "INVOKE",
            target = "Lcom/sun/tools/javac/jvm/Items$Item;load()Lcom/sun/tools/javac/jvm/Items$Item;",
            shift = At.Shift.AFTER
        )
    )
    @CInline
    private void endJump(
        @CLocalVariable List<JCTree.JCExpression> l,
        @CLocalVariable ToIntBiFunction<PoolWriter, Type> putClass
    ) {
        if (l.head instanceof JCTree.JCFieldAccess field && CommonFields.NULLABLE_ACCESS.contains(field)) {
            if (
                !field.type.isPrimitive() &&
                    !types.isSameType(field.selected.type, field.type) &&
                    types.asSub(field.selected.type, field.type.tsym) == null
            ) {
                final Code.Chain ifNonNull = code.branch(ByteCodes.goto_);
                code.resolve(CommonFields.JUMP_CHAINS.removeLast());
                code.emitop2(ByteCodes.checkcast, checkDimension(field.pos(), field.type), putClass);
                code.resolve(ifNonNull);
            } else {
                code.resolve(CommonFields.JUMP_CHAINS.removeLast());
            }
        }
    }
}
