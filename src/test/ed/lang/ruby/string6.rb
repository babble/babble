
puts "abc"[:foo] == nil

class String

  alias blah []

  def [](name)
    puts name
    return 17
  end
end

puts "abc"[:foo]
puts "abc"[:foo] == nil

class String
  alias [] blah
end

puts "abc"[:foo] == nil

