
Foo = function(){};
Foo.prototype = { a : 1 , b : 2 };
for(var key in Foo.prototype){ print(key); }

A = { a : 1 , prototype : { b : 2 } };
for(var key in A){ print(key); }



