package io.github.gaming32.nullabilitysyntax;

import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.parser.Tokens;
import com.sun.tools.javac.util.JavacMessages;
import com.sun.tools.javac.util.Names;
import net.lenni0451.classtransform.TransformerManager;
import net.lenni0451.classtransform.mixinstranslator.MixinsTranslator;
import net.lenni0451.classtransform.utils.tree.BasicClassProvider;
import net.lenni0451.reflect.Agents;
import net.lenni0451.reflect.Modules;
import net.lenni0451.reflect.stream.RStream;

import java.util.Map;

public class NullabilitySyntaxPlugin implements Plugin {
    public static boolean debug;

    @Override
    public String getName() {
        return "nullability";
    }

    @Override
    public void init(JavacTask task, String... args) {
        debug = Utils.indexOf(args, "debug") != -1;
        try {
            setup();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        refreshTokens((BasicJavacTask)task);
        addMessages((BasicJavacTask)task);

        if (debug) {
            task.addTaskListener(new TaskListener() {
                @Override
                public void finished(TaskEvent e) {
                    try {
                        if (e.getKind() == TaskEvent.Kind.PARSE) {
                            System.out.println(e.getCompilationUnit());
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private static void setup() throws Exception {
        Modules.openEntireModule(JavacTask.class);
        final String pkg = NullabilitySyntaxPlugin.class.getPackageName();
        final TransformerManager transformerManager = new TransformerManager(new BasicClassProvider());

        transformerManager.addTransformerPreprocessor(new MixinsTranslator());
        if (debug) {
            transformerManager.getDebugger().setDumpClasses(true);
        }
        transformerManager.addTransformer(pkg + ".transform.GenTransformer");
        transformerManager.addTransformer(pkg + ".transform.JavacParserTransformer");
        transformerManager.addTransformer(pkg + ".transform.JavaTokenizerTransformer");
        transformerManager.addTransformer(pkg + ".transform.PrettyTransformer");

        transformerManager.hookInstrumentation(Agents.getInstrumentation());

        CustomTokens.init();
    }

    private static void refreshTokens(BasicJavacTask task) {
        final Tokens tokens = Tokens.instance(task.getContext());
        final Names names = Names.instance(task.getContext());
        final Map<String, Tokens.TokenKind> keywords = RStream.of(Tokens.class).fields().by("keywords").get(tokens);
        for (final Tokens.TokenKind t : Tokens.TokenKind.values()) {
            if (t.name != null) {
                names.fromString(t.name);
                keywords.put(t.name, t);
            }
        }
    }

    private static void addMessages(BasicJavacTask task) {
        final JavacMessages messages = JavacMessages.instance(task.getContext());
        messages.add("NullabilitySyntax");
    }
}
