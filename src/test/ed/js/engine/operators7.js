a = 5
print( ( a += 1 ) )
print( a );

a = { a : 1 };
a.a += 1;

print( ( a.a += 1 ) );
print( a.a );


a = 5
print( ( a -= 1 ) )
print( a );

a = { a : 3 };
a.a -= 1;
print( ( a.a -= 1 ) );
print( a.a );



a = 5
print( ( a *= 1 ) )
print( a );

a = { a : 3 };
a.a *= 1;
print( ( a.a *= 1 ) );
print( a.a );



a = 5
print( ( a /= 2 ) )
print( a );

a = { a : 3 };
a.a /= 2;
print( ( a.a /= 2 ) );
print( a.a );


