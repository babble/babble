/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end, using $lt
 */

db = connect( "ed_perf_find_w_lt_sort1" );
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

results.oneInOrderLTFirst = Date.timeFunc(
    function(){
        assert( t.find( { num : {$lt : 20} } ).sort( { num : 1 } ).limit(10).toArray().length == 10);
    } , calls );

results.oneInOrderLTLast = Date.timeFunc(
    function(){
        assert( t.find( { num : {$lt : size-20 }} ).sort( { num : 1 } ).limit(10).toArray().length == 10);
    } , calls );


//  Uncomment once 924 is fixed

//assert(   0.9 < (results.oneInOrderLTFirst / results.oneInOrderLTLast) > 1.1,
//        "first / last (" + results.oneInOrderLTFirst + " / " + results.oneInOrderLTLast + " ) = " +
//        results.oneInOrderLTFirst /  results.oneInOrderLTLast + " not in [0.9, 1.1]" );
