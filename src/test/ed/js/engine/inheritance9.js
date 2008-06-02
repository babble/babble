
function A(){
}

A.foo = function(){
    print( "A.foo" );
}

A.prototype.foo = function(){
    print( "A.prototype.foo" );
}

A.foo();
a = new A();
a.foo();

B = function(){
};

B.__proto__ = A;
B.prototype.__proto__ = A.prototype;

B.foo();
b = new B();
b.foo();


