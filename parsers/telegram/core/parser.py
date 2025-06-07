from telethon import TelegramClient
from telethon.tl.functions.messages import GetHistoryRequest

api_id = 22770724
api_hash = '8a3754cfbd6b6514decbb82b547ba948'
channel_username = 'rabota_dostavka_narashvat'

async def fetch_messages():
    async with TelegramClient('session_name', api_id, api_hash) as client:
        channel = await client.get_entity(channel_username)
        history = await client(GetHistoryRequest(
            peer=channel,
            limit=10,
            offset_date=None,
            offset_id=0,
            max_id=0,
            min_id=0,
            add_offset=0,
            hash=0
        ))

        data = []
        for msg in history.messages:
            if msg.message:
                message = msg.message
                if isinstance(message, bytes):
                    try:
                        message = message.decode('utf-8')
                    except UnicodeDecodeError:
                        message = message.decode('utf-8', errors='replace')

                print(repr(message))

                data.append({
                    "date": str(msg.date),
                    "text": message
                })
        return data