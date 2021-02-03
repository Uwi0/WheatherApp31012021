package com.kakapo.weatherapps.model

import android.os.Parcel
import android.os.Parcelable

data class WeatherResponse(
    val coord: Coord?,
    val weather: List<Weather>?,
    val base: String?,
    val main: Main?,
    val visibility: Int,
    val wind: Wind?,
    val cloud: Clouds?,
    val dt: Int,
    val sys: Sys?,
    val id: Int,
    val name: String?,
    val cod:Int
): Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readParcelable(Coord::class.java.classLoader),
            parcel.createTypedArrayList(Weather),
            parcel.readString(),
            parcel.readParcelable(Main::class.java.classLoader),
            parcel.readInt(),
            parcel.readParcelable(Wind::class.java.classLoader),
            parcel.readParcelable(Clouds::class.java.classLoader),
            parcel.readInt(),
            parcel.readParcelable(Sys::class.java.classLoader),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeParcelable(coord, flags)
        parcel.writeTypedList(weather)
        parcel.writeString(base)
        parcel.writeParcelable(main, flags)
        parcel.writeInt(visibility)
        parcel.writeParcelable(wind, flags)
        parcel.writeParcelable(cloud, flags)
        parcel.writeInt(dt)
        parcel.writeParcelable(sys, flags)
        parcel.writeInt(id)
        parcel.writeString(name)
        parcel.writeInt(cod)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<WeatherResponse> {
        override fun createFromParcel(parcel: Parcel): WeatherResponse {
            return WeatherResponse(parcel)
        }

        override fun newArray(size: Int): Array<WeatherResponse?> {
            return arrayOfNulls(size)
        }
    }
}
