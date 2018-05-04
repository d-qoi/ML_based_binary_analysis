package edu.rose.feature_finder

import org.apache.spark.ml.classification.LogisticRegression
import org.apache.spark.ml.linalg.{Vector, Vectors}
import org.apache.spark.ml.param.ParamMap
import org.apache.spark.sql.Row
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.SQLContext
import org.apache.spark.ml.feature.OneHotEncoderEstimator
import org.apache.spark.ml.feature.NGram

object Runner {

  def run(args: Array[String]) {
    val spark = SparkSession.builder.getOrCreate()
    import spark.implicits._

    val data = spark.read.format("csv").option("header","true").option("inferSchema","true").load(args(0))

    val encoder = new OneHotEncoderEstimator()
      .setInputCols(Array("categoryIndex1", "categoryIndex2"))
      .setOutputCols(Array("categoryVec1", "categoryVec2"))
    val model = encoder.fit(df)

    val encoded = model.transform(df)
    encoded.show()


  }

}

object FeatureExtractor {
  def main(args: Array[String]) {

    val spark = SparkSession.builder.appName("TestApplicationExampl").master("spark://hirschag-0:7077").getOrCreate()

    import spark.implicits._

    Runner.run(args)

    spark.stop()
  }
}


/**
  * Use this to test the app locally, from sbt:
  * sbt "run inputFile.txt outputFile.txt"
  *  (+ select CountingLocalApp when prompted)
  */
/*
object CountingLocalApp extends App{
  val (inputFile, outputFile) = (args(0), args(1))
  val conf = new SparkConf()
    .setMaster("local")
    .setAppName("This is a test app.")

  Runner.run(conf)
}

/**
  * Use this when submitting the app to a cluster with spark-submit
  * */
object CountingApp extends App{
  val (inputFile, outputFile) = (args(0), args(1))

  // spark-submit command should supply all necessary config elements
  Runner.run(new SparkConf())
}

object Runner {
  def run(conf: SparkConf): Unit = {
    val sc = new SparkContext(conf)
    println("In the runner")
  }
}
*/
