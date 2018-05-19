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

    df.select("1").show()

    val vecass = new VectorAssembler()
      .setInputCols(Array("1","2","3","4","5","6","7","8","9","10"))
      .setOutputCol("labels")

    //val vecassModel = vecass.fit(df)

    val regtok = new RegexTokenizer()
      .setInputCol("data")
      .setOutputCol("data_tok")
      .setPattern(".")

    val indexer = new StringIndexer().setInputCol("data_tok").setOutputCol("data_ind_tok")

    val ng = new NGram().setN(2).setInputCol("data_ind_tok").setOutputCol("data_ngrams")

    // val MHLSH = new MinHashLSH()
    //   .setNumHashTable(5)
    //   .setInputCol("data_ngrams")
    //   .setOutputCol("hashes").fit(df)

    //val MHLSHModel = MHLSH.fit(df)

    val bkm = new BisectingKMeans().setK(2)
      .setFeaturesCol("data_ngrams")
      .setPredictionCol("bkm_predictions")
      .setMaxIter(15).fit(df)

    val Array(trainingData, testData) = df.randomSplit(Array(0.7, 0.3))

    val rf = new RandomForestClassifier()
      .setLabelCol("labels")
      .setFeaturesCol("data_ngrams")
      .setNumTrees(15)

    val pipeline = new Pipeline()
      .setStages(Array(vecass, regtok, indexer, ng, bkm, rf))

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
