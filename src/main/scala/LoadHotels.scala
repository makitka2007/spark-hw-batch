// Copyright (C) 2021 Nikita

import java.sql.Date

import org.apache.spark.sql.expressions.{UserDefinedFunction, Window}
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types.{LongType, StringType, StructType}
import org.apache.spark.sql._

object LoadHotels {
  val spark: SparkSession = SparkSession.builder.appName("Load Hotels").getOrCreate()
  import spark.implicits._

  def getVisitDays(checkin: Date, checkout: Date): Seq[Date] = {
    Iterator.iterate(checkin.toLocalDate)(d => d.plusDays(1)).takeWhile(_.isBefore(checkout.toLocalDate))
      .map(Date.valueOf).toList
  }

  val getVisitDaysUDF: UserDefinedFunction = udf(getVisitDays _)

  def getInvalidHotels(expedia: DataFrame): Dataset[Row] = {
    val visits = expedia.select($"hotel_id", $"srch_ci", $"srch_co")
    val flattenDates = visits.filter("srch_co > srch_ci")
      .withColumn("visit_date", getVisitDaysUDF($"srch_ci", $"srch_co"))
      .withColumn("visit_date", explode($"visit_date"))
      .drop("srch_ci", "srch_co")
    val idleDays = flattenDates.withColumn("idle_days", datediff(
      lead($"visit_date", offset = 1).over(Window.partitionBy($"hotel_id").orderBy($"visit_date")), $"visit_date"
    ) - 1)
      .filter("idle_days > 0")
    val invalidHotels = idleDays.groupBy($"hotel_id").agg(sum($"idle_days").alias("idle_days")).filter("idle_days between 2 and 30")
    invalidHotels
  }

  def main(args: Array[String]) {
    val kafkaSchema = new StructType()
      .add($"Id".string)
      .add($"Name".string)
      .add($"Country".string)
      .add($"City".string)

    val hotelsWithWeather = spark.read.format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("subscribe", "hotelsWithWeather")
      .option("startingOffsets", "earliest")
      .load()
      .select(from_json($"value".cast(StringType), kafkaSchema).as("data"))
      .select("data.*")
    val hotels = hotelsWithWeather.select(
        $"Id".cast(LongType).alias("hotel_id"),
        $"Name".alias("hotel_name"),
        $"Country".alias("hotel_country"),
        $"City".alias("hotel_city")
      )
      .dropDuplicates("hotel_id")
    hotels.cache().count()

    val expedia = spark.read.format("avro").load("/user/nikita/expedia")

    val invalidHotels = getInvalidHotels(expedia)
    invalidHotels.cache().count()
    invalidHotels.show()

    val invalidHotelsWithName = invalidHotels.alias("ih").join(hotels.alias("h"), $"ih.hotel_id" === $"h.hotel_id")

    val validExpedia = expedia.alias("v").join(invalidHotels.alias("ih"), $"ih.hotel_id" === $"v.hotel_id", "leftanti")
    val validExpediaWithHotelInfo = validExpedia.alias("v").join(hotels.alias("h"), $"h.hotel_id" === $"v.hotel_id")
    val validBookingsByCountry = validExpediaWithHotelInfo.groupBy("hotel_country").count().as("bookings_count")
    val validBookingsByCity = validExpediaWithHotelInfo.groupBy("hotel_city").count().as("bookings_count")

    invalidHotelsWithName.show()
    validBookingsByCountry.show()
    validBookingsByCity.show()

    val validExpediaWithYear = validExpedia.withColumn("year", year($"srch_ci"))
    validExpediaWithYear.write.mode(SaveMode.Overwrite).partitionBy("year").format("delta").save("/user/nikita/valid_expedia")

    spark.stop()
  }
}
