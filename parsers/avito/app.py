from flask import Flask, request

app = Flask(__name__)


@app.route('/start', methods = ["POST"])
def hello_world():
    # TODO: входящий json с параметром
    filters = request.get_json()
    return 'Hello World!'

if __name__ == '__main__':
    app.run()
