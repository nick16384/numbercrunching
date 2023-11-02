from math import *

radius = 10;
inCircle = 0;
total = radius * radius;

for x in range(0, radius - 1):
    for y in range(0, radius - 1):
        if (sqrt(x*x+y*y) <= radius):
            inCircle = inCircle + 1

print(4 * inCircle / total)