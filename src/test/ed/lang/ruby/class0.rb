
class Bar
  def initialize
    @counter = 1;
  end

  def silly
    puts "hi\n";
    puts @counter;
  end
  
end

b = Bar.new();
b.silly();
