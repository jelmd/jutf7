# jutf7

This library provides UTF-7 and Modified UTF-7 charset implementations for
Java.

Java 6 or later is required.

This version is based on
http://sourceforge.net/projects/jutf7/files/jutf7/1.0.0/
and includes the following minor modifications by Jens Elkner
(Otto-von-Guericke-Universität Magdeburg):
- removed hacks for a flush bug in JDK versions prior to 1.6
- minor code formatting to comply with coding conventions
- adapted to JUnit 4
- uses a pure Ant build instead of Maven
- added version information
- improved encoding performance (approximately 4–5× faster)
- implemented benchmarking for jutf7, Zimbra's UTF-7, and the charutils implementation

# Build
```
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
/bin/ant
```
