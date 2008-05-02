
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
