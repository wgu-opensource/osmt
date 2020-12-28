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
        val expectedMinor = "11-1000"
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
        val expectedMinor = "1-1000"
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

    @Test
    fun dotInsteadOfHyphenTest() {
        // Setup
        val code = "11.1111.11"

        val expectedMajor = "11-0000"
        val expectedMinor = "11-1000"
        val expectedBroad = "11-1110"
        val expectedDetailed = "11-1111"
        val expectedJobRole = "11-1111.11"

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
        assert(jobRoleResult == expectedJobRole) { "Wrong job role code.  Expected=[null] Actual=[$jobRoleResult]" }
    }

    @Test
    fun minorExceptionGroupsTest() {
        val code = "31-1110"
        val code2 = "15-1210.01"
        val code3 = "51-5132.00"

        val normalMinorCode = "31-1331"

        val expectedMinor = "31-1100"
        val expectedMinor2 = "15-1200"
        val expectedMinor3= "51-5100"

        val expectedNormalMinorCode = "31-1000"

        val minorResult = JobCodeBreakout.minorCode(code)
        val minorResult2 = JobCodeBreakout.minorCode(code2)
        val minorResult3 = JobCodeBreakout.minorCode(code3)
        val normalMinorResult = JobCodeBreakout.minorCode(normalMinorCode)

        assert(expectedMinor == minorResult)
        assert(expectedMinor2 == minorResult2)
        assert(expectedMinor3 == minorResult3)
        assert(expectedNormalMinorCode == normalMinorResult)
    }
}
