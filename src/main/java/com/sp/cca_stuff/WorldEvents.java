package com.sp.cca_stuff;

import com.sp.SPBRevamped;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModEntities;
import com.sp.init.ModSounds;
import com.sp.sounds.voicechat.BackroomsVoicechatPlugin;
import com.sp.world.events.AbstractEvent;
import com.sp.world.events.infinite_grass.InfiniteGrassAmbience;
import com.sp.world.events.level0.Level0Blackout;
import com.sp.world.events.level0.Level0Flicker;
import com.sp.world.events.level0.Level0IntercomBasic;
import com.sp.world.events.level0.Level0Music;
import com.sp.world.events.level1.Level1Ambience;
import com.sp.world.events.level1.Level1Blackout;
import com.sp.world.events.level1.Level1Flicker;
import com.sp.world.events.level2.Level2Ambience;
import com.sp.world.events.level2.Level2Warp;
import com.sp.world.events.poolrooms.PoolroomsAmbience;
import com.sp.world.events.poolrooms.PoolroomsSunset;
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent;
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent;
import net.minecraft.entity.ai.TargetPredicate;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class WorldEvents implements AutoSyncedComponent, ServerTickingComponent {
    private World world;

    //Level0
    private boolean level0Blackout;
    private boolean level0On;
    private boolean level0Flicker;
    private int intercomCount;
    private int blackoutCount;

    //Level1
    private boolean level1Blackout;
    private boolean level1Flicker;

    //Level2
    private boolean level2Flicker;
    private boolean level2Blackout;
    private boolean level2Warp;

    //Poolrooms
    private boolean sunsetTransition;
    private float currentPoolroomsTime;
    private boolean noon;

    private boolean inLevel0;
    private boolean inLevel1;
    private boolean inLevel2;
    private boolean inPoolRooms;
    private boolean inInfgrass;

    private boolean prevLevel0Blackout;
    private boolean prevLevel0On;
    private boolean prevLevel0Flicker;
    private int prevIntercomCount;

    private boolean prevLevel1Blackout;
    private boolean prevLevel1Flicker;

    private boolean prevLevel2Blackout;
    private boolean prevLevel2Flicker;
    private boolean prevLevel2Warp;

    private boolean prevSunsetTransition;
    private boolean prevNoon;

    private boolean prevInLevel0;
    private boolean prevInLevel1;
    private boolean prevInLevel2;
    private boolean prevInPoolRooms;
    private boolean prevInInfGrass;

    private static List<Supplier<AbstractEvent>> level0EventList;
    private static List<Supplier<AbstractEvent>> level1EventList;
    private static List<Supplier<AbstractEvent>> level2EventList;
    private static List<Supplier<AbstractEvent>> poolroomsEventList;
    private static List<Supplier<AbstractEvent>> infiniteGrassEventList;
    boolean registered;

    private AbstractEvent activeEvent;
    private boolean eventActive;
    public int ticks;
    private int delay;

    private UUID nullUUID = UUID.randomUUID();
    private UUID activeSkinwalkerTarget;
    private UUID prevActiveSkinwalkerTarget;
    public SkinWalkerEntity activeSkinWalkerEntity;

    private boolean done;
    private int tick;

    public WorldEvents(World world){
        this.world = world;
        this.level0Blackout = false;
        this.level1Blackout = false;
        this.level2Blackout = false;
        this.level0Flicker = false;
        this.level1Flicker = false;
        this.level2Flicker = false;
        this.level0On = true;
        this.level2Warp = false;
        this.intercomCount = 0;
        this.sunsetTransition = false;
        this.currentPoolroomsTime = 0.0f;
        this.noon = true;
        this.inLevel0 = false;
        this.inLevel1 = false;
        this.inLevel2 = false;
        this.inPoolRooms = false;
        this.registered = false;
        this.eventActive = false;
        this.ticks = 0;
        this.delay = 1800;
        this.activeSkinwalkerTarget = nullUUID;

        this.done = false;
    }

    public boolean isEventActive() {
        return eventActive;
    }
    public void setEventActive(boolean eventActive) {
        this.eventActive = eventActive;
    }

    public void setActiveEvent(AbstractEvent activeEvent) {
        this.activeEvent = activeEvent;
    }
    public AbstractEvent getActiveEvent() {
        return this.activeEvent;
    }

    public int getIntercomCount() {
        return intercomCount;
    }
    public void addIntercomCount() {
        this.intercomCount = this.intercomCount + 1;
    }

    public boolean isLevel0Blackout() {
        return level0Blackout;
    }
    public void setLevel0Blackout(boolean level0Blackout) {
        this.level0Blackout = level0Blackout;
    }

    public boolean isLevel1Blackout() {
        return level1Blackout;
    }
    public void setLevel1Blackout(boolean level1Blackout) {
        this.level1Blackout = level1Blackout;
    }

    public boolean isLevel2Blackout() {
        return level2Blackout;
    }
    public void setLevel2Blackout(boolean level2Blackout) {
        this.level2Blackout = level2Blackout;
    }

    public boolean isLevel0On() {
        return level0On;
    }
    public void setLevel0On(boolean level0On) {
        this.level0On = level0On;
    }

    public boolean isLevel0Flicker() {
        return level0Flicker;
    }
    public void setLevel0Flicker(boolean level0Flicker) {
        this.level0Flicker = level0Flicker;
    }

    public boolean isLevel1Flicker() {
        return level1Flicker;
    }
    public void setLevel1Flicker(boolean level1Flicker) {
        this.level1Flicker = level1Flicker;
    }

    public boolean isLevel2Flicker() {
        return level2Flicker;
    }
    public void setLevel2Flicker(boolean level2Flicker) {
        this.level2Flicker = level2Flicker;
    }

    public boolean isLevel2Warp() {
        return level2Warp;
    }
    public void setLevel2Warp(boolean level2Warp) {
        this.level2Warp = level2Warp;
        this.sync();
    }

    public boolean isSunsetTransition() {
        return sunsetTransition;
    }
    public void setSunsetTransition(boolean sunsetTransition) {
        this.sunsetTransition = sunsetTransition;
    }

    public float getCurrentPoolroomsTime() {
        return currentPoolroomsTime;
    }
    public void setCurrentPoolroomsTime(float currentPoolroomsTime) {
        this.currentPoolroomsTime = currentPoolroomsTime;
    }

    public boolean isNoon() {
        return noon;
    }
    public void setNoon(boolean noon) {
        this.noon = noon;
    }

    public PlayerEntity getActiveSkinwalkerTarget() {
        if(this.activeSkinwalkerTarget == null || this.activeSkinwalkerTarget.equals(nullUUID)){
            return null;
        }
        return this.world.getPlayerByUuid(this.activeSkinwalkerTarget);
    }
    public void setActiveSkinwalkerTarget(UUID uuid) {
        this.activeSkinwalkerTarget = uuid;
        this.sync();
    }

    public void sync(){
        InitializeComponents.EVENTS.sync(this.world);
    }

    @Override
    public void readFromNbt(NbtCompound tag) {
        this.level0Blackout = tag.getBoolean("level0Blackout");
        this.level1Blackout = tag.getBoolean("level1Blackout");
        this.level2Blackout = tag.getBoolean("level2Blackout");
        this.level0On = tag.getBoolean("level0On");
        this.level0Flicker = tag.getBoolean("level0Flicker");
        this.level1Flicker = tag.getBoolean("level1Flicker");
        this.level2Flicker = tag.getBoolean("level2Flicker");
        this.level2Warp = tag.getBoolean("level2Warp");
        this.sunsetTransition = tag.getBoolean("sunsetTransition");
        this.currentPoolroomsTime = tag.getFloat("currentPoolroomsTime");
        this.noon = tag.getBoolean("noon");
        this.intercomCount = tag.getInt("intercomCount");
        this.inLevel0 = tag.getBoolean("inLevel0");
        this.inLevel1 = tag.getBoolean("inLevel1");
        this.inLevel2 = tag.getBoolean("inLevel2");
        this.inPoolRooms = tag.getBoolean("inPoolRooms");
        this.inInfgrass = tag.getBoolean("inInfgrass");
        this.activeSkinwalkerTarget = tag.getUuid("activeSkinwalkerTarget");
    }

    @Override
    public void writeToNbt(NbtCompound tag) {
        tag.putBoolean("level0Blackout", this.level0Blackout);
        tag.putBoolean("level1Blackout", this.level1Blackout);
        tag.putBoolean("level2Blackout", this.level2Blackout);
        tag.putBoolean("level0On", this.level0On);
        tag.putBoolean("level0Flicker", this.level0Flicker);
        tag.putBoolean("level1Flicker", this.level1Flicker);
        tag.putBoolean("level2Flicker", this.level2Flicker);
        tag.putBoolean("level2Warp", this.level2Warp);
        tag.putBoolean("sunsetTransition", this.sunsetTransition);
        tag.putFloat("currentPoolroomsTime", this.currentPoolroomsTime);
        tag.putBoolean("noon", this.noon);
        tag.putInt("intercomCount", this.intercomCount);
        tag.putBoolean("inLevel0", this.inLevel0);
        tag.putBoolean("inLevel1", this.inLevel1);
        tag.putBoolean("inLevel2", this.inLevel2);
        tag.putBoolean("inPoolRooms", this.inPoolRooms);
        tag.putBoolean("inInfgrass", this.inInfgrass);
        tag.putUuid("activeSkinwalkerTarget", this.activeSkinwalkerTarget);
    }

    @Override
    public void serverTick() {
        Random random = Random.create();
        getPrevSettings();

        if (!this.registered) {
            registerEvents();
            this.registered = true;
        }

        if (world != null && !world.getPlayers().isEmpty() && BackroomsLevels.isInBackrooms(world.getRegistryKey())) {
            ticks++;

            checkDimension();
            //Tick the currently active event and choose a random one every min and a half
            tickWorldEvents(random);

            //Start Looking for a player to take and take them when they're not talking and can't be seen
            tickSkinWalkerCapturing();

            shouldReleasePlayer();
        }

        shouldSync();
    }

    private void shouldReleasePlayer() {
        if (this.getActiveSkinwalkerTarget() != null && this.activeSkinWalkerEntity == null) {
            ServerPlayerEntity target = (ServerPlayerEntity) this.getActiveSkinwalkerTarget();
            PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);

            if (targetComponent.hasBeenCaptured() || targetComponent.isBeingCaptured()) {
                target.changeGameMode(GameMode.SURVIVAL);
                targetComponent.setHasBeenCaptured(false);
                targetComponent.setShouldBeMuted(false);
                targetComponent.sync();
                SPBRevamped.sendPersonalPlaySoundPacket(target, ModSounds.SKINWALKER_RELEASE, 1.0f, 1.0f);
            }

            this.activeSkinwalkerTarget = nullUUID;

            return;
        }

        if (activeSkinWalkerEntity == null) {
            return;
        }

        SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(this.activeSkinWalkerEntity);

        if (!component.shouldBeginRelease()) {
            if (this.getActiveSkinwalkerTarget() != null) {
                ((ServerPlayerEntity) this.getActiveSkinwalkerTarget()).changeGameMode(GameMode.SPECTATOR);
                ((ServerPlayerEntity) this.getActiveSkinwalkerTarget()).setCameraEntity(this.activeSkinWalkerEntity);
            }

            return;
        }

        PlayerComponent targetComponent = InitializeComponents.PLAYER.get(this.getActiveSkinwalkerTarget());
        ServerPlayerEntity target = (ServerPlayerEntity) this.getActiveSkinwalkerTarget();
        tick++;

        if (this.tick == 1) {
            this.setLevel0Flicker(true);
        }

        if (this.tick == 80) {
            this.setLevel0Flicker(false);
            this.setLevel0On(false);

            targetComponent.setBeingReleased(true);
            targetComponent.sync();

            SPBRevamped.sendPersonalPlaySoundPacket(target, ModSounds.SKINWALKER_RELEASE, 1.0f, 1.0f);

            target.changeGameMode(targetComponent.getPrevGameMode() != null ? targetComponent.getPrevGameMode() : GameMode.SURVIVAL);
            target.setCameraEntity(target);

            for (PlayerEntity player : this.world.getPlayers()) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                playerComponent.setFlashLightOn(false);
                playerComponent.sync();
            }
            this.activeSkinWalkerEntity.discard();
        }

        if (this.tick >= 105) {
            this.setLevel0On(true);
            targetComponent.setBeingReleased(false);
            targetComponent.setHasBeenCaptured(false);
            targetComponent.setShouldBeMuted(false);
            targetComponent.sync();
            this.activeSkinWalkerEntity = null;
        }
    }

    private void tickSkinWalkerCapturing() {
        if (this.getIntercomCount() < 2 || world.getPlayers().size() <= 1) {
            return;
        }

        if (done || this.world.getRegistryKey() != BackroomsLevels.LEVEL0_WORLD_KEY) {
            return;
        }
        //Thank goodness for https://stackoverflow.com/questions/2776176/get-minvalue-of-a-mapkey-double
        Map.Entry<UUID, Float> min = null;
        for (Map.Entry<UUID, Float> entry : BackroomsVoicechatPlugin.speakingTime.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;

            }
        }

        if (min != null) {
            PlayerEntity target = this.world.getPlayerByUuid(min.getKey());
            if (target != null && target.isAlive()) {
                this.setActiveSkinwalkerTarget(target.getUuid());
            }
        }

        if (this.getActiveSkinwalkerTarget() == null) {
            return;
        }

        PlayerEntity target = this.getActiveSkinwalkerTarget();
        PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);

        if (targetComponent.isSpeaking()) {
            return;
        }

        List<PlayerEntity> playerEntityList = target.getWorld().getPlayers(
                TargetPredicate.DEFAULT
                        .ignoreDistanceScalingFactor()
                        .ignoreVisibility()
                        .setBaseMaxDistance(50)
                        .setPredicate(EntityPredicates.EXCEPT_CREATIVE_OR_SPECTATOR::test),
                target,
                target.getBoundingBox().expand(50));

        boolean seen = false;
        for (PlayerEntity player : playerEntityList) {
            if (player != target) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                if (playerComponent.canSeeActiveSkinWalkerTarget()) {
                    seen = true;
                    break;
                }
            }
        }

        if (seen) {
            return;
        }
        //Take em
        SkinWalkerEntity skinWalkerEntity = ModEntities.SKIN_WALKER_ENTITY.create(this.world);
        if (skinWalkerEntity == null) {
            return;
        }

        skinWalkerEntity.refreshPositionAndAngles((double) target.getX(), (double) target.getY(), (double) target.getZ(), target.getYaw(), target.getPitch());
        skinWalkerEntity.setVelocity(target.getVelocity());
        this.world.spawnEntity(skinWalkerEntity);
        this.activeSkinWalkerEntity = skinWalkerEntity;

        targetComponent.setPrevGameMode(((ServerPlayerEntity) target).interactionManager.getGameMode());
        targetComponent.setBeingCaptured(true);
        targetComponent.setHasBeenCaptured(true);
        targetComponent.setShouldBeMuted(true);
        targetComponent.sync();

        ((ServerPlayerEntity) target).changeGameMode(GameMode.SPECTATOR);
        ((ServerPlayerEntity) target).setCameraEntity(skinWalkerEntity);
        this.done = true;
    }

    private void tickWorldEvents(Random random) {
        if (this.activeSkinWalkerEntity != null) {

            return;
        }

        if (!eventActive) {
            this.delay--;
//                    delay = 100;
            if (this.delay > 0) {
                return;
            }

            this.delay = 0;

            if (level0EventList.isEmpty() || level1EventList.isEmpty() || level2EventList.isEmpty() || poolroomsEventList.isEmpty() || infiniteGrassEventList.isEmpty()) {
                return;
            }

            int currentDimension = getCurrentDimension();

            switch (currentDimension) {
                case 1: {
                    tickLevel0Events(random);
                }
                break;
                case 2: {
                    tickLevel1Events(random);
                }
                break;
                case 3: {
                    tickLevel2Events(random);
                }
                break;
                case 4: {
                    tickPoolroomsEvents(random);
                }
                break;
                case 5: {
                    tickInfiniteGrassEvents(random);
                }
                break;
            }

            return;
        }

        if (activeEvent.duration() <= ticks) {
            activeEvent.reset(this.world);
            if (activeEvent.isDone()) setEventActive(false);
        } else {
            activeEvent.ticks(ticks, this.world);
        }
    }

    private void tickInfiniteGrassEvents(Random random) {
        activeEvent = infiniteGrassEventList.get(0).get();

        activeEvent.init(this.world);
        setEventActive(true);
        ticks = 0;
        this.delay = random.nextBetween(1000, 1200);
    }

    private void tickPoolroomsEvents(Random random) {
        int index = random.nextBetween(0, poolroomsEventList.size() - 1);
        activeEvent = poolroomsEventList.get(index).get();

        activeEvent.init(this.world);
        setEventActive(true);
        ticks = 0;
        this.delay = random.nextBetween(800, 1000);
    }

    private void tickLevel2Events(Random random) {
        int index = random.nextBetween(0, level2EventList.size() - 1);
        activeEvent = level2EventList.get(index).get();

        activeEvent.init(this.world);
        setEventActive(true);
        ticks = 0;
        this.delay = random.nextBetween(500, 800);
    }

    private void tickLevel1Events(Random random) {
        int index = random.nextBetween(0, level1EventList.size() - 1);
        activeEvent = level1EventList.get(index).get();

        activeEvent.init(this.world);
        setEventActive(true);
        ticks = 0;
        this.delay = random.nextBetween(1000, 1600);
    }

    private void tickLevel0Events(Random random) {
        int index = random.nextBetween(0, level0EventList.size() - 1);
        activeEvent = level0EventList.get(index).get();

        if (activeEvent instanceof Level0Blackout) {
            this.blackoutCount++;
            if (this.blackoutCount > 2) {
                while (activeEvent instanceof Level0Blackout) {
                    activeEvent = level0EventList.get(random.nextBetween(0, level0EventList.size() - 1)).get();
                }
            }
        }

        activeEvent.init(this.world);
        setEventActive(true);
        ticks = 0;
        this.delay = random.nextBetween(1000, 1500);
    }

    private void registerEvents() {
        level0EventList = new ArrayList<>();
        level0EventList.add(Level0IntercomBasic::new);
        level0EventList.add(Level0Blackout::new);
        level0EventList.add(Level0Flicker::new);
        level0EventList.add(Level0Music::new);


        level1EventList = new ArrayList<>();
        level1EventList.add(Level1Blackout::new);
        level1EventList.add(Level1Flicker::new);
        level1EventList.add(Level1Ambience::new);


        level2EventList = new ArrayList<>();
        level2EventList.add(Level2Warp::new);
        level2EventList.add(Level2Ambience::new);


        poolroomsEventList = new ArrayList<>();
        poolroomsEventList.add(PoolroomsSunset::new);
        poolroomsEventList.add(PoolroomsAmbience::new);

        infiniteGrassEventList = new ArrayList<>();
        infiniteGrassEventList.add(InfiniteGrassAmbience::new);
    }

    private void shouldSync() {
        boolean sync = false;

        if(this.prevLevel0Blackout != this.level0Blackout){
            sync = true;
        }

        if(this.prevLevel1Blackout != this.level1Blackout){
            sync = true;
        }

        if(this.prevLevel2Blackout != this.level2Blackout){
            sync = true;
        }

        if(this.prevLevel0On != this.level0On){
            sync = true;
        }

        if(this.prevLevel0Flicker != this.level0Flicker){
            sync = true;
        }

        if(this.prevLevel1Flicker != this.level1Flicker){
            sync = true;
        }

        if(this.prevLevel2Flicker != this.level2Flicker){
            sync = true;
        }

        if(this.prevLevel2Warp != this.level2Warp){
            sync = true;
        }

        if(this.prevSunsetTransition != this.sunsetTransition){
            sync = true;
        }

        if(this.prevNoon != this.noon){
            sync = true;
        }

        if(this.prevIntercomCount != this.intercomCount){
            sync = true;
        }


        if(this.prevInLevel0 != this.inLevel0){
            sync = true;
        }

        if(this.prevInLevel1 != this.inLevel1){
            sync = true;
        }

        if(this.prevInLevel2 != this.inLevel2){
            sync = true;
        }

        if(this.prevInPoolRooms != this.inPoolRooms){
            sync = true;
        }

        if(this.prevInInfGrass != this.inInfgrass){
            sync = true;
        }

        if(sync){
            this.sync();
        }

    }

    private void getPrevSettings(){
        this.prevLevel0Blackout = this.level0Blackout;
        this.prevLevel1Blackout = this.level1Blackout;
        this.prevLevel2Blackout = this.level2Blackout;
        this.prevLevel0On = this.level0On;
        this.prevLevel0Flicker = this.level0Flicker;
        this.prevLevel1Flicker = this.level1Flicker;
        this.prevLevel2Flicker = this.level2Flicker;
        this.prevLevel2Warp = this.level2Warp;
        this.prevSunsetTransition = this.sunsetTransition;
        this.prevNoon = this.noon;
        this.prevIntercomCount = this.intercomCount;

        this.prevInLevel0 = this.inLevel0;
        this.prevInLevel1 = this.inLevel1;
        this.prevInLevel2 = this.inLevel2;
        this.prevInPoolRooms = this.inPoolRooms;
        this.prevInInfGrass = this.inInfgrass;
    }

    private int getCurrentDimension(){
        if(this.inLevel0){
            return 1;
        }
        else if(this.inLevel1){
            return 2;
        }
        else if(this.inLevel2){
            return 3;
        }
        else if(this.inPoolRooms){
            return 4;
        }
        else if(this.inInfgrass) {
            return 5;
        }
        else {
            return 0;
        }
    }

    private void checkDimension(){
        if (world != null) {
            if (world.getRegistryKey() == BackroomsLevels.LEVEL0_WORLD_KEY) {
                this.inLevel0 = true;
                this.inLevel1 = false;
                this.inLevel2 = false;
                this.inPoolRooms = false;
                this.inInfgrass = false;
            }
            else if (world.getRegistryKey() == BackroomsLevels.LEVEL1_WORLD_KEY) {
                this.inLevel0 = false;
                this.inLevel1 = true;
                this.inLevel2 = false;
                this.inPoolRooms = false;
                this.inInfgrass = false;
            }
            else if (world.getRegistryKey() == BackroomsLevels.LEVEL2_WORLD_KEY) {
                this.inLevel0 = false;
                this.inLevel1 = false;
                this.inLevel2 = true;
                this.inPoolRooms = false;
                this.inInfgrass = false;
            }
            else if (world.getRegistryKey() == BackroomsLevels.POOLROOMS_WORLD_KEY) {
                this.inLevel0 = false;
                this.inLevel1 = false;
                this.inLevel2 = false;
                this.inPoolRooms = true;
                this.inInfgrass = false;
            }
            else if (world.getRegistryKey() == BackroomsLevels.INFINITE_FIELD_WORLD_KEY) {
                this.inLevel0 = false;
                this.inLevel1 = false;
                this.inLevel2 = false;
                this.inPoolRooms = false;
                this.inInfgrass = true;
            }
            else{
                this.inLevel0 = false;
                this.inLevel1 = false;
                this.inLevel2 = false;
                this.inPoolRooms = false;
                this.inInfgrass = false;
            }
        }
    }
}
