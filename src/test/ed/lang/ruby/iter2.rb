
"asd".each_byte do |abc|
  puts "a #{abc}";
  puts "b";
end

def silly
  return "blah".each_byte do |blah|
    puts blah
  end
end

silly

