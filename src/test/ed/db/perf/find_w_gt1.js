/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end 
 */

db = connect( "ed_perf_test1" );
t = db.sort2;
t.drop();

size = 500000;


for ( var i=0; i<size; i++ ){
    t.save( { num : i } );
    if ( i == 0 )
        t.ensureIndex( { num : 1 } );
}


calls = 100;

results = {};

results.oneInOrderGTFirst = Date.timeFunc(
    function(){
        assert( t.find( { num : {$gt : 5} } ).sort( { num : 1 } ).limit(10).toArray().length == 10);
    } , calls );

results.oneInOrderGTLast = Date.timeFunc(
    function(){
        assert( t.find( { num : {$gt : size-20 }} ).sort( { num : 1 } ).limit(10).toArray().length == 10);
    } , calls );


//  Uncomment once 923 is fixed

//assert(   0.25 < (results.oneInOrderGTFirst / results.oneInOrderGTLast) > 4.0,
//        "first / last (" + results.oneInOrderGTFirst + " / " + results.oneInOrderGTLast + " ) = " +
//        results.oneInOrderGTFirst /  results.oneInOrderGTLast + " not in [0.25, 4.0]" );
