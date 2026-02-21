// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Extracts task names from Concord plugin JARs.
 * Reads @Named annotations from classes listed in META-INF/sisu/javax.inject.Named
 */
public final class TaskNameExtractor {

    private static final Logger LOG = Logger.getInstance(TaskNameExtractor.class);

    private static final String SISU_JAVAX_INDEX_PATH = "META-INF/sisu/javax.inject.Named";
    private static final String JAVAX_NAMED_DESCRIPTOR = "Ljavax/inject/Named;";

    /**
     * Extracts task names from a single JAR file.
     */
    public @NotNull Set<String> extract(@NotNull Path jarPath) {
        Set<String> taskNames = new LinkedHashSet<>();

        try (var jarFile = new JarFile(jarPath.toFile())) {
            var classNames = readSisuIndex(jarFile, SISU_JAVAX_INDEX_PATH);

            for (var className : classNames) {
                var taskName = extractTaskName(jarFile, className);
                if (taskName != null) {
                    taskNames.add(taskName);
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to read JAR: " + jarPath, e);
        }

        return taskNames;
    }

    private @NotNull List<String> readSisuIndex(@NotNull JarFile jarFile, @NotNull String indexPath) throws IOException {
        var entry = jarFile.getEntry(indexPath);
        if (entry == null) {
            return List.of();
        }

        List<String> classNames = new ArrayList<>();
        try (var reader = new BufferedReader(
                new InputStreamReader(jarFile.getInputStream(entry), StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("#")) {
                    classNames.add(line);
                }
            }
        }
        return classNames;
    }

    private @Nullable String extractTaskName(@NotNull JarFile jarFile, @NotNull String className) {
        var classPath = className.replace('.', '/') + ".class";
        var entry = jarFile.getEntry(classPath);
        if (entry == null) {
            return null;
        }

        try (var is = jarFile.getInputStream(entry)) {
            var reader = new ClassReader(is);
            var visitor = new NamedAnnotationVisitor();
            reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return visitor.getNamedValue();
        } catch (Exception e) {
            LOG.debug("Failed to read class: " + className, e);
            return null;
        }
    }

    private static class NamedAnnotationVisitor extends ClassVisitor {

        private String namedValue;

        NamedAnnotationVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public AnnotationVisitor visitAnnotation(String descriptor, boolean visible) {
            if (JAVAX_NAMED_DESCRIPTOR.equals(descriptor)) {
                return new AnnotationVisitor(Opcodes.ASM9) {
                    @Override
                    public void visit(String name, Object value) {
                        if ("value".equals(name) && value instanceof String s) {
                            namedValue = s;
                        }
                    }
                };
            }
            return null;
        }

        @Nullable String getNamedValue() {
            return namedValue;
        }
    }
}
