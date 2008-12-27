
s = new Cloud.Server( "asldjsad" );
assert.eq( "127.0.0.1:27016" , s.getGridLocation() );

s = new Cloud.Server( "iad-sb-n6.10gen.cc" );
assert.eq( "iad-sb-grid.10gen.cc:27016" , s.getGridLocation() );

// dallas is a bit different right now
// TODO: figure out a better way to test this
//s = new Cloud.Server( "dal-sb-n6.10gen.cc" );
//assert.eq( "dal-sb-grid-l.10gen.cc:27016,dal-sb-grid-r.10gen.cc:27016" , s.getGridLocation() );

