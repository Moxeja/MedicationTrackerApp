package com.jake.medicationtracker.pojo

data class Medication(val id: Int,
                      val name: String,
                      val time: String,
                      val doseSize: Int,
                      val stockLeft: Int)
