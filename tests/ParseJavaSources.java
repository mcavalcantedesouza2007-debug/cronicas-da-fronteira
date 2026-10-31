import javax.tools.*;
import com.sun.source.util.*;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Testa apenas parsing Java (não checa API do Minecraft). */
public class ParseJavaSources {
    public static void main(String[] args) throws Exception {
        List<File> sourceFiles = new ArrayList<>();
        try (var stream = Files.walk(Path.of("src"))) {
            stream.filter(f -> f.toString().endsWith(".java")).forEach(f -> sourceFiles.add(f.toFile()));
        }
        DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<>();
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        try (StandardJavaFileManager manager=compiler.getStandardFileManager(diagnostics, null, null)) {
            var files=manager.getJavaFileObjectsFromFiles(sourceFiles);
            JavacTask task=(JavacTask)compiler.getTask(null,manager,diagnostics,List.of("-proc:none"),null,files);
            task.parse();
            List<Diagnostic<? extends JavaFileObject>> errors = diagnostics.getDiagnostics().stream()
                .filter(d -> d.getKind() == Diagnostic.Kind.ERROR).toList();
            if (!errors.isEmpty()) {
                errors.forEach(System.err::println);
                throw new AssertionError("Erros de sintaxe: " + errors.size());
            }
        }
        System.out.println("APROVADO: " + sourceFiles.size() + " arquivos Java analisados sintaticamente.");
        System.out.println("Não comprova compatibilidade com as APIs Fabric/Minecraft 26.3.");
    }
}
