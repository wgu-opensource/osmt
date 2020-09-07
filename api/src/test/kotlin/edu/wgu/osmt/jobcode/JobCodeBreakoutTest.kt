package edu.wgu.osmt.jobcode

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

internal class JobCodeBreakoutTest {

    @Test
    fun goldenPathTest() {
        // Setup
        val code = "11-1011.01"

        val expectedMajor = "11-0000"
        val expectedMinor = "11-1000"
        val expectedBroad = "11-1010"
        val expectedDetailed = "11-1011"
        val expectedJobRole = "11-1011.01"

        // Execute
        val majorResult = JobCodeBreakout.majorCode(code)
        val minorResult = JobCodeBreakout.minorCode(code)
        val broadResult = JobCodeBreakout.broadCode(code)
        val detailedResult = JobCodeBreakout.detailedCode(code)
        val jobRoleResult = JobCodeBreakout.jobRoleCode(code)

        // Verify
        assert(majorResult == expectedMajor) { "Wrong major code.  Expected=[$expectedMajor] Actual=[$majorResult]" }
        assert(minorResult == expectedMinor) { "Wrong minor code.  Expected=[$expectedMinor] Actual=[$minorResult]" }
        assert(broadResult == expectedBroad) { "Wrong broad code.  Expected=[$expectedBroad] Actual=[$broadResult]" }
        assert(detailedResult == expectedDetailed) { "Wrong detailed code.  Expected=[$expectedDetailed] Actual=[$detailedResult]" }
        assert(jobRoleResult == expectedJobRole) { "Wrong job role code.  Expected=[$expectedJobRole] Actual=[$jobRoleResult]" }
    }

    @Test
    fun broadCodeAndBroaderTest() {
        // Setup
        val code = "11-1110"

        val expectedMajor = "11-0000"
        val expectedMinor = "11-1100"
        val expectedBroad = "11-1110"

        // Execute
        val majorResult = JobCodeBreakout.majorCode(code)
        val minorResult = JobCodeBreakout.minorCode(code)
        val broadResult = JobCodeBreakout.broadCode(code)
        val detailedResult = JobCodeBreakout.detailedCode(code)
        val jobRoleResult = JobCodeBreakout.jobRoleCode(code)

        // Verify
        assert(majorResult == expectedMajor) { "Wrong major code.  Expected=[$expectedMajor] Actual=[$majorResult]" }
        assert(minorResult == expectedMinor) { "Wrong minor code.  Expected=[$expectedMinor] Actual=[$minorResult]" }
        assert(broadResult == expectedBroad) { "Wrong broad code.  Expected=[$expectedBroad] Actual=[$broadResult]" }
        assert(detailedResult == null) { "Wrong detailed code.  Expected=[null] Actual=[$detailedResult]" }
        assert(jobRoleResult == null) { "Wrong job role code.  Expected=[null] Actual=[$jobRoleResult]" }
    }

    @Test
    fun oneDigitMajorCodeTest() {
        // Setup
        val code = "1-1111.11"

        val expectedMajor = "1-0000"
        val expectedMinor = "1-1100"
        val expectedBroad = "1-1110"
        val expectedDetailed = "1-1111"
        val expectedJobRole = "1-1111.11"

        // Execute
        val majorResult = JobCodeBreakout.majorCode(code)
        val minorResult = JobCodeBreakout.minorCode(code)
        val broadResult = JobCodeBreakout.broadCode(code)
        val detailedResult = JobCodeBreakout.detailedCode(code)
        val jobRoleResult = JobCodeBreakout.jobRoleCode(code)

        // Verify
        assert(majorResult == expectedMajor) { "Wrong major code.  Expected=[$expectedMajor] Actual=[$majorResult]" }
        assert(minorResult == expectedMinor) { "Wrong minor code.  Expected=[$expectedMinor] Actual=[$minorResult]" }
        assert(broadResult == expectedBroad) { "Wrong broad code.  Expected=[$expectedBroad] Actual=[$broadResult]" }
        assert(detailedResult == expectedDetailed) { "Wrong detailed code.  Expected=[$expectedDetailed] Actual=[$detailedResult]" }
        assert(jobRoleResult == expectedJobRole) { "Wrong job role code.  Expected=[$expectedJobRole] Actual=[$jobRoleResult]" }
    }
}