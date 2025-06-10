import re
import redis
import os
from dotenv import load_dotenv
from telethon import TelegramClient
from telethon.tl.functions.messages import GetHistoryRequest

# Загрузка .env переменных
load_dotenv()

api_id = int(os.getenv("API_ID"))
api_hash = os.getenv("API_HASH")
redis_host = os.getenv("REDIS_HOST", "localhost")
redis_port = int(os.getenv("REDIS_PORT", 6379))

# Каналы, откуда парсим
channel_usernames = ['job_javadevs', 'job_java']

# Фильтр по IT-вакансиям (регулярка с ключевыми словами)
it_pattern = re.compile(
    r'\b(java|spring|backend|fullstack|developer|разработчик|vacancy|вакансия|it|remote|удаленка)\b',
    re.IGNORECASE
)

# Redis
r = redis.Redis(host=redis_host, port=redis_port, decode_responses=True)
processed_set_key = 'processed_message_ids'

def clean_text(text: str) -> str:
    """Удаляет пустые строки и лишние пробелы, оставляя переносы только между блоками"""
    lines = [line.strip() for line in text.splitlines() if line.strip()]
    return "\n".join(lines)

async def fetch_messages():
    data = []

    async with TelegramClient('session_name', api_id, api_hash) as client:
        for channel_username in channel_usernames:
            channel = await client.get_entity(channel_username)
            history = await client(GetHistoryRequest(
                peer=channel,
                limit=50,  # Увеличим лимит до 50
                offset_date=None,
                offset_id=0,
                max_id=0,
                min_id=0,
                add_offset=0,
                hash=0
            ))

            for msg in history.messages:
                if not msg.message:
                    continue

                unique_id = f"{channel_username}:{msg.id}"
                if r.sismember(processed_set_key, unique_id):
                    continue

                message = msg.message
                if isinstance(message, bytes):
                    try:
                        message = message.decode('utf-8')
                    except UnicodeDecodeError:
                        message = message.decode('utf-8', errors='replace')

                # Фильтрация по ключевым словам
                if it_pattern.search(message):
                    cleaned_message = clean_text(message)
                    print(f"[{channel_username}] {cleaned_message[:80]}...")  # краткий вывод
                    data.append({
                        "link": f'@{channel_username}',
                        "date": msg.date.date().isoformat(),
                        "text": cleaned_message
                    })
                    r.sadd(processed_set_key, unique_id)

    return data
