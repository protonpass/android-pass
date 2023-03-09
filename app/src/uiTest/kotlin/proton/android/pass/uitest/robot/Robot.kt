package proton.android.pass.uitest.robot

import me.proton.test.fusion.Fusion
import me.proton.test.fusion.ui.compose.builders.OnNode

interface Robot {

    fun <T : Robot> OnNode.clickTo(goesTo: T): T = goesTo.apply { click() }

    fun nodeWithTextDisplayed(text: String) =
        Fusion.node.withText(text).await { assertIsDisplayed() }
}
