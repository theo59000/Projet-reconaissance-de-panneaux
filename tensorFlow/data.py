from tools import *  
import tensorflow as tf
import os

# Load label map

# Obtenir le chemin absolu du fichier .py courant
base_path = os.path.dirname(__file__)

label_map_path = os.path.abspath(os.path.join(base_path, '..', 'Detection_panneaux-11', 'test', 'objects_label_map.pbtxt'))
print("LABEL MAP PATH:", label_map_path)
label_map = load_label_map('Detection_panneaux-11/test/objects_label_map.pbtxt')#label_map_path
print("TAILLE MAP §§§",len(label_map))

raw_dataset = tf.data.TFRecordDataset("Detection_panneaux-11/train/objects.tfrecord")
count = 0
for _ in raw_dataset:
    count += 1
print("Nombre d'exemples dans le TFRecord:", count)

# Crée le dataset
util = Tools("Detection_panneaux-11/train/objects.tfrecord"
                ,"Detection_panneaux-11/test/objects.tfrecord"
                ,"Detection_panneaux-11/valid/objects.tfrecord")

train_dataset = util.train_dataset
test_dataset = util.test_dataset
val_dataset = util.val_dataset


model = tf.keras.models.Sequential([
    tf.keras.layers.Input(shape=(224, 224, 3)),
    tf.keras.layers.Conv2D(32, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Conv2D(64, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),

    tf.keras.layers.Conv2D(128, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Conv2D(256, 3, activation='relu'),
    tf.keras.layers.MaxPooling2D(),
    tf.keras.layers.Flatten(),
    tf.keras.layers.Dense(512, activation='relu'),
    tf.keras.layers.Dropout(0.6),
    tf.keras.layers.Dense(len(label_map), activation='softmax')
])

model.compile(optimizer='Adam',
              loss='sparse_categorical_crossentropy',
              metrics=['accuracy'])

model.fit(train_dataset, epochs=15,verbose = 1, validation_data = val_dataset,shuffle = True)

# Évaluer le modèle sur les données de test
loss, accuracy = model.evaluate(test_dataset, batch_size = 64, verbose = 2)
print(f"Test Loss: {loss}")
print(f"Test Accuracy: {accuracy}")

model.summary()

model.save("Detection_panneaux-11/my_model.keras")

