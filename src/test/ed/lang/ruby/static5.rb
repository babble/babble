class Foo
  SILLY = {
    "A" => "a" ,
    "B" => "b" 
  }

  def initialize
    @blah = 5
  end

  def foo
    puts SILLY["A"]
  end
end

f = Foo.new
f.foo


