from flask import Flask, jsonify, request
from flask_cors import CORS, cross_origin

import json
import subprocess
import tempfile
import time
import os
import base64

app = Flask(__name__)
CORS(app)

def add_cors(response):
    # response.headers['Access-Control-Allow-Origin'] = '*'
    response.headers['Cache-Control'] = 'no-cache, no-store, must-revalidate'
    response.headers['Pragma'] = 'no-cache'
    response.headers['Expires'] = 0

IDPASS_PATH = '/Volumes/idpass/pass.txt'

@app.route('/create_idpass', methods=['POST', 'OPTIONS'])
def create_idpass():
    with open(IDPASS_PATH, 'wb') as f:
        f.write(request.data)

    response = jsonify({
        'success': True,
    })

    add_cors(response)

    return response

@app.route('/read_idpass', methods=['GET', 'OPTIONS'])
def read_idpass():
    with open(IDPASS_PATH, 'r') as f:
        idpass = json.load(f)

    response = jsonify(idpass)

    add_cors(response)

    return response

@app.route('/capture_iris', methods=['GET', 'OPTIONS'])
def capture_iris():
    result_dir = tempfile.gettempdir() + "/" + str(time.time())
    os.mkdir(result_dir)
    print(result_dir)
    subprocess.run(["../iriscapture/run", result_dir])
    png_file = result_dir + '/iris.png'
    subprocess.run(["convert", result_dir + '/iris.jp2', png_file])
    with open(png_file, 'rb') as f:
        iris_image = "data:image/png;base64," + base64.b64encode(f.read()).decode()

    template_file = result_dir + '/template.tpl'
    with open(template_file, 'rb') as f:
        template_data = base64.b64encode(f.read()).decode()

    response = jsonify({
        'image': iris_image,
        'template': template_data,
    })

    add_cors(response)

    return response


if __name__ == "__main__":
    app.run(host='0.0.0.0', port=10888, debug=True)
