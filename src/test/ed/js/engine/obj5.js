
function Foo(){
    this.a = 1;
}

Foo.prototype.b = 2;

f = new Foo();
print( "a" in f );
print( "b" in f );

print( f.hasOwnProperty( "a" ) );
print( f.hasOwnProperty( "b" ) );
