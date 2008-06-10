
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


print(String.fromCharCode(65));

print(String.fromCharCode(65, 69));

print(typeof String.fromCharCode());
print(String.fromCharCode().length);

print( "asdfasdf".split("d").length );
print( "asdfasdf".split("d")[0] );

print( "asdfasdf".split("d",1).length );
print( "asdfasdf".split("d",1)[0] );

print( "abc".replace( /a/ , null ) );
