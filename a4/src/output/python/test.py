def funcA(paramA):
    paramA = None
    foo = 42
    foo = None
    funcA = None
    if paramA > 0:
        foo = None

def funcC(paramA):
    funcC = None
    foo = funcC(1)

def funcB(paramB):

funcA(1)
