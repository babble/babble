
# def foo(z)
#   if ( z == 4 )
#     "four"
#   elsif ( z == 5 )
#     "five"
#   else
#     true
#   end
# end

# puts foo(4) 
# puts foo(5) 
# puts foo(6) 
    

# def blah(z)
#   (z==5) ? true : false
# end

# blah(5)
# blah(6)

def blah2(z)
  if( z== 5)
    false
  else
    puts "a"
    true
  end
end


puts blah2(5)
puts blah2(6)


def blah3(z)
  if( z== 5)
    false
  else
    puts "a"
    true
  end
  puts "z"
end


puts blah3(5) == nil
puts blah3(6) == nil
