import requests
from datetime import datetime
import pandas as pd
from dotenv import load_dotenv
import os

load_dotenv()

API_KEY = os.getenv("SUPERJOB_API_KEY")
url = 'https://api.superjob.ru/2.0/resumes/?period=30&keyword=Курьер&t[]=4&no_agreement=1'

headers = {
    'X-Api-App-Id': API_KEY
}


def parse_experience(experience_text, experience_months):
    """Возвращает опыт в годах, округляя вниз. Или None."""
    if experience_months is not None and experience_months > 0:
        return experience_months // 12
    if isinstance(experience_text, str):
        import re
        match = re.search(r'(\d+)', experience_text)
        return int(match.group(1)) if match else 0
    return 0

def fetch_and_store():
    page = 0
    all_resumes = []

    while True:
        paged_url = f'https://api.superjob.ru/2.0/resumes/?period=30&keyword=Курьер&t[]=4&no_agreement=1&page={page}'
        response = requests.get(paged_url, headers=headers)

        if response.status_code != 200:
            print(f"Ошибка запроса: {response.status_code} - {response.text}")
            break

        data = response.json()
        resumes = data.get('objects', [])
        all_resumes.extend(resumes)

        if not data.get('more'):
            break  # больше страниц нет
        page += 1

    if not all_resumes:
        print("Резюме не найдены.")
        return

    parsed = []
    for resume in all_resumes:
        salary_from = resume.get('payment')
        salary_to = None

        source = 'superjob'
        position = resume.get('profession', 'N/A')

        date_published_ts = resume.get('date_published')
        published_date = datetime.fromtimestamp(date_published_ts) if date_published_ts else None

        experience_text = resume.get('experience_text', '')
        experience_months = resume.get('experience_month_count', 0)
        experience = parse_experience(experience_text, experience_months)

        city = resume.get('town', {}).get('title', '')
        work_history = resume.get('work_history', [])
        company = work_history[0]['name'] if work_history else ''

        benefits = resume.get('additional_info', '')
        benefits = [b.strip() for b in benefits.split(',')] if isinstance(benefits, str) else []

        employment_type = resume.get('type_of_work', {}).get('title', '')

        parsed.append({
            "minSalary": salary_from,
            "maxSalary": salary_to,
            "source": source,
            "position": position,
            "publishedDate": published_date,
            "experience": experience,
            "city": city,
            "company": company,
            "benefits": benefits,
            "employmentType": employment_type
        })

    df = pd.DataFrame(parsed)

    if df.empty:
        print("нет данных")
        return

    from parsers.superjob.client.mongodb.mongodb import MongoDB
    mongo = MongoDB(
        host=os.getenv("MONGO_HOST", "localhost"),
        port=os.getenv("MONGO_PORT", "27017"),
        database=os.getenv("MONGO_DB", "vacancy_db"),
        collection=os.getenv("MONGO_COLLECTION", "vacancies")
    )

    mongo.insert(df)
    print(f"Успешно сохранено {len(df)} резюме в MongoDB.")


if __name__ == "__main__":
    fetch_and_store()
