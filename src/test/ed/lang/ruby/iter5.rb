

puts "break"

[ 1 , 2 , 3 ].each do|z|
  puts z
  break if  z == 2 
  puts "-"
end

puts "next"

[ 1 , 2 , 3 ].each do|z|
  puts z
  next if  z == 2 
  puts "-"
end


puts "redo"

blah = 1
[ 1 , 2 , 3 ].each do|z|
  puts z
  if z == 2 and blah == 1
    blah = blah + 1
    redo
  end
  puts "-"
end
