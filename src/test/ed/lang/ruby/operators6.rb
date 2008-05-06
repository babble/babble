
$ASD = 5
puts $ASD 

class A
  
  def initialize
    @foo = 7
  end
  
  def a
    @foo ||= 2
    @bar ||= 2
    puts @foo
    puts @bar
  end
end

t = A.new
t.a


