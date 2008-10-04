
var before = scope.myApproxSize();
assert( scope.myApproxSize() > before );

var o = {};
o.a = 5;
o.o = o;

assert( scope.myApproxSize() > before ); // this tets inf. loops

o.b = {};
o.b.o = 0;
o.b.a = {};
o.b.a.o = {};

assert( scope.myApproxSize() > before ); // this tets inf. loops

before = scope.myApproxSize();
var x = 5;
assert( scope.myApproxSize() > before ); 
before = scope.myApproxSize();
x = 7;
assert.eq( scope.myApproxSize() , before ); 


z = 7;
assert.eq( scope.myApproxSize() , before , "a variable added to a higher scope shouldn't count" );

before = scope.myApproxSize();
s = scope;
assert.eq( scope.myApproxSize() , before ); 

s = scope.child();
assert( scope.myApproxSize() > before , "not bigger" );
