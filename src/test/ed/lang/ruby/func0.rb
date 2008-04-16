
def simple()
  puts( "yo" );
end

simple;
simple();

def welcome(name)
  puts( "howdy" + name );
  puts "howdy #{name}"
end
welcome("eliot");

puts( "\n" );

def blah( z )
  return z;
end
puts( blah( "hhh" ) );
