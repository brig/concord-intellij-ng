interface Variables {

    Object get(String key);
    boolean has(String key);
    String getString(String key);
    String getString(String key, String defaultValue);
    String assertString(String key);
    String assertString(String message, String key);
    Number getNumber(String key, Number defaultValue);
    Number assertNumber(String key);
    boolean getBoolean(String key, boolean defaultValue);
    boolean assertBoolean(String key);
    int getInt(String key, int defaultValue);
    int assertInt(String key);
    long getLong(String key, long defaultValue);
    long assertLong(String key);
    java.util.UUID getUUID(String key);
    java.util.UUID assertUUID(String key);
    <E> java.util.Collection<E> getCollection(String key, java.util.Collection<E> defaultValue);
    <E> java.util.Collection<E> assertCollection(String key);

    <K, V> java.util.Map<K, V> getMap(String key, java.util.Map<K, V> defaultValue);
    <K, V> java.util.Map<K, V> assertMap(String name);

    <T> java.util.List<T> getList(String key, java.util.List<T> defaultValue);
    <T> java.util.List<T> assertList(String key);
}

interface Context {
    java.nio.file.Path workingDirectory();

    java.util.UUID processInstanceId();

    Object eval(String expression, java.util.Map<String, Object> variables);

    Variables variables();
}

class ScriptResult {
    void set(String key, String value) {}

    void set(String key, Object value) {}
}

interface TaskAccessor {
    Object get(String taskName);
}

interface Logger {
    void debug(String format);
    void debug(String format, Object... arguments);
    void debug(String format, Throwable arguments);

    void info(String format);
    void info(String format, Object... arguments);
    void info(String format, Throwable arguments);

    void error(String format);
    void error(String format, Object... arguments);
    void error(String format, Throwable arguments);

    void warn(String format);
    void warn(String format, Object... arguments);
    void warn(String format, Throwable arguments);
}

Context context = null
TaskAccessor tasks = null
Logger log = null
boolean isDryRun = false
ScriptResult result = null
