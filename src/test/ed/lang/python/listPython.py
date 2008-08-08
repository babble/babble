def getList():
    return [1, 2]

def pyManipList(l): # expecting [1, 3, 5]
    if len([x for x in l if x % 3 == 0]) != 1: return False

    if l.count(3) != 1: return False


    return True

def pyGetLength(l):
    return len(l)

def pyCheckEven(l, n):
    return l[n] % 2 == 0
