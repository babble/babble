

print(Math.sin(0) )
print(Math.sin(Math.PI / 2) )
print(Math.sin(Math.PI * 3 / 2) )

print(Math.cos(Math.PI) )

print(Math.pow(10,0) )
print(Math.pow(10,2) )

print(Math.pow(2,2) )

Math.PI = 0;
print( Math.PI );
Math.max( 2, 4 );

tempFunc = Math.max;
Math.max = "foo";
print( Math.max );
Math.max = tempFunc;
