package rs.covtakt.util

enum  class NotificationType(var type:String){
    INFECTION("infection"),
    EVENT("event")

}
enum class FirebaseExceptionType(var type:String){
    UNKNOWN("UNKNOWN"),
    FAILED_PRECONDITION("FAILED_PRECONDITION"),
    INVALID_ARGUMENT("INVALID_ARGUMENT")
}