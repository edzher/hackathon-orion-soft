from flask import Flask, Response
import asyncio
import threading
import redis
from core.parser import fetch_messages
import os
from dotenv import load_dotenv

app = Flask(__name__)
load_dotenv()
api_id = int(os.getenv("API_ID"))
api_hash = os.getenv("API_HASH")
redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = int(os.getenv("REDIS_PORT", 6379))

def run_parser_background():
    loop = asyncio.new_event_loop()
    asyncio.set_event_loop(loop)
    data = loop.run_until_complete(fetch_messages())
    with open("vacancies.txt", "w", encoding="utf-8") as file:
        for item in data:
            file.write(item['text'] + "\n")
            file.write("=" * 40 + "\n")

    r = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)

    # Очистка старых вакансий и счетчика (по желанию)
    keys = r.keys('vacancy:*')
    if keys:
        r.delete(*keys)
    r.delete('vacancy:id')

    for item in data:
        new_id = r.incr('vacancy:id')
        r.hset(f'vacancy:{new_id}', mapping={
            'date': item['date'],
            'text': item['text']
        })
    loop.close()


@app.route('/parse', methods=["GET"])
def parse():
    thread = threading.Thread(target=run_parser_background)
    thread.start()
    return Response("Парсинг запущен", status=200, mimetype='text/plain')


@app.route('/vacancies', methods=["GET"])
def get_vacancies():
    r = redis.Redis(host='127.0.0.1', port=6379, decode_responses=True)
    keys = r.keys('vacancy:*')
    keys = sorted(keys, key=lambda x: int(x.split(':')[1]))

    vacancies = []
    for key in keys:
        vacancy = r.hgetall(key)
        if vacancy:
            vacancies.append(vacancy)

    output = []
    for v in vacancies:
        output.append(f"{v['date']}\n{v['text']}\n{'=' * 40}")

    return Response('\n\n'.join(output), mimetype='text/plain')


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)

