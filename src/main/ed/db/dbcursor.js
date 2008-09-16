/**
 *  API for the DB  Cursor class
 * *  
 * @fileOverview dbcursor.js - 10gen Database  Cursor API
 * @name 10gen Database DB Cursor Object API
 */
 
 /**
  * Database Cursor API 
 *  @class 10gen Database Cursor  API
 *
 *  When you call find() on a database collection, a DBCursor is returned which can be used to iterate the results.
 * 
 *  Example:
 *    var cursor = db.students.find();
 * 
 * @docmodule System.DB.cursor
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
 * Limits the returned result set to a maximum number of objects.  This function is analogous to 
 * the LIMIT keyword in MySQL.
 * 
 * Use of limit(), when appropriate, is very important for optimal performance.
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
 * Skips past n objects before beginning to return data.  Often used with "paged" user interfaces to 
 * start at a certain position.  Skip is performed server side, and is thus reasonable fast if the 
 * query is not too expensive.
 * </p>
 * 
 * <p>Example :</p>
 * <code>
 * var cursor = db.test.find().skip(20);
 * </code>
 * 
 * @param {Integer} count Number of objects to skip 
 * @return {Cursor} 
 */
DBCursor.prototype.skip = function(count) {
    throw {exception : "Native Call skip() : shouldn't have JS implementation"};
}
