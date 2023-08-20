package com.iksanova.mingle.models

class StoryModel(
    val storyImg: String,
    val timeStart: Long,
    val timeEnd: Long,
    val userId: String,
    val storyId: String,
    val timeUpload: String
) {
    constructor() : this("", 0, 0, "", "", "")
}
