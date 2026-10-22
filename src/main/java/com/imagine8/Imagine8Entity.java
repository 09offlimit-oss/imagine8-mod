package com.imagine8;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class Imagine8Entity extends Monster {
    /** Ticks between touching the player and the actual kill (lets the jumpscare play). */
    private static final int JUMPSCARE_KILL_DELAY = 34;

    private boolean summoned = false;
    private int chaseTicks = 0;
    private int jumpscareTimer = -1;
    private ServerPlayer victim = null;
    private boolean loopStarted = false; // client side

    public Imagine8Entity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        if (getNavigation() instanceof GroundPathNavigation nav) {
            nav.setCanOpenDoors(true);
        }
        this.xpReward = 0;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30)   // a bit faster than a sprinting player
                .add(Attributes.ATTACK_DAMAGE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new OpenDoorGoal(this, false));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.0, true));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 0.8));
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, false));
    }

    public void setSummoned(boolean summoned) {
        this.summoned = summoned;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            if (!loopStarted) {
                loopStarted = true;
                Imagine8Mod.CLIENT_LOOP_STARTER.accept(this);
            }
            return;
        }

        if (jumpscareTimer >= 0) {
            tickJumpscare();
            return;
        }
        if (tickCount > 2400) { vanish(); return; }                      // never lives past 2 minutes
        if (!summoned && this.level().isDay()) { vanish(); return; }     // natural ones only exist at night
        if (getTarget() != null && ++chaseTicks > Imagine8Mod.MAX_CHASE_TICKS) {
            vanish();                                                    // gave up after 30 seconds
        }
    }

    private void vanish() {
        if (this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 1.2, getZ(), 40, 0.4, 0.8, 0.4, 0.02);
        }
        this.playSound(SoundEvents.ENDERMAN_TELEPORT, 1.0f, 0.4f);
        this.discard();
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        // Touching a player = jumpscare then death. No normal damage.
        if (!this.level().isClientSide && jumpscareTimer < 0
                && target instanceof ServerPlayer sp && !sp.isCreative() && !sp.isSpectator()) {
            startJumpscare(sp);
        }
        return false;
    }

    private void startJumpscare(ServerPlayer player) {
        victim = player;
        jumpscareTimer = 0;
        setNoAi(true);
        getNavigation().stop();
        player.lookAt(EntityAnchorArgument.Anchor.EYES, this.getEyePosition());
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 100, false, false, false));
        player.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 128, false, false, false));
        ServerPlayNetworking.send(player, new JumpscarePayload());
    }

    private void tickJumpscare() {
        jumpscareTimer++;
        ServerPlayer v = victim;
        if (v == null || v.isRemoved() || !v.isAlive()) {
            discard();
            return;
        }
        // Hover right in the victim's face.
        Vec3 front = Vec3.directionFromRotation(0f, v.getYRot()).scale(1.3);
        this.setDeltaMovement(Vec3.ZERO);
        this.setPos(v.getX() + front.x, v.getY(), v.getZ() + front.z);
        v.lookAt(EntityAnchorArgument.Anchor.EYES, this.getEyePosition());

        if (jumpscareTimer >= JUMPSCARE_KILL_DELAY) {
            v.hurt(this.damageSources().mobAttack(this), 10000f);
            discard();
        }
    }

    // Invulnerable except to /kill and the void.
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return super.hurt(source, amount);
        }
        return false;
    }

    @Override
    protected void playStepSound(BlockPos pos, BlockState state) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.5f, 0.6f);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Summoned", summoned);
        tag.putInt("ChaseTicks", chaseTicks);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        summoned = tag.getBoolean("Summoned");
        chaseTicks = tag.getInt("ChaseTicks");
    }
}
