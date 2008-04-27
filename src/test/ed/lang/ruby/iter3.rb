

def silly( a , &b )
  puts a;
  b.call()
end

silly( 4 ) do 
  puts "hi"
end

