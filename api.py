import io
from flask import Flask, request, jsonify
import tensorflow as tf

from PIL import Image
import numpy as np
import os

from PIL import ImageDraw, ImageFont
import base64

app = Flask(__name__)

def load_label_map(path):
    label_map = {}
    with open(path, 'r') as f:
        lines = f.readlines()
        id = None
        name = None
        for line in lines:
            line = line.strip()
            if line.startswith("id:"):
                id_str = line.split(":")[1].strip().rstrip(',')  # enlève la virgule
                id = int(id_str)
            elif line.startswith("name:"):
                name = line.split(":")[1].strip().replace('"', '')
            if id is not None and name is not None:
                label_map[id] = name
                id = None
                name = None
    return label_map
# Charger le modèle
model = tf.keras.models.load_model("Detection_panneaux-3/my_model.keras")  # Ajuste le chemin si besoin
label_map = load_label_map('Detection_panneaux-3/test/objects_label_map.pbtxt')

def preprocess_image(image_path):
    img = Image.open(image_path).resize((224, 224))
    img = np.array(img) / 255.0
    return np.expand_dims(img, axis=0)

@app.route("/predict", methods=["POST"])
def predict():
    image = Image.open(io.BytesIO(request.data))

    image_path = "temp.jpg"
    image.save(image_path)

    img = preprocess_image(image_path)
    prediction = model.predict(img)
    predicted_class = int(np.argmax(prediction))
    label = label_map[predicted_class].strip().strip(',')

    # Annoter l'image
    draw = ImageDraw.Draw(image)
    font = ImageFont.load_default()
    draw.text((10, 10), label, fill="red", font=font)

    # Convertir l'image annotée en base64
    buffered = io.BytesIO()
    image.save(buffered, format="JPEG")
    img_bytes = buffered.getvalue()
    img_base64 = base64.b64encode(img_bytes).decode('utf-8')

    os.remove(image_path)

    return jsonify({"class": label, "image":img_base64})

if __name__ == "__main__":
    app.run(port=5000, debug=True)
