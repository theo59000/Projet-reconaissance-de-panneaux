
# from roboflow import Roboflow
# rf = Roboflow(api_key="POKrCxImcg3gAbmhpe2W")
# project = rf.workspace("roboflowrn").project("detection_panneaux")
# version = project.version(3)
# dataset = version.download("tfrecord")

from tools import *  
import tensorflow as tf
import os

# Load label map

# Obtenir le chemin absolu du fichier .py courant
base_path = os.path.dirname(__file__)
label_map_path = os.path.abspath(os.path.join(base_path, '..', 'Detection_panneaux-3', 'test', 'objects_label_map.pbtxt'))
print("LABEL MAP PATH:", label_map_path)
label_map = load_label_map('Detection_panneaux-3/test/objects_label_map.pbtxt')#label_map_path


# Crée le dataset
util = Tools("Detection_panneaux-3/train/objects.tfrecord"
                ,"Detection_panneaux-3/test/objects.tfrecord"
                ,"Detection_panneaux-3/valid/objects.tfrecord")
train_dataset = util.train_dataset
test_dataset = util.test_dataset
val_dataset = util.val_dataset


model = tf.keras.models.Sequential([
    tf.keras.layers.Input(shape=(224, 224, 3)),
    tf.keras.layers.Conv2D(32, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Conv2D(64, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(128, activation='relu'),
    tf.keras.layers.Dropout(0.5),
    tf.keras.layers.Dense(len(label_map), activation='softmax')
])

model.compile(optimizer='adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.fit(train_dataset, epochs=10,verbose = 1, validation_data = val_dataset,shuffle = True)


# Évaluer le modèle sur les données de test
loss, accuracy = model.evaluate(test_dataset, batch_size = 64, verbose = 2)
print(f"Test Loss: {loss}")
print(f"Test Accuracy: {accuracy}")

model.summary()
