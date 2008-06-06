
class Foo

  def go
    @z = 1
    @a = [ 1 , 2 ]
    for thing in @a
      puts thing
      puts @z
    end
  end
  
end

f = Foo.new
f.go
