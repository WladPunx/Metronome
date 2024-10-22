package com.wladkoshelev.metronome.metronome

import android.media.ToneGenerator

/** модель звуков для Метранома (data-слой)
 * @param sound используется для проигрывания звуков в {[MetronomeLDS.Impl.setNextBeat]}
 * @param title используется как для отображения на UI {[MetronomeLDS.Face.soundNameList]}
 * так и для нахождение звука по названия в {[MetronomeLDS.Impl.setMainSound]} {[MetronomeLDS.Impl.setSecondSound]} */
enum class MetronomeSoundEntity(
    val title: String,
    val sound: Int
) {
    TONE_PROP_BEEP("Prop Beep", ToneGenerator.TONE_PROP_BEEP), // основной по умолчанию
    TONE_CDMA_PIP("Cdma Pip", ToneGenerator.TONE_CDMA_PIP), // второй по умолчанию

    // дополнительные
    TONE_CDMA_ABBR_ALERT("Abbr Alert", ToneGenerator.TONE_CDMA_ABBR_ALERT),
    TONE_CDMA_NETWORK_BUSY_ONE_SHOT("Busy", ToneGenerator.TONE_CDMA_NETWORK_BUSY_ONE_SHOT),
    TONE_CDMA_ANSWER("Answer", ToneGenerator.TONE_CDMA_ANSWER),
    TONE_CDMA_NETWORK_BUSY("Network Busy", ToneGenerator.TONE_CDMA_NETWORK_BUSY),
    TONE_CDMA_INTERCEPT("Cdma Intercept", ToneGenerator.TONE_CDMA_INTERCEPT),
    TONE_CDMA_DIAL_TONE_LITE("Dial Tone Lite", ToneGenerator.TONE_CDMA_DIAL_TONE_LITE),
    TONE_CDMA_NETWORK_USA_RINGBACK("Network Usa Ringback", ToneGenerator.TONE_CDMA_NETWORK_USA_RINGBACK),
    TONE_SUP_CONFIRM("Sup Confirm", ToneGenerator.TONE_SUP_CONFIRM),
    TONE_PROP_NACK("Nack", ToneGenerator.TONE_PROP_NACK),
    TONE_PROP_ACK("Prop Ack", ToneGenerator.TONE_PROP_ACK),
    TONE_SUP_ERROR("Sup Error", ToneGenerator.TONE_SUP_ERROR)
}