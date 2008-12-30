
print( null ==
       function(){
       }() 
     );

print( null ==
       function(){
           a = 5;
       }() 
     );

print( null ==
       function(){
           var a = 5;
       }() 
     );

print( null ==
       function(){
           var a = 5;
           a += 3;
       }() 
     );

print( 
    function(){
        var a = 5;
        a += 3;
        return a;
    }() 
);
