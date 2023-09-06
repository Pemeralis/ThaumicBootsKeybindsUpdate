package thaumicboots.item.boots.voidwalker;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraftforge.common.ISpecialArmor;
import net.minecraftforge.common.MinecraftForge;

import baubles.common.lib.PlayerHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ic2.api.item.ElectricItem;
import taintedmagic.common.registry.ItemRegistry;
import thaumcraft.api.IWarpingGear;
import thaumcraft.client.fx.ParticleEngine;
import thaumcraft.client.fx.particles.FXWispEG;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.items.armor.Hover;
import thaumicboots.api.ItemElectricBoots;
import thaumicboots.main.utils.TabThaumicBoots;

public class ItemElectricVoidwalkerBoots extends ItemElectricBoots implements IWarpingGear, ISpecialArmor {

    public ItemElectricVoidwalkerBoots(final ArmorMaterial material, final int j, final int k) {
        super(material, j, k);
        setCreativeTab(TabThaumicBoots.tabThaumicBoots);
        setUnlocalizedName(unlocalisedName);
        setBootsData();
        MinecraftForge.EVENT_BUS.register(this);
    }

    protected void setBootsData() {
        maxCharge = 1_000_000;
        energyPerDamage = 500;
        runicCharge = 0;
        visDiscount = 5 + 2; // voidwalker + electric discount
        provideEnergy = false;
        damageAbsorptionRatio = 2.25D;
        transferLimit = 400;
        jumpBonus = 0.275D * 1.7;
        baseBonus = 0.200F;

        tier = 3;
        iconResPath = "thaumicboots:electricVoid_16x";
        armorResPath = "thaumicboots:model/electricbootsVoidwalker.png";
        unlocalisedName = "ItemElectricVoid";
        rarity = EnumRarity.epic;
    }

    @Override
    public int getWarp(final ItemStack stack, final EntityPlayer player) {
        return 5;
    }

    @SideOnly(Side.CLIENT)
    protected void particles(final World world, final EntityPlayer player) {
        final FXWispEG fx = new FXWispEG(
                world,
                player.posX + (Math.random() - Math.random()) * 0.5D,
                player.boundingBox.minY + 0.05D + (Math.random() - Math.random()) * 0.1D,
                player.posZ + (Math.random() - Math.random()) * 0.5D,
                player);
        ParticleEngine.instance.addEffect(world, fx);
    }

    @Override
    public void onArmorTick(final World world, final EntityPlayer player, final ItemStack stack) {
        super.onArmorTick(world, player, stack);

        // particles
        final double motion = Math.abs(player.motionX) + Math.abs(player.motionZ) + Math.abs(0.5 * player.motionY);
        if (world.isRemote && (motion > 0.1D || !player.onGround) && world.rand.nextInt(3) == 0) {
            particles(world, player);
        }

        if (player.moveForward > 0.0F) {
            // increased step height
            if (player.worldObj.isRemote && !player.isSneaking()) {
                if (!Thaumcraft.instance.entityEventHandler.prevStep.containsKey(player.getEntityId())) {
                    Thaumcraft.instance.entityEventHandler.prevStep.put(player.getEntityId(), player.stepHeight);
                }
                player.stepHeight = 1.0F;
            }

            // speed boost
            if (player.onGround || player.capabilities.isFlying) {
                float bonus = 0.200F;
                final ItemStack sash = PlayerHandler.getPlayerBaubles(player).getStackInSlot(3);
                if (sash != null && sash.getItem() == ItemRegistry.ItemVoidwalkerSash) {
                    bonus *= 3.0F;
                }
                if (ElectricItem.manager.getCharge(stack) == 0) {
                    bonus *= 0;
                }

                player.moveFlying(0.0F, 1.0F, player.capabilities.isFlying ? bonus * 0.75F : bonus);
            } else if (Hover.getHover(player.getEntityId())) {
                player.jumpMovementFactor = 0.03F;
            } else {
                player.jumpMovementFactor = player.isSprinting() ? 0.045F : 0.04F;
            }
        }
        // negate fall damage
        if (player.fallDistance > 3.0F) {
            player.fallDistance = 1.0F;
        }
    }

}
