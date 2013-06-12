hiccupspotter
=============

My internet connection has small hiccups - it runs fine for hours and then several times I have to wait a minute to open a page. 

With this simpel program, I measure what is going on before calling the service desk:

    $ java -jar ~v/.m2/repository/hiccupspotter/hiccupspotter/0.1-SNAPSHOT/hiccupspotter-0.1-SNAPSHOT.jar 
    Fetching http://www.google.com
    Press Ctrl-D to exit
    12/06/13 22:29:44: 140ms
    12/06/13 22:30:05: 73ms
    Exit
    2 requests in 25630ms
    min/max: 73/140
    failed: 0
    >15s: 0
