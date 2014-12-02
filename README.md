<h1>GoCD Plugin Docker Registry Poller</h1>

The goal of this project is to offer a plugin for the GoCD build management tool. The plugin looks for changes in the Docker registry and triggers a pipeline
when the hash has changed. It enables continuous deployment with Docker from Go.CD.


<h2>ToDo</h2>
It's hard to keep up with two rapidly changing APIs. I've used Docker 1.3.1 and Go 14.3.0. 

<h2>Compiling</h2>
In order to compile the project, you need to download the go-plugin-api-current.jar from the Go-Server. The best way is to copy the JAR from your Go-Server environment and install
it in your local repository.

<blockquote>
mvn install:install-file -Dfile=go-plugin-api-current.jar -DgroupId=com.thoughtworks.go -DartifactId=go-plugin-api -Dversion=14.3.0 -Dpackaging=jar
</blockquote>


<h2>Credits</h2>

The project has been inspired by https://github.com/hammerdr/go-docker-registry-poller and the yum plugin from Thoughtworks.
