from flask import Flask, jsonify
import os
from dotenv import load_dotenv
from parsers.superjob.client.mongodb.mongodb import MongoDB
from parsers.headhunter.core.parser import HHParser

load_dotenv()

app = Flask(__name__)

@app.route('/parse', methods=["GET"])
def parse():
    mongo = MongoDB(
        host=os.getenv("MONGO_HOST", "localhost"),
        port=os.getenv("MONGO_PORT", "27017"),
        database=os.getenv("MONGO_DB", "vacancy_db"),
        collection=os.getenv("MONGO_COLLECTION", "vacancies")
    )

    parser = HHParser(["Курьер"], mongo)
    parser.parsing()
    return jsonify({"message": "Парсинг завершён и данные сохранены в MongoDB"}), 200


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)