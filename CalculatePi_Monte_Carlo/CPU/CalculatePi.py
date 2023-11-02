from math import *

radius = 100;
inCircle = 0;
total = radius * radius;

# Note: range(a, b) includes a but excludes b
for x in range(0, radius):
    x = x + 0.5
    print("x: ", x)
    for y in range(0, radius):
        y = y + 0.5
        print("y: ", y)
        if (x*x+y*y <= radius * radius):
            inCircle = inCircle + 1

print(4*inCircle / total)