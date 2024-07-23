package library.android.support.helper

fun Long.withMb(): String {
    val result  = this / 1024 / 1024

    return "$result M"
}