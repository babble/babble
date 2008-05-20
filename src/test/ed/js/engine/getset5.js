
foo = function(){
    this.a = 4;
}


foo.prototype.__defineGetter__( "b" , 
                                function() { 
                                    return this.a+1; 
                                }
                              );

foo.prototype.__defineSetter__( "b" , 
                                function( z ) { 
                                    return this.c = z;
                                }
                              );

f = new foo();
print( f.b );
f.b = 9;
print( f.b );
print( f.c );
