
Below link provides overview of how to enale virtualization in windows.

See the 'Setting Up the Virtualization Environment' topic (https://docs.okd.io/latest/minishift/getting-started/setting-up-virtualization-environment.html) f
or more information.

In my case, I had to start Oracle VM VirtualBox. After this I had to start minishift with following command:

minishift start --vm-driver virtualbox

to get lib logs use following command

minishift start --vm-driver virtualbox --show-libmachine-logs -v5

The above command start minishift with Oracle virtual box.

To gracefully stop minishift use below command:

minishift stop.

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

Below are few errors encountered in the installation and start of minishift:

Error 1 mentioned below was due to path used to start minishift. For some reason minishift or for that sake minikube should be started from C: drive. Created folder minishift, copied the exe and executed from this path above mentioned start command and error was resolved.

1) Error starting the VM: Error creating the VM. Error creating machine: Error in driver during machine creation: open /Users/qrizvi/.minishift/cache/iso/centos/v1
.13.0/minishift-centos7.iso: The system cannot find the path specified.

Error 2 was resolved by executing command minishift delete and then minishift start --vm-driver virtualbox. This deleted the previous state information and created a fresh instance.
 
2) Error starting the VM: Error creating the VM. Error creating machine: Error in driver during machine creation: open minishift-centos7.iso: The system cannot find the path specified.
-- Starting Minishift VM .... FAIL E1121 11:34:49.120202   20724 start.go:491] Error starting the VM: Error getting the state for host: machine does not exist.
Retrying.

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

Below are the configuration parameters or credentails for minishift. Administrator has user name as system and passwprd as admin.

The server is accessible via web console at:
    https://192.168.99.101:8443/console

You are logged in as:
    User:     developer
    Password: <any value>
To login as administrator:
    oc login -u system:admin	
-----------------------------------------------------------------------------------------------------------------------------------------------------------------

Command to run docker deamon present as part of minishift running process
	
@FOR /f "tokens=* delims=^L" %i IN ('minishift docker-env') DO @call %i

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

1.  Docker file:
	FROM openjdk:8-jre-alpine
	ENV CLUSTERSHARDING_FILE sharding-sample-assembly-1.0.jar
	ENV CLUSTERSHARDING_HOME D:\Code\Sharding POC\new\lightbend-microservices-sample\sharding-sample
	EXPOSE 8080
	COPY $CLUSTERSHARDING_FILE $CLUSTERSHARDING_HOME/
	WORKDIR $CLUSTERSHARDING_HOME
	ENTRYPOINT ["sh", "-c"]
	CMD ["exec java -jar $CLUSTERSHARDING_FILE"]


The above docker file creates an image from the jar file which can be used to run the container. Important to note is that jar should be Uber/FAT jar, which means that all the code files and dependencies are build as a single executable jar. sbt creates Uber/FAT jar using assembly plugin. To create a fAT jar using assembly follow below steps:

Under {project-home}/project folder/directory create assembly.sbt. Inside assembly.sbt add following line:

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.6") //note that sbt-assembly version is 0.14.6 which is compatible with sbt 1.0, currently used.

Now run sbt (only sbt) command in the terminal, and then once the project gets hooked, use assembly command. This will lead to creation of Uber(FAT) jar in path {project-home}/target/scala-2.12/<project-name-assembly-1.0.jar>.

Feed this assembled jar to the docker file (put this jar name as CLUSTERSHARDING_FILE name) and then execute {docker build -t clustersampleassemble .} command. 

2. After successful creation of image, check the images (including new one) by executing command - docker images. This will list all the images out of which the latest one will be created just now.

3. Command to run docker command from the DockerFile. Create account in docker hub and push the image to docker hub.
#qad12 is my docker account id.
docker build -t qad12/clustersampleassemble .

4. Then push the image to the docker hub using below mentioned command.

docker push qad12/clustersampleassemble

5. Use below command to create a new app with open shift

	a) oc new-app qad12/clustersampleassemble

	b) oc expose service clustersampleassemble
	
	c) To expose service to external ip use following command:
	   oc patch svc clustersampleassemble -p '{\"spec\":{\"externalIPs\":[\"192.174.120.10\"]}}'
	   https://192.168.99.101:8443/api/v1/namespaces/myproject/services/clustersampleassemble

Above command deploys application to open shift. Application can be accessed using below url. Use developer/developer credential to login as this user has access to default profile.
https://192.168.99.101:8443/console/

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

Below 2 points are not applicable to current project setup.

To run the docker image in a container execute command - docker run <image id>

Even though this information may not be immediately relevant for this topic, but to create a jar with manifest file use command - jar cmf META-INF/MANIFEST.MF <jar_name.jar> <ProjectRootFolder/>. Jar executable is present in java/bin folder. Example command is "C:\Program Files\Java\jdk1.8.0_151\bin\jar" cmf META-INF/MANIFEST.MF test3.jar sample/ . If Manifest is not properly updated then open with 7zip or winrar and select manifest file and go to edit option,change and save the file. It will update manifest file to select proper main class file (Example -Main-Class: sample.sharding.ShardingApp). To run a executable jar file use command java -jar <ExecutableJarName.jar>

----------------------------------------------------------------------------------------------------------------------------------------

Docker Commands:

1) Below command can be used to run cassandra docker image on local machine:
docker run --name docker-cassandra -d cassandra:latest
docker run --name c1 -v /Users/MyProjects/scripts/:/script -d -p "7191:7191" -p "7000:7000" -p "7001:7001" -p "9160:9160" -p "9042:9042" -e 
CASSANDRA_BROADCAST_ADDRESS=192.168.99.100 cassandra:latest

docker run --name docker-cassandra0.1  -d -p "7191:7191" -p "7000:7000" -p "7001:7001" -p "9160:9160" -p "9042:9042" -e CASSANDRA_BROADCAST_ADDRESS=192.168.99.100 cassandra:latest


2) Command to create instance of cqlsh for cassandra container:
docker run -it --link dockercassandra:cassandra --rm cassandra sh -c 'exec cqlsh "$CASSANDRA_PORT_9042_TCP_ADDR"'

docker run -it --link dockercassandra:cassandra --rm cassandra cqlsh cassandra

3) Below command can be used to run Java application with linking to cassandra image:
docker run --name AkkaMicroservice1.1 --link dockercassandra:cassandra -d -p 8082:8082 -p 9042:9042 qad12/akkamicroservice:0.7

-----------------------------------------------------------------------------------------------------------------------------------------------------------------

Below are the commands to create keyspace, table and insert data in Cassandra:

1) create keyspace userkeyspace with replication = {'class':'SimpleStrategy','replication_factor':1};


4) CREATE TABLE userkeyspace.billing_account_by_account (
    account_number text PRIMARY KEY,
    account_active_from timestamp,
    account_name text,
    account_status text,
    account_subtype text,
    account_type text,
  );


insert into userkeyspace.billing_account_by_account ("account_name","account_number","account_status","account_subtype","account_type") values ('ALMA SAVOIE  MRS','260026233606','Disconnected','test_subtype','Resident');

insert into userkeyspace.billing_account_by_account ("account_name","account_number","account_status","account_subtype","account_type") values ('QAD RIZ','260026233605','Connected','test_subtype','Non Resident');

insert into userkeyspace.billing_account_by_account ("account_name","account_number","account_status","account_subtype","account_type") values ('TEST USER','260026233604','Connected','test_subtype','Test Resident');
