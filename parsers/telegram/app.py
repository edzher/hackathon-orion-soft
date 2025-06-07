from flask import Flask, Response, render_template_string, request
import asyncio
from core.parser import fetch_messages


app = Flask(__name__)


@app.route('/parse', methods = ["GET"])
def hello_world():
    # TODO: входящий json с параметром
    # filters = request.get_json()
    data = asyncio.run(fetch_messages())

    # HTML-шаблон прямо в коде (можно заменить на render_template, если подключишь шаблоны)
    html_template = """
    <!DOCTYPE html>
    <html lang="ru">
    <head>
        <meta charset="UTF-8">
        <title>Вакансии из Telegram</title>
        <style>
            body { font-family: sans-serif; background: #f8f8f8; padding: 20px; }
            .message { background: white; padding: 15px; margin-bottom: 15px; border-radius: 8px; box-shadow: 0 2px 5px rgba(0,0,0,0.05); white-space: pre-wrap; }
            .date { font-size: 12px; color: gray; margin-bottom: 8px; }
        </style>
    </head>
    <body>
        <h1>Последние вакансии</h1>
        {% for item in data %}
        <div class="message">
            <div class="date">{{ item.date }}</div>
            {{ item.text | e | replace('\n', '<br>') | safe }}
        </div>
        {% endfor %}
    </body>
    </html>
    """

    # Рендерим HTML с подстановкой данных
    return render_template_string(html_template, data=data)


if __name__ == '__main__':
    app.run(debug=True, host='0.0.0.0', port=5001)

