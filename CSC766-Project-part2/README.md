# 766project
766 project

The implementation is in LER/FormatListener.java
It is implemented by making record of all relevant data structures and when the listener
exists the R node, which is the last part of LER notations, it tries to remove redundancies.

To compile, run:

1.javac Glory*.java
2.javac Glory.java DirectiveListener.java FormatListener.java

To run test case:

java Glory ../testcases/case.txt

Example output:
Optimized LER: 

Optimized LER:
Σj∫0,N∫b[j]=a[2*i]

Optimized Program:
for (j=0;j<N;j++) {
 a[2*i] = a[2*i] + b[j]
}