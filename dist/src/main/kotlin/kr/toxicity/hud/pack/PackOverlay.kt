package kr.toxicity.hud.pack

import kr.toxicity.hud.util.PLUGIN

enum class PackOverlay(
    val overlayName: String,
    val minVersion: Int,
    val maxVersion: Int,
    val shaderVersion: Int
) {
    V1_21_2("betterhud_1_21_2", 9, 45, 0),
    V1_21_4("betterhud_1_21_4", 46, 55, 1),
    V1_21_6("betterhud_1_21_6", 56, 83, 3),
    V26_1("betterhud_26_1", 84, 99, 3)
    ;
    fun loadAssets() {
        PLUGIN.loadAssets(overlayName) { n, i ->
            val read = i.readAllBytes()
            PackGenerator.addTask(buildList {
                add(overlayName)
                addAll(n.split('/'))
            }) {
                read
            }
        }
    }
}
