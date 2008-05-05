
def foo z
  puts "foo"
  puts z
  raise 5
end

def save
  puts "save"
  return 111;
end

def bar
  return ( foo 6 rescue save )
end

puts bar
