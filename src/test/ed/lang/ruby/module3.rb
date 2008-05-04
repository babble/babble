
module A 

  def self.included( base )
    puts "yo"
    base.extend( B )
    base.send( :include ,  C );
  end
  
  module B
    def silly
      puts "silly"
      return 17
    end
  end
  
  module C 
    def bar
      puts "bar";
    end
  end
  
end

class Foo
  include A

  def initialize
    puts "fooie"
  end
  
end


f = Foo.new
f = Foo.new

puts Foo.silly

f.bar


  
