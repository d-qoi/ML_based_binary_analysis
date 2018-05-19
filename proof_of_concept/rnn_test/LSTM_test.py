import csv

import tensorflow as tf
import numpy as np

## Data (as csv)
DATA_PATH = "../generator/bulk_random_gen_test.csv"

unprocessed_data = []
with open(DATA_PATH, 'r') as raw_csv:
    csv_file = csv.DictReader(raw_csv)
    fields = csv_file.fieldnames
    count = fields[-2] # due to the way it is processed, it is assuming that -2 is the max number of elements in any set of data. (may change generator later)
    for row in csv_file:
        elements = []
        for i in range(int(fields[-2])+1):
            if row[str(i)]:
                elements.append(int(row.get(str(i), '-1')))
        unprocessed_data.append([elements, [int(c, 16) for c in row['data']]])

# unprocessed_data = unprocessed_data[0:10]
print('len unprocessed_data: {}'.format(len(unprocessed_data)))

np.random.shuffle(unprocessed_data)

training_data_len = round(len(unprocessed_data)*0.7)
test_data_len = len(unprocessed_data) - training_data_len

# print(training_data_len, test_data_len)

training_data = unprocessed_data[:training_data_len]
test_data = unprocessed_data[training_data_len:]

print('training_data_len: {}\ntest_data_len: {}'.format(len(training_data), len(test_data)))

SIZE_OF_ELEMENT = 2*8 # each is a pointer, so everything is 8 bytes, and this is currently split into 4 bit segments.
SIZE_OF_STRUCT = SIZE_OF_ELEMENT*3 # structure is currently three pointers


def batch_producter(raw_data, batch_size, num_steps):
    raw_data = tf.convert_to_tensor(raw_data, name='raw_raw_data', dtype=tf.int32)
    raw_data_len = tf.size(raw_data)
    batch_len = raw_data_len // batch_size # devide without remainder.
    d = tf.reshape(raw_data[0:batch_size*batch_len], [batch_size, batch_len])

    epoch_size = (batch_len - 1) // num_steps

    i = tf.train.range_input_producer(epoch_size, shuffle=False).dequeue()
    x = raw_data[:, i * num_steps:(i + 1) * num_steps]
    x.set_shape([batch_size, num_steps])
    y = raw_data[:, i * num_steps + 1: (i + 1) * num_steps + 1]
    y.set_shape([batch_size, num_steps])
    return x, y

class Input(object):
    def __init__(self, batch_size, num_steps, data):
        self.batch_size = batch_size
        self.num_steps = num_steps
        self.epoch_size = ((len(data) // batch_size) - 1) // num_steps
        self.input_data, self.targets = batch_producer(data, batch_size, num_steps)

class Model(object):

    def __init__(self, inpt, is_training, hidden_size, vocab_size, num_layers, dropout=0.5, init_scale=0.05, forget_bias=1.0):
        self.is_training = is_training
        self.input_obj = inpt
        self.batch_size = inpt.batch_size
        self.num_steps = inpt.num_steps
        self.hidden_size = hidden_size

        embedding = tf.Variable(tf.random_uniform([vocab_size, self.hidden_size], -init_scale, init_scale))
        inputs = tf.nn.embedding_lookup(embedding, self.input_obj.input_data)

        if is_training and dropout < 1:
            inputs = tf.nn.dropout(inputs, dropout)

        self.init_state = tf.placeholder(tf.float32, [num_layers, 2, self.batch_size, self.hidden_size])

        state_per_layer_list = tf.unstack(self.init_state, axis=0)
        rnn_tuple_state = tuple(
            [tf.contrib.rnn.LSTMStateTuple(state_per_layer_list[idx][0], state_per_layer_list[idx][1])
             for idx in range(num_layers)]
        )

        cell = tf.contrib.rnn.LSTMCell(hidden_size, forget_bias=forget_bias)


        if is_training and dropout < 1:
            cell = tf.contrib.rnn.DropoutWrapper(cell, output_keep_prob=dropout)

        if num_layers > 1:
            cell = tf.contrib.rnn.MultiRNNCell([cell for _ in range(num_layers)], state_is_tuple=True)

        output, self.state = tf.nn.dynamic_rnn(cell, inputs, dtype=tf.float32, initial_state=rnn_tuple_state)

        output = tf.reshape(output, [-1, hidden_size])
