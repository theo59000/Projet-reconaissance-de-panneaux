
#pip install ultralytics
from ultralytics import YOLO

# Load a model
model = YOLO("yolov8n.pt")  # build a model pretrain

# Use the model
model.train(data="C:\GitFiles\Projet-reconaissance-de-panneaux\Detection_panneaux-18\data.yaml", epochs=50, patience=5)  # mettre le chemin absolu vers data.yaml

#patience : nombre d'époques sans améliorations avant d'arrêter l'entrainement du réseaux prématurément
#les poids des arrêtes ainsi que certaines données sur l'entrainement sont stockées dans le dossier run
                