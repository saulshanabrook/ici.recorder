import unittest
import pyspark.sql
import os

spark = pyspark.sql.SparkSession.builder \
    .master("local[*]") \
    .getOrCreate()

base_uri = os.environ['CLOJUSH-PARQUET-URI']
configs = spark.read.parquet(base_uri + "configs")

class TestReadConfigs(unittest.TestCase):

    def test_configs_contains(self):
        self.assertEqual(
            configs.collect(),
             [pyspark.sql.Row(uuid="uuid", name="hi")]
        )
