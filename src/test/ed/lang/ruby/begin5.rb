
def foo z
  puts "foo"
  puts z
  raise 5
end

def save
  puts "save"
end

def bar
  return ( foo 6 rescue save )
end

bar

def bar2
  return ( foo 7 rescue null )
end

bar

def bar3
  6
end

puts bar3
