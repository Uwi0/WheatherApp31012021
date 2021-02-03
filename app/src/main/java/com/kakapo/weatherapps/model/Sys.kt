package com.kakapo.weatherapps.model

import android.os.Parcel
import android.os.Parcelable

data class Sys(
        val type: Int,
        val message: Double,
        val country: String?,
        val sunnrise: Long,
        val sunset: Long
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readDouble(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(type)
        parcel.writeDouble(message)
        parcel.writeString(country)
        parcel.writeLong(sunnrise)
        parcel.writeLong(sunset)
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
