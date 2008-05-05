
begin
  puts "A"
rescue
  puts "B"
end

begin
  puts "A"
  raise "Asd"
  puts "C"
rescue
  puts "B"
end

class Silly < Exception
  def silly
    puts "silly"
  end
end

begin
  begin
    raise 5
  rescue Silly
    puts "inner1"
  end
  puts "what1"
rescue
  puts "outer1"
end


begin
  begin
    raise Silly
  rescue Silly
    puts "inner2"
  end
  puts "what2"
rescue
  puts "outer2"
end
