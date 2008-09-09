
// setup.js
assert( db );

function checkDB( name , length ){
    if ( Cloud.findDBByName( name ) )
        return;

    assert.eq( length , db.dbs.find().toArray().length , "no [" + name + "] db, but lengths don't match for creating new one" );

    log( "adding fake db [" + name + "]" );
    db.dbs.save( { name : name , type : "TEST" , server : "FAKE" } );
}

function checkPool( name , length ){
    if ( Cloud.Pool.findByName( name ) )
        return;

    assert.eq( length , db.pools.find().toArray().length , "no [" + name + "] pool, but lengths don't match for creating new one" );

    log( "adding fake pool [" + name + "]" );
    db.pools.save( { name : name , envType : "TEST" , machines : [ "FAKE" ] } );
}

checkDB( "test1" , 0 );
checkDB( "prod1" , 1 );
checkPool( "test1" , 0 );
checkPool( "prod1" , 1 );


    
