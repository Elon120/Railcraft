/*------------------------------------------------------------------------------
 Copyright (c) CovertJaguar, 2011-2016
 http://railcraft.info

 This code is the property of CovertJaguar
 and may only be used with explicit written
 permission unless otherwise specified on the
 license page at http://railcraft.info/wiki/info:license.
 -----------------------------------------------------------------------------*/
package mods.railcraft.common.blocks.machine.beta;

import mods.railcraft.common.fluids.FluidTools;
import mods.railcraft.common.fluids.Fluids;
import mods.railcraft.common.fluids.TankManager;
import mods.railcraft.common.fluids.tanks.FilteredTank;
import mods.railcraft.common.gui.EnumGui;
import mods.railcraft.common.gui.GuiHandler;
import mods.railcraft.common.util.effects.EffectManager;
import mods.railcraft.common.util.misc.MiscTools;
import mods.railcraft.common.util.sounds.RailcraftSoundEvents;
import mods.railcraft.common.util.sounds.SoundHelper;
import mods.railcraft.common.util.steam.ISteamUser;
import mods.railcraft.common.util.steam.Steam;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.Random;

/**
 * @author CovertJaguar <http://www.railcraft.info>
 */
public abstract class TileEngineSteam extends TileEngine implements ISteamUser {

    private static final int TANK_CAPACITY = 8 * FluidTools.BUCKET_VOLUME;
    public static final int TANK_STEAM = 0;
    private final FilteredTank steamTank;
    private final TankManager tankManager = new TankManager();
    private int steamUsed;

    protected TileEngineSteam() {
        steamTank = new FilteredTank(TANK_CAPACITY, Fluids.STEAM.get(), this);
        tankManager.add(steamTank);
    }

    @Override
    protected void playSoundOut() {
        SoundHelper.playSoundClient(worldObj, getPos(), RailcraftSoundEvents.MECHANICAL_STEAM_BURST, SoundCategory.BLOCKS, 0.15F, (float) (0.5F + MiscTools.RANDOM.nextGaussian() * 0.1));
    }

    @Override
    protected void playSoundIn() {
        SoundHelper.playSoundClient(worldObj, getPos(), RailcraftSoundEvents.MECHANICAL_STEAM_BURST, SoundCategory.BLOCKS, 0.15F, (float) (1 + MiscTools.RANDOM.nextGaussian() * 0.1));
    }

    private int getParticleRate() {
        switch (getEnergyStage()) {
            case BLUE:
                return 1;
            case GREEN:
                return 2;
            case YELLOW:
                return 3;
            case ORANGE:
                return 4;
            case RED:
                return 5;
            case OVERHEAT:
                return 8;
            default:
                return 0;
        }
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        GuiHandler.openGui(EnumGui.ENGINE_STEAM, player, worldObj, getPos());
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(Random rand) {
        if (isActive() || getEnergyStage() == EnergyStage.OVERHEAT) {
            int steamRate = getParticleRate();
            for (int i = 0; i < steamRate; i++) {
                EffectManager.instance.steamEffect(worldObj, this, 0);
            }
        }
    }

    @Override
    public void burn() {
        int output = 0;

        if (getEnergyStage() != EnergyStage.OVERHEAT) {
            if (isPowered()) {
                FluidStack steam = steamTank.getFluid();
                if (steam != null && steam.amount >= steamTank.getCapacity() / 2 - Steam.STEAM_PER_UNIT_WATER) {
                    steam = tankManager.drain(0, steamUsedPerTick() - 1, true);
                    if (steam != null)
                        steamUsed += steam.amount;
                }
            }
            FluidStack steam = tankManager.drain(0, 1, true);
            if (steam != null)
                steamUsed += steam.amount;

            if (isPowered()) {
                if (steamUsed >= steamUsedPerTick()) {
                    steamUsed -= steamUsedPerTick();
                    output = getMaxOutputRF();
                    addEnergy(output);
                }
            } else {
                steamUsed = 0;
                ventSteam();
            }
        }

        currentOutput = (currentOutput * 74 + output) / 75f;
    }

    @Override
    protected void overheat() {
        super.overheat();
        ventSteam();
    }

    protected void ventSteam() {
        getTankManager().drain(TANK_STEAM, 5, true);
    }

    @Override
    public final int maxEnergyExtracted() {
        return getMaxOutputRF() * 8;
    }

    public abstract int getMaxOutputRF();

    public abstract int steamUsedPerTick();


    @Override
    public NBTTagCompound writeToNBT( NBTTagCompound data) {
        super.writeToNBT(data);

        tankManager.writeTanksToNBT(data);
        return data;
    }

    @Override
    public void readFromNBT( NBTTagCompound data) {
        super.readFromNBT(data);

        tankManager.readTanksFromNBT(data);
    }

    @Override
    public FluidStack drain(EnumFacing from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(EnumFacing from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public int fill(EnumFacing from, FluidStack resource, boolean doFill) {
        if (!isPowered())
            return 0;
        return tankManager.fill(0, resource, doFill);
    }

    @Override
    public boolean canDrain(EnumFacing from, Fluid fluid) {
        return false;
    }

    @Override
    public boolean canFill(EnumFacing from, Fluid fluid) {
        return getOrientation() != from && Fluids.STEAM.is(fluid);
    }

    @Override
    public FluidTankInfo[] getTankInfo(EnumFacing from) {
        if (getOrientation() == from)
            return null;
        return tankManager.getTankInfo();
    }

    public TankManager getTankManager() {
        return tankManager;
    }
}
