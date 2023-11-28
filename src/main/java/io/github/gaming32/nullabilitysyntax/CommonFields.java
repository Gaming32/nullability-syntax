package io.github.gaming32.nullabilitysyntax;

import com.sun.tools.javac.jvm.Code;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.JCDiagnostic;

import java.util.*;

public class CommonFields {
    public static final Set<JCTree.JCExpression> NULLABLE_ACCESS = Collections.newSetFromMap(new WeakHashMap<>());
    public static final List<Code.Chain> JUMP_CHAINS = new ArrayList<>();

    public static final JCDiagnostic.Error CANT_USE_QUESTION_DOT_HERE = new JCDiagnostic.Error("compiler", "cant.use.question.dot.here");
}
