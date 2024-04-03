package com.vaibhav.socialsphere

 data class UserData(
    val name: String = " ",
    val email: String = " ",
    val profileImage : String = "",
     val password :String = ""
     ){
     constructor():this(" "," "," "," ")
 }

