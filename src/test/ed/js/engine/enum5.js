

Function.prototype.z = 1;
function Foo(){
}

for ( x in Foo ) print( x );

delete Function.prototype.z;
