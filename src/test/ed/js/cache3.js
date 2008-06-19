
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
