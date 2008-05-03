
bar = false;
puts bar
bar ||= true;
puts bar

bar = true
foo = false;
puts foo
foo ||= bar;
puts foo

def blah( a , b )

  if ( a && b )
    puts "and"
  end

  if ( a || b )
    puts "or"
  end
end

blah( true , false );
blah( false , true );
blah( false , false );
blah( true , true );

puts( ( true ? "yay" : "bak" ) );

puts "----"

foo = [ true , true , false , false ]
puts foo[3];
foo[3] ||= true;
puts foo[3];
