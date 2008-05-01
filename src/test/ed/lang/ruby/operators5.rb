
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
