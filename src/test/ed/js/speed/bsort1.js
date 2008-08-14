/*
 * Generates n random strings and then sorts them using bubble sort.
 * Uses JavaScript types and functions wherever possible.
 *   - Strings to be sorted are JavaScript Strings.
 *   - random methos is a JavaScript function.
 * Execute in interpreted mode: 
 *   java -cp js.jar org.mozilla.javascript.tools.shell.Main bsort1.js
 * Compile and execute:
 *   java -cp js.jar org.mozilla.javascript.tools.jsc.Main bsort1.js
 *   java -cp js.jar bsort1 
 */
var num = 10000;  // number of strings to sort
var ssize = 20;  // size of each string.
var chars = [ 
  '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
  'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
  'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
  'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
  'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'
];

function bsort(array){
  var n = array.length;
  for (var i = n - 1; i > 0; i--){
    for (var j = 0; j < i; j++){
      if (array[j] < array[j+1]){
        var t = array[j];
        array[j] = array[j+1];
        array[j+1] = t;
      }
    }
  }
}

function printa(array){
  for (var i = 0; i < array.length; i++){
    print("[" + i + "] " + array[i]);
  }
}

function populate(array, num){
  for (var i = 0; i < num; i++){
    var s = "";
    for (var j = 0; j < ssize; j++){
      var r = Math.floor(chars.length * Math.random());
      s += chars[r];
    }
    array[i] = s;
  }
}

function go() { 
var array = new Array(); // Do not specify the size on purpose



print("Populating the array with " + num + " Strings, each of " + ssize + " characters ...");
var st = (new Date()).getTime();
populate(array, num)
var et = (new Date()).getTime();
print(" ... done. Elapsed time: " + (et - st) + " millis.");

//print("Original Array::"); printa(array);

print("Sorting the array ...");
var st = (new Date()).getTime();
bsort(array);
//array.sort();
et = (new Date()).getTime();
print(" ... done. Elapsed time: " + (et - st) + " millis.");

//print("Sorted Array::"); printa(array);
}


go();
go();
