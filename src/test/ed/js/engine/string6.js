
print( "\70" );
print( "\4".length );


print( /\64/.test( "4" ) );


A = function(){
    this.z = 11;
};

A.prototype.toString = function(){
    return "hi " + this.z;
};

a = new A();
print( a );
