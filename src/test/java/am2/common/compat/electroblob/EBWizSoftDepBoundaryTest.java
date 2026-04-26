package am2.common.compat.electroblob;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies that direct {@code import electroblob.*} statements only appear inside
 * the two designated compat packages.  All other source files must interact with
 * EBWiz exclusively through the {@code am2.*.compat.electroblob} facade, ensuring
 * the mod loads correctly when EBWiz is absent.
 *
 * <p>Allowed directories (relative to {@code src/main/java/}):
 * <ul>
 *   <li>{@code am2/common/compat/electroblob/}</li>
 *   <li>{@code am2/client/compat/electroblob/}</li>
 * </ul>
 */
public class EBWizSoftDepBoundaryTest {

    /** Path segments that identify an allowed EBWiz compat directory. */
    private static final String[] ALLOWED_SEGMENTS = {
        "am2/common/compat/electroblob",
        "am2/client/compat/electroblob",
        // normalised separators on Windows
        "am2\\common\\compat\\electroblob",
        "am2\\client\\compat\\electroblob"
    };

    private static final String IMPORT_MARKER = "import electroblob.";

    static class Violation {
        final String filePath;
        final List<ImportLocation> imports;

        Violation(String filePath) {
            this.filePath = filePath;
            this.imports = new ArrayList<>();
        }

        void addImport(int lineNumber, String importStatement) {
            imports.add(new ImportLocation(lineNumber, importStatement));
        }
    }

    static class ImportLocation {
        final int lineNumber;
        final String importStatement;

        ImportLocation(int lineNumber, String importStatement) {
            this.lineNumber = lineNumber;
            this.importStatement = importStatement;
        }
    }

    @Test
    public void noDirectEBWizImportsOutsideCompatPackage() throws IOException {
        Path sourceRoot = resolveSourceRoot();
        List<Violation> violations = new ArrayList<>();

        try (Stream<Path> files = Files.walk(sourceRoot)) {
            files.filter(p -> p.toString().endsWith(".java"))
                 .filter(p -> !isInAllowedDirectory(p))
                 .forEach(p -> {
                     try {
                         String content = new String(Files.readAllBytes(p), StandardCharsets.UTF_8);
                         if (content.contains(IMPORT_MARKER)) {
                             Violation violation = findViolations(p, content, sourceRoot);
                             if (!violation.imports.isEmpty()) {
                                 violations.add(violation);
                             }
                         }
                     } catch (IOException e) {
                         throw new RuntimeException("Failed to read " + p, e);
                     }
                 });
        }

        if (!violations.isEmpty()) {
            System.err.println("========================================");
            System.err.println("EBWiz Soft Dependency Boundary Violation");
            System.err.println("========================================");
            System.err.println("The following source file(s) contain direct 'import electroblob.*' statements");
            System.err.println("outside the allowed compat packages:");
            System.err.println();
            for (Violation violation : violations) {
                System.err.println("File: " + violation.filePath);
                for (ImportLocation imp : violation.imports) {
                    System.err.println("  Line " + imp.lineNumber + ": " + imp.importStatement);
                }
                System.err.println();
            }
            System.err.println("Move the EBWiz references into am2/common/compat/electroblob/");
            System.err.println("or am2/client/compat/electroblob/ and access them through the");
            System.err.println("ElectroblobCompat facade.");
            System.err.println("========================================");
        }

        assertTrue(violations.isEmpty(), buildAssertionMessage(violations));
    }

    private static Violation findViolations(Path file, String content, Path sourceRoot) {
        Violation violation = new Violation(sourceRoot.relativize(file).toString());
        String[] lines = content.split("\r?\n");
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.startsWith(IMPORT_MARKER)) {
                violation.addImport(i + 1, line);
            }
        }
        return violation;
    }

    private static String buildAssertionMessage(List<Violation> violations) {
        if (violations.isEmpty()) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("The following source file(s) contain direct 'import electroblob.*' statements outside ");
        sb.append("the allowed compat packages. Move the EBWiz references into ");
        sb.append("am2/common/compat/electroblob/ or am2/client/compat/electroblob/ and access them ");
        sb.append("through the ElectroblobCompat facade:\n\n");
        for (Violation violation : violations) {
            sb.append("File: ").append(violation.filePath).append("\n");
            for (ImportLocation imp : violation.imports) {
                sb.append("  Line ").append(imp.lineNumber).append(": ").append(imp.importStatement).append("\n");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    // -------------------------------------------------------------------------

    /**
     * Resolves {@code src/main/java} relative to the Gradle project directory
     * ({@code user.dir}).  Falls back to walking up the directory tree if the
     * standard layout is not found.
     */
    private static Path resolveSourceRoot() {
        Path candidate = Paths.get(System.getProperty("user.dir"))
                              .resolve("src/main/java");
        if (Files.isDirectory(candidate)) {
            return candidate;
        }
        // Walk up up to 3 levels in case user.dir is a sub-directory
        Path dir = Paths.get(System.getProperty("user.dir"));
        for (int i = 0; i < 3; i++) {
            dir = dir.getParent();
            if (dir == null) break;
            candidate = dir.resolve("src/main/java");
            if (Files.isDirectory(candidate)) {
                return candidate;
            }
        }
        throw new IllegalStateException(
            "Could not locate src/main/java relative to user.dir=" +
            System.getProperty("user.dir"));
    }

    private static boolean isInAllowedDirectory(Path path) {
        String normalised = path.toString().replace('\\', '/');
        for (String seg : ALLOWED_SEGMENTS) {
            if (normalised.contains(seg.replace('\\', '/'))) {
                return true;
            }
        }
        return false;
    }
}
