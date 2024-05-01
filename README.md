#### README.md

Teammate: Xiangwei Wang; Ruifeng Zhang

There are two subfolders:

- CSC766-Project-part1

For Part1, `CSC766-Project-part1.pdf` is the written LER notation, optimized LER notation and time comparision. `benchmark`  folder includes optmized code.

- CSC766-Project-part2

The implementation is in LER/FormatListener.java
It is implemented by making record of all relevant data structures and when the listener
exists the R node, which is the last part of LER notations, it tries to remove redundancies.

To compile, run:

```shell
javac Glory*.java
javac Glory.java DirectiveListener.java FormatListener.java
```

To run test case:

```
java Glory ../testcases/case.txt
```

Example output:
Optimized LER: 

Optimized LER:
Σj∫0,N∫b[j]=a[2*i]

Optimized Program:
for (j=0;j<N;j++) {
 a[2*i] = a[2*i] + b[j]
}
