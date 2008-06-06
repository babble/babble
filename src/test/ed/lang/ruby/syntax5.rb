
puts( 5 | 2 )

if ( true | false )
  puts "y";
end

blah = 5;

crap = false;
silly = false;
silly |= ( blah == 5 ) if crap 
puts silly

crap = true;
silly = false;
silly |= ( blah == 5 ) if crap 
if( silly )
  puts "y"
end
