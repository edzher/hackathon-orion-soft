from flask import Flask, Response
import threading
from core.parser import fetch_and_store
import io


app = Flask(__name__)
parsed_data = []

def run_parser_background():
    global parsed_data
    parsed_data = fetch_and_store()


@app.route('/parse', methods=["GET"])
def parse():
    thread = threading.Thread(target=run_parser_background)
    thread.start()
    return Response("Парсинг запущен", status=200, mimetype='text/plain')


@app.route('/vacancies', methods=["GET"])
def get_vacancies():
    if not parsed_data:
        return Response("Нет данных. Сначала вызовите /parse", status=400, mimetype='text/plain')

    output = io.StringIO()
    for item in parsed_data:
        output.write(f"{item['date']}\n{item['text']}\n{'=' * 40}\n\n")

    return Response(output.getvalue(), mimetype='text/plain')


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)
