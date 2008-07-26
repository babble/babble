
for(var key in []){ print(key); }
print( "POS0" );
Array.prototype.fooooooenum3 = function(){ print("Hi"); }
for(var key in []){ print(key); }
print( "POS1" );
for(var key in [1,2]){ print(key); }

print( "POS2" );

for(var key in (new Object())){ print( "Object [" + key + "]" ); }
for(var key in (new Date())){ print( "Date [" + key + "]" ); }
for(var key in (new RegExp("/"))){ print( "RegExp [" + key + "]" ); }

print( "POS3" );
for(var key in []){ print(key); }
for(var key in ""){ print(key); }

print( "POS4" );
String.prototype.fooooooenum3 = 7;
for(var key in ""){ print(key); }

