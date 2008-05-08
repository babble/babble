
function b(){
    print( "b" );
}

function foo(){
    
};

foo.prototype.a = function(){
    print("A.a");
    b();
};

foo.prototype.b = function(){
    print( "A.b" );
}

f = new foo();
f.a();

