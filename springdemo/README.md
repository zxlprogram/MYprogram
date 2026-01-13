# date
2026-01-13
## description
this is the practicing project, it have the most basic object for the maven project
## the structure of the proj
root:
    pom.xml
    ```
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    <groupId>demo</groupId>
    <artifactId>web</artifactId>
    <version>1</version>
    <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <maven.compiler.source>25</maven.compiler.source>
        <maven.compiler.target>25</maven.compiler.target>
    </properties>
</project>
    ```
    src:
        main:
            java:
                demo:
                    web:
                        Main.java
                            ```
                            package demo.web;
                            public class Main {
                                public static void main(Stringp[]args) {
                                    System.out.println("hello world");
                                }
                            }
                            ```
    target:
        classes:
            "same as src/demo..." but Main is .class
    generated-sources:??
    maven-status:??

## why maven
    it is a popular structure so you can merge another project easily
    and it is also convenience to add module