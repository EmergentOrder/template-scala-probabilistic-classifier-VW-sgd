package org.template.classification

import io.prediction.controller.P2LAlgorithm
import io.prediction.controller.Params

import org.apache.spark.mllib.linalg.Vectors
import org.apache.spark.SparkContext
import org.apache.spark.mllib.linalg.Vector
import grizzled.slf4j.Logger

import java.nio.file.{Files, Paths}

import vw.VWScorer

case class AlgorithmParams(
  maxIter: Int,
  regParam: Double,
  stepSize: Double,
  bitPrecision: Int,
  modelName: String,
  namespace: String,
  ngram: Int
) extends Params

// extends P2LAlgorithm because VW doesn't contain RDD.
class VowpalLogisticRegressionWithSGDAlgorithm(val ap: AlgorithmParams)
  extends P2LAlgorithm[PreparedData, Array[Byte], Query, PredictedResult] {

  @transient lazy val logger = Logger[this.type]

  def train(sc: SparkContext, data: PreparedData): Array[Byte] = {
   
    require(!data.labeledPoints.take(1).isEmpty,
      s"RDD[labeldPoints] in PreparedData cannot be empty." +
      " Please check if DataSource generates TrainingData" +
      " and Preprator generates PreparedData correctly.")
  
    val reg = "--l2 " + ap.regParam
    val iters = "-c -k --passes " + ap.maxIter
    val lrate = "-l " + ap.stepSize
    val ngram = "--ngram " + ap.ngram 
  
    val vw = new VWScorer("--loss_function logistic -b " + ap.bitPrecision + " " + "-f " + ap.modelName + " " + reg + " " + iters + " " + lrate + " " + ngram)
    
    val inputs = for (point <- data.labeledPoints) yield (if (point.label == 0.0) "-1.0" else "1.0") + " |" + ap.namespace + " "  + rawTextToVWFormattedString(point.text) 
    
    //for (item <- inputs.collect()) logger.info(item)

    val results = for (item <- inputs.collect()) yield vw.doLearnAndGetPrediction(item)  
   
    vw.closeInstance()
     
    Files.readAllBytes(Paths.get(ap.modelName))
  }

  def predict(byteArray: Array[Byte], query: Query): PredictedResult = {
    Files.write(Paths.get(ap.modelName), byteArray)

    val vw = new VWScorer("--link logistic -i " + ap.modelName)
    val pred = vw.getPrediction("|" + ap.namespace + " " + rawTextToVWFormattedString(query.text)).toDouble 
    vw.closeInstance()
  
    val result = new PredictedResult(pred)
   
    result
  }

  def rawTextToVWFormattedString(str: String) : String = {
     //VW input cannot contain these characters 
     str.replaceAll("[|:]", " ")
  }

  def vectorToVWFormattedString(vec: Vector): String = {
     vec.toArray.zipWithIndex.map{ case (dbl, int) => s"$int:$dbl"} mkString " "
  }

}
