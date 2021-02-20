#### ECSE 316: Signals and Networks
***
**Assignment 1 - Network Programming and DNS**

Charles Bourbeau (260868653), Felix Simard (260865674)

Feb 22nd, 2021

***
IDE: 
```
IntelliJ IDEA
```

Java:

```
# java -version

openjdk version "1.8.0_275"
OpenJDK Runtime Environment Corretto-8.275.01.1 (build 1.8.0_275-b01)
OpenJDK 64-Bit Server VM Corretto-8.275.01.1 (build 25.275-b01, mixed mode)
```

Compile:
```
# javac DnsClient.java
```

Run:
```
# java DnsClient –t 10 –r 2 –mx @8.8.8.8 mcgill.ca

DnsClient sending request for mcgill.ca
Server: 8.8.8.8
Request type: A

Response received after 0.024 seconds (0 retries)

***Answer Section (1 records)***
IP      132.216.177.160 264     nonauth

```

***