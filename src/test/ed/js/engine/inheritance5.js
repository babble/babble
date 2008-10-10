
function foo(){
    this.a = 5;
}

foo.prototype.b = 6;

f = new foo();
print( f.a );
print( f.b );

print( "a" in f );
print( "b" in f );
