
print( Array.apply == null );

Function.prototype.silly = 18;
print( function(){ return 1}.silly );

delete Function.prototype.silly;
