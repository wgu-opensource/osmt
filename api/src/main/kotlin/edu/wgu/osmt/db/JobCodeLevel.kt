package edu.wgu.osmt.db

const val ONET = "onet"
const val DETAILED = "detailed"
const val BROAD = "broad"
const val MINOR = "minor"
const val MAJOR = "major"

enum class JobCodeLevel(val apiValue: String) {
    ONet(ONET),
    Detailed(DETAILED),
    Broad(BROAD),
    Minor(MINOR),
    Major(MAJOR);

    companion object {
        fun forApiValue(apiValue: String) = values().find { it.apiValue.toLowerCase() == apiValue.toLowerCase() }
    }
}