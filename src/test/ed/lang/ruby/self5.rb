
class A
  
  def initialize
    @silly = 6
  end

  def [] n
    puts n
    return "blah"
  end

  def foo
    puts self['silly']
  end
end

a = A.new
a.foo
    
