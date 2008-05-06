
class StandardError < Exception
end

class LoadError < Exception
end

def raiseLoadError(msg)
  raise LoadError , msg
end

