var arr = new Array();

var before = arr.approxSize();

arr.push("moooooooooooooooo");

assert.gt(arr.approxSize(), before, "Array size didn't change");