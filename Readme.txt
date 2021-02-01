Name: Tri Han
Assignment name: Programming Assignment 1
Date: January 31, 2021

Description: 
For this assignment, we are to build a web server that can process and service HTTP GET requests from web browsers.
When run, this program listens on a port and uses a multi threaded approach to servicing client requests.

Submitted Files:
1. Readme.txt
2. Server.java
3. java-web-server.jar
4. ./files/index.html
5. ./files/data.txt
6. ./files/scu_campus.jpg
7. ./files/scu_campus_2.jpeg
8. ./files/scu_sports.gif
9. ./files/file_not_world_readable.txt
10. ./files/empty_directory

Instructions for Running the Program:
Option 1: Using the jar file
$ java -jar java-web-server.jar -document_root "<absolute_path_to_document_root_here>" -port <port_number>

Option 2: Compile and run the program
$ javac Server.java
$ java Server -document_root "<absolute_path_to_document_root_here>" -port <port_number>
