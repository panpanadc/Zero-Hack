package cum.xiaomao.zerohack.command

import kotlinx.coroutines.launch
import cum.xiaomao.zerohack.command.args.AbstractArg
import cum.xiaomao.zerohack.event.AlwaysListening
import cum.xiaomao.zerohack.event.ClientExecuteEvent
import cum.xiaomao.zerohack.event.SafeExecuteEvent
import cum.xiaomao.zerohack.gui.hudgui.AbstractHudElement
import cum.xiaomao.zerohack.module.AbstractModule
import cum.xiaomao.zerohack.module.modules.client.CommandSetting
import cum.xiaomao.zerohack.util.PlayerProfile
import cum.xiaomao.zerohack.util.Wrapper
import cum.xiaomao.zerohack.util.threads.defaultScope
import cum.xiaomao.zerohack.util.threads.toSafe
import net.minecraft.block.Block
import net.minecraft.item.Item
import net.minecraft.util.math.BlockPos
import java.io.File

abstract class ClientCommand(
    name: String,
    alias: Array<out String> = emptyArray(),
    description: String = "No description",
) : CommandBuilder<ClientExecuteEvent>(name, alias, description), AlwaysListening {

    val prefixName get() = "$prefix$name"

    @CommandBuilder
    protected inline fun AbstractArg<*>.module(
        name: String,
        block: BuilderBlock<AbstractModule>
    ) {
        arg(ModuleArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.hudElement(
        name: String,
        block: BuilderBlock<AbstractHudElement>
    ) {
        arg(HudElementArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.block(
        name: String,
        block: BuilderBlock<Block>
    ) {
        arg(BlockArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.item(
        name: String,
        block: BuilderBlock<Item>
    ) {
        arg(ItemArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.player(
        name: String,
        block: BuilderBlock<PlayerProfile>
    ) {
        arg(PlayerArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.blockPos(
        name: String,
        block: BuilderBlock<BlockPos>
    ) {
        arg(BlockPosArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.baritoneBlock(
        name: String,
        block: BuilderBlock<Block>
    ) {
        arg(BaritoneBlockArg(name), block)
    }

    @CommandBuilder
    protected inline fun AbstractArg<*>.schematic(
        name: String,
        file: BuilderBlock<File>
    ) {
        arg(SchematicArg(name), file)
    }

    @CommandBuilder
    protected fun AbstractArg<*>.executeAsync(
        description: String = "No description",
        block: ExecuteBlock<ClientExecuteEvent>
    ) {
        val asyncExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            defaultScope.launch { block() }
        }
        this.execute(description, block = asyncExecuteBlock)
    }

    @CommandBuilder
    protected fun AbstractArg<*>.executeSafe(
        description: String = "No description",
        block: ExecuteBlock<SafeExecuteEvent>
    ) {
        val safeExecuteBlock: ExecuteBlock<ClientExecuteEvent> = {
            toSafe()?.block()
        }
        this.execute(description, block = safeExecuteBlock)
    }

    protected companion object {
        val mc = Wrapper.minecraft
        val prefix: String get() = CommandSetting.prefix
    }

}