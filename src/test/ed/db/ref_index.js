db=connect("test");
user=db.users;
media=db.media;

user.drop();
media.drop();

u = { name : "joe" } ;
user.save(u);

m = { song : "la", usr : u } ;
media.save(m);
media.save( { song : "foo" } );

assert( media.find( { usr: u } ).count() == 1 );
media.ensureIndex({usr:1});
assert( media.find( { usr: u } ).count() == 1 );

assert( user.validate().valid );
assert( media.validate().valid );
