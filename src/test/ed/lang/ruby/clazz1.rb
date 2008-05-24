
class Blah
  def z
    puts "Blah.z"
  end
end

class Foo
  A = Blah.new
  A.z()

  Z = 4

  Y = [ 1 , 2 ]

  X = [ 1 , 2 ].length
end

puts Foo::Z
puts Foo::Y.length
puts Foo::X
