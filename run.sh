#!/bin/sh

gradle bootRun --args="--spring.config.location=classpath:/config/common/,classpath:/config/dev/ --spring.profiles.active=dev"

mvnd spring-boot:run