package cum.xiaomao.zerohack.gui.hudgui.elements.client

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import cum.xiaomao.zerohack.event.events.TickEvent
import cum.xiaomao.zerohack.event.safeParallelListener
import cum.xiaomao.zerohack.gui.hudgui.HudElement
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.module.ModuleManager
import cum.xiaomao.zerohack.module.modules.client.Hud
import cum.xiaomao.zerohack.util.TimeUnit
import cum.xiaomao.zerohack.util.atTrue
import cum.xiaomao.zerohack.util.collections.ArrayMap
import cum.xiaomao.zerohack.util.delegate.AsyncCachedValue
import cum.xiaomao.zerohack.util.extension.sumOfFloat
import cum.xiaomao.zerohack.util.graphics.Easing
import cum.xiaomao.zerohack.util.graphics.HAlign
import cum.xiaomao.zerohack.util.graphics.RenderUtils2D
import cum.xiaomao.zerohack.util.graphics.VAlign
import cum.xiaomao.zerohack.util.graphics.color.ColorRGB
import cum.xiaomao.zerohack.util.graphics.color.ColorUtils
import cum.xiaomao.zerohack.util.graphics.font.TextComponent
import cum.xiaomao.zerohack.util.graphics.font.renderer.MainFontRenderer
import cum.xiaomao.zerohack.util.interfaces.DisplayEnum
import cum.xiaomao.zerohack.util.state.TimedFlag
import cum.xiaomao.zerohack.util.text.format
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.util.text.TextFormatting
import kotlin.math.max

internal object ActiveModules : HudElement(
    name = "ActiveModules",
    category = Category.CLIENT,
    description = "List of enabled modules",
    enabledByDefault = true
) {
    private val mode by setting("Mode", Mode.LEFT_TAG)
    private val sortingMode by setting("Sorting Mode", SortingMode.LENGTH)
    private val showInvisible by setting("Show Invisible", false)
    private val bindOnly by setting("Bind Only", true, { !showInvisible })
    private val rainbow0 = setting("Rainbow", true)
    private val rainbow by rainbow0
    private val rainbowLength by setting("Rainbow Length", 10.0f, 1.0f..20.0f, 0.5f, rainbow0.atTrue())
    private val indexedHue by setting("Indexed Hue", 0.5f, 0.0f..1.0f, 0.05f, rainbow0.atTrue())
    private val saturation by setting("Saturation", 0.5f, 0.0f..1.0f, 0.01f, rainbow0.atTrue())
    private val brightness by setting("Brightness", 1.0f, 0.0f..1.0f, 0.01f, rainbow0.atTrue())
    private val frameColor by setting("Frame Color", ColorRGB(12, 16, 20, 127), true)

    private enum class Mode {
        LEFT_TAG,
        RIGHT_TAG,
        FRAME
    }

    @Suppress("UNUSED")
    private enum class SortingMode(
        override val displayName: CharSequence,
        val comparator: Comparator<AbstractModule>
    ) : DisplayEnum {
        LENGTH("Length", compareByDescending { it.textLine.getWidth() }),
        ALPHABET("Alphabet", compareBy { it.nameAsString }),
        CATEGORY("Category", compareBy { it.category.ordinal })
    }

    private var cacheWidth = 20.0f
    private var cacheHeight = 20.0f
    override val hudWidth: Float get() = cacheWidth
    override val hudHeight: Float get() = cacheHeight

    private val textLineMap = Int2ObjectOpenHashMap<TextComponent.TextLine>()
    private var lastSorted = ModuleManager.modules.toTypedArray()

    private val sortedModuleList by AsyncCachedValue(5L) {
        val modules = ModuleManager.modules
        if (modules.size != lastSorted.size) {
            lastSorted = modules.toTypedArray()
        }

        lastSorted.sortWith(sortingMode.comparator)
        lastSorted
    }

    private var prevToggleMap = ArrayMap<ModuleToggleFlag>()
    private val toggleMap by AsyncCachedValue(1L, TimeUnit.SECONDS) {
        ArrayMap<ModuleToggleFlag>().apply {
            ModuleManager.modules.forEach {
                this[it.id] = prevToggleMap[it.id] ?: ModuleToggleFlag(it)
            }
            prevToggleMap = this
        }
    }

    init {
        safeParallelListener<TickEvent.Post> {
            for ((id, flag) in toggleMap) {
                flag.update()
                if (flag.progress <= 0.0f) continue
                textLineMap[id] = flag.module.newTextLine()
            }

            cacheWidth = sortedModuleList.maxOfOrNull {
                if (toggleMap[it.id]?.value == true) it.textLine.getWidth() + 4.0f
                else 20.0f
            }?.let {
                max(it, 20.0f)
            } ?: 20.0f

            cacheHeight = max(toggleMap.values.sumOfFloat { it.displayHeight }, 20.0f)
        }
    }

    override fun renderHud() {
        super.renderHud()
        GlStateManager.pushMatrix()

        GlStateManager.translate(width / scale * dockingH.multiplier, 0.0f, 0.0f)
        if (dockingV == VAlign.BOTTOM) {
            GlStateManager.translate(0.0f, height / scale - (MainFontRenderer.getHeight() + 2.0f), 0.0f)
        } else if (dockingV == VAlign.TOP) {
            GlStateManager.translate(0.0f, -1.0f, 0.0f)
        }

        if (dockingH == HAlign.LEFT) {
            GlStateManager.translate(-1.0f, 0.0f, 0.0f)
        }

        when (mode) {
            Mode.LEFT_TAG -> {
                if (dockingH == HAlign.LEFT) {
                    GlStateManager.translate(2.0f, 0.0f, 0.0f)
                }
            }
            Mode.RIGHT_TAG -> {
                if (dockingH == HAlign.RIGHT) {
                    GlStateManager.translate(-2.0f, 0.0f, 0.0f)
                }
            }
            else -> {
                // 0x22 cute catgirl owo
            }
        }

        drawModuleList()

        GlStateManager.popMatrix()
    }

    private fun drawModuleList() {
        if (rainbow) {
            val lengthMs = rainbowLength * 1000.0f
            val timedHue = System.currentTimeMillis() % lengthMs.toLong() / lengthMs
            var index = 0

            for (module in sortedModuleList) {
                val timedFlag = toggleMap[module.id] ?: continue
                val progress = timedFlag.progress

                if (progress <= 0.0f) continue

                GlStateManager.pushMatrix()

                val hue = timedHue + indexedHue * 0.05f * index
                val color = ColorUtils.hsbToRGB(hue, saturation, brightness)

                val textLine = module.textLine
                val textWidth = textLine.getWidth()
                val animationXOffset = textWidth * dockingH.offset * (1.0f - progress)
                val stringPosX = textWidth * dockingH.multiplier
                val margin = 2.0f * dockingH.offset

                var yOffset = timedFlag.displayHeight

                GlStateManager.translate(animationXOffset - margin - stringPosX, 0.0f, 0.0f)

                when (mode) {
                    Mode.LEFT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                        RenderUtils2D.drawRectFilled(-4.0f, 0.0f, -2.0f, yOffset, color)
                    }
                    Mode.RIGHT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                        RenderUtils2D.drawRectFilled(textWidth + 2.0f, 0.0f, textWidth + 4.0f, yOffset, color)
                    }
                    Mode.FRAME -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                    }
                }

                module.newTextLine(color).drawLine(progress, HAlign.LEFT)

                if (dockingV == VAlign.BOTTOM) yOffset *= -1.0f
                GlStateManager.popMatrix()
                GlStateManager.translate(0.0f, yOffset, 0.0f)
                index++
            }
        } else {
            val color = secondaryColor
            for (module in sortedModuleList) {
                val timedFlag = toggleMap[module.id] ?: continue
                val progress = timedFlag.progress

                if (progress <= 0.0f) continue

                GlStateManager.pushMatrix()

                val textLine = module.textLine
                val textWidth = textLine.getWidth()
                val animationXOffset = textWidth * dockingH.offset * (1.0f - progress)
                val stringPosX = textWidth * dockingH.multiplier
                val margin = 2.0f * dockingH.offset

                var yOffset = timedFlag.displayHeight

                GlStateManager.translate(animationXOffset - margin - stringPosX, 0.0f, 0.0f)

                when (mode) {
                    Mode.LEFT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                        RenderUtils2D.drawRectFilled(-4.0f, 0.0f, -2.0f, yOffset, color)
                    }
                    Mode.RIGHT_TAG -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                        RenderUtils2D.drawRectFilled(textWidth + 2.0f, 0.0f, textWidth + 4.0f, yOffset, color)
                    }
                    Mode.FRAME -> {
                        RenderUtils2D.drawRectFilled(-2.0f, 0.0f, textWidth + 2.0f, yOffset, frameColor)
                    }
                }

                textLine.drawLine(progress, HAlign.LEFT)

                if (dockingV == VAlign.BOTTOM) yOffset *= -1.0f
                GlStateManager.popMatrix()
                GlStateManager.translate(0.0f, yOffset, 0.0f)
            }
        }
    }

    private val AbstractModule.textLine
        get() = textLineMap.getOrPut(this.id) {
            this.newTextLine()
        }

    private fun AbstractModule.newTextLine(color: ColorRGB = Hud.secondaryColor) =
        TextComponent.TextLine(" ").apply {
            add(TextComponent.TextElement(nameAsString, color))
            getHudInfo().let {
                if (it.isNotBlank()) {
                    add(TextComponent.TextElement("${TextFormatting.GRAY format "["}${it}${TextFormatting.GRAY format "]"}", ColorRGB(255, 255, 255)))
                }
            }
            if (dockingH == HAlign.RIGHT) reverse()
        }

    private val TimedFlag<Boolean>.displayHeight
        get() = (MainFontRenderer.getHeight() + 2.0f) * progress

    private val TimedFlag<Boolean>.progress
        get() = if (value) {
            Easing.OUT_CUBIC.inc(Easing.toDelta(lastUpdateTime, 300L))
        } else {
            Easing.IN_CUBIC .dec(Easing.toDelta(lastUpdateTime, 300L))
        }

    private class ModuleToggleFlag(val module: AbstractModule) : TimedFlag<Boolean>(module.state) {
        fun update() {
            value = module.state
        }
    }

    private val AbstractModule.state: Boolean
        get() = this.isEnabled && (showInvisible || this.isVisible && (!bindOnly || !this.bind.value.isEmpty))

    init {
        relativePosX = -2.0f
        relativePosY = 2.0f
        dockingH = HAlign.RIGHT
    }

}