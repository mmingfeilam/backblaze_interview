# Take-home Project
## Assignment
Write a command-line program in Java to perform simple data analysis on some files stored in the B2 cloud storage system. The program should accept two command-line arguments: an application key ID, and an application key to use when connecting to B2.
Using the ​b2-sdk-java​ library and the application key id and key from the command line, find all the buckets that belong to the account, find all the files in those buckets, and count how many times the lower-case letter "a" appears in each file. (You can assume the files contain ASCII.)
Then print a report listing the count and filename for each file, showing how many times the letter "a" appears in the file. The output should be sorted, with primary sorting on the count, and secondary sorting on the file name in the cases where the count is the same. The output should look like this, with a space between the count and the file name:
9 file7.txt 23 file3.txt 23 file4.txt 62 file2.txt
If any exceptions happen during execution, report the error to stderr and exit with non-zero status.

## To Run
- Create runnable jar file called char_counter.jar
- java -jar char_counter.jar -B2_APPLICATION_KEY K000SswEzGehf0fKzj2ZK+THpxRWzrY -B2_APPLICATION_KEY_ID 00035541c4cce760000000001
- Result files will be in the files folder