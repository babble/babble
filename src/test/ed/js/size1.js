
var before = scope.approxSize();
assert( scope.approxSize() > before );

var o = {};
o.a = 5;
o.o = o;

assert( scope.approxSize() > before ); // this tets inf. loops

o.b = {};
o.b.o = 0;
o.b.a = {};
o.b.a.o = {};

assert( scope.approxSize() > before ); // this tets inf. loops

before = scope.approxSize();
var x = 5;
assert( scope.approxSize() > before ); 
before = scope.approxSize();
x = 7;
assert.eq( scope.approxSize() , before ); 


z = 7;
assert.eq( scope.approxSize() , before );  // a variable added to a higher scope shouldn't count

before = scope.approxSize();
s = scope;
assert.eq( scope.approxSize() , before ); 

s = scope.child();
assert( scope.approxSize() > before , "not bigger" );
