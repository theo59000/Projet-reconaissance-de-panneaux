Bonjour,

bienvenue sur notre projet de reconaissance de panneaux.

Pour lancer le code, il vous faudra Java (nous avons utiliser Java 21.0.5).

Le code est localisé dans Projet-reconaissance-de-panneaux\Projet\src sous la forme d'un fichier Main.java et d'un fichier tools.java.

normalement la bibliothèque opencv est importé directement, mais vérifié bien que vous avez le fichier opencv-2413.jar 
dans le dossier Projet-reconaissance-de-panneaux\Projet\lib

Nous avons utilisé la version 2.4.13.0 d'opencv. 

## Partie réseaux de neurones
1) Le réseaux de neurones ne peut pas être push sur GitHub car trop volumeux,
Pour l'avoir en local, lancer le fichier tensorflow/data.py

2) Lancer l'api qui va appeler ce réseau , ouvrir l'éditeur de commande 
dans le dossier Projet-Reconaissance-de-panneaux et écrire :
python api.py 

3) Lancer l'interface , ouvrir un deuxième éditeur de commande dans
le dossier Projet-reconaissance-de-panneaux\Projet\src , tapez :
javac SimpleGui.java (pour compiler le code)
java SimpleGui       (lancer l'interface)