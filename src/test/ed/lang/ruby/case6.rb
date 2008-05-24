
def foo(a,b)
  c = 
    case a
      when 5
      puts "Y"
      a = a + 1
      puts "Y#{a}"
      b = b + 2
      else
      puts "Z#{a}"
      a = a - 1
      puts "Z#{a}"
      b = b + 1
    end
  puts a
  puts b
  puts c 
  [ a , b , c ]
end

a = foo(2 , 3 )
puts( a[0] )
puts( a[1] )
puts( a[2] )

