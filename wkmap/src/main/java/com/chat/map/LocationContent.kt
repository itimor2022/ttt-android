package com.chat.map

import android.os.Parcel
import android.os.Parcelable
import com.chat.base.msgitem.WKContentType
import org.json.JSONObject

/**
 * 位置消息内容
 */
class LocationContent() : com.xinbida.wukongim.msgmodel.WKMessageContent(), Parcelable {

    var latitude: Double = 0.0
    var longitude: Double = 0.0
    var address: String = ""
    var title: String = ""

    init {
        type = WKContentType.WK_LOCATION
    }

    constructor(parcel: Parcel) : this() {
        latitude = parcel.readDouble()
        longitude = parcel.readDouble()
        address = parcel.readString() ?: ""
        title = parcel.readString() ?: ""
    }

    override fun encodeMsg(): JSONObject {
        val jsonObject = JSONObject()
        jsonObject.put("latitude", latitude)
        jsonObject.put("longitude", longitude)
        jsonObject.put("address", address)
        jsonObject.put("title", title)
        return jsonObject
    }

    override fun decodeMsg(jsonObject: JSONObject): LocationContent {
        latitude = jsonObject.optDouble("latitude", 0.0)
        longitude = jsonObject.optDouble("longitude", 0.0)
        address = jsonObject.optString("address", "")
        title = jsonObject.optString("title", "")
        return this
    }

    override fun getDisplayContent(): String {
        return "[位置] $title"
    }

    override fun getSearchableWord(): String {
        return "$title $address"
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeDouble(latitude)
        parcel.writeDouble(longitude)
        parcel.writeString(address)
        parcel.writeString(title)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LocationContent> {
        override fun createFromParcel(parcel: Parcel): LocationContent {
            return LocationContent(parcel)
        }

        override fun newArray(size: Int): Array<LocationContent?> {
            return arrayOfNulls(size)
        }
    }
}
