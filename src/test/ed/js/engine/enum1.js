
Foo = function(){};
Foo.prototype = { a : 1 , b : 2 };
for(var key in Foo.prototype){ print(key); }

A = { a : 1 , prototype : { b : 2 } };
for(var key in A){ print(key); }

print( "POS1" );

function Foo(){}
Foo.prototype.a = 1;
Foo.b = 2;
for(var key in Foo){ print(key); }
print( "POS2" );
for(var key in Foo.prototype){ print(key); }
print( "POS3" );
for(var key in (new Foo())){ print(key); }
print( "POS4" );

function Bar(){}
Bar.__proto__ = Foo;
for(var key in (new Bar())){ print(key); }


