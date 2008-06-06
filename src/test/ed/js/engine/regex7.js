
var ary = "abcdef".match(/(abc)(def)(ooo)?/)
print(ary.length);
print(ary[0]);
print(ary[1]);
print(ary[2]);
print( typeof ary[3] );
print(ary[3] == null );
