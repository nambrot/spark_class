package timeusage

import org.apache.spark.sql.{ColumnName, DataFrame, Row}
import org.apache.spark.sql.types.{
  DoubleType,
  StringType,
  StructField,
  StructType
}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfterAll, FunSuite}

import scala.util.Random

@RunWith(classOf[JUnitRunner])
class TimeUsageSuite extends FunSuite with BeforeAndAfterAll {
  test("timeusagesummary") {
    import TimeUsage.spark.implicits._
    val (columns, initDf) = TimeUsage.read("/timeusage/atussum.csv")
    val (primaryNeedsColumns, workColumns, otherColumns) = TimeUsage.classifiedColumns(columns)
    val summaryDf = TimeUsage.timeUsageSummary(primaryNeedsColumns, workColumns, otherColumns, initDf)
    val firstDf = TimeUsage.timeUsageGrouped(summaryDf)
    val secondDf = TimeUsage.timeUsageGroupedSql(summaryDf)
    val thirdDf = TimeUsage.timeUsageGroupedTyped(summaryDf.as[TimeUsageRow])
    assert(firstDf.collect().deep == secondDf.collect().deep, "should be equal")
    val firstSeq = firstDf.collect().map(row => row.toSeq)
    val thirdSeq = thirdDf.collect().map((row) => Row(row.working, row.sex, row.age, row.primaryNeeds, row.work, row.other).toSeq)
    assert(firstSeq.deep == thirdSeq.deep, "should be equal")
  }
}
