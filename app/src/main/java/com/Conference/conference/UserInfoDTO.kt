package com.Conference.conference

data class UserInfoDTO(
    var email : String = "",
    var uid : String = "",
    var favoritePost : MutableMap<String,Boolean> = HashMap()
)