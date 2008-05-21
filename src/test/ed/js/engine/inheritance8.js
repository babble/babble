
A = function(){
    
}

A.prototype.foo = function(){
    return "A.foo";
}

A.prototype.fun = function(){
    return "A";
}

B = function(){
    
}

B.prototype.bar = function(){
    return "B.bar"
}

B.prototype.fun = function(){
    return "B";
}

B.prototype.__proto__ = A.prototype;


b = new B();
print( b.bar() );
print( b.foo() );
print( b.fun() );

print( "---" );

function A() { }
A.prototype.myMethod = function() { return "A"; }

function B() { }
B.prototype.myMethod = function() { return "B"; }

var a = new A();

a.__proto__ = B.prototype;

print( a.myMethod() );

