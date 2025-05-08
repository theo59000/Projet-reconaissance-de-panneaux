
#pip install ultralytics
from ultralytics import YOLO

# Load a model
model = YOLO("yolov8n.yaml")  # build a new model from scratch

# Use the model
model.train(data="C:\\Users\\chris_cp05xbg\\Documents\\GitHub\\Projet-reconaissance-de-panneaux\\Detection_panneaux-12\\data.yaml", epochs=25, patience=5)  # mettre le chemin absolu vers data.yaml

#patience : nombre d'époques sans améliorations avant d'arrêter l'entrainement du réseaux prématurément
#les poids des arrêtes ainsi que certaines données sur l'entrainement sont stockées dans le dossier run