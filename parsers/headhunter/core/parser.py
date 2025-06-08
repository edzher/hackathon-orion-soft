from bs4 import BeautifulSoup
import json
import time
import requests
import logging

from typing import List

class HHParser:
    def __init__(self, jobs: List[str]):
        self.__jobs = jobs
        self.__logger = logging.getLogger("hh_parser")
        self.__headers = {
            "Accept": "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8",
            "User-Agent": "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.5 Safari/605.1.15",
        }
        # self.__mongodb = mongodb

    def parsing(self):
        for job in self.__jobs:
            # page: int = 0
            for page in range(1):
                url = f"https://hh.ru/search/vacancy?text={job}&page={page}&items_on_page=100&order_by=publication_time"
                result, success = self.__get_vacancies(page, url)
                if not success:
                    print("Out of stock vacancies")
                    break
                self.__logger.info(f"Successful parsing vacancies: ${len(result)}")
                # TODO: Добавить сохранение в базу данных
                self.__logger.info("Successful saving vacancies")
                # page += 1
                time.sleep(2)

            self.__logger.info(f"Сбор данных завершён. Обработано страниц: {page}")

    @staticmethod
    def __count_salary(salary: int, currency: str):
        if salary is None:
            return None
        if currency == "RUR":
            return salary
        elif currency == "USD":
            return salary * 80
        else:
            return None

    def __get_vacancies(self, page, url) -> (list[dict], bool):
        self.__logger.info(f"Get page: {page}")
        response = requests.get(url, headers=self.__headers)

        if response.status_code != 200:
            self.__logger.error("Error request", response.status_code)
            return [], False

        soup = BeautifulSoup(response.text, "html.parser")
        template_tag = soup.select_one("html > body > noindex > template")

        if not template_tag:
            self.__logger.error("Error tag")
            return [], False

        try:
            inner_soup = BeautifulSoup(template_tag.decode_contents(), "html.parser")
            data_json = json.loads(inner_soup.string)['vacancySearchResult']['vacancies']
            results = []

            for data in data_json:
                salary_info = data.get("compensation", {})
                if salary_info.get("noCompensation") is None and salary_info.get("currencyCode") not in ['RUR', 'USD']:
                    continue
                vacancy = {
                    "id": data["vacancyId"],
                    "name": data["name"],
                    "area": data["area"]["name"],
                    "company": data.get("company", {}).get("name"),
                    "work_experience": data.get("workExperience", "unknown"),
                    "employment_form": data.get("employmentForm", "unknown"),
                    "salary_from": self.__count_salary(salary_info.get("from"), salary_info.get("currencyCode")),
                    "salary_to": self.__count_salary(salary_info.get("to"), salary_info.get("currencyCode")),
                    "salary_mode": salary_info.get("mode", "unknown"),
                    "salary_freq": salary_info.get("frequency", "unknown")
                }
                results.append(vacancy)
            return results, True
        except Exception as e:
            print("Ошибка при обработке JSON:", e)
            return False



if name == "__main__":
    parser = HHParser(
        ["курьер"]
    )
    print("start")
    parser.parsing()