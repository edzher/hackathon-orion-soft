from bs4 import BeautifulSoup
import json
import time
import requests
import logging
import pandas as pd
from typing import List
from datetime import datetime
import os
from parsers.superjob.client.mongodb.mongodb import MongoDB

class HHParser:
    def __init__(self, jobs: List[str], mongodb):
        self.__jobs = jobs
        self.__logger = logging.getLogger("hh_parser")
        self.__headers = {
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.5 Safari/605.1.15",
        }
        self.__mongodb = mongodb

    def parsing(self):
        all_vacancies = []

        for job in self.__jobs:
            page = 0

            while True:
                url = f"https://hh.ru/search/vacancy?text={job}&page={page}&items_on_page=100&order_by=publication_time"
                vacancies, success = self.__get_vacancies(page, url)
                if not success or not vacancies:
                    break
                all_vacancies.extend(vacancies)
                self.__logger.info(f"Page {page} — загружено {len(vacancies)} вакансий.")
                page += 1
                time.sleep(1.5)

        if not all_vacancies:
            print("Нет вакансий для сохранения.")
            return

        df = pd.DataFrame(all_vacancies)
        df["source"] = "hh"
        df["publishedDate"] = df["publishedDate"].apply(lambda x: x if x else datetime.now())

        self.__mongodb.insert(df)
        print(f"Сохранено {len(df)} вакансий в MongoDB.")

    @staticmethod
    def __count_salary(salary: int, currency: str):
        if salary is None:
            return None
        if currency == "RUR":
            return salary
        elif currency == "USD":
            return salary * 80
        return None

    def __get_vacancies(self, page, url) -> (list[dict], bool):
        self.__logger.info(f"Загрузка страницы {page}")
        response = requests.get(url, headers=self.__headers)

        if response.status_code != 200:
            self.__logger.error(f"Ошибка запроса: {response.status_code}")
            return [], False

        soup = BeautifulSoup(response.text, "html.parser")
        template_tag = soup.select_one("html > body > noindex > template")

        if not template_tag:
            self.__logger.error("Не найден тег template")
            return [], False

        try:
            inner_soup = BeautifulSoup(template_tag.decode_contents(), "html.parser")
            data_json = json.loads(inner_soup.string)['vacancySearchResult']['vacancies']
            results = []

            for data in data_json:
                salary_info = data.get("compensation", {})
                link = data.get("links", {}).get("desktop", "")
                salary_from = self.__count_salary(salary_info.get("from"), salary_info.get("currencyCode"))
                salary_to = self.__count_salary(salary_info.get("to"), salary_info.get("currencyCode"))

                # Извлечение даты публикации
                published_date = None
                try:
                    properties = data.get("vacancyProperties", {}).get("properties", [])
                    for prop_group in properties:
                        for prop in prop_group.get("property", []):
                            start_time = prop.get("startTimeIso")
                            if start_time:
                                published_date = start_time
                                break
                        if published_date:
                            break
                except Exception as e:
                    self.__logger.warning(f"Не удалось извлечь дату публикации: {e}")

                experience_raw = data.get("workExperience")
                experience = "" if experience_raw == "noExperience" else experience_raw

                vacancy = {
                    "minSalary": salary_from,
                    "maxSalary": salary_to,
                    "source": "hh",
                    "position": data.get("name"),
                    "experience": experience,
                    "employmentType": data.get("employmentForm", "unknown"),
                    "city": data.get("area", {}).get("name", ""),
                    "company": data.get("company", {}).get("name", ""),
                    "benefits": [],
                    "link": link,
                    "publishedDate": published_date
                }

                results.append(vacancy)

            return results, True
        except Exception as e:
            self.__logger.error(f"Ошибка при обработке JSON: {e}")
            return [], False

if __name__ == "__main__":
    mongo = MongoDB(
        host=os.getenv("MONGO_HOST", "localhost"),
        port=os.getenv("MONGO_PORT", "27017"),
        database=os.getenv("MONGO_DB", "vacancy_db"),
        collection=os.getenv("MONGO_COLLECTION", "vacancies")
    )

    parser = HHParser(["курьер"], mongo)
    parser.parsing()
