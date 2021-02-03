package com.kakapo.weatherapps.model

import android.os.Parcel
import android.os.Parcelable

data class Sys(
        val type: Int,
        val message: Double,
        val country: String?,
        val sunnrise: Int,
        val sunset: Int
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeDouble(message)
        parcel.writeString(country)
        parcel.writeInt(sunnrise)
        parcel.writeInt(sunset)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Sys> {
        override fun createFromParcel(parcel: Parcel): Sys {
            return Sys(parcel)
        }

        override fun newArray(size: Int): Array<Sys?> {
            return arrayOfNulls(size)
        }
    }

}
