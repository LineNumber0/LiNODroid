package ma.oprojet.linodroid.models.comments

import com.google.gson.annotations.SerializedName

data class AvatarUrls(

    @SerializedName("24")
    val small: String,

    @SerializedName("48")
    val medium: String,

    @SerializedName("96")
    val large: String
)
