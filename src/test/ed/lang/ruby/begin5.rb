
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
