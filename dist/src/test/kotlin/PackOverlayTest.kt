import kr.toxicity.hud.pack.PackOverlay
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class PackOverlayTest {
    @Test
    fun `pack format 75 uses modern 1_21_6 shader branch`() {
        assertSame(PackOverlay.V1_21_6, overlayForPackFormat(75))
        assertEquals(3, PackOverlay.V1_21_6.shaderVersion)
    }

    @Test
    fun `known overlay shader versions stay explicit`() {
        assertEquals(0, PackOverlay.V1_21_2.shaderVersion)
        assertEquals(1, PackOverlay.V1_21_4.shaderVersion)
        assertEquals(3, PackOverlay.V1_21_6.shaderVersion)
        assertEquals(3, PackOverlay.V26_1.shaderVersion)
    }

    private fun overlayForPackFormat(format: Int): PackOverlay {
        return PackOverlay.entries.single { format in it.minVersion..it.maxVersion }
    }
}
