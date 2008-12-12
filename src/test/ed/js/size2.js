
// make sure it's consistent
cons = Array;
cons2 = Array;
assert( cons.approxSize() == cons2.approxSize() );

var str1 = new String( "foo" );
var str2 = new String( "foo" );

assert( str1.approxSize() == str2.approxSize() );

str3 = "foo";
assert( str1.approxSize() == str3.approxSize() );


// strings share the same constructor, so the size 
// doesn't increase at the same scale
var obj = { s1 : str1 };
var size1 = obj.approxSize();

obj.s2 = str2;
var size2 = obj.approxSize();
assert( size2 > size1 );
assert( size2 < size1 + str1.approxSize() );


// an empty obj
obj = {};
assert( obj.approxSize() == 120 );

