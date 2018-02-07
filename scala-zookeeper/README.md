# scala-zookeeper
1. Run Zookeeper from docker-compose
2. Run Monitor.scala - it should create zNode if not exist and monitor given path
3. To change value in node path - download zookeeper instalation:
    
    3.1 Connect to Zookeeper - bin/zkCli.sh -server localhost:2181
 
    3.2 Set value for path -  set /ActiveStack "V666"
 
    3.3. Observe changes on Monitor console

    

