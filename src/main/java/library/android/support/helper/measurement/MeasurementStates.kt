package library.android.support.helper.measurement

sealed class MeasurementStates {

    class OnTick(val tick: Int): MeasurementStates()
    object OnShortEnd: MeasurementStates()
    object StateNothing : MeasurementStates()
    class OnEndMeasurement(val type: Int, val repeat: Boolean): MeasurementStates()
}