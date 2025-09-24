def mult(a, n, b):
    return a*b

def ofp_max(a, b):
    if a > b:
        return a
    else:
        return b

f = 2.34
ff = 2.0
fff = mult(f, 5, ff)
print(fff)
fff = ofp_max(f, ff)
print(fff)
