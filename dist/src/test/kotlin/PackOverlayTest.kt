import kr.toxicity.hud.pack.PackOverlay
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue

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

    @Test
    fun `text shader limits hud positioning to gui projection`() {
        val shaderPath = listOf(
            Path.of("common-resources/text.vsh"),
            Path.of("../common-resources/text.vsh")
        ).first(Files::exists)
        val source = Files.readString(shaderPath)

        assertTrue(source.contains("bool betterHudGuiProjection"))
        assertTrue(source.contains("abs(ProjMat[3].x + 1.0) < 0.01"))
        assertTrue(source.contains("abs(ProjMat[3].y - 1.0) < 0.01"))
        assertTrue(source.contains("if (betterHudGuiProjection && pos.y >= ui.y)"))
        assertFalse(source.contains("if (pos.y >= ui.y) {"))
        assertFalse(source.contains("if (pos.y >= ui.y && ProjMat[3].x == -1)"))
    }

    private fun overlayForPackFormat(format: Int): PackOverlay {
        return PackOverlay.entries.single { format in it.minVersion..it.maxVersion }
    }
}
