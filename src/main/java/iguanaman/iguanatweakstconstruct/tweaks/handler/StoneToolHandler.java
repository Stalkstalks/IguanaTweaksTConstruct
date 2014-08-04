package iguanaman.iguanatweakstconstruct.tweaks.handler;

import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import tconstruct.library.TConstructRegistry;
import tconstruct.library.event.ToolCraftEvent;
import tconstruct.library.tools.ToolMaterial;
import tconstruct.library.util.IToolPart;

public class StoneToolHandler {
    // we can initialize this statically, because it wont be initialized until PostInit, where all materials are already registered
    private static ToolMaterial stoneMaterial = TConstructRegistry.getMaterial("Stone");

    @SubscribeEvent
    public void onTooltip(ItemTooltipEvent event)
    {
        if(event.entityPlayer == null)
            return;

        // we're only interested if it's a tool part
        if(!(event.itemStack.getItem() instanceof IToolPart))
            return;

        ItemStack stack = event.itemStack;
        IToolPart part = (IToolPart)stack.getItem();

        // stone parts disabled?
        if(TConstructRegistry.getMaterial(part.getMaterialID(stack)) == stoneMaterial)
        {
            event.toolTip.add(1, "");
            event.toolTip.add(2, "\u00a74Can only be used to make casts,");
            event.toolTip.add(3, "\u00a74cannot be used to make a tool");
            // we abuse the fact that the result is not used by anything to signal our other handlers to not add another tooltip
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public void onToolCraft(ToolCraftEvent event)
    {
        // don't allow stone tools
        for(ToolMaterial mat : event.materials)
            if(mat == stoneMaterial)
                event.setResult(Event.Result.DENY);
    }
}