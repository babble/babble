//
//  replication test : basic repl
//
//  Save an object in master
//  Wait for repl to slave
//  modify the object in master
//  Wait for repl to slave
//  check

__path__._repl( 
    function( dbm , dbs, info){

    dbm.test.save({n:1});

    assert( dbm.test.findOne() );
    assert( dbm.test.findOne().n == 1 );
    assert.eq( dbm.test.count() , 1 );

    print( " ****  20 sec wait for replication, then check slave ****" );
    
    sleep(20000);
    
    assert( dbs.test.findOne() , "nothing in slave table" );
    assert( dbs.test.findOne().n == 1 , "what's in slave table is wrong" );

    var cnt = dbs.test.count();    
    assert( cnt == 1 , "object count in slave != 1  found :  " + cnt);
     
    print( " **** modify the object in master ***" );
    
    var o = dbm.test.findOne();
    
    o.n = 2;
    
    dbm.test.save(o);
    assert.eq(dbm.test.count() , 1);

     print( " **** 20 sec wait for replication***" );
    
    sleep(20000);
    
    assert( dbs.test.findOne() , "error : slave table empty" );
    
    // check to see that we have only 1 object, and it reflects the change
    
    var n = dbs.test.count();
    assert( n == 1, "error count != 1 : " + n);
    
    n = dbs.test.find({n:1}).length();
    assert(n == 0, "incorrect count for {n:1} objects in slave table : " + n);

    n = dbs.test.find({n:2}).length();
    assert(n == 1, "incorrect count for {n:2} objects in slave table : " + n);
    
    assert(dbm.test.validate().valid, "master table test failed validation");
    assert(dbs.test.validate().valid, "slave table test failed validation");
});
