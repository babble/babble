
def foo( h )
  puts( h["z"]  );
end

foo( "z" => 17 );

def foo?
  puts "blah";
end

foo?

def foo2?(z)
  puts "blah #{z}";
end

foo2?( 5 )

class A 
  def go?
    puts 17
  end
end

a = A.new
a.go?




