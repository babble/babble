

for(var key in (new Date())){ assert(0); }

Date.prototype.foo = 7;
found = 0;
for(var key in (new Date())){ found++; }
assert( found == 1 );

Date.prototype.dontEnum( "foo" );
for(var key in (new Date())){ assert(0); }


Date.prototype.bar = 7;
found = 0;
for(var key in (new Date())){ found++; }
assert( found == 1 );

Date.prototype._dontEnum = true;
for(var key in (new Date())){ assert(0); }
