/**
 *  Performance tests for various finders
 */

var calls = 100;
var rnd = core.util.random();
var myRandom = rnd.getRandom(seed);
var size = 500000;
var collection_name = "sort2";

function testSetup(dbConn) {
    var t = dbConn[collection_name];
    t.drop();

    for (var i=0; i<size; i++){
        t.save({ num : i });
        if (i == 0 )
            t.ensureIndex( { num : 1 } );
    }
}

/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end, using $lt
 */
function testFindLTFrontBack(dbConn) {

    var results = {};
    var t = dbConn[collection_name];

    results.oneInOrderLTFirst = Date.timeFunc(
        function(){
            assert( t.find( { num : {$lt : 20} } ).sort( { num : 1 } ).limit(10).toArray().length == 10);
        } , calls );

    results.oneInOrderLTLast = Date.timeFunc(
        function(){
            assert( t.find( { num : {$lt : size-20 }} ).sort( { num : 1 } ).limit(10).toArray().length == 10);
        } , calls );


    assert(   0.9 < (results.oneInOrderLTFirst / results.oneInOrderLTLast) < 1.1,
        "first / last (" + results.oneInOrderLTFirst + " / " + results.oneInOrderLTLast + " ) = " +
        results.oneInOrderLTFirst /  results.oneInOrderLTLast + " not in [0.9, 1.1]" );
}



/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end
 */
function testFindGTFrontBack(dbConn) {

    var results = {};
    var t = dbConn[collection_name];
    
    results.oneInOrderGTFirst = Date.timeFunc(
        function(){
            assert( t.find( { num : {$gt : 5} } ).sort( { num : 1 } ).limit(10).toArray().length == 10);
        } , calls );

    results.oneInOrderGTLast = Date.timeFunc(
        function(){
            assert( t.find( { num : {$gt : size-20 }} ).sort( { num : 1 } ).limit(10).toArray().length == 10);
        } , calls );


    assert(   0.25 < (results.oneInOrderGTFirst / results.oneInOrderGTLast) < 4.0,
            "first / last (" + results.oneInOrderGTFirst + " / " + results.oneInOrderGTLast + " ) = " +
            results.oneInOrderGTFirst /  results.oneInOrderGTLast + " not in [0.25, 4.0]" );

}

var db = connect( "ed_perf_find_tests" );

testSetup(db);

testFindLTFrontBack(db);
testFindGTFrontBack(db);