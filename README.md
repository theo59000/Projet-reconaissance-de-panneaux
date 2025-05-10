# Projet de Reconnaissance de Panneaux

Ce projet permet de détecter et reconnaître des panneaux de signalisation à partir d'images ou de vidéos en utilisant un modèle de deep learning TensorFlow.

## Prérequis

- Python 3.8 ou supérieur
- Java JDK 11 ou supérieur
- VLC Media Player (pour la détection vidéo)

## Installation

1. Cloner le dépôt :
```bash
git clone https://github.com/theo59000/Projet-reconaissance-de-panneaux.git
cd Projet-reconaissance-de-panneaux
```

2. Installer les dépendances Python :
```bash
pip install tensorflow flask pillow numpy
```

3. Installer les dépendances Java :
   - Téléchargez les fichiers JAR suivants et placez-les dans le dossier `Projet/lib/` :

   a. VLCJ (pour la lecture vidéo) :
   - [vlcj-4.8.2.jar](https://repo1.maven.org/maven2/uk/co/caprica/vlcj/4.8.2/vlcj-4.8.2.jar)
   - [vlcj-natives-4.8.2.jar](https://repo1.maven.org/maven2/uk/co/caprica/vlcj-natives/4.8.2/vlcj-natives-4.8.2.jar)

   b. JNA (Java Native Access) :
   - [jna-5.13.0.jar](https://repo1.maven.org/maven2/net/java/dev/jna/jna/5.13.0/jna-5.13.0.jar)
   - [jna-platform-5.13.0.jar](https://repo1.maven.org/maven2/net/java/dev/jna/jna-platform/5.13.0/jna-platform-5.13.0.jar)

   c. OpenCV (pour le traitement d'images) :
   - [opencv-4.8.0.jar](https://github.com/opencv/opencv/releases/download/4.8.0/opencv-480.jar)
   - [opencv_java480.dll](https://github.com/opencv/opencv/releases/download/4.8.0/opencv_java480.dll) (Windows)
   - [libopencv_java480.so](https://github.com/opencv/opencv/releases/download/4.8.0/libopencv_java480.so) (Linux)
   - [libopencv_java480.dylib](https://github.com/opencv/opencv/releases/download/4.8.0/libopencv_java480.dylib) (Mac)

   d. JSON (pour le traitement des réponses API) :
   - [json-20231013.jar](https://repo1.maven.org/maven2/org/json/json/20231013/json-20231013.jar)

   Note : Pour OpenCV, assurez-vous que le fichier natif (.dll, .so ou .dylib) est dans le PATH système ou dans le même dossier que le JAR.

## Lancement de l'API TensorFlow

1. Ouvrir une fenêtre de commande (CMD) :
   - Appuyez sur `Windows + R`
   - Tapez `cmd` et appuyez sur Entrée

2. Naviguer vers le dossier du projet :
```bash
cd chemin/vers/Projet-reconaissance-de-panneaux
```

3. Lancer l'API TensorFlow :
```bash
python api_tensor.py
```

L'API démarrera sur `http://localhost:5000`. Gardez cette fenêtre ouverte pendant l'utilisation de l'application.

## Lancement de l'Application

1. Compiler le projet Java :
```bash
javac -cp "Projet/lib/*" Projet/src/*.java
```

2. Lancer l'application :
```bash
java -cp "Projet/lib/*;Projet/src" Interface_image1
```

## Utilisation

### Détection d'Images
1. Cliquez sur "Choisir une image" pour sélectionner une image
2. Cliquez sur "Détection panneaux" pour lancer la détection
3. Les résultats s'afficheront dans une nouvelle fenêtre

### Détection Vidéo
1. Cliquez sur "Détection Vidéo" pour ouvrir la fenêtre de détection vidéo
2. Cliquez sur "Ouvrir Vidéo" pour sélectionner une vidéo
3. Dessinez une zone ROI sur la vidéo en cliquant et faisant glisser
4. La détection démarrera automatiquement sur la zone sélectionnée

### Options
- Échelle de l'image : Ajustez la taille de l'image principale
- Échelle de détection : Ajustez la taille de l'image de détection
- Afficher les bordures : Activez/désactivez l'affichage des bordures
- Détection automatique : Activez la détection automatique lors du chargement d'une image

## Structure du Projet

```
Projet-reconaissance-de-panneaux/
├── Projet/
│   ├── src/
│   │   ├── Interface_image1.java
│   │   └── VideoDetectionWindow.java
│   └── lib/
│       ├── vlcj-4.8.2.jar
│       └── json-20231013.jar
├── Detection_panneaux-11/
│   ├── my_model.keras
│   └── test/
│       └── objects_label_map.pbtxt
├── api_tensor.py
└── README.md
```

## Notes

- Assurez-vous que l'API TensorFlow est en cours d'exécution avant de lancer l'application Java
- Le modèle de détection est entraîné sur un ensemble spécifique de panneaux de signalisation
- Les performances peuvent varier selon la qualité de l'image ou de la vidéo

## Dépannage

Si vous rencontrez des erreurs lors de l'exécution :

1. Vérifiez que VLC est correctement installé :
   ```bash
   vlc --version
   ```

2. Assurez-vous que tous les fichiers JAR sont présents dans le dossier `Projet/lib/`

3. Vérifiez que le chemin vers VLC est correctement configuré dans les variables d'environnement système

4. En cas d'erreur "NoClassDefFoundError", vérifiez que tous les fichiers JAR sont bien inclus dans le classpath

5. Si vous rencontrez des problèmes de compilation ou d'exécution inexpliqués :
   - Supprimez le dossier `bin` s'il existe
   - Exécutez `javac -cp "Projet/lib/*" Projet/src/Interface_image1.java` pour recompiler
   - Si le problème persiste, essayez de nettoyer le workspace :
     ```bash
     # Windows
     rmdir /s /q bin
     del /s /q *.class
     
     # Linux/Mac
     rm -rf bin/
     rm -f *.class
     ```
   Puis recompilez le projet.

6. Pour nettoyer le workspace sous VSCode :
   - Appuyez sur `Ctrl+Shift+P` (Windows/Linux) ou `Cmd+Shift+P` (Mac)
   - Tapez "Java: Clean Java Language Server Workspace"
   - Sélectionnez "Restart and delete" dans le menu déroulant
   - Attendez que VSCode redémarre
   - Si le problème persiste, vous pouvez aussi :
     - Fermer VSCode
     - Supprimer le dossier `.vscode` du projet
     - Supprimer le dossier `bin` s'il existe
     - Rouvrir VSCode et recompiler le projet

## Support

Pour toute question ou problème, veuillez créer une issue dans le dépôt GitHub du projet. 
