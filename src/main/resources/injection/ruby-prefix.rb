class Variables
  def get(key); end
  def has(key); end
  def getString(key, defaultValue = nil); end
  def assertString(key, message = nil); end
  def getNumber(key, defaultValue = nil); end
  def assertNumber(key); end
  def getBoolean(key, defaultValue = nil); end
  def assertBoolean(key); end
  def getInt(key, defaultValue = nil); end
  def assertInt(key); end
  def getLong(key, defaultValue = nil); end
  def assertLong(key); end
  def getUUID(key); end
  def assertUUID(key); end
  def getCollection(key, defaultValue = nil); end
  def assertCollection(key); end
  def getMap(key, defaultValue = nil); end
  def assertMap(name); end
  def getList(key, defaultValue = nil); end
  def assertList(key); end
end

class Context
  def workingDirectory; end
  def processInstanceId; end
  def eval(expression, variables); end
  def variables; end
end

class ScriptResult
  def set(key, value); end
end

class TaskAccessor
  def get(taskName); end
end

class Logger
  def debug(format, *args); end
  def info(format, *args); end
  def error(format, *args); end
  def warn(format, *args); end
end

context = Context.new
tasks = TaskAccessor.new
log = Logger.new
isDryRun = false
result = ScriptResult.new