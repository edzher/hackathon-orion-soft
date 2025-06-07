import re
import redis
from telethon import TelegramClient
from telethon.tl.functions.messages import GetHistoryRequest
import os
from dotenv import load_dotenv


load_dotenv()

api_id = int(os.getenv("API_ID"))
api_hash = os.getenv("API_HASH")
redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = int(os.getenv("REDIS_PORT", 6379))
channel_usernames = ['rabota_dostavka_narashvat', 'vse_vacancy']

# Регулярка для поиска слов, связанных с курьером
courier_pattern = re.compile(
    r'\b(курьер|велокурьер|автокурьер|доставщик|курьером|курьерская|курьерские|курьеры|доставка)\b',
    re.IGNORECASE
)

# Подключение к локальному Redis
r = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
processed_set_key = 'processed_message_ids'  # ключ множества с уже обработанными ID

def clean_text(text: str) -> str:
    # Удаляем пустые строки и лишние пробелы
    # Разбиваем на строки, фильтруем пустые, убираем пробелы по краям
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    # Склеиваем обратно с одним переносом между строками
    return "\n".join(lines)

async def fetch_messages():
    data = []
    async with TelegramClient('session_name', api_id, api_hash) as client:
        for channel_username in channel_usernames:
            channel = await client.get_entity(channel_username)
            history = await client(GetHistoryRequest(
                peer=channel,
                limit=100,
                offset_date=None,
                offset_id=0,
                max_id=0,
                min_id=0,
                add_offset=0,
                hash=0
            ))

            for msg in history.messages:
                if msg.message:
                    unique_id = f"{channel_username}:{msg.id}"
                    if not r.sismember(processed_set_key, unique_id):
                        message = msg.message
                        if isinstance(message, bytes):
                            try:
                                message = message.decode('utf-8')
                            except UnicodeDecodeError:
                                message = message.decode('utf-8', errors='replace')

                        if courier_pattern.search(message):
                            cleaned_message = clean_text(message)
                            print(f"[{channel_username}] {repr(cleaned_message)}")
                            data.append({
                                "link": f'@{channel_username}',
                                "date": msg.date.date().isoformat(),
                                "text": cleaned_message
                            })
                            r.sadd(processed_set_key, unique_id)
    return data
