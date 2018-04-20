import csv
import argparse
from os import listdir
from os.path import isfile, join, exists
from functools import reduce

def parse_line(line, file_name):
    d = line.strip().split(';')
    head = d[0].split(',')
    nodes = d[1].split(',')
    tail = d[2].strip()
    d = {}
    d['start_of_region'] = head[0].strip()
    d['end_of_region'] = head[1].strip()
    d['number_of_nodes'] = int(head[2].strip(), 16)
    for i in range(len(nodes)-1):
        d[i] = nodes[i].strip()
    d['data'] = tail
    d['file_name'] = file_name
    return d

parser = argparse.ArgumentParser(description = "Will take a file generated by Generator and turn it into an actual CSV file")
parser.add_argument('source', type=str, help='directory or file to convert')
parser.add_argument('dest', type=str, help='directory or file to use out output, directory will make a new file per conversion, file will combine everyting into one file')
parser.add_argument('--debug', action='store_true', help='debug flag')

args = parser.parse_args()

source_file = args.source.strip()
dest_file = args.dest.strip()
debug = args.debug

if debug:
    print("""
Source: {}
Dest: {}
""".format(source_file, dest_file))

data = []
max_nodes = 0

if isfile(source_file):
    if debug:
        print("Reading Single File")
    with open(source_file, 'r') as source:
        data.append(parse_line(source.read(), source_file))
        if debug:
            print("Read: {}".format(data))
else:
    if debug:
        print("Reading Directory")
    files = listdir(source_file)
    if debug:
        ("File list:\n{}\n".format(files))
    for f in files:
        file_path = join(source_file, f)
        if not isfile(file_path):
            continue
        with open(file_path, 'r') as source:
            data.append(parse_line(source.read(), f))
            if debug:
                print("data: {}\n".format(data[-1]))

if len(data) > 1:
    max_nodes = reduce((lambda x, y: x if x['number_of_nodes']>y['number_of_nodes'] else y), data)['number_of_nodes']
else:
    max_nodes = data[0]['number_of_nodes']
if debug:
    print("Max number of nodes: {}".format(max_nodes))

header = []
header += ['start_of_region','end_of_region','number_of_nodes']
header += list(range(max_nodes))
header += ['data']
header += ['file_name']

if not exists(dest_file) or isfile(dest_file):
    if debug:
        print("writing to {}".format(dest_file))
    with open(dest_file, 'w', newline='') as dest:
        writer = csv.DictWriter(dest, fieldnames=header)
        writer.writeheader()
        for d in data:
            writer.writerow(d)
else:
    for d in data:
        with open(join(dest_file, d.file_name), 'w', newline=''):
            if debug:
                print("writing to {}".format(join(dest_file, d.file_name)))
            writer = csv.DictWriter(dest, fieldnames=header)
            writer.writeheader()
            writer.writerow(d)
