FROM maven:3.8.3-openjdk-17
 
WORKDIR /app
 
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
RUN mvn dependency:go-offline
 
COPY src ./src
 
CMD ["mvn", "spring-boot:run"]