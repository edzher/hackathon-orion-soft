from flask import Flask
from core.parser import fetch_messages
from parsers.avito.client.mongodb.mongodb import MongoDB
from parsers.headhunter.core.parser import HHParser
import os

app = Flask(__name__)


@app.route('/parse', methods = ["GET"])
def parse():
    mongo = MongoDB(
        host=os.getenv("MONGO_HOST", "localhost"),
        port=os.getenv("MONGO_PORT", "27017"),
        database=os.getenv("MONGO_DB", "vacancy_db"),
        collection=os.getenv("MONGO_COLLECTION", "vacancies")
    )
    parser = HHParser([
        'Курьер'
    ], mongo)
    parser.parsing()



if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5010)

