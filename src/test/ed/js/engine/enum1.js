for(var key in (new Object())){ print(key); }
for(var key in (new Date())){ print(key); }
for(var key in (new RegExp("/"))){ print(key); }
for(var key in []){ print(key); }


//Foo = function(){};
//Foo.prototype = { a : 1 , b : 2 };
//for(var key in Foo.prototype){ print(key); }
//Foo.prototype.dontEnum( "a" );
//for(var key in Foo.prototype){ print(key); }

// A = { a : 1 , prototype : { b : 2 } };
// for(var key in A){ print(key); }

// for(var key in []){ print(key); }
// Array.prototype.foo = function(){ print("Hi"); }
// for(var key in []){ print(key); }
// Array.prototype.dontEnum( "foo" );
// for(var key in []){ print(key); }


// function Foo(){}
// Foo.prototype.a = 1;
// Foo.b = 2;
// for(var key in Foo){ print(key); }
// for(var key in Foo.prototype){ print(key); }
// for(var key in (new Foo())){ print(key); }

//function Bar(){}
//Bar.__proto__ = Foo;
//for(var key in (new Bar())){ print(key); }
