
a = 1
b = 5
puts a && b

a = false
b = 5
puts ( a && b )? "A1" : "A2"

a = { :foo => nil }
puts ( a[:foo] && a[:foo].z ) ? "A2" : "B2"
