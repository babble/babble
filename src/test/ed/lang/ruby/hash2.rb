
foo = { :a => "1" , :b => "2" }
puts foo[:a]
puts foo[:b]
puts ( foo[:b] == nil )
puts foo.delete(:b)
puts ( foo[:b] == nil )
