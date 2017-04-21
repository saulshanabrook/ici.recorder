import unittest
import pyspark.sql
import os

spark = pyspark.sql.SparkSession.builder \
    .master("local[*]") \
    .getOrCreate()

base_uri = os.environ['CLOJUSH-PARQUET-URI']
configs = spark.read.parquet(base_uri + "configs")
generations = spark.read.parquet(base_uri + "generations")

class TestReadConfigs(unittest.TestCase):

    def test_config_exists(self):
        self.assertTrue(configs.collect())

    def test_generation_exists(self):
        self.assertTrue(generations.collect())
