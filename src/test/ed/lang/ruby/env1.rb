
before = $:.length
$: << "/foo"
puts $:.length - before
