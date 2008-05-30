def foo(x, mode = "a" )
  x.each do |char|
    puts "z#{char}"
    case mode
    when "a"
      123
    when "b"
      4
    else 
      5
    end
  end
  
  return 5
end

puts( foo( ["abc","def"] ) )

