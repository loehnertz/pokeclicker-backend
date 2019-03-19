# backend

[![Build Status](http://jenkins.uva-se.nl/buildStatus/icon?job=Backend)](http://jenkins.uva-se.nl/job/Backend/)

The back-end of PokéClicker

Application is running at the following URL: http://docker.uva-se.nl/users


## Building & Running

### Mandatory steps
1) Make sure that `maven` is installed
2) Make sure that `docker` is installed and running
3) Make sure that `docker-compose` is installed
4) Copy the `.env.template` file to a file with the name `.env`
and change the values accordingly
5) Run `mvn clean package`

### Executing the full stack locally
1) Run `docker-compose build --no-cache`
2) Run `docker-compose up --force-recreate`

### Developing locally
1) Run `docker-compose up --force-recreate database redis`
2) Run the `backend` manually making sure that it has access to
the environment variables from the `.env` file

The application should now be accessible under: `http://localhost:${backend_port}/`


## Remarks about the environment variables

- The variables `db_host`, `redis_master_host`, and `redis_slave_host` are directly mapped to `docker-compose`
services. Thus, the chosen names in the `docker-compose.yml` strictly have to match those two variables.
- The variables `backend_port`, `db_port`, and `redis_port` are also used 
to expose the respective services to your host machine.
Consequently, make sure that you don't overlap a port already used on your host machine.


## Deployment

The application is built in a way that it is fully session-less, meaning that the `backend`
service can be easily horizontally scaled. Only the `database` and `redis` services need to stay
singleton instances.


## TODO

Add more information on the `Deployment`.
