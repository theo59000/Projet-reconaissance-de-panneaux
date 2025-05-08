import io
from flask import Flask, request, jsonify
from PIL import Image, ImageDraw, ImageFont
import numpy as np
import base64
import os

from ultralytics import YOLO

app = Flask(__name__)

# Charger le modèle YOLOv8
model = YOLO("C:\\Users\\chris_cp05xbg\\Documents\\GitHub\\Projet-reconaissance-de-panneaux\\runs\\detect\\train8\\weights\\best.pt")  # Remplace par le chemin de ton modèle, ex : "best.pt"

@app.route("/predict", methods=["POST"])
def predict():
    # Lire l'image depuis la requête
    image = Image.open(io.BytesIO(request.data)).convert("RGB")
    
    # Sauvegarde temporaire
    temp_path = "temp.jpg"
    image.save(temp_path)

    # Prédiction
    results = model(temp_path)

    # Dessiner les détections sur l'image
    draw = ImageDraw.Draw(image)
    font = ImageFont.load_default()

    detections = []
    for result in results:
        for box in result.boxes:
            cls_id = int(box.cls[0])
            conf = float(box.conf[0])
            label = model.names[cls_id]
            bbox = box.xyxy[0].tolist()  # [x1, y1, x2, y2]
            detections.append({
                "class": label,
                "confidence": round(conf, 2),
                "bbox": [round(x, 2) for x in bbox]
            })

            # Dessiner la boîte et le label
            draw.rectangle(bbox, outline="red", width=2)
            draw.text((bbox[0], bbox[1] - 10), f"{label} {conf:.2f}", fill="red", font=font)

    # Convertir l'image annotée en base64
    buffered = io.BytesIO()
    image.save(buffered, format="JPEG")
    img_base64 = base64.b64encode(buffered.getvalue()).decode("utf-8")

    os.remove(temp_path)

    return jsonify({"detections": detections, "image": img_base64})

if __name__ == "__main__":
    app.run(port=5000, debug=True)
