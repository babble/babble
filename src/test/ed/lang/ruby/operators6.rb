
$ASD = 5
puts $ASD 

class A
  
  def initialize
    @fooz = 7
  end

  def a
    @fooz ||= 2
    @bar ||= 2
    puts @fooz
    puts @bar
  end

  def self.fooz=(z)
    puts z
    return z
  end
  
    
end

t = A.new
t.a


