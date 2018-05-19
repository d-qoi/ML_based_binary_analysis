package edu.rose.feature_finder

import org.apache.spark.sql.SparkSession

import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.feature.{SQLTransformer, VectorAssembler, RegexTokenizer, NGram, StringIndexer, MinHashLSH}
import org.apache.spark.ml.clustering.BisectingKMeans
import org.apache.spark.ml.Pipeline

import org.apache.spark.ml.classification.{RandomForestClassificationModel, RandomForestClassifier}
import org.apache.spark.ml.evaluation.MulticlassClassificationEvaluator

object Runner {

  def run(args: Array[String]) {
    val spark = SparkSession.builder.getOrCreate()
    import spark.implicits._

    val df = spark.read.format("csv").option("header","true").option("inferSchema","true").load(args(0))

    //df.select($"*", Array($"v1", $"v2", $"v3", $"v4", $"v5", $"v6", $"v7", $"v8", $"v9", $"v10").as("labels"))

    df.select("v1").show()

    //return

    val sqlt = new SQLTransformer().setStatement("SELECT *, (v1, v2, v3, v4, v5, v6, v7, v8, v9, v10) as labels FROM __THIS__")

    // val vecass = new VectorAssembler()
    //   .setInputCols(Array("1","2","3","4","5","6","7","8","9","10"))
    //   .setOutputCol("labels")

    val res1 = sqlt.transform(df)

    res1.select("labels").show()

    def from_hex(input:Array[String]):Array[Int] = input.map(x=>Integer.parseInt(x, 16))
    spark.udf.register("from_hex", from_hex(_:Array[String]))


    //val labelindex = new StringIndexer()
    //  .setInputCol("labels")
    //  .setOutputCol("labels_ind")

    //val vecassModel = vecass.fit(df)

    val regtok = new RegexTokenizer()
      .setInputCol("data")
      .setOutputCol("data_tok")
      .setPattern(".").setGaps(false)

    //val model2 = regtok.transform(model1)

    //val indexer = new StringIndexer().setInputCol("data_tok").setOutputCol("data_ind_tok")

    //val model3 = indexer.fit(model2).transform(model2)

    val sqlt2 = new SQLTransformer().setStatement("SELECT *, from_hex(data_tok) as data_tok_ind FROM __THIS__")

    val ng = new NGram().setN(2*8*3).setInputCol("data_tok").setOutputCol("data_ngrams")

    //val model4 = ng.transform(model3)

    //model4.select("data_ngrams").show()

    // val mhlsh = new MinHashLSH()
    //   .(5)
    //   .setInputCol("data_ngrams")
    //   .setOutputCol("hashes")


    val bkm = new BisectingKMeans().setK(2)
      .setFeaturesCol("data_ngrams")
      .setPredictionCol("bkm_predictions")
      .setMaxIter(15)


    // val pipeline = new Pipeline()
    //   .setStages(Array( regtok, sqlt, ng, bkm))

    // pipeline.fit(df).transform(df).select("data_tok", "data_ngrams").show()


    // return

    //val MHLSHModel = MHLSH.fit(df)

    val Array(trainingData, testData) = df.randomSplit(Array(0.7, 0.3))

    val rf = new RandomForestClassifier()
      .setLabelCol("labels")
      .setFeaturesCol("data_ngrams")
      .setNumTrees(15)

    val pipeline = new Pipeline()
      .setStages(Array(regtok, sqlt, sqlt2, ng, bkm, rf))


    val model = pipeline.fit(trainingData)

    val predictions = model.transform(testData)

    predictions.show(2)

    val evaluator = new MulticlassClassificationEvaluator()
      .setLabelCol("labels")
      .setPredictionCol("prediction")
      .setMetricName("accuracy")

    val accuracy = evaluator.evaluate(predictions)
    println(s"Test Error = ${(1.0 - accuracy)}")
  }

}


object FeatureExtractor {
  def main(args: Array[String]) {
    println(args);

    val spark = SparkSession.builder.appName("TestApplicationExampl").master("spark://hirschag-0:7077").getOrCreate()

    import spark.implicits._

    Runner.run(args)

    spark.stop()
  }
}
