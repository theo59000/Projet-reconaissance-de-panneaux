import tensorflow as tf

def parse_tfrecord(example):
    features = {
        'image/encoded': tf.io.FixedLenFeature([], tf.string),
        'image/object/class/label': tf.io.VarLenFeature(tf.int64),
    }
    example = tf.io.parse_single_example(example, features)
    image = tf.image.decode_jpeg(example['image/encoded'], channels=3)
    image = tf.image.resize(image, [224, 224]) / 255.0
    label = tf.sparse.to_dense(example['image/object/class/label'])
    
    return image, label[0]  # prend juste la première classe (pour classification simple)

def adjust_labels(image, labels):
    # change les indices des labels de 0 à n-1 et plus 1 à n
    adjusted_labels = tf.map_fn(lambda x: x - 1, labels, dtype=tf.int64)
    return image, adjusted_labels

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

class Tools:
    def __init__(self,path_train,path_test,path_val):
        self.path_train = path_train
        self.path_test = path_test
        self.path_val = path_val 
        self.train_dataset = self.load_train_data()
        self.test_dataset = self.load_test_val_data(self.path_test)
        self.val_dataset = self.load_test_val_data(self.path_val)

    
    def load_train_data(self):
        train_dataset = tf.data.TFRecordDataset(self.path_train)
        train_dataset = train_dataset.map(parse_tfrecord)
        train_dataset = train_dataset.shuffle(100).batch(32).prefetch(tf.data.AUTOTUNE)
        # Appliquer la réindexation dans le pipeline
        train_dataset = train_dataset.map(adjust_labels)
        return train_dataset
    def load_test_val_data(self,path):
        dataset = tf.data.TFRecordDataset(path)
        dataset = dataset.map(parse_tfrecord)
        dataset = dataset.batch(32).prefetch(tf.data.AUTOTUNE)
        dataset = dataset.map(adjust_labels)
        return dataset