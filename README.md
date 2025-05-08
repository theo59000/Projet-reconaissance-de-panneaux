# Projet Twizy - Reconnaissance de Panneaux

Ce projet est une application Java qui permet la reconnaissance de panneaux de signalisation à partir d'images et de vidéos.

## Prérequis

- Java JDK 11 ou supérieur
- VLC Media Player (version 3.0 ou supérieure)
- Maven (optionnel, pour la gestion des dépendances)

## Installation des Dépendances

### 1. Installation de VLC Media Player

1. Téléchargez VLC Media Player depuis le site officiel : [https://www.videolan.org/vlc/](https://www.videolan.org/vlc/)
2. Installez VLC en suivant les instructions d'installation standard
3. Assurez-vous que VLC est bien installé et accessible dans votre PATH système

### 2. Installation des Bibliothèques Java

#### Option 1 : Utilisation de Maven (Recommandé)

Si vous utilisez Maven, les dépendances seront automatiquement téléchargées. Le fichier `pom.xml` est déjà configuré avec les dépendances nécessaires.

#### Option 2 : Installation Manuelle

Si vous n'utilisez pas Maven, vous devez télécharger manuellement les fichiers JAR suivants et les placer dans le dossier `Projet/lib/` :

1. VLCJ :
   - [vlcj-4.8.2.jar](https://repo1.maven.org/maven2/uk/co/caprica/vlcj/4.8.2/vlcj-4.8.2.jar)
   - [vlcj-natives-4.8.2.jar](https://repo1.maven.org/maven2/uk/co/caprica/vlcj-natives/4.8.2/vlcj-natives-4.8.2.jar)

2. JNA (Java Native Access) :
   - [jna-5.13.0.jar](https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar)
   - [jna-platform-5.13.0.jar](https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar)

### 3. Configuration de l'Environnement de Développement

#### Pour Eclipse :
1. Clic droit sur le projet
2. Sélectionnez "Build Path" > "Configure Build Path"
3. Dans l'onglet "Libraries", cliquez sur "Add JARs"
4. Sélectionnez tous les fichiers JAR du dossier `Projet/lib/`

#### Pour IntelliJ IDEA :
1. File > Project Structure
2. Dans la section "Libraries", cliquez sur le "+"
3. Sélectionnez "Java"
4. Naviguez vers le dossier `Projet/lib/` et sélectionnez tous les fichiers JAR

## Compilation et Exécution

### Avec Maven :
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="Interface_image1"
```

### Sans Maven :
```bash
javac -cp "Projet/lib/*" Projet/src/Interface_image1.java
java -cp "Projet/lib/*;Projet/src" Interface_image1
```

## Dépannage

Si vous rencontrez des erreurs lors de l'exécution :

1. Vérifiez que VLC est correctement installé :
   ```bash
   vlc --version
   ```

2. Assurez-vous que tous les fichiers JAR sont présents dans le dossier `Projet/lib/`

3. Vérifiez que le chemin vers VLC est correctement configuré dans les variables d'environnement système

4. En cas d'erreur "NoClassDefFoundError", vérifiez que tous les fichiers JAR sont bien inclus dans le classpath

## Support

Pour toute question ou problème, veuillez créer une issue dans le dépôt GitHub du projet. 