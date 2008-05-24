
def blah(z)
  gar = nil;
  foo = 
    if z == 5
      gar = 1
      "five"
    elsif z == 6
      gar = 2
      "six"
    elsif z == 7
      gar = 3
      "six"
    else
      gar = 4
      "stupid"
    end
  puts gar
  return foo
end

puts blah(5)
puts blah(6)
puts blah(7)
puts blah(8)
