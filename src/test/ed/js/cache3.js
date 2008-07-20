
/**
*    Copyright (C) 2008 10gen Inc.
*  
*    This program is free software: you can redistribute it and/or  modify
*    it under the terms of the GNU Affero General Public License, version 3,
*    as published by the Free Software Foundation.
*  
*    This program is distributed in the hope that it will be useful,
*    but WITHOUT ANY WARRANTY; without even the implied warranty of
*    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
*    GNU Affero General Public License for more details.
*  
*    You should have received a copy of the GNU Affero General Public License
*    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

stupid = {inc: 0};
var foo = function(args){
    stupid.inc += 2;
    return stupid.inc;
};

assert(foo.cache(1000*20, {blah:1}) == 2);
assert(foo.cache(1000*20, {blah:1}) == 2);

var obj = 
    { "title" : "Second Post - Silicon Alley Insider" , 
      "user" :
      {  "_id" : ObjectId( "4852e082725a77df0037983d" ) ,  "name" :
         "Some Admin" ,  "email" : "sadmin@10gen.com" ,  "pass_ha1_name" :
         "ee7544ba4411d9a060dc3d1e2b6477a9" ,  "pass_ha1_email" :
         "d954f282c783e1264761286e31502a54" ,  "permissions" :
         [ "admin" , "confirmed_email" ] ,  "uniqueness_hash" :
         "0abbe73d2a7d368a4e967fdc34d40305" ,  "tokens" :
         [   {   "hash" : "40514d026a1f0588b8f4b93b7375e54a" ,   "expires" :
                 new Date( 1213480126963 )     }
         ]  }
    };

assert(foo.cache(1000*10, obj) == 4);
assert(foo.cache(1000*10, obj) == 4);

obj.title = "Comments Defeated - Silicon Alley Insider";

assert(foo.cache(1000*10, obj) == 6);
