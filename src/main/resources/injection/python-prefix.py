class Variables:
    def get(self, key): pass
    def has(self, key): pass
    def getString(self, key, default_value=None): pass
    def assertString(self, key, message=None): pass
    def getNumber(self, key, default_value=None): pass
    def assertNumber(self, key): pass
    def getBoolean(self, key, default_value=None): pass
    def assertBoolean(self, key): pass
    def getInt(self, key, default_value=None): pass
    def assertInt(self, key): pass
    def getLong(self, key, default_value=None): pass
    def assertLong(self, key): pass
    def getUUID(self, key): pass
    def assertUUID(self, key): pass
    def getCollection(self, key, default_value=None): pass
    def assertCollection(self, key): pass
    def getMap(self, key, default_value=None): pass
    def assertMap(self, name): pass
    def getList(self, key, default_value=None): pass
    def assertList(self, key): pass

class Context:
    def workingDirectory(self): pass
    def processInstanceId(self): pass
    def eval(self, expression, variables): pass
    def variables(self) -> Variables: pass

class ScriptResult:
    def set(self, key, value): pass

class TaskAccessor:
    def get(self, taskName): pass

class Logger:
    def debug(self, format, *args): pass
    def info(self, format, *args): pass
    def error(self, format, *args): pass
    def warn(self, format, *args): pass

context = Context()
tasks = TaskAccessor()
log = Logger()
isDryRun = False
result = ScriptResult()