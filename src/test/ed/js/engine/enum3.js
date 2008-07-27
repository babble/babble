
for(var key in []){ print(key); }
Array.prototype.arr1 = function(){ print("Hi"); }
for(var key in []){ print(key); }
for(var key in [1,2]){ print(key); }

for(var key in (new Object())){ print( "Object [" + key + "]" ); }
for(var key in (new Date())){ print( "Date [" + key + "]" ); }
for(var key in (new RegExp("/"))){ print( "RegExp [" + key + "]" ); }

print( "--" );
for(var key in []){ print(key); }
for(var key in ""){ print(key); }
print( "--" );

String.prototype.stre1 = 7;
for(var key in "abc"){ print(key); } // TODO: fix



