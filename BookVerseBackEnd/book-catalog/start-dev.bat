@echo off
echo Starting Book Catalog Service in Development Mode...

set JWT_SECRET=mySecretKeyForDevelopmentOnlyMinimum32Characters
set DB_URL=jdbc:h2:mem:testdb
set DB_USERNAME=sa
set DB_PASSWORD=
set DB_DRIVER=org.h2.Driver
set DB_DIALECT=org.hibernate.dialect.H2Dialect

mvn spring-boot:run -Dspring-boot.run.profiles=dev