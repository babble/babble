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
 *   Compares performance of using a range selector object {x : {$gt:y, $lt:z}} + sort
 *    versus a {x : {$gt : y}} + sort + limit
 * 
 * @param dbConn
 */
function testFindRangeVsLTAndLimit(dbConn) {

    // remove once 927 fixed
    print("ed/db/find1.js:testFindRangeVsLTAndLimit() - not run - BUG 927");
    return;

    var results = {};
    var t = dbConn[collection_name];

    results.ltAndLimit = Date.timeFunc(
        function(){
            assert(t.find({num: {$gte : myRandom.nextInt(size-11)}}).sort({num: 1}).limit(10).toArray().length == 10);
        } , calls );

    results.useRange = Date.timeFunc(
        function(){
            var n = myRandom.nextInt(size-11);
            assert(t.find({num: {$gte : n, $lt : n+10}}).sort({num: 1}).toArray().length == 10);
        } , calls );


     assert(   0.75 < (results.ltAndLimit / results.useRange) > 1.25,
            "first / last (" + results.ltAndLimit + " / " + results.useRange + " ) = " +
            results.ltAndLimit /  results.useRange + " not in [0.75, 1.25]" );
}

/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end, using $lt
 */
function testFindLTFrontBack(dbConn) {

    // remove once 924 fixed
    print("ed/db/find1.js:testFindLTFrontBack() - not run - BUG 924");
    return;

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


    assert(   0.9 < (results.oneInOrderLTFirst / results.oneInOrderLTLast) > 1.1,
        "first / last (" + results.oneInOrderLTFirst + " / " + results.oneInOrderLTLast + " ) = " +
        results.oneInOrderLTFirst /  results.oneInOrderLTLast + " not in [0.9, 1.1]" );
}



/**
 *  Tests fetching a set of 10 objects in sorted order, comparing getting
 *  from  front of collection vs end
 */
function testFindGTFrontBack(dbConn) {
    // remove once 923 fixed
    print("ed/db/find1.js:testFindGTFrontBack() - not run - BUG 923");
    return;

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


    assert(   0.25 < (results.oneInOrderGTFirst / results.oneInOrderGTLast) > 4.0,
            "first / last (" + results.oneInOrderGTFirst + " / " + results.oneInOrderGTLast + " ) = " +
            results.oneInOrderGTFirst /  results.oneInOrderGTLast + " not in [0.25, 4.0]" );

}

var db = connect( "ed_perf_find_tests" );

testFindRangeVsLTAndLimit(db);
testFindLTFrontBack(db);
testFindGTFrontBack(db);