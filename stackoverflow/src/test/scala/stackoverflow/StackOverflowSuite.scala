package stackoverflow

import org.scalatest.{FunSuite, BeforeAndAfterAll}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.apache.spark.SparkConf
import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._
import org.apache.spark.rdd.RDD
import java.net.URL
import java.nio.channels.Channels
import java.io.File
import java.io.FileOutputStream

@RunWith(classOf[JUnitRunner])
class StackOverflowSuite extends FunSuite with BeforeAndAfterAll {


  lazy val testObject = new StackOverflow {
    override val langs =
      List(
        "JavaScript", "Java", "PHP", "Python", "C#", "C++", "Ruby", "CSS",
        "Objective-C", "Perl", "Scala", "Haskell", "MATLAB", "Clojure", "Groovy")
    override def langSpread = 50000
    override def kmeansKernels = 45
    override def kmeansEta: Double = 20.0D
    override def kmeansMaxIterations = 150
  }

  test("testObject can be instantiated") {
    val instantiatable = try {
      testObject
      true
    } catch {
      case _: Throwable => false
    }
    assert(instantiatable, "Can't instantiate a StackOverflow object")
  }

  test("test groupedPostings") {
    import StackOverflow._
    val question = Posting(1, 1, None, None, 23, Some("Javascript"))
    val answer = Posting(2, 2, None, Some(1), 45, None)
    val postings = List(question, answer)
    val result = testObject.groupedPostings(sc.parallelize(postings)).collect

    result match {
      case Array((qId, (q, a) :: _), _*) => {
        assert(qId == 1, "wrong question id")
        assert(q == question, "wrong question")
        assert(a == answer, "wrong answer")
      }
      case _ => assert(false, "completely wrong")
    }
  }

  test("test scoredPostings") {
    import StackOverflow._
    val question = Posting(1, 1, None, None, 23, Some("Javascript"))
    val highAnswer = Posting(2, 2, None, Some(1), 45, None)
    val lowAnswer = Posting(2, 3, None, Some(1), 5, None)
    val postings = List(question, highAnswer, lowAnswer)
    val groupedRdd = sc.parallelize(Array((1, List((question, highAnswer), (question, lowAnswer)).toIterable)))
    val result = testObject.scoredPostings(groupedRdd).collect()
    result match {
      case Array((q, score)) => {
        assert(q == question, "wrong question")
        assert(score == 45, "wrong score")
      }
      case _ => assert(false, "complete wrong")
    }
  }

  test("test vectors") {
    import StackOverflow._
    val questionJS = (Posting(1, 1, None, None, 23, Some("JavaScript")), 45)
    val questionPerl = (Posting(1, 2, None, None, 23, Some("PHP")), 23)
    val rdd = sc.parallelize(List(questionJS, questionPerl))
    val result = testObject.vectorPostings(rdd).collect()
    result match {
      case Array((q1, s1), (q2, s2)) => {
        assert(q1 == 0, "js index correct")
        assert(s1 == 45, "js score correct")
        assert(q2 == 2 * langSpread, "perl index correct")
        assert(s2 == 23, "js score correct")
      }
      case _ => assert(false, "completely wrong")
    }
  }

  test("test kmeans") {
    import StackOverflow._
    val vectors = sc.parallelize(List((0, 20), (0, 40), (5000, 10), (5000, 30)))
    val means = testObject.kmeans(Array((0,0), (5000, 0)), vectors)
    assert(means(0) == ((0, 30)))
    assert(means(1) == ((5000, 20)))
  }

  test("test clusterResults") {
    import StackOverflow._
    val vectors = sc.parallelize(List((0, 20), (0, 40)))
    val res = testObject.clusterResults(Array((0, 40)),vectors).head
    assert(res == ("JavaScript", 100.0, 2, 30))
  }
}
