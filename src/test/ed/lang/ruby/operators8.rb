
p = /a/

text = "ceaf";
a = []
a << "abc" if text =~ p
puts text
puts a.length
puts a[0]

text = "cef";
a = []
a << "abc" if text =~ p
puts text
puts a.length

5.times do
  puts "hi"
end
