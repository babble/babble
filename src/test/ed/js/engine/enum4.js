
function Foo(){}
Foo.prototype.a = 1;
Foo.b = 2;
for(var key in Foo){ print("Foo:" + key); }
for(var key in Foo.prototype){ print("Foo.prototype:" + key); }
for(var key in (new Foo())){ print("new Foo:" + key); }

function Bar(){}
Bar.__proto__ = Foo;
for(var key in (new Bar())){ print("new Bar:" + key); }

