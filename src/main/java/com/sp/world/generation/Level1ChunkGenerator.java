package com.sp.world.generation;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.world.generation.maze_generator.Level1MazeGenerator;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.MinecraftServer;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.util.math.noise.PerlinNoiseSampler;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.HeightLimitView;
import net.minecraft.world.Heightmap;
import net.minecraft.world.StructureWorldAccess;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.chunk.Blender;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorSettings;
import net.minecraft.world.gen.chunk.VerticalBlockSample;
import net.minecraft.world.gen.noise.NoiseConfig;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@SuppressWarnings("OptionalIsPresent")
public final class Level1ChunkGenerator extends ChunkGenerator {
    public static final Codec<Level1ChunkGenerator> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            ChunkGeneratorSettings.REGISTRY_CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(Level1ChunkGenerator::new))
    );
    private final RegistryEntry<ChunkGeneratorSettings> settings;
    Random random = Random.create();
    PerlinNoiseSampler noiseSampler = new PerlinNoiseSampler(random);

    public Level1ChunkGenerator(BiomeSource biomeSource, RegistryEntry<ChunkGeneratorSettings> settings) {
        super(biomeSource);
        this.settings = settings;
    }




    public void generateMaze(StructureWorldAccess world, Chunk chunk) {
        int x = chunk.getPos().getStartX();
        int z = chunk.getPos().getStartZ();
        int lights = random.nextBetween(1,6);
        int exit;

        BlockPos.Mutable mutable = new BlockPos.Mutable();
        MinecraftServer server = world.getServer();

        StructureTemplateManager structureTemplateManager = world.getServer().getStructureTemplateManager();
        Optional<StructureTemplate> optional;

        Identifier roomIdentifier;
        StructurePlacementData structurePlacementData = new StructurePlacementData();


        if((float) chunk.getPos().x == 0 && (float) chunk.getPos().z  == 0){
            roomIdentifier = new Identifier(SPBRevamped.MOD_ID, "level1/stairwell_1");
            structurePlacementData.setMirror(BlockMirror.NONE).setRotation(BlockRotation.NONE).setIgnoreEntities(true);
            optional = structureTemplateManager.getTemplate(roomIdentifier);

            if(optional.isPresent()){
                optional.get().place(
                        world,
                        mutable.set(-1,19,-1),
                        mutable.set(-1,19,-1),
                        structurePlacementData, random, 2
                );
            }

            Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
            level1MazeGenerator.setup(world, false);
        } else if (((float)chunk.getPos().x) % SPBRevamped.finalMazeSize == 0 && ((float)chunk.getPos().z) % SPBRevamped.finalMazeSize == 0){
            double noise1 = noiseSampler.sample((x) * 0.002, 0, (z) * 0.002);
            if (server != null) {

                if(!chunk.getPos().getBlockPos(0,20,0).isWithinDistance(new Vec3i(0,20,0), 1000)){
                    if(noise1 <= 0){
                        exit = random.nextBetween(1,1);
                        if(exit == 1){

                            roomIdentifier = new Identifier(SPBRevamped.MOD_ID, "level1/stairwell2_1");
                            structurePlacementData.setMirror(BlockMirror.NONE).setRotation(BlockRotation.NONE).setIgnoreEntities(true);
                            optional = structureTemplateManager.getTemplate(roomIdentifier);

                            if (optional.isPresent()) {
                                optional.get().place(
                                        world,
                                        mutable.set(x + 16,11,z + 16),
                                        mutable.set(x + 16,11,z + 16),
                                        structurePlacementData, random, 2
                                );
                            }

                        }
                    }
                }

                if(noise1 > 0){
                    roomIdentifier = new Identifier(SPBRevamped.MOD_ID, "level1/megaroom1");
                    structurePlacementData.setMirror(BlockMirror.NONE).setRotation(BlockRotation.NONE).setIgnoreEntities(true);
                    optional = structureTemplateManager.getTemplate(roomIdentifier);

                    if (optional.isPresent()) {
                        optional.get().place(
                                world,
                                mutable.set(x - 32, 19, z - 32),
                                mutable.set(x - 32, 19, z - 32),
                                structurePlacementData, random, 2);
                        optional.get().place(
                                world,
                                mutable.set(x, 19, z - 32),
                                mutable.set(x, 19, z - 32),
                                structurePlacementData, random, 2);

                        roomIdentifier = new Identifier(SPBRevamped.MOD_ID, "level1/light" + lights);
                        structurePlacementData.setMirror(BlockMirror.NONE).setRotation(BlockRotation.NONE).setIgnoreEntities(true);
                        optional = structureTemplateManager.getTemplate(roomIdentifier);

                        if (optional.isPresent()){
                        optional.get().place(
                                world,
                                mutable.set(x - 32, 19, z - 32),
                                mutable.set(x - 32, 19, z - 32),
                                structurePlacementData, random, 16);
                        optional.get().place(
                                world,
                                mutable.set(x, 19, z - 32),
                                mutable.set(x, 19, z - 32),
                                structurePlacementData, random, 16);
                        }


                    } else {
                        if(world.getBlockState(mutable.set(x, 19, z)) != Blocks.RED_WOOL.getDefaultState()) {
                            Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
                            level1MazeGenerator.setup(world, true);
                        }

                    }

                } else{

                    if(world.getBlockState(mutable.set(x, 19, z)) != Blocks.RED_WOOL.getDefaultState()) {
                        Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
                        level1MazeGenerator.setup(world, true);
                    }

                }
            }
        }

//        for (int j = 0; j < 16; j++){
//            for (int i = 0; i < 16; i++){
//                world.setBlockState(mutable.set(x + j, 25, z + i), Blocks.AIR.getDefaultState(), 16);
//            }
//        }

    }

    protected Codec<? extends ChunkGenerator> getCodec() {
        return CODEC;
    }

    /* this method builds the shape of the terrain. it places stone everywhere, which will later be overwritten with grass, terracotta, snow, sand, etc
         by the buildSurface method. it also is responsible for putting the water in oceans. it returns a CompletableFuture-- you'll likely want this to be delegated to worker threads. */
    @Override
    public CompletableFuture<Chunk> populateNoise(Executor executor, Blender blender, NoiseConfig noiseConfig, StructureAccessor structureAccessor, Chunk chunk) {
        return CompletableFuture.completedFuture(chunk);
    }


    @Override
    public int getSeaLevel() {
        return 0;
    }

    /* the lowest value that blocks can be placed in the world. in a vanilla world, this is -64. */
    @Override
    public int getMinimumY() {
        return 0;
    }

    /* this method returns the height of the terrain at a given coordinate. it's used for structure generation */
    @Override
    public int getHeight(int x, int z, Heightmap.Type heightmap, HeightLimitView world, NoiseConfig noiseConfig) {
        return this.getWorldHeight();
    }

    /* this method returns a "core sample" of the world at a given coordinate. it's used for structure generation */
    @Override
    public VerticalBlockSample getColumnSample(int x, int z, HeightLimitView world, NoiseConfig noiseConfig) {
        BlockState[] states = new BlockState[world.getHeight()];

        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.getDefaultState();
        }

        return new VerticalBlockSample(0, states);
    }

    /* this method adds text to the f3 menu. for NoiseChunkGenerator, it's the NoiseRouter line */
    @Override
    public void getDebugHudText(List<String> text, NoiseConfig noiseConfig, BlockPos pos) {
    }

    /* the distance between the highest and lowest points in the world. in vanilla, this is 384 (64+325) */
    @Override
    public int getWorldHeight() {
        return 384;
    }




    /* the method that creates non-noise caves (i.e., all the caves we had before the caves and cliffs update) */
    @Override
    public void carve(ChunkRegion chunkRegion, long seed, NoiseConfig noiseConfig, BiomeAccess biomeAccess, StructureAccessor structureAccessor, Chunk chunk, GenerationStep.Carver carverStep) {
    }

    /* the method that places grass, dirt, and other things on top of the world, as well as handling the bedrock and deepslate layers,
    as well as a few other miscellaneous things. without this method, your world is just a blank stone (or whatever your default block is) canvas (plus any ores, etc) */
    @Override
    public void buildSurface(ChunkRegion region, StructureAccessor structures, NoiseConfig noiseConfig, Chunk chunk) {

    }

    /* this method spawns entities in the world */
    @Override
    public void populateEntities(ChunkRegion region) {

    }



}

