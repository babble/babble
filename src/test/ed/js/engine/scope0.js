
var a = 1;
var b = 2;

function foo(){
    return a + b;
}

print( "3=" + foo() );
a = 2;
b = 3;
print( "5=" + foo() );
