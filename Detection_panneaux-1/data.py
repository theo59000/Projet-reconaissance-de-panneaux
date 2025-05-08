# from roboflow import Roboflow

# rf = Roboflow(api_key="POKrCxImcg3gAbmhpe2W")
# project = rf.workspace("roboflowrn").project("detection_panneaux") # permet d'importer l'ensemble des images avec labels sur roboflow
# version = project.version(1)
# dataset = version.download("yolov8")


#pip install ultralytics
from ultralytics import YOLO

# Load a model
model = YOLO("yolov8n.yaml")  # build a new model from scratch

# Use the model
model.train(data="data.yaml", epochs=25, patience=5)  # mettre le chemin absolu vers data.yaml

#patience : nombre d'époques sans améliorations avant d'arrêter l'entrainement du réseaux prématurément
#les poids des arrêtes ainsi que certaines données sur l'entrainement sont stockées dans le dossier run