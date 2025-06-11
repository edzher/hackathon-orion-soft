from pymongo import MongoClient
from utils.logger import get_logger
import pandas

class MongoDB:
    def __init__(self, host: str, port: str, database: str, collection: str):
        self.__name_collection = collection
        self.__uri = f"mongodb://{host}:{port}"
        self.__client = MongoClient(self.__uri)
        self.__database = self.__client[database]
        self.__collection = self.__database[collection]
        self.__logger = get_logger("mongodb")

    def clear(self):
        self.__collection.delete_many({})
        self.__logger.info(f"MongoDB collection: ${self.__name_collection} cleared")

    def insert(self, df: pandas.DataFrame):
        data = df.to_dict(orient="records")
        self.__collection.insert_many(data)
        self.__logger.info(f"MongoDB collection: ${self.__name_collection} inserted ${len(data)} documents")






