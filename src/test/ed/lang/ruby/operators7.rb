    
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
