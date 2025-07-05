package ru.zznty.create_factory_logistics.logistics.abstractions.box;

import com.simibubi.create.AllSoundEvents;
import com.simibubi.create.content.logistics.box.PackageItem;
import com.simibubi.create.content.logistics.box.PackageStyles;
import com.tterrag.registrate.util.entry.EntityEntry;
import net.createmod.catnip.math.VecHelper;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import ru.zznty.create_factory_logistics.CreateFactoryLogistics;

import java.lang.ref.WeakReference;

public abstract class AbstractPackageItem extends PackageItem {
    public AbstractPackageItem(Item.Properties properties, PackageStyles.PackageStyle style) {
        super(properties, style);

        // we don't want our packages to be appearing from other packaging appliances
        PackageStyles.ALL_BOXES.remove(this);
        (style.rare() ? PackageStyles.RARE_BOXES : PackageStyles.STANDARD_BOXES).remove(this);
    }

    @Override
    public String getDescriptionId() {
        return "item." + CreateFactoryLogistics.MODID + (style.rare() ? ".rare_" : ".") + getIdSuffix();
    }

    protected abstract String getIdSuffix();

    @Override
    public abstract Entity createEntity(Level world, Entity location, ItemStack itemstack);

    protected abstract EntityEntry<? extends AbstractPackageEntity> getEntityEntry();

    @Override
    public InteractionResultHolder<ItemStack> open(Level worldIn, Player playerIn, InteractionHand handIn) {
        ItemStack box = playerIn.getItemInHand(handIn);

        BlockHitResult hitResult = getPlayerPOVHitResult(worldIn, playerIn, ClipContext.Fluid.NONE);

        if (hitResult.getType() == HitResult.Type.MISS)
            return InteractionResultHolder.pass(box);

        ItemStack particle = box.copy();

        InteractionResultHolder<ItemStack> result;
        if (!worldIn.isClientSide()) {
            result = openServerSide(worldIn, playerIn, handIn, box, hitResult);
        } else result = InteractionResultHolder.pass(box);

        if (result.getResult().consumesAction()) {
            Vec3 position = playerIn.position();
            AllSoundEvents.PACKAGE_POP.playOnServer(worldIn, playerIn.blockPosition());

            if (worldIn.isClientSide()) {
                for (int i = 0; i < 10; i++) {
                    Vec3 motion = VecHelper.offsetRandomly(Vec3.ZERO, worldIn.getRandom(), .125f);
                    Vec3 pos = position.add(0, 0.5, 0)
                            .add(playerIn.getLookAngle()
                                         .scale(.5))
                            .add(motion.scale(4));
                    worldIn.addParticle(new ItemParticleOption(ParticleTypes.ITEM, particle), pos.x, pos.y, pos.z,
                                        motion.x,
                                        motion.y, motion.z);
                }
            }
        }

        return result;
    }

    protected abstract InteractionResultHolder<ItemStack> openServerSide(Level worldIn, Player playerIn,
                                                                         InteractionHand handIn, ItemStack box,
                                                                         BlockHitResult hitResult);

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer()
                .isShiftKeyDown()) {
            return open(context.getLevel(), context.getPlayer(), context.getHand()).getResult();
        }

        Vec3 point = context.getClickLocation();
        float h = style.height() / 16f;
        float r = style.width() / 2f / 16f;

        if (context.getClickedFace() == Direction.DOWN)
            point = point.subtract(0, h + .25f, 0);
        else if (context.getClickedFace()
                .getAxis()
                .isHorizontal())
            point = point.add(Vec3.atLowerCornerOf(context.getClickedFace()
                                                           .getNormal())
                                      .scale(r));

        AABB scanBB = new AABB(point, point).inflate(r, 0, r)
                .expandTowards(0, h, 0);
        Level world = context.getLevel();
        if (!world.getEntities(getEntityEntry().get(), scanBB, e -> true)
                .isEmpty())
            return InteractionResult.PASS;

        ItemStack itemInHand = context.getItemInHand();
        AbstractPackageEntity entity = (AbstractPackageEntity) createEntity(world,
                                                                            new ItemEntity(world, point.x, point.y,
                                                                                           point.z,
                                                                                           itemInHand, 0, 0, 0),
                                                                            itemInHand);
        world.addFreshEntity(entity);
        itemInHand.shrink(1);
        return InteractionResult.SUCCESS;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int ticks) {
        if (!(entity instanceof Player player))
            return;
        int i = this.getUseDuration(stack) - ticks;
        if (i < 0)
            return;

        float f = getPackageVelocity(i);
        if (f < 0.1D)
            return;
        if (world.isClientSide)
            return;

        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW,
                        SoundSource.NEUTRAL, 0.5F, 0.5F);

        ItemStack copy = stack.copy();
        if (!player.getAbilities().instabuild)
            stack.shrink(1);

        Vec3 vec = new Vec3(entity.getX(), entity.getY() + entity.getBoundingBox()
                .getYsize() / 2f, entity.getZ());
        Vec3 motion = entity.getLookAngle()
                .scale(f * 2);
        vec = vec.add(motion);

        AbstractPackageEntity packageEntity = (AbstractPackageEntity) createEntity(world,
                                                                                   new ItemEntity(world, vec.x, vec.y,
                                                                                                  vec.z,
                                                                                                  copy, motion.x,
                                                                                                  motion.y, motion.z),
                                                                                   copy);
        packageEntity.tossedBy = new WeakReference<>(player);
        world.addFreshEntity(packageEntity);
    }
}
