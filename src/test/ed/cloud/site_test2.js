

s = new Cloud.Site( "laksjdalsdj" );

assert.eq( 0 , s.dbs.length );
assert( s.upsertDB( "dev" , "test1" ) );
assert( ! s.upsertDB( "dev" , "test1" ) );
assert.eq( 1 , s.dbs.length );

assert.raises( 
    function(){
        assert( s.upsertDB( "devasdasd" , "asdasds" ) );        
    }
);
assert.eq( 1 , s.dbs.length );
assert.eq( "dev" , s.findDB( "dev" ).name );
assert.eq( "test1" , s.findDB( "dev" ).server );
assert.eq( "test1" , s.findDB( s.dbs[0].id ).server );


assert.eq( 0 , s.environments.length );
assert( s.upsertEnvironment( "dev" , "master" , null , "test1" ) );
assert.eq( 1 , s.environments.length );
assert.eq( "dev" , s.environments[0].db );
assert( ! s.upsertEnvironment( "dev" , "master" , null , "test1" ) );


assert.raises(
    function(){
        assert( s.upsertEnvironment( "dev" , "master" , "a123123xssd" , "test1" ) );
    }
);

assert.raises(
    function(){
        assert( s.upsertEnvironment( "dev" , "master" , null , "teasdasd" ) );
    }
);


assert( s.upsertDB( "www" , "prod1" ) );
assert.eq( 2 , s.dbs.length );
assert( s.removeDB( "www" ) );
assert.eq( 1 , s.dbs.length );
assert( s.upsertDB( "www" , "prod1" ) );
assert.eq( 2 , s.dbs.length );

assert( s.upsertEnvironment( { name : "v1" , branch : "stable" , db : "www" , pool : "prod1" , aliases : "www" } ) );
assert.eq( "v1" , s.findEnvironment( "www" ).name );
assert( ! s.upsertEnvironment.apply( s , null , { name : "v1" , branch : "stable" , db : "www" , pool : "prod1" , aliases : "www" } ) );
assert( s.upsertEnvironment.apply( s , null , { name : "v1" , branch : "stable" , db : "www" , pool : "prod1" , aliases : "www,play" } ) );
assert.eq( 2 , s.environments.length );
assert.eq( "v1" , s.findEnvironment( "v1" ).name );
assert.eq( "v1" , s.findEnvironment( "www" ).name );
assert.eq( "v1" , s.findEnvironment( "play" ).name );

assert.eq( 2 , s.environments.length );

assert.raises(
    function(){
        s.removeDB( "www" );
    }
);


assert( s.upsertDB( { name : "z" , server : "prod1" } ) );
assert( ! s.upsertDB( { name : "z" , server : "prod1" } ) );
