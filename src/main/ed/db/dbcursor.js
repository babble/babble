/**
 *  API for the DB  Cursor class
 * *  
 * @fileOverview dbcursor.js - 10gen Database  Cursor API
 * @name 10gen Database DB Cursor Object API
 */
 
 /**
  * Database Cursor API 
 *  @class 10gen Database Cursor  API
 */
 var DBCursor = function(){}

/**
 *  <p>
 * Sorts the returned object set based on the supplied criteria
 * </p>
 * 
 * <p> 
 * Criteria defines which field to sort on, and sort oder (1 = ascending, -1 = descending)
 * </p>
 * 
 * <p>Example :</p>
 * <code>
 * db.test.find().sort({date:1});
 * </code>
 * 
 *  <p>
 * Note : alpha: only works for indexed keys
 * </p>
 * 
 * @param {Object} criteria Criteria object that specifies the key to sort on, and the order.  Use 1 for ascending order, -1 for decending.
 * @return {Cursor} 
 */
DBCursor.prototype.sort = function(criteria) {
    throw {exception : "Native Call sort() : shouldn't have JS implementation"};
}

/**
 *  <p>
 * Limits the returned result set to a fixed number of objects
 * </p>
 * 
 * <p>Example :</p>
 * <code>
 * db.test.find().limit(100);
 * </code>
 * 
 * @param {Integer} count Maximum number of objects that will be returned by the find()
 * @return {Cursor} 
 */
DBCursor.prototype.limit = function(count) {
    throw {exception : "Native Call limit() : shouldn't have JS implementation"};
}

/**
 *  <p>
 * Skips the number of specified objects that would be returned by the find()
 * </p>
 * 
 * <p>Example :</p>
 * <code>
 * db.test.find().skip(20);
 * </code>
 * 
 * @param {Integer} count Number of objects to skip 
 * @return {Cursor} 
 */
DBCursor.prototype.skip = function(count) {
    throw {exception : "Native Call skip() : shouldn't have JS implementation"};
}
