
class Foo

  def initialize
    @counter = 1;
  end
  
  def inc
    @counter += 1;
  end
  
  def jump( num )
    @counter += num;
  end
  
  def print
    puts "num clicks #{@counter}\n";
  end
end


f = Foo.new();
f.print();
f.inc();
f.print();
f.jump( 5 );
f.print();

#testing empty class
class Foo2
end


#class Foo3 < Foo 
#end
