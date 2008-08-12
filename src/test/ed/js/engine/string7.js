
s = "hellothere";

print(s.slice(0) )
print(s.slice(1) )
print(s.slice(1,4) )
print(s.slice(-1) )
print(s.slice(1,-1) )
print(s.slice(0,20) )
print(s.slice(-4) )

s = "asdasdasdasdsd23rasdadasd";
for ( var i=-50; i<50; i++ )
    for ( var j=-50; j<50; j++ )
	print( s.slice( i , j ) );
