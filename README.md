# backend
The back-end of PokéClicker

[![Build Status](http://jenkins.uva-se.nl/buildStatus/icon?job=Backend)](http://jenkins.uva-se.nl/job/Backend/)

Application is running at the following URL: http://docker.uva-se.nl/users

## Building & Running

1) Make sure that `maven` is installed
2) Make sure that `docker` is installed and running
3) Make sure that `docker-compose` is installed
4) Run `mvn clean package`
5) Run `docker-compose build --no-cache`
6) Run `docker-compose up --force-recreate`

The application should now be accessible under:
`http://localhost:8080/`
