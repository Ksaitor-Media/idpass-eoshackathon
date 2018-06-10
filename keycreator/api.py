from flask import Flask, jsonify, request
import json

app = Flask(__name__)

def add_cors(response):
    response.headers['Access-Control-Allow-Origin'] = '*'
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

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=10888, debug=True)
