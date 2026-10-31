package br.com.cronicasdafronteira;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.Witch;
import net.minecraft.world.entity.monster.skeleton.WitherSkeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import java.util.Set;

/** Cripta instanciada no mundo normal, somente com comando explícito e aviso de destruição. */
public final class Dungeon {
    private Dungeon() {}
    public static int create(ServerPlayer player) {
        Progress.State s = Progress.get(player);
        int phase = s.completed == 4 ? 1 : s.completed == 11 ? 2 : -1;
        if (!s.active || phase < 0) {CronicasMod.tell(player,"§cEsta fase da campanha não possui cripta desbloqueada.");return 0;}
        if (s.dungeonPhase == phase) {CronicasMod.tell(player,"§eCripta já criada. /cronicas entrar");return 0;}
        ServerLevel level = player.level();
        if (!level.dimension().equals(Level.OVERWORLD)) {CronicasMod.tell(player,"§cConstrua a cripta no Overworld.");return 0;}
        BlockPos origin = player.blockPosition().offset(30,-18,30);
        if (origin.getY() < level.getMinY()+3 || origin.getY()+10>=level.getMaxY()) {
            CronicasMod.tell(player,"§cY insuficiente para a cripta.");return 0;
        }
        // Não sobrescreve área previamente usada pela mesma fase; exige confirmação externa.
        for (int x=0;x<21;x++) for(int z=0;z<25;z++) for(int y=0;y<8;y++) {
            boolean outer = x==0 || x==20 || z==0 || z==24 || y==0 || y==7;
            level.setBlockAndUpdate(origin.offset(x,y,z), outer ? Blocks.DEEPSLATE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState());
        }
        for(int x=1;x<20;x++) for(int y=1;y<7;y++) {
            if(x>=9 && x<=11 && y<=3)continue;
            level.setBlockAndUpdate(origin.offset(x,y,12),Blocks.CRACKED_DEEPSLATE_BRICKS.defaultBlockState());
        }
        for(int z:new int[]{4,8,16,20}) for(int y=1;y<=4;y++) {
            level.setBlockAndUpdate(origin.offset(3,y,z),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            level.setBlockAndUpdate(origin.offset(17,y,z),Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
        }
        for(int z:new int[]{4,8,16,20}) {
            level.setBlockAndUpdate(origin.offset(5,1,z),Blocks.SOUL_TORCH.defaultBlockState());
            level.setBlockAndUpdate(origin.offset(15,1,z),Blocks.SOUL_TORCH.defaultBlockState());
        }
        BlockPos chest = origin.offset(10,1,23);
        level.setBlockAndUpdate(chest,Blocks.CHEST.defaultBlockState());
        if(level.getBlockEntity(chest) instanceof ChestBlockEntity c) {
            c.setItem(0,new ItemStack(Items.EMERALD,12));
            c.setItem(5,new ItemStack(Items.DIAMOND,3));
            c.setItem(13,new ItemStack(Items.GOLDEN_APPLE,2));
            c.setChanged();
        }
        for(BlockPos pos:new BlockPos[]{origin.offset(6,1,7),origin.offset(14,1,9)})
            EntityTypes.ZOMBIE.spawn(level,pos,EntitySpawnReason.COMMAND);
        for(BlockPos pos:new BlockPos[]{origin.offset(7,1,18),origin.offset(15,1,19)})
            EntityTypes.SKELETON.spawn(level,pos,EntitySpawnReason.COMMAND);
        if (phase==1) {
            WitherSkeleton boss = EntityTypes.WITHER_SKELETON.spawn(level,origin.offset(10,1,19),EntitySpawnReason.COMMAND);
            if(boss!=null) {boss.setCustomName(Component.literal(CronicasMod.CARCEREIRO));boss.setCustomNameVisible(true);boss.setPersistenceRequired();}
        } else {
            Witch boss = EntityTypes.WITCH.spawn(level,origin.offset(10,1,19),EntitySpawnReason.COMMAND);
            if(boss!=null) {boss.setCustomName(Component.literal(CronicasMod.DAMA));boss.setCustomNameVisible(true);boss.setPersistenceRequired();}
            EntityTypes.ZOMBIE.spawn(level,origin.offset(5,1,16),EntitySpawnReason.COMMAND);
            EntityTypes.SKELETON.spawn(level,origin.offset(15,1,16),EntitySpawnReason.COMMAND);
        }
        s.returnX=player.getBlockX();s.returnY=player.getBlockY();s.returnZ=player.getBlockZ();
        s.dungeonX=origin.getX();s.dungeonY=origin.getY();s.dungeonZ=origin.getZ();
        s.dungeonPhase=phase;s.dungeonCreated=true;s.inDungeon=true;
        Progress.save(player);
        player.teleportTo(level,origin.getX()+10.5,origin.getY()+1,origin.getZ()+3.5,Set.of(),player.getYRot(),player.getXRot(),true);
        CronicasMod.tell(player,"§6Cripta da Fronteira — derrote o chefe para avançar. /cronicas sair para voltar.");
        return 1;
    }
    public static int enter(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        if(!s.dungeonCreated || s.dungeonPhase==0 || !p.level().dimension().equals(Level.OVERWORLD)) return 0;
        s.inDungeon=true;Progress.save(p);
        p.teleportTo(p.level(),s.dungeonX+10.5,s.dungeonY+1,s.dungeonZ+3.5,Set.of(),p.getYRot(),p.getXRot(),true);
        return 1;
    }
    public static int leave(ServerPlayer p) {
        Progress.State s=Progress.get(p);
        if(!s.inDungeon || !p.level().dimension().equals(Level.OVERWORLD))return 0;
        s.inDungeon=false;Progress.save(p);
        p.teleportTo(p.level(),s.returnX+.5,s.returnY,s.returnZ+.5,Set.of(),p.getYRot(),p.getXRot(),true);
        return 1;
    }
}
