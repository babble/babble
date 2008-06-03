
var d = new Date( 999999999 );
d.setMonth(0);
d.setDate(3);
d.setYear(1984); 

print( d );
print( "Z" + d );
print( 5 + d );

var d2 = new Date( 999999999 );
d2.setMonth(0);
d2.setDate(6); // Friday; lexically less than Tuesday
d2.setYear(1984);

var s = "" + d;
var s2 = "" + d2;

print(d < d2);
print(s < s2);

