
class Blah
  def z
    puts "Blah.z"
  end
end

class Foo
  A = Blah.new
  A.z()

  Z = 4
end

puts Foo::Z
