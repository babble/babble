
s = "foo\nbar";

re = /^bar/;
print(re.test(s))  

re = /^bar/m;
print(re.test(s))  

re = /foo.bar/;
print(re.test(s))  

re = /foo\nbar/;
print(re.test(s))  

re = /.*foo\nbar.*/;
print(re.test(s))  

re = /.*foo.bar.*/;
print(re.test(s))  

re = /.*foo\s+bar.*/;
print(re.test(s))  

