
class Bar
  def initialize
    @counter = 1;
  end

  def print
    puts "hi\n";
    puts @counter;
  end
  
end

b = Bar.new();
b.print();
