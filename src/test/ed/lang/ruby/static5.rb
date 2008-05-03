class Foo
  SILLY = {
    "A" => "a" ,
    "B" => "b" 
  }

  def foo
    puts SILLY["A"]
  end
end

f = Foo.new
f.foo


