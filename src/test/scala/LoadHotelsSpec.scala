// Copyright (C) 2021 Nikita

import org.scalatest.funspec._
import java.sql.Date

class LoadHotelsSpec extends AnyFunSpec {
  describe("Visit days") {
    it("should return dates range from checkin to checkout - 1") {
      assert(LoadHotels.getVisitDays(Date.valueOf("2020-01-01"), Date.valueOf("2020-01-03"))
        == Seq(Date.valueOf("2020-01-01"), Date.valueOf("2020-01-02")))
    }

    it("should return empty list if checkin = checkout") {
      assert(LoadHotels.getVisitDays(Date.valueOf("2020-01-01"), Date.valueOf("2020-01-01"))
        == Seq[Date]())
    }

    it("should return empty list if checkout < checkin") {
      assert(LoadHotels.getVisitDays(Date.valueOf("2020-01-02"), Date.valueOf("2020-01-01"))
        == Seq[Date]())
    }
  }
}
