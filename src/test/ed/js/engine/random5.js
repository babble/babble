print( "--- obj " );
var a = {};
print(a.valueOf() === a);
print(typeof a.valueOf());
print(typeof a );

print( "--- date " );

var d = new Date();
print(typeof d.valueOf());
print(typeof d );

print( "--- number " );

var n = 5;
print(typeof n.valueOf());
print(typeof n );

print( "--- string " );

var s = "abcd";
print(typeof s.valueOf());
print(typeof s );

print( "---" );


a = {};

b = {b: "hi"};

a[b] = 5;

print(a[b]);

d = new Date();

a[d] = 1345;
print(a[d]);

for ( foo in a ){
    print( typeof foo );
}
    
