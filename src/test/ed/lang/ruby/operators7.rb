    
class A
  def foo
    (@feed_icons ||= []) << { :url => "url", :title => "title" }
    puts @feed_icons.length
    puts @feed_icons[0][:url]
    puts @feed_icons[0][:title]
  end
end

a = A.new
a.foo

a = "abc"

h = { :conditions => { "#{a}z" => true} }
puts h[:conditions]["abcz"]

a = { :foo => "1" }
puts a.has_key? :foo
puts a.has_key? :bar

b = { :bar => "2" }
puts b.has_key? :foo
puts b.has_key? :bar

puts "--merge--"

c = b.merge a

puts a.has_key? :foo
puts a.has_key? :bar
puts b.has_key? :foo
puts b.has_key? :bar
puts c.has_key? :foo
puts c.has_key? :bar

a = { :foo => "1" }
b = { :foo => "2" }

c = b.merge a
puts c[:foo]

c = a.merge b
puts c[:foo]

z = 5
z.times do
  puts "a"
end


z = "123"
puts z

z << "456"
puts z

puts ( z << "a" )

z = 4
z.upto 10 do |z|
  puts z
end

puts "zzzzz"

foo = "abc";
puts foo
z = "1"
puts( foo &&= " 123" + z )
puts foo

foo = false;
puts foo
z = "1"
puts( foo &&= " 123" + z )
