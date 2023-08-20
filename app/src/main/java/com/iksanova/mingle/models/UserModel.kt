package com.iksanova.mingle.models

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator

class UserModel(
    var username: String?,
    var emailAddress: String?,
    var imageUrl: String?,
    var key: String?,
    var token: String?,
    var location: String?,
    var headline: String?,
    var about: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(emailAddress)
        parcel.writeString(imageUrl)
        parcel.writeString(key)
        parcel.writeString(token)
        parcel.writeString(headline)
        parcel.writeString(location)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<UserModel> {
        override fun createFromParcel(parcel: Parcel): UserModel {
            return UserModel(parcel)
        }

        override fun newArray(size: Int): Array<UserModel?> {
            return arrayOfNulls(size)
        }
    }
}
