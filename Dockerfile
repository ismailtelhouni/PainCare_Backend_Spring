# Étape 1 : Utiliser une image Maven pour construire l'application
FROM maven:3.8.5-openjdk-17 AS build

# Définir le répertoire de travail
WORKDIR /app

# Copier le fichier de configuration Maven et les fichiers de l'application
COPY pom.xml .
COPY src ./src

# Construire l'application
RUN mvn clean package -DskipTests

# Étape 2 : Utiliser une image OpenJDK pour exécuter l'application
FROM openjdk:17

# Copier l'artefact généré de l'étape précédente
COPY --from=build /app/target/pain-care-backend.jar pain-care-backend.jar

# Définir le point d'entrée de l'application
ENTRYPOINT ["java", "-jar", "/pain-care-backend.jar"]
