from pymongo import MongoClient
import os

class MongoDB:
    def __init__(self, host, port, database, collection):
        user = os.getenv("MONGO_USER")
        password = os.getenv("MONGO_PASSWORD")
        auth_db = "admin"  # База, в которой хранится пользователь

        uri = f"mongodb://{user}:{password}@{host}:{port}/{database}?authSource={auth_db}"
        client = MongoClient(uri)

        self.__db = client[database]
        self.__collection = self.__db[collection]

    def insert(self, data):
        if isinstance(data, list):
            self.__collection.insert_many(data)
        else:
            self.__collection.insert_many(data.to_dict(orient="records"))

    def clear(self):
        self.__collection.delete_many({})
        #self.__logger.info(f"MongoDB collection: ${self.__name_collection} cleared")