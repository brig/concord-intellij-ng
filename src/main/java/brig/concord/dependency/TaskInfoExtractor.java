// SPDX-License-Identifier: Apache-2.0
package brig.concord.dependency;

import brig.concord.schema.SchemaType;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.*;
import java.util.jar.JarFile;

/**
 * Extracts task names and public methods from Concord plugin JARs.
 * Reads @Named annotations from classes listed in META-INF/sisu/javax.inject.Named
 */
public final class TaskInfoExtractor {

    private static final Logger LOG = Logger.getInstance(TaskInfoExtractor.class);

    private static final String SISU_JAVAX_INDEX_PATH = "META-INF/sisu/javax.inject.Named";
    private static final String JAVAX_NAMED_DESCRIPTOR = "Ljavax/inject/Named;";
    private static final String V2_TASK_INTERFACE = "com/walmartlabs/concord/runtime/v2/sdk/Task";

    private static final Set<String> EXCLUDED_METHODS = Set.of(
            "execute", "equals", "hashCode", "toString", "getClass",
            "notify", "notifyAll", "wait", "clone", "finalize"
    );

    /**
     * Extracts task names with their public methods from a single JAR file.
     */
    public @NotNull Map<String, TaskInfo> extract(@NotNull Path jarPath) {
        Map<String, TaskInfo> result = new LinkedHashMap<>();

        try (var jarFile = new JarFile(jarPath.toFile())) {
            var classNames = readSisuIndex(jarFile, SISU_JAVAX_INDEX_PATH);

            for (var className : classNames) {
                var taskInfo = extractTaskInfo(jarFile, className);
                if (taskInfo != null) {
                    result.put(taskInfo.name(), taskInfo);
                }
            }
        } catch (IOException e) {
            LOG.warn("Failed to read JAR: " + jarPath, e);
        }

        return result;
    }

    private static @NotNull List<String> readSisuIndex(@NotNull JarFile jarFile, @NotNull String indexPath) throws IOException {
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

    private static @Nullable TaskInfo extractTaskInfo(@NotNull JarFile jarFile, @NotNull String className) {
        var internalName = className.replace('.', '/');
        var entry = jarFile.getEntry(internalName + ".class");
        if (entry == null) {
            return null;
        }

        try (var is = jarFile.getInputStream(entry)) {
            var reader = new ClassReader(is);
            var visitor = new TaskClassVisitor();
            reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);

            if (!isV2Task(jarFile, visitor, new HashSet<>())) {
                return null;
            }

            var namedValue = visitor.getNamedValue();
            if (namedValue == null) {
                return null;
            }

            return new TaskInfo(namedValue, List.copyOf(visitor.getMethods()));
        } catch (Exception e) {
            LOG.debug("Failed to read class: " + className, e);
            return null;
        }
    }

    // Checks if the already-visited class is a v2 Task, walking superclasses and interfaces within the JAR.
    // The target interface (V2_TASK_INTERFACE) itself typically lives outside the JAR (in the SDK),
    // so it's matched by name before attempting to load the class file.
    private static boolean isV2Task(@NotNull JarFile jarFile, @NotNull HierarchyVisitor visitor,
                                    @NotNull Set<String> visited) {
        for (var iface : visitor.getInterfaces()) {
            if (V2_TASK_INTERFACE.equals(iface) || isV2Task(jarFile, iface, visited)) {
                return true;
            }
        }

        var superName = visitor.getSuperName();
        if (superName != null) {
            return isV2Task(jarFile, superName, visited);
        }
        return false;
    }

    private static boolean isV2Task(@NotNull JarFile jarFile, @NotNull String internalName,
                                    @NotNull Set<String> visited) {
        if (internalName.equals("java/lang/Object") || !visited.add(internalName)) {
            return false;
        }

        var entry = jarFile.getEntry(internalName + ".class");
        if (entry == null) {
            return false;
        }

        try (var is = jarFile.getInputStream(entry)) {
            var reader = new ClassReader(is);
            var visitor = new HierarchyVisitor();
            reader.accept(visitor, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES);
            return isV2Task(jarFile, visitor, visited);
        } catch (Exception e) {
            LOG.debug("Failed to check class hierarchy: " + internalName, e);
        }
        return false;
    }

    static @NotNull SchemaType toSchemaType(@NotNull Type type) {
        return switch (type.getSort()) {
            case Type.VOID -> new SchemaType.Any("void");
            case Type.BOOLEAN -> SchemaType.Scalar.BOOLEAN;
            case Type.INT, Type.LONG, Type.SHORT, Type.BYTE, Type.FLOAT, Type.DOUBLE -> SchemaType.Scalar.INTEGER;
            case Type.CHAR -> SchemaType.Scalar.STRING;
            case Type.ARRAY -> SchemaType.Array.ANY;
            case Type.OBJECT -> mapObjectType(type.getInternalName());
            default -> SchemaType.ANY;
        };
    }

    private static @NotNull SchemaType mapObjectType(@NotNull String internalName) {
        return switch (internalName) {
            case "java/lang/String", "java/lang/CharSequence", "java/lang/Character" -> SchemaType.Scalar.STRING;
            case "java/lang/Boolean" -> SchemaType.Scalar.BOOLEAN;
            case "java/lang/Integer", "java/lang/Long", "java/lang/Short", "java/lang/Byte",
                 "java/lang/Float", "java/lang/Double", "java/lang/Number",
                 "java/math/BigInteger", "java/math/BigDecimal" -> SchemaType.Scalar.INTEGER;
            case "java/util/Map", "java/util/LinkedHashMap", "java/util/HashMap",
                 "java/util/TreeMap", "java/util/concurrent/ConcurrentHashMap" -> SchemaType.Scalar.OBJECT;
            case "java/util/List", "java/util/ArrayList", "java/util/LinkedList",
                 "java/util/Collection", "java/util/Set", "java/util/HashSet",
                 "java/util/LinkedHashSet", "java/util/TreeSet" -> SchemaType.Array.ANY;
            default -> {
                var simpleName = internalName.substring(internalName.lastIndexOf('/') + 1);
                yield new SchemaType.Any(simpleName);
            }
        };
    }

    private static class HierarchyVisitor extends ClassVisitor {

        private String superName;
        private String @NotNull [] interfaces = new String[0];

        HierarchyVisitor() {
            super(Opcodes.ASM9);
        }

        @Override
        public void visit(int version, int access, String name, String signature,
                          String superName, String[] interfaces) {
            this.superName = superName;
            if (interfaces != null) {
                this.interfaces = interfaces;
            }
        }

        @NotNull String[] getInterfaces() {
            return interfaces;
        }

        @Nullable String getSuperName() {
            return superName;
        }
    }

    private static class TaskClassVisitor extends HierarchyVisitor {

        private String namedValue;
        private final List<TaskMethod> methods = new ArrayList<>();

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

        @Override
        public MethodVisitor visitMethod(int access, String name, String descriptor,
                                         String signature, String[] exceptions) {
            if (!isEligibleMethod(access, name)) {
                return null;
            }

            var argTypes = Type.getArgumentTypes(descriptor);
            var returnType = Type.getReturnType(descriptor);

            List<SchemaType> parameterTypes = new ArrayList<>();
            for (var argType : argTypes) {
                parameterTypes.add(toSchemaType(argType));
            }

            methods.add(new TaskMethod(name, toSchemaType(returnType), List.copyOf(parameterTypes)));
            return null;
        }

        private static boolean isEligibleMethod(int access, String name) {
            if ((access & Opcodes.ACC_PUBLIC) == 0) {
                return false;
            }
            if ((access & (Opcodes.ACC_SYNTHETIC | Opcodes.ACC_BRIDGE)) != 0) {
                return false;
            }
            if (name.startsWith("<")) {
                return false;
            }
            return !EXCLUDED_METHODS.contains(name);
        }

        @Nullable String getNamedValue() {
            return namedValue;
        }

        @NotNull List<TaskMethod> getMethods() {
            return methods;
        }
    }
}
