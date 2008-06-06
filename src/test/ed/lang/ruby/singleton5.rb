

class Test
  puts "1"

  def meth
    puts "9"
  end

  class << self
    puts "2"

    def meth2
      puts "4"
    end
  end
end  


Test.meth2

t = Test.new
t.meth
