/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.gui.slots;

import mods.railcraft.common.fluids.FluidItemHelper;
import mods.railcraft.common.fluids.FluidTools;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;

public class SlotWaterLimited extends SlotWater {

    public SlotWaterLimited(IInventory iinventory, int slotIndex, int posX, int posY) {
        super(iinventory, slotIndex, posX, posY);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        FluidStack fluidStack = FluidItemHelper.getFluidStackInContainer(stack);
        if (fluidStack != null && fluidStack.amount > FluidTools.BUCKET_VOLUME)
            return false;
        return super.isItemValid(stack);
    }

    @Override
    public int getSlotStackLimit() {
        return 4;
    }
}
