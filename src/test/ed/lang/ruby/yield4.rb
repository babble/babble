
def foo( a )
  puts "before";
  yield a
  puts "after";
end

foo( 7 ) do |n|
  puts "in the middle #{n}"
end
  
