//
//  replication test : index repl
//
//  do nothing.
//  check to see that zero objects and zero indexes exist in m and s
//  create index in master
//  check repl
//  

__path__._repl( 
    function( dbm , dbs, info){

    assert(dbm.test.count() == 0);
    assert(dbs.test.count() == 0);

    assert(dbm.test.getIndexes().length() == 0);
    assert(dbs.test.getIndexes().length() == 0);

    dbm.test.ensureIndex({n:1});

    assert(dbm.test.getIndexes().length() == 1);
    
    dbm.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    
    print( " ****  20 sec wait for replication, then check slave ****" );
    
    sleep(20000);
    
    assert(dbm.test.getIndexes().length() == 1);
    assert(dbs.test.getIndexes().length() == 1);
    
    dbm.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    
    dbs.test.getIndexes().forEach(function(x) { 
        assert(x.key.n == 1);
    });
    
    assert(dbm.test.validate().valid, "master table test failed validation");
    assert(dbs.test.validate().valid, "slave table test failed validation");
});
