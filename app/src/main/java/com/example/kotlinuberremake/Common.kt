package com.example.kotlinuberremake

import com.example.kotlinuberremake.model.DriverInfoModel

/**
 * Created by iddangunawan on 30/06/20
 */
object Common {
    val DRIVER_INFO_REFERENCE: String = "DriverInfo"
    val DRIVERS_LOCATION_REFERENCE: String = "DriversLocation"

    var currentUser: DriverInfoModel? = null

    fun buildWelcomeMessage(): String {
        return if (currentUser != null)
            StringBuilder("Welcome ")
                .append(currentUser?.firstName)
                .append(" ")
                .append(currentUser?.lastName).toString()
        else
            ""
    }
}