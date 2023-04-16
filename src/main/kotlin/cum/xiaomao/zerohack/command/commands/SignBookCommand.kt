package cum.xiaomao.zerohack.command.commands

import cum.xiaomao.zerohack.command.ClientCommand
import cum.xiaomao.zerohack.util.extension.max
import cum.xiaomao.zerohack.util.extension.remove
import cum.xiaomao.zerohack.util.items.itemPayload
import cum.xiaomao.zerohack.util.text.MessageSendUtils
import cum.xiaomao.zerohack.util.text.MessageSendUtils.sendNoSpamChatMessage
import cum.xiaomao.zerohack.util.text.formatValue
import net.minecraft.item.ItemWritableBook
import net.minecraft.nbt.NBTTagList
import net.minecraft.nbt.NBTTagString

object SignBookCommand : ClientCommand(
    name = "signbook",
    alias = arrayOf("sign"),
    description = "Colored book names. §f#n§7 for a new line and §f&§7 for color codes"
) {
    init {
        string("title") { titleArg ->
            executeSafe {
                val item = player.inventory.getCurrentItem()

                if (item.item is ItemWritableBook) {
                    val title = titleArg.value
                        .remove("null")
                        .replace("&", 0x00A7.toString())
                        .replace("#n", "\n")
                        .max(31)

                    val pages = NBTTagList()
                    val bookData = item.tagCompound // have to save this
                    pages.appendTag(NBTTagString(""))

                    if (item.hasTagCompound()) {
                        bookData?.let { item.tagCompound = it }
                        item.tagCompound!!.setTag("title", NBTTagString(title))
                        item.tagCompound!!.setTag("author", NBTTagString(player.name))
                    } else {
                        item.setTagInfo("pages", pages)
                        item.setTagInfo("title", NBTTagString(title))
                        item.setTagInfo("author", NBTTagString(player.name))
                    }

                    itemPayload(item, "MC|BSign")
                    sendNoSpamChatMessage("Signed book with title: ${formatValue(title)}")
                } else {
                    MessageSendUtils.sendNoSpamErrorMessage("You're not holding a writable book!")
                }
            }
        }
    }
}